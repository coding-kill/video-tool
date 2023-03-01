package com.huadongfeng.project.filemanageweb.upload.controller;

import com.huadongfeng.project.filemanageweb.upload.service.IStreamService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 耗费时间Controller
 *
 * @author ruoyi
 * @date 2021-10-26
 */
@Controller
@RequestMapping("/project/webUpload")
public class WebUploadController extends BaseController
{
    private String prefix = "project/upload";

    @Autowired
    private IStreamService streamService;

    @GetMapping()
    public String index(HttpServletRequest request, HttpServletResponse response)throws Exception
    {
        String path = request.getParameter("path");
        if(StringUtils.isNotEmpty(path)){
            try {
                path = java.net.URLDecoder.decode(path,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw e;
            }
        }
        request.setAttribute("platform",request.getParameter("platform"));
        request.setAttribute("path",path);
        return prefix + "/upload";
    }


    @RequestMapping("/upload")
    public void webUploadPost(MultipartFile upfile, HttpServletRequest request, HttpServletResponse response) {
        try{
            streamService.webUploadPost(request,response,upfile);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @GetMapping("/newWebUpload")
    public String newWebUpload(HttpServletRequest request, HttpServletResponse response)throws Exception
    {
        String path = request.getParameter("path");
        if(StringUtils.isNotEmpty(path)){
            try {
                path = java.net.URLDecoder.decode(path,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw e;
            }
        }
        request.setAttribute("platform",request.getParameter("platform"));
        request.setAttribute("path",path);
        return prefix + "/newUpload";
    }







}
