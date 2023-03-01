package com.huadongfeng.project.filemanageweb.publishoperationlog.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 操作日志表 ei_publish_operation_log
 * 
 * @author dongao
 * @date 2022-03-18
 */
public class PublishOperationLog extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** 主键id */
	private Long id;
	/** 平台 1腾讯云 2cms文件PC  3cms文件m */
	private Integer platform;
	/** 操作类型 1文件夹 2.文件 */
	private Integer operationType;
	/** 壳子任务id，直接操作为空 */
	private Long jobId;
	/** 关联任务明细id，直接操作为空 */
	private Long detailId;
	/** 事件名称 */
	private String logName;
	/** 日志对应文件目录 */
	private String path;
	/** 日志对应文件名称 */
	private String fileName;
	/** 是否有效 */
	private Integer isValid;
	/** 创建时间 */
	private Date createDate;
	/** 创建人 */
	private Long createUser;
	/** 更新时间 */
	private Date updateDate;
	/** 更新人 */
	private Long updateUser;
	/** 操作人中文 */
	private String createUserName;
	private Date createStartDate;
	private Date createEndDate;
	/** 当前目录 */
	private String nowPath;
	/** 模糊目录 */
	private String likePath;
    /** 文件版本id */
    private Long fileVersionId;


	public void setId(Long id) 
	{
		this.id = id;
	}

	public Long getId() 
	{
		return id;
	}
	public void setPlatform(Integer platform) 
	{
		this.platform = platform;
	}

	public Integer getPlatform() 
	{
		return platform;
	}
	public void setOperationType(Integer operationType) 
	{
		this.operationType = operationType;
	}

	public Integer getOperationType() 
	{
		return operationType;
	}
	public void setJobId(Long jobId) 
	{
		this.jobId = jobId;
	}

	public Long getJobId() 
	{
		return jobId;
	}
	public void setDetailId(Long detailId) 
	{
		this.detailId = detailId;
	}

	public Long getDetailId() 
	{
		return detailId;
	}
	public void setLogName(String logName) 
	{
		this.logName = logName;
	}

	public String getLogName() 
	{
		return logName;
	}
	public void setPath(String path) 
	{
		this.path = path;
	}

	public String getPath() 
	{
		return path;
	}
	public void setFileName(String fileName) 
	{
		this.fileName = fileName;
	}

	public String getFileName() 
	{
		return fileName;
	}
	public void setIsValid(Integer isValid) 
	{
		this.isValid = isValid;
	}

	public Integer getIsValid() 
	{
		return isValid;
	}
	public void setCreateDate(Date createDate) 
	{
		this.createDate = createDate;
	}

	public Date getCreateDate() 
	{
		return createDate;
	}
	public void setCreateUser(Long createUser)
	{
		this.createUser = createUser;
	}

	public Long getCreateUser()
	{
		return createUser;
	}
	public void setUpdateDate(Date updateDate) 
	{
		this.updateDate = updateDate;
	}

	public Date getUpdateDate() 
	{
		return updateDate;
	}
	public void setUpdateUser(Long updateUser)
	{
		this.updateUser = updateUser;
	}

	public Long getUpdateUser()
	{
		return updateUser;
	}

	@Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("platform", getPlatform())
            .append("operationType", getOperationType())
            .append("jobId", getJobId())
            .append("detailId", getDetailId())
            .append("logName", getLogName())
            .append("path", getPath())
            .append("fileName", getFileName())
            .append("isValid", getIsValid())
            .append("createDate", getCreateDate())
            .append("createUser", getCreateUser())
            .append("updateDate", getUpdateDate())
            .append("updateUser", getUpdateUser())
            .toString();
    }

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public Date getCreateStartDate() {
		return createStartDate;
	}

	public Date getCreateEndDate() {
		return createEndDate;
	}

	public void setCreateStartDate(Date createStartDate) {
		this.createStartDate = createStartDate;
	}

	public void setCreateEndDate(Date createEndDate) {
		this.createEndDate = createEndDate;
	}

	public String getNowPath() {
		return nowPath;
	}

	public void setNowPath(String nowPath) {
		this.nowPath = nowPath;
	}

	public String getLikePath() {
		return likePath;
	}

	public void setLikePath(String likePath) {
		this.likePath = likePath;
	}

    public Long getFileVersionId() {
        return fileVersionId;
    }

    public void setFileVersionId(Long fileVersionId) {
        this.fileVersionId = fileVersionId;
    }
}
