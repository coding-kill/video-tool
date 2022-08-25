package com.code.filemanageweb.publishfileversion.service;


import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.ruoyi.common.core.domain.AjaxResult;

import java.util.List;

/**
 * 文件版本 服务层
 * 
 * @author dongao
 * @date 2022-03-18
 */
public interface IPublishFileVersionService 
{
	/**
     * 查询文件版本信息
     * 
     * @param id 文件版本ID
     * @return 文件版本信息
     */
	public PublishFileVersion selectPublishFileVersionById(Long id);
	
	/**
     * 查询文件版本列表
     * 
     * @param publishFileVersion 文件版本信息
     * @return 文件版本集合
     */
	public List<PublishFileVersion> selectPublishFileVersionList(PublishFileVersion publishFileVersion);
	
	/**
     * 新增文件版本
     * 
     * @param publishFileVersion 文件版本信息
     * @return 结果
     */
	public int insertPublishFileVersion(PublishFileVersion publishFileVersion);
	
	/**
     * 修改文件版本
     * 
     * @param publishFileVersion 文件版本信息
     * @return 结果
     */
	public int updatePublishFileVersion(PublishFileVersion publishFileVersion);
		
	/**
     * 删除文件版本信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deletePublishFileVersionByIds(String ids);


	/**
	 * 根据条件查询最大版本号
	 * @param platform
	 * @param path
	 * @param name
	 * @return
	 */
	public Long getMaxVersionNum(int platform, String path, String name);

	/**
	 * 公共的保存版本日志
	 * @param platform
	 * @param jobId
	 * @param path
	 * @param name
	 * @param bakPath
	 * @param bakName
	 * @param type
	 * @param size
	 * @param delFlag
	 * @param versionNum
	 * @param remark
     * @return
	 */
	public Long commonSavePublishFileVersion(int platform, long jobId, String path, String name, String bakPath, String bakName, String type, long size, int delFlag, long versionNum, String remark, String obfuscateSourceName, Integer obfuscateFlag);

	/**
	 * cms查询版本list
	 * @param publishFileVersion
	 * @return
	 */
	List<PublishFileVersion> selectCmsFileVersionList(PublishFileVersion publishFileVersion);

	/**
	 * 根据条件获取单个文件所有的历史版本
	 * @param publishFileVersion
	 * @return
	 */
	List<PublishFileVersion> getOneFileHistory(PublishFileVersion publishFileVersion);

	/**
	 * 还原文件的方法
	 * @param id
	 * @return
	 */
	AjaxResult rollbackFile(Long id);

	/**
	 * 根据path name 获取最后一条版本记录信息
	 * @param path
	 * @param name
	 * @return
	 */
    PublishFileVersion getLastFileVersion(Integer platform, String path, String name);
}
