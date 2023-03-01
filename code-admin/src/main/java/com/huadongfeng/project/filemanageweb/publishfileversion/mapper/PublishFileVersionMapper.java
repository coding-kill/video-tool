package com.huadongfeng.project.filemanageweb.publishfileversion.mapper;

import com.huadongfeng.project.filemanageweb.publishfileversion.domain.PublishFileVersion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件版本 数据层
 * 
 * @author dongao
 * @date 2022-03-18
 */
@Repository
public interface PublishFileVersionMapper 
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
     * 删除文件版本
     * 
     * @param id 文件版本ID
     * @return 结果
     */
	public int deletePublishFileVersionById(Long id);
	
	/**
     * 批量删除文件版本
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deletePublishFileVersionByIds(String[] ids);

	/**
	 * cms查询版本list
	 * @param publishFileVersion
	 * @return
	 */
	List<PublishFileVersion> selectCmsFileVersionList(PublishFileVersion publishFileVersion);

	/**
	 * 根据条件获取单个文件的所有历史版本
	 * @param publishFileVersion
	 * @return
	 */
	List<PublishFileVersion> getOneFileHistory(PublishFileVersion publishFileVersion);

	/**
	 * 根据path name 获取最后一条记录
	 * @param path
	 * @param name
	 * @return
	 */
    PublishFileVersion getLastFileVersion(@Param("platform") Integer platform,@Param("path") String path, @Param("name") String name);
}