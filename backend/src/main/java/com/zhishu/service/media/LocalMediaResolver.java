package com.zhishu.service.media;

import com.zhishu.entity.Video;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * local 模式：优先返回手工配置的 play_url，否则由 /media/** 静态资源按 mediaKey 拼接。
 * 你 NAS 上的视频若挂载到本地 media 目录，放入对应路径即可被 /media/** 暴露。
 */
@Component
@ConditionalOnProperty(name = "zhishu.media.mode", havingValue = "local", matchIfMissing = true)
public class LocalMediaResolver implements StreamSource {

    @Override
    public String resolvePlayUrl(Video video) {
        if (StringUtils.hasText(video.getPlayUrl())) {
            return video.getPlayUrl();
        }
        if (StringUtils.hasText(video.getMediaKey())) {
            return "/media/" + video.getMediaKey();
        }
        return null;
    }
}