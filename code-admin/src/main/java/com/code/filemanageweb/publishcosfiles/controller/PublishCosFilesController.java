package com.code.filemanageweb.publishcosfiles.controller;

import com.code.config.properties.CosProperties;
import com.code.filemanageweb.publishcosfiles.domain.PublishFiles;
import com.code.filemanageweb.publishcosfiles.service.IPublishCosFilesService;
import com.code.util.CosClientUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:PublishCosFilesController
 * @author:dongao
 * @date 2022/3/15 18:26
 */
@Controller
@RequestMapping("project/publishCosFiles")
public class PublishCosFilesController extends BaseController {

    private String prefix = "project/publishcosfiles";

    @Autowired
    private IPublishCosFilesService publishCosFilesService;
    @Autowired
    private CosProperties cosProperties;

    @RequiresPermissions("project:publishCosFiles:view")
    @GetMapping()
    public String view(ModelMap mmap) {
        mmap.put("root", cosProperties.getBucketName());
        mmap.put("platform", 1);
        mmap.put("cosdomain", cosProperties.getPrefixDomain());
        return prefix + "/publishCosFiles";
    }

    @RequiresPermissions("project:publishCosFiles:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(PublishFiles publishFiles) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        List<PublishFiles> list = publishCosFilesService.selectContentList(publishFiles, pageDomain);
        return getDataTable(list);
    }

    /**
     * 加载目录树
     */
    @RequiresPermissions("project:publishCosFiles:treeData")
    @RequestMapping("/treeData")
    @ResponseBody
    public AjaxResult treeData(String name) {
        List<Map> list = publishCosFilesService.getTreeData(name);
        return AjaxResult.success(list);
    }

    /**
     * 展示图片
     *
     * @param request
     * @param mmap
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:showImage")
    @RequestMapping("/showImage")
    public String showImage(HttpServletRequest request, ModelMap mmap) {
        String filePath = request.getParameter("filePath");
        mmap.put("filePath", cosProperties.getPrefixDomain() + filePath);
        return prefix + "/showImage";
    }

    /**
     * 下载文件
     */
    @RequiresPermissions("project:publishCosFiles:download")
    @RequestMapping("/downloadFile")
    @ResponseBody
    public void downloadFile(HttpServletResponse response, HttpServletRequest request) throws IOException {
        publishCosFilesService.downloadFile(request, response);
    }

    /**
     * 下载文件夹
     */
    @RequiresPermissions("project:publishCosFiles:download")
    @RequestMapping("/downloadDirectory")
    @ResponseBody
    public void downloadDirectory(HttpServletResponse response, HttpServletRequest request) throws IOException {
        publishCosFilesService.downloadDirectory(request, response);
    }

    /**
     * 批量下载
     */
    @RequiresPermissions("project:publishCosFiles:batchDownload")
    @RequestMapping("/batchDownloadFiles")
    @ResponseBody
    public void batchDownloadFiles(HttpServletResponse response, HttpServletRequest request) throws IOException {
        publishCosFilesService.batchDownloadFiles(request, response);
    }

    /**
     * 删除单个文件
     *
     * @param title
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:remove")
    @PostMapping("/removeFile")
    @ResponseBody
    public AjaxResult removeFile(String title) {
        try {
            return toAjax(publishCosFilesService.deleteFile(title));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 删除文件夹
     *
     * @param title
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:remove")
    @PostMapping("/removeDirectory")
    @ResponseBody
    public AjaxResult removeDirectory(String title) {
        try {
            return toAjax(publishCosFilesService.deleteDirectory(title));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @RequiresPermissions("project:publishCosFiles:batchRemove")
    @RequestMapping("/batchRemoveFiles")
    @ResponseBody
    public AjaxResult batchRemoveFiles(HttpServletResponse response, HttpServletRequest request) throws IOException {

        try {
            return toAjax(publishCosFilesService.batchRemoveFiles(request, response));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 重命名文件
     *
     * @param request
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:rename")
    @RequestMapping("/toRenameFile")
    public String toRenameFile(HttpServletRequest request, ModelMap mmap) {
        String title = request.getParameter("title");
        String fileName = request.getParameter("fileName");
        mmap.put("title", title);
        mmap.put("fileName", fileName);
        return prefix + "/rename";
    }

    /**
     * 校验文件名是否重复
     *
     * @param request
     * @return
     */
    @RequestMapping("/checkFileNameUnique")
    @ResponseBody
    public String checkFileNameUnique(HttpServletRequest request) {
        String title = request.getParameter("title");
        return publishCosFilesService.checkFileNameUnique(title);
    }


    /**
     * 重命名文件
     *
     * @param publishFiles
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:rename")
    @PostMapping("/renameFile")
    @ResponseBody
    public AjaxResult renameFile(PublishFiles publishFiles) {
        try {
            //先校验文件是否已经存在
            CosClientUtil cosClientUtil = new CosClientUtil();
            String title = publishFiles.getTitle();
            String filePath = title.substring(0, title.lastIndexOf("/") + 1);
            publishFiles.setFilePath(filePath);
            boolean objectExist = cosClientUtil.doesObjectExist(filePath+publishFiles.getFileName());
            if (objectExist) {
                return AjaxResult.error("重命名的文件已存在，请重新修改名称");
            }

            return toAjax(publishCosFilesService.renameFile(publishFiles));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 获取目录历史版本页面
     */
    @RequestMapping("/getHistoryVersion")
    @RequiresPermissions("project:publishCosFiles:history")
    public String getHistoryVersion(HttpServletRequest request) throws Exception {
        String title = request.getParameter("title");
        String platform = request.getParameter("platform");
        request.setAttribute("title", title);
        request.setAttribute("platform", platform);

        return prefix + "/publishCosFileVersion";
    }

    /**
     * 创建文件夹
     *
     * @param publishFiles
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:createFolder")
    @PostMapping("/createFolder")
    @ResponseBody
    public AjaxResult createFolder(PublishFiles publishFiles) {
        try {
            //先校验文件夹是否已经存在
            CosClientUtil cosClientUtil = new CosClientUtil();
            String filePath = publishFiles.getFilePath();
            String fileName = publishFiles.getFileName();
            boolean directoryExist = cosClientUtil.doesDirectoryExist(filePath + fileName+"/");
            if (directoryExist) {
                return AjaxResult.error("当前文件夹已存在，请重新命名！");
            }
            return toAjax(publishCosFilesService.createFolder(publishFiles));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }



    /**
     * 创建文件夹
     *
     * @param title
     * @return
     */
    @PostMapping("/checkPathExist")
    @ResponseBody
    public AjaxResult checkPathExist(String title) {
        try {
            //先校验文件夹是否已经存在
            CosClientUtil cosClientUtil = new CosClientUtil();
            boolean directoryExist = cosClientUtil.doesDirectoryExist(title);
            if (!directoryExist) {
                return AjaxResult.error("目录不存在请重新编辑！");
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
