package com.zhishu.service.media;

import com.zhishu.entity.Video;

/**
 * 视频流来源抽象。骨架默认 LocalMediaResolver；
 * 接入 NAS / Jellyfin / Plex 直链只需新增一个实现，并在配置切换 mode。
 */
public interface StreamSource {

    /** 根据视频元信息返回可直接播放的地址。 */
    String resolvePlayUrl(Video video);
}