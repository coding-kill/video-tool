package com.huadongfeng.project.videotool.domain;


/**
 * @author: 小熊
 * @date: 2021/8/27 14:22
 * @description:phone 17521111022
 */
public class VideoInfoDto {
    private String author;//视频作者
    private String avatar;//作者头像
    private String time;
    private String title; //视频标题
    private String cover;//视频封面
    private String url;//视频无水印链接

    public String getAuthor() {
        return author;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public String getUrl() {
        return url;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
