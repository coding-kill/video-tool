package com.code.filemanageweb.upload.domain;

/**
 * @description:  文件上传常量
 * @date: 2019/9/4 18:29
 * @author: fdh
 */
public enum FileUploadConstants {
    /** 音频 */
    MEDIA("media"),
    /** 图片 */
    IMAGE("image"),
    /** 文件 */
    FILE("file");

    private String value;

    FileUploadConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
