package com.code.filemanageweb.publishcosfiles.controller;

import com.code.config.constants.Constants;
import com.code.config.properties.CosProperties;
import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.publishcosfiles.domain.PublishFiles;
import com.code.filemanageweb.publishcosfiles.service.IPublishCosFilesService;
import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.code.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.code.filemanageweb.upload.service.IStreamService;
import com.code.util.CosClientUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.StringUtils;
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
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Autowired
    private IPublishFileVersionService publishFileVersionService;
    @Autowired
    private IStreamService streamService;

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

        String fileName = request.getParameter("fileName");
        if(StringUtils.isNotEmpty(fileName)){
            request.setAttribute("name",fileName);
        }

        return prefix + "/publishCosFileVersion";
    }

    /**
     * 获取待编辑文件内容
     */
    @RequestMapping("/codeOnline")
    @RequiresPermissions("project:publishCosFiles:codeOnline")
    public String codeOnline(HttpServletRequest request,ModelMap mmap){
        //获取存储路径
        String path = request.getParameter("path");
        mmap.put("path",path);
        //获取文件名称
        String name = request.getParameter("name");
        mmap.put("name",name);
        //获取平台
        String platform = request.getParameter("platform");
        mmap.put("platform",platform);
        String text1path = null;
        try {
            //临时文件存放目录
            String profile = ConfigConstant.cosTempPath;
            //考虑到部分文件通过第三方平台上传至腾讯云，因此  版本库有且不混淆0/版本库没有==直接取腾讯云文件  版本库有且混淆1==取版本库原始文件
            PublishFileVersion version = publishFileVersionService.getLastFileVersion(Integer.parseInt(platform),path,name);
            if (Objects.nonNull(version)) {
                //版本库有
                if (version.getObfuscateFlag() == 1) {
                    //混淆了  获取备份路径
                    String bakpath = version.getBakPathPrefix();
                    //获取备份名称
                    String bakname = version.getBakName();
                    //file.text_0
                    String filename = version.getName();
                    //下载的文件名
                    String downname = filename +"_"+version.getVersionNum();
                    if (filename.contains(".")) {
                        downname = filename.replace(".","_"+version.getVersionNum()+".");
                    }
                    if(version.getObfuscateFlag()==1){
                        //	混淆的时候文件名用混淆的那个名字
                        bakname = version.getObfuscateSourceName();
                    }
                    //备份文件所在的路径 key
                    String versionkey = bakpath + bakname;
                    if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
                        CosClientUtil cosClientUtil = new CosClientUtil();
                        try {
                            //下载到服务器临时目录的文件路径及名称
                            text1path = profile + downname;
                            //开始下载
                            cosClientUtil.download(versionkey, text1path);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        text1path = bakpath+ "/"+bakname;
                    }
                }else {
                    //没有混淆
                    String versionkey = path + name;
                    if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
                        CosClientUtil cosClientUtil = new CosClientUtil();
                        try {
                            //下载到服务器临时目录的文件路径及名称
                            text1path = profile + name;
                            //开始下载
                            cosClientUtil.download(versionkey, text1path);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        text1path = path+ "/"+name;
                    }
                }
                mmap.put("versionId",version.getId());
            }else {
                //版本库没有  原始文件地址
                String versionkey = path + name;
                if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
                    CosClientUtil cosClientUtil = new CosClientUtil();
                    try {
                        //下载到服务器临时目录的文件路径及名称
                        text1path = profile + name;
                        //开始下载
                        cosClientUtil.download(versionkey, text1path);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    text1path = path+ "/"+name;
                }
            }
            //逐行读取文件内容
            BufferedReader reader;
            StringBuffer sb = new StringBuffer();
            mmap.put("filearea","");
            try {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(text1path), "utf-8"));
                while (reader.ready()) {
                    sb.append(reader.readLine()+"\r\n");
                }
                reader.close();
                mmap.put("filearea",sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
                //删除下载的临时文件
                if (StringUtils.isNotEmpty(text1path)) {
                    boolean delete = new File(text1path).delete();
                }
            }
        }
        return prefix + "/codeOnline";
    }

    /**
     * 保存在线编辑内容
     *
     * @param request
     * @return
     */
    @RequiresPermissions("project:publishCosFiles:codeOnline")
    @PostMapping("/saveCode")
    @ResponseBody
    public AjaxResult saveCode(HttpServletRequest request) {
        String filearea = request.getParameter("filearea");
        String fileName = request.getParameter("fileName");
        String versionId = request.getParameter("versionId");
        String platform = request.getParameter("platform");
        String path = request.getParameter("path");
        String remark = request.getParameter("remark");
        try {
            //保存文件编辑内容并上传
            streamService.editUpload(filearea,fileName,versionId,platform,path,remark);
            return AjaxResult.success("上传成功！");
        }catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("上传失败！");
        }
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
