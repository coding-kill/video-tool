package com.code.filemanageweb.publishoperationlog.service;

import com.code.filemanageweb.publishoperationlog.domain.PublishOperationLog;
import com.code.filemanageweb.publishoperationlog.mapper.PublishOperationLogMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 操作日志 服务层实现
 * 
 * @author dongao
 * @date 2022-03-18
 */
@Service
public class PublishOperationLogServiceImpl implements IPublishOperationLogService 
{
	@Autowired
	private PublishOperationLogMapper publishOperationLogMapper;

	@Autowired
	private ISysUserService userService;

	/**
     * 查询操作日志信息
     * 
     * @param id 操作日志ID
     * @return 操作日志信息
     */
    @Override
	public PublishOperationLog selectPublishOperationLogById(Long id)
	{
	    return publishOperationLogMapper.selectPublishOperationLogById(id);
	}
	
	/**
     * 查询操作日志列表
     * 
     * @param publishOperationLog 操作日志信息
     * @return 操作日志集合
     */
	@Override
	public List<PublishOperationLog> selectPublishOperationLogList(PublishOperationLog publishOperationLog)
	{

		List<PublishOperationLog> publishOperationLogs = publishOperationLogMapper.selectPublishOperationLogList(publishOperationLog);
		for(PublishOperationLog getPublishOperationLog : publishOperationLogs){
//			创建人名称
			if(getPublishOperationLog.getCreateUser()==null||getPublishOperationLog.getCreateUser()==0L){
				continue;
			}
			SysUser createUser = userService.selectUserById(getPublishOperationLog.getCreateUser());
			getPublishOperationLog.setCreateUserName(createUser.getUserName());
		}
	    return publishOperationLogs;
	}
	
    /**
     * 新增操作日志
     * 
     * @param publishOperationLog 操作日志信息
     * @return 结果
     */
	@Override
	public int insertPublishOperationLog(PublishOperationLog publishOperationLog)
	{
	    return publishOperationLogMapper.insertPublishOperationLog(publishOperationLog);
	}
	
	/**
     * 修改操作日志
     * 
     * @param publishOperationLog 操作日志信息
     * @return 结果
     */
	@Override
	public int updatePublishOperationLog(PublishOperationLog publishOperationLog)
	{
	    return publishOperationLogMapper.updatePublishOperationLog(publishOperationLog);
	}

	/**
     * 删除操作日志对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	@Override
	public int deletePublishOperationLogByIds(String ids)
	{
		return publishOperationLogMapper.deletePublishOperationLogByIds(Convert.toStrArray(ids));
	}

	/**
	 * 公共的保存操作日志方法
	 * @param platform
	 * @param operationType
	 * @param jobId
	 * @param detailId
	 * @param logName
	 * @param path
	 * @param fileName
	 * @return
	 */
	@Override
	public int commonSaveOperationLog(int platform,int operationType,Long jobId,Long detailId,String logName,String path,String fileName) {
		PublishOperationLog publishOperationLog = new PublishOperationLog();
		publishOperationLog.setPlatform(platform);
		publishOperationLog.setOperationType(operationType);
		publishOperationLog.setJobId(jobId);
		publishOperationLog.setDetailId(detailId);
		publishOperationLog.setLogName(logName);
		publishOperationLog.setPath(path);
		publishOperationLog.setFileName(fileName);
		publishOperationLog.setCreateDate(new Date());
		publishOperationLog.setCreateUser(ShiroUtils.getUserId());

		int i = publishOperationLogMapper.insertPublishOperationLog(publishOperationLog);

		return i;
	}

	/**
	 * 模糊搜索列表的方法
	 * @param publishOperationLog
	 * @return
	 */
	@Override
	public List<PublishOperationLog> fuzzySelectOperationLogList(PublishOperationLog publishOperationLog) {
		List<PublishOperationLog> publishOperationLogs = publishOperationLogMapper.fuzzySelectOperationLogList(publishOperationLog);
		for(PublishOperationLog getPublishOperationLog : publishOperationLogs){
//			创建人名称
			if(getPublishOperationLog.getCreateUser()==null||getPublishOperationLog.getCreateUser()==0L){
				continue;
			}
			SysUser createUser = userService.selectUserById(getPublishOperationLog.getCreateUser());
			getPublishOperationLog.setCreateUserName(createUser.getUserName());
		}
		return publishOperationLogs;
	}

}
