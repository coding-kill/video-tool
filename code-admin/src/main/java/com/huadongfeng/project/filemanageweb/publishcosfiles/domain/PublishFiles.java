package com.huadongfeng.project.filemanageweb.publishcosfiles.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * @ClassName:Content
 * @author:dongao
 * @date 2022/3/17 13:36
 */
public class PublishFiles extends BaseEntity {

    private static final long serialVersionUID = -7781909703911039592L;
    /**文件名 1.JPG 或者文件夹名 ../dongao/*/
    private String fileName;
    /**文件类型 js jpg*/
    private String fileType;
    /**文件大小 */
    private Long fileSize;
    /**是否是目录 默认false 为文件*/
    private boolean directory = false;
    /**文件名的全路径*/
    private String title;
    /**文件对象路径*/
    private String filePath;
    /**是否需要按搜索条件筛选*/
    private String filterFlag;
    private String filepx;

    private Date date = new Date();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileTypeSort() {
        if (fileType != null) {
            return fileType;
        }else {
            return "";
        }
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getUpdateTimeSort() {
        Date updateTime = getUpdateTime();
        if (updateTime != null) {
            return updateTime;
        }else {
            return date;
        }
    }

    public String getFilterFlag() {
        return filterFlag;
    }

    public void setFilterFlag(String filterFlag) {
        this.filterFlag = filterFlag;
    }

    public String getFilepx() {
        return filepx;
    }

    public void setFilepx(String filepx) {
        this.filepx = filepx;
    }
}
