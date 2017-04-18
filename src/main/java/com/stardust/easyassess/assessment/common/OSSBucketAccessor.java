package com.stardust.easyassess.assessment.common;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;

import java.io.InputStream;

public class OSSBucketAccessor implements StorageAccessor {
    private String endpoint = "oss-cn-beijing.aliyuncs.com";
    private String accessKeyId = "LTAI8L5rmT1XGcFj";
    private String accessKeySecret = "bY3jO5KTYFUimCU9ViNSYfr4Dv4Pqw";
    private OSSClient client;

    public OSSBucketAccessor() {
        client = new OSSClient("http://" + endpoint, accessKeyId, accessKeySecret);
    }


    @Override
    public String put(String bucketName, String fileKey, InputStream inputStream) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("application/octet-stream");
        client.putObject(bucketName, fileKey, inputStream, meta);
        return "http://" + bucketName + "." + endpoint + "/" + fileKey;
    }

    @Override
    public void delete() {

    }
}
