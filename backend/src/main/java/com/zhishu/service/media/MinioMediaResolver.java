package com.zhishu.service.media;

import com.zhishu.config.MediaProperties;
import com.zhishu.entity.Video;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * minio 模式：对 bucket 里的对象生成临时预签名 URL（默认 2 小时）。
 * 自托管 MinIO 的视频同样经过这里解析得到可播放地址。
 */
@Component
@ConditionalOnProperty(name = "zhishu.media.mode", havingValue = "minio")
public class MinioMediaResolver implements StreamSource {

    private final MinioClient client;
    private final String bucket;

    public MinioMediaResolver(MediaProperties props) {
        this.bucket = props.getMinio().getBucket();
        this.client = MinioClient.builder()
                .endpoint(props.getMinio().getEndpoint())
                .credentials(props.getMinio().getAccessKey(), props.getMinio().getSecretKey())
                .build();
    }

    @Override
    public String resolvePlayUrl(Video video) {
        try {
            String objectKey = StringUtils.hasText(video.getMediaKey())
                    ? video.getMediaKey()
                    : "videos/" + video.getId() + ".mp4";
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(2, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            throw new com.zhishu.common.BusinessException(500, "视频解析失败：" + e.getMessage());
        }
    }
}