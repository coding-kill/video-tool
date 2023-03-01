package com.huadongfeng.project.videotool.domain;


import java.util.Date;

/**
 * @author: 小熊
 * @date: 2021/6/15 15:33
 * @description:phone 17521111022
 */
public class ParsingInfo {
    private Long id;
    private String userOpenId;
    private String downloadUrl;
    private String title;
    private Date createTime;
    private String author;//视频作者
    private String cover;//视频封面地址

    public Long getId() {
        return id;
    }

    public String getUserOpenId() {
        return userOpenId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getAuthor() {
        return author;
    }

    public String getCover() {
        return cover;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserOpenId(String userOpenId) {
        this.userOpenId = userOpenId;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
