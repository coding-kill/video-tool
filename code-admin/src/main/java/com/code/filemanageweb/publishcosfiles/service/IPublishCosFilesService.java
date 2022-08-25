package com.code.filemanageweb.publishcosfiles.service;

import com.code.filemanageweb.publishcosfiles.domain.PublishFiles;
import com.ruoyi.common.core.page.PageDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:IContentService
 * @author:dongao
 * @date 2022/3/16 9:55
 */
public interface IPublishCosFilesService {

    /**
     * 获取目录结构
     * @return
     */
    List<Map> getTreeData(String name);

    /**
     * 获取当前选中目录下的子级目录及文件
     * @param publishFiles
     * @return
     */
    List<PublishFiles> selectContentList(PublishFiles publishFiles, PageDomain pageDomain);

    /**
     * 下载单个文件
     * @param request
     * @param response
     */
    void downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 下载文件夹及文件
     * @param request
     * @param response
     */
    void downloadDirectory(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 批量下载
     * @param request
     * @param response
     */
    void batchDownloadFiles(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 删除单个文件
     * @param title
     * @return
     */
    int deleteFile(String title);

    /**
     * 删除文件夹
     * @param title
     * @return
     */
    int deleteDirectory(String title);

    /**
     * 批量删除
     * @param request
     * @param response
     * @return
     */
    int batchRemoveFiles(HttpServletRequest request, HttpServletResponse response);

    /**
     * 重命名文件
     * @param publishFiles
     * @return
     */
    int renameFile(PublishFiles publishFiles);

    /**
     * 校验文件名是否重复
     * @param title
     * @return
     */
    String checkFileNameUnique(String title);

    /**
     * 创建文件夹
     * @param publishFiles
     * @return
     */
    int createFolder(PublishFiles publishFiles);
}
