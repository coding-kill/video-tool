package com.code.filemanageweb.publishoperationlog.mapper;


import com.code.filemanageweb.publishoperationlog.domain.PublishOperationLog;

import java.util.List;

/**
 * 操作日志 数据层
 * 
 * @author dongao
 * @date 2022-03-18
 */
public interface PublishOperationLogMapper 
{
	/**
     * 查询操作日志信息
     * 
     * @param id 操作日志ID
     * @return 操作日志信息
     */
	public PublishOperationLog selectPublishOperationLogById(Long id);
	
	/**
     * 查询操作日志列表
     * 
     * @param publishOperationLog 操作日志信息
     * @return 操作日志集合
     */
	public List<PublishOperationLog> selectPublishOperationLogList(PublishOperationLog publishOperationLog);
	
	/**
     * 新增操作日志
     * 
     * @param publishOperationLog 操作日志信息
     * @return 结果
     */
	public int insertPublishOperationLog(PublishOperationLog publishOperationLog);
	
	/**
     * 修改操作日志
     * 
     * @param publishOperationLog 操作日志信息
     * @return 结果
     */
	public int updatePublishOperationLog(PublishOperationLog publishOperationLog);
	
	/**
     * 删除操作日志
     * 
     * @param id 操作日志ID
     * @return 结果
     */
	public int deletePublishOperationLogById(Long id);
	
	/**
     * 批量删除操作日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deletePublishOperationLogByIds(String[] ids);

	/**
	 * 模糊搜索列表的方法
	 * @param publishOperationLog
	 * @return
	 */
	List<PublishOperationLog> fuzzySelectOperationLogList(PublishOperationLog publishOperationLog);

}