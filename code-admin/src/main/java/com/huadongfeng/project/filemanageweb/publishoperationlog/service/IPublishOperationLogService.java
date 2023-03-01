package com.huadongfeng.project.filemanageweb.publishoperationlog.service;

import com.huadongfeng.project.filemanageweb.publishoperationlog.domain.PublishOperationLog;

import java.util.List;

/**
 * 操作日志 服务层
 * 
 * @author dongao
 * @date 2022-03-18
 */
public interface IPublishOperationLogService 
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
     * 删除操作日志信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deletePublishOperationLogByIds(String ids);

	/**
	 * 公共的保存操作日志方法
	 * @param platform
	 * @param operationType
	 * @param jobId
	 * @param detailId
	 * @param logName
	 * @param path
	 * @param fileName
	 * @param fileVersionId
	 * @return
	 */
	public int commonSaveOperationLog(int platform,int operationType,Long jobId,Long detailId,String logName,String path,String fileName ,Long fileVersionId);


	/**
	 * 模糊搜索列表的方法
	 * @param publishOperationLog
	 * @return
	 */
	List<PublishOperationLog> fuzzySelectOperationLogList(PublishOperationLog publishOperationLog);
}
