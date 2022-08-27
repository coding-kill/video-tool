package com.code.filemanageweb.upload.service;


import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户和角色关联 服务层
 * 
 * @author dongao
 * @date 2022-03-04
 */
public interface IStreamService
{
	/**
     * 方法
     * @param req 请求
     * @param resp 返回
     * @return 用户和角色关联信息
     */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException;

	/**
	 * 方法
	 * @param req 请求
	 * @param resp 返回
	 * @return 用户和角色关联信息
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException ;

	/**
	 * 腾讯云保存备份文件
	 * @param fileName 只是文件名，不包含文件路径
	 * @param filePath 只是文件路径，不包含文件名
	 * @param fileSize
	 * @param srcPath 包含路径的文件名
	 * @param bakPath
	 * @throws StreamException
	 */
	void saveCosBak(String fileName, String filePath, long fileSize, String srcPath, String bakPath, String bakName,
                    int delFlag, String remark, String fileNameSuffix, Long maxVersionNum, String obfuscateSourceName,
                    Integer obfuscateFlag, String logName) throws StreamException ;

	/**
	 * 保存备份公共方法
	 * @param fileName
	 * @param filePath
	 * @param bakPath
	 * @param delFlag
	 * @param logPlatform
	 * @param logName
	 * @return
	 */
	public Long saveCmsBak(String fileName, String filePath, String bakPath, int delFlag, int logPlatform, String logName, String remark);

	/**
	 * 新增加的上传插件的公共方法
	 * @param request
	 * @param response
	 * @param upfile
	 */
	void webUploadPost(HttpServletRequest request, HttpServletResponse response, MultipartFile upfile)throws ServletException, IOException ;
}
