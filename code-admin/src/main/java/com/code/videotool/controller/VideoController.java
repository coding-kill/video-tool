package com.code.videotool.controller;

import com.code.videotool.service.VideoService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: 小熊
 * @date: 2021/6/9
 * @description:phone 17521111022
 */
@Controller
@RequestMapping("video")
public class VideoController {
    @Resource
    VideoService videoService;

    /**
     * 新增用户
     */
    @GetMapping("")
    public String add(ModelMap mmap) {
        return "videotool/video";
    }

    /***
     * 视频无水印链接解析
     * @param url 分享地址
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getVideoInfo")
    public AjaxResult getVideoInfo(@RequestParam(value = "url") String url) {
        return AjaxResult.success(videoService.getVideoInfo(url));
    }

}
