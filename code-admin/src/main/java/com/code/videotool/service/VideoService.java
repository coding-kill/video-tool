package com.code.videotool.service;

import com.code.videotool.domain.VideoInfoDto;
import org.springframework.http.HttpHeaders;

import java.io.IOException;


public interface VideoService {
    /**
     * 解析视频并保存解析信息
     * @param url
     * @return
     */
    VideoInfoDto getVideoInfo(String url);

    /**
     * 抖音解析
     * @param url 分享链接
     * @return
     */
    VideoInfoDto parsingDyVideoInfo(String url, HttpHeaders httpHeaders);

    /**
     * 快手解析
     * @param url 分享链接
     * @return
     */
    VideoInfoDto parsingKsuVideoInfo(String url, HttpHeaders httpHeaders) throws IOException;

    /**
     * PHP服务解析（暂用）
     * @param url 分享链接
     * @return
     */
    VideoInfoDto phpParsingVideoInfo(String url);
}
