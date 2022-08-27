package com.code.filemanageweb.publishcmsfiles.service;

import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件版本 服务层
 * 
 * @author dongao
 * @date 2022-03-18
 */
public interface IPublishCmsFilesService
{

	/**
	 * 获取文件树
	 * @param name
	 * @return
	 */
	List<PublishFileVersion> getFilesTreeData(String name);

	/**
	 * 根据条件获取文件列表
	 * @param publishFileVersion
	 * @param pageDomain
	 * @return
	 */
	List<PublishFileVersion> getFilesByPath(PublishFileVersion publishFileVersion, PageDomain pageDomain);

	/**
	 * 创建文件夹方法
	 * @param request
	 * @return
	 */
	AjaxResult createFolder(HttpServletRequest request);

	/**
	 * 批量下载的方法
	 * @param filePath
	 * @param response
	 * @param platform
	 * @throws IOException
	 */
	void batchDownloadFiles(String[] filePath, HttpServletResponse response, Integer platform)throws IOException;

	/**
	 * 文件下载的方法
	 * @param request
	 * @param response
	 * @throws Exception
	 */
    void downloadFile(HttpServletRequest request, HttpServletResponse response)throws Exception;

	/**
	 * 删除文件或者文件夹的方法
	 * @param filePath
	 * @param platform
	 * @return
	 */
	AjaxResult deleteFile(String[] filePath, String platform);

	/**
	 * 文件重命名
	 * @param request
	 * @return
	 */
    AjaxResult renameFileSave(HttpServletRequest request);
}
