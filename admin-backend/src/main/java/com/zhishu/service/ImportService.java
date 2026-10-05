package com.zhishu.service;

import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.entity.Video;
import com.zhishu.mapper.VideoMapper;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 批量导入视频：选作者 → 上传 mp4 → ffmpeg 抽帧封面 → 传 MinIO → 建 video 行。
 * 依赖本机 ffmpeg / ffprobe（admin 后端部署机需装）。
 */
@Slf4j
@Service
public class ImportService {

    private final VideoMapper videoMapper;
    private final DistributedLock lock;
    private final MinioClient minioClient;

    @Value("${zhishu.admin.minio.bucket}")
    private String bucket;

    public ImportService(VideoMapper videoMapper, DistributedLock lock, MinioClient minioClient) {
        this.videoMapper = videoMapper;
        this.lock = lock;
        this.minioClient = minioClient;
    }

    /** 批量导入：每个文件抽帧+上传+建行。返回新建的 video id 列表。 */
    public List<Long> importVideos(Long bloggerId, Long categoryId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(400, "未上传文件");
        }
        return lock.withLock("zhishu:admin:lock:import", 300_000, () -> {
            List<Long> ids = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String original = file.getOriginalFilename() == null ? "video.mp4" : file.getOriginalFilename();
                if (!original.toLowerCase().endsWith(".mp4")) {
                    continue;
                }
                try {
                    Long id = importOne(bloggerId, categoryId, file, original);
                    ids.add(id);
                } catch (Exception e) {
                    log.warn("导入失败 {}: {}", original, e.getMessage());
                }
            }
            return ids;
        });
    }

    private Long importOne(Long bloggerId, Long categoryId, MultipartFile file, String original) throws Exception {
        String slug = UUID.randomUUID().toString().substring(0, 8);
        File tmp = File.createTempFile("import-", ".mp4");
        file.transferTo(tmp);

        // 抽帧封面
        File cover = File.createTempFile("cover-", ".jpg");
        exec("ffmpeg", "-y", "-v", "error", "-ss", "3", "-i", tmp.getAbsolutePath(),
                "-frames:v", "1", "-q:v", "3", "-vf", "scale=640:-2", cover.getAbsolutePath());
        // 时长
        String dur = execOut("ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "csv=p=0", tmp.getAbsolutePath());
        int duration = dur == null || dur.isBlank() ? 0 : (int) Double.parseDouble(dur.trim());

        String mp4Key = "admin-import/" + slug + ".mp4";
        String coverKey = "covers/admin-import/" + slug + ".jpg";

        // 上传 MinIO
        try (InputStream in = Files.newInputStream(tmp.toPath())) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(mp4Key)
                    .stream(in, tmp.length(), -1).build());
        }
        try (InputStream in = Files.newInputStream(cover.toPath())) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(coverKey)
                    .stream(in, cover.length(), -1).build());
        }

        String title = original.substring(0, original.length() - 4);
        Video video = new Video();
        video.setTitle(title);
        video.setBloggerId(bloggerId);
        video.setCategoryId(categoryId);
        video.setCover("http://192.168.1.38:9000/" + bucket + "/" + coverKey);
        video.setMediaKey(mp4Key);
        video.setDuration(duration);
        video.setHotScore(0);
        video.setSourceType("minio");
        videoMapper.insert(video);

        tmp.delete();
        cover.delete();
        return video.getId();
    }

    private void exec(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        p.getInputStream().readAllBytes();
        p.waitFor();
    }

    private String execOut(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        return new String(p.getInputStream().readAllBytes());
    }
}