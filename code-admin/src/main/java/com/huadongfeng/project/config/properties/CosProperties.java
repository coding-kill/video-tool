package com.huadongfeng.project.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName:CosProperties
 * @author:dongao
 * @date 2022/3/15 17:50
 */
@Component
@ConfigurationProperties(prefix = "cos")
public class CosProperties {

    private String secretId = "AKIDb0ASVJE9F6h2FEFUlqgGaomUw88tem0z1111";

    private String secretKey = "vd5KK4Yu1sToxqTq56wXs1uYwDF1Xnqe1111";

    private String region = "ap-beijing";

    private String bucketName = "ei-d-1252590610";

    private String projectName = "ei-dongao";

    private String imageSize = "2";

    private String prefixDomain = "http://ei-d-files.dongao.com/";

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getPrefixDomain() {
        return prefixDomain;
    }

    public void setPrefixDomain(String prefixDomain) {
        this.prefixDomain = prefixDomain;
    }
}
