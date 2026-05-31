package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云 OSS 文件上传工具类
 */
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传（字节数组方式）
     *
     * @param bytes      文件字节数组
     * @param objectName 存储在 OSS 中的对象名称（包含路径）
     * @return 文件的访问 URL 地址
     */
    public String upload(byte[] bytes, String objectName) {
        return upload(new ByteArrayInputStream(bytes), objectName);
    }

    /**
     * 文件上传（输入流方式）
     *
     * @param inputStream 文件输入流
     * @param objectName  存储在 OSS 中的对象名称（包含路径）
     * @return 文件的访问 URL 地址
     */
    public String upload(InputStream inputStream, String objectName) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (OSSException oe) {
            log.error("阿里云OSS服务异常，错误码：{}，错误信息：{}，请求ID：{}，主机ID：{}", 
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId());
            throw new RuntimeException("文件上传失败，请稍后重试", oe);
        } catch (ClientException ce) {
            log.error("阿里云OSS客户端异常，错误信息：{}", ce.getMessage());
            throw new RuntimeException("文件上传失败，请检查网络连接", ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        String url = buildFileUrl(objectName);
        log.info("文件上传成功，访问路径：{}", url);
        return url;
    }

    /**
     * 构建文件的访问 URL
     *
     * @param objectName 对象名称
     * @return 拼接后的完整 URL
     */
    private String buildFileUrl(String objectName) {
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }
}
