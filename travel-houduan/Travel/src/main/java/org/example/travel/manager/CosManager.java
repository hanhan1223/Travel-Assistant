package org.example.travel.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import org.example.travel.config.CosClientConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 上传文件
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putObject(String key , File file){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传MultipartFile文件
     * @param file MultipartFile文件
     * @param key 存储路径
     * @return 文件访问路径
     */
    public String putObject(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(), key, inputStream, metadata);
            cosClient.putObject(putObjectRequest);
        }
        
        return key;
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 获取对象的访问URL
     *
     * @param key 唯一键
     * @return 对象的访问URL
     */
    public String getObjectUrl(String key) {
        // 如果配置了 host，优先使用 host
        if (cosClientConfig.getHost() != null && !cosClientConfig.getHost().isEmpty()) {
            String host = cosClientConfig.getHost();
            if (!host.endsWith("/")) {
                host = host + "/";
            }
            return host + key;
        }
        // 否则使用标准 COS URL 格式
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosClientConfig.getBucket(),
                cosClientConfig.getRegion(),
                key);
    }

}
