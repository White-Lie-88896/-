package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性类
 * 用于映射 application.yml 中以 sky.oss 为前缀的配置项
 */
@Component
@ConfigurationProperties(prefix = "sky.oss")
@Data
public class AliOssProperties {

    /** OSS 访问域名 */
    private String endpoint;
    /** 访问密钥 ID */
    private String accessKeyId;
    /** 访问密钥密钥 */
    private String accessKeySecret;
    /** 存储空间名称 */
    private String bucketName;

}
