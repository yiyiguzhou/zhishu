package com.zhishu.service.media;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.zhishu.common.BusinessException;
import com.zhishu.config.MediaProperties;
import com.zhishu.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Date;

/**
 * oss 模式：对阿里云私有 bucket 中的对象生成 2 小时有效的预签名 URL，
 * 客户端凭 URL 直接访问 OSS（或其 CDN/自定义域名），视频流量不经过后端。
 * media_key 存储中立，与 minio/local 模式共用同一套 key 规则。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "zhishu.media.mode", havingValue = "oss")
public class OssMediaResolver implements StreamSource {

    private final OSS client;
    private final String bucket;

    public OssMediaResolver(MediaProperties props) {
        MediaProperties.Oss oss = props.getOss();
        this.bucket = oss.getBucket();
        this.client = new OSSClientBuilder().build(
                oss.getEndpoint(), oss.getAccessKey(), oss.getSecretKey());
    }

    @Override
    public String resolvePlayUrl(Video entity) {
        String objectKey = StringUtils.hasText(entity.getMediaKey())
                ? entity.getMediaKey()
                : "videos/" + entity.getId() + ".mp4";
        try {
            Date expiration = new Date(System.currentTimeMillis() + 2L * 60 * 60 * 1000);
            URL url = client.generatePresignedUrl(bucket, objectKey, expiration, HttpMethod.GET);
            log.debug("OSS 预签名 bucket={} key={}", bucket, objectKey);
            return url.toString();
        } catch (Exception e) {
            throw new BusinessException(500, "视频解析失败：" + e.getMessage());
        }
    }
}
