package com.code.filemanageweb.upload.controller;

import com.code.filemanageweb.upload.service.IStreamService;
import com.code.filemanageweb.upload.service.ITokenService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
@RequestMapping("/project/stream/file")
public class SysFileController extends BaseController
{
    private String prefix = "project/upload";

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private IStreamService streamService;

    @GetMapping()
    public String index(HttpServletRequest request, HttpServletResponse response)throws Exception
    {
        String param2 = request.getParameter("param2");
        if(StringUtils.isNotEmpty(param2)){
            try {
                param2 = java.net.URLDecoder.decode(param2,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw e;
            }
        }
        request.setAttribute("param1",request.getParameter("param1"));
        request.setAttribute("param2",param2);
        request.setAttribute("param3",request.getParameter("param3"));
        return prefix + "/index";
    }


    @GetMapping("/tk")
    public void tk(HttpServletRequest request, HttpServletResponse response)
    {
        try{
            tokenService.doGet(request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("/upload")
    public void uploadGet(HttpServletRequest request, HttpServletResponse response)
    {
        try{
            streamService.doGet(request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping("/upload")
    public void uploadPost(HttpServletRequest request, HttpServletResponse response)
    {
        try{
            streamService.doPost(request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
