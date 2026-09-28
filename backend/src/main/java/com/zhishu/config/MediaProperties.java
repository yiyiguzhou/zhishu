package com.zhishu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zhishu.media")
public class MediaProperties {
    /** local | minio | oss | nas（线上推荐 oss） */
    private String mode = "local";
    private String localDir = "./media";

    private Nas nas = new Nas();
    private Minio minio = new Minio();
    private Oss oss = new Oss();

    @Data
    public static class Nas {
        /** SMB 共享在本机的挂载目录，WebConfig 把 /media/** 指向这里 */
        private String mountDir;
        /** NAS 上的共享名（mount_smbfs 使用，Java 端仅记录/排查用） */
        private String share;
        /** NAS 主机地址（仅记录/排查用，连接由挂载脚本建立） */
        private String host;
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }

    @Data
    public static class Oss {
        /** 对外访问 endpoint（含 scheme）；绑 CDN/自定义域名时填该域名，须与客户端实际访问地址一致 */
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }
}