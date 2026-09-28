package com.zhishu.service.media;

import com.zhishu.common.BusinessException;
import com.zhishu.config.MediaProperties;
import com.zhishu.entity.Video;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * nas 模式：视频文件在 NAS 共享中（经 mount_smbfs 挂载到本机），
 * 由 /media/** 静态资源对外提供（原生支持 Range/206）。
 */
@Component
@ConditionalOnProperty(name = "zhishu.media.mode", havingValue = "nas")
public class NasMediaResolver implements StreamSource {

    private final MediaProperties mediaProperties;

    public NasMediaResolver(MediaProperties mediaProperties) {
        this.mediaProperties = mediaProperties;
    }

    @Override
    public String resolvePlayUrl(Video video) {
        if (StringUtils.hasText(video.getPlayUrl())) {
            return video.getPlayUrl();
        }
        if (!StringUtils.hasText(video.getMediaKey())) {
            return null;
        }
        Path file = Paths.get(mediaProperties.getNas().getMountDir(), video.getMediaKey());
        if (!Files.isRegularFile(file)) {
            throw new BusinessException(404, "视频文件在 NAS 挂载目录不存在：" + file
                    + "，请检查共享挂载与文件是否上传");
        }
        return "/media/" + video.getMediaKey();
    }
}
