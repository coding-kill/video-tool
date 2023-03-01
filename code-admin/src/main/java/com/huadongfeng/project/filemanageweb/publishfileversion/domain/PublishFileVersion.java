package com.huadongfeng.project.filemanageweb.publishfileversion.domain;


import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 文件版本表 ei_publish_file_version
 * 
 * @author dongao
 * @date 2022-03-18
 */
public class PublishFileVersion extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** 主键id */
	private Long id;
	/** 平台类型。1腾讯云，2.cms的pc 3.cms的h5 */
	private Integer platform;
	/** 任务id，没有的时候默认0 */
	private Long jobId;
	/** 文件路径前缀 */
	private String path;
	/** 文件名称 */
	private String name;
	/** 备份路径的前缀 */
	private String bakPathPrefix;
	/** 备份的当前文件文件名称。path固定。 */
	private String bakName;
	/** 混淆前源文件名*/
	private String obfuscateSourceName;
	/** 是否混淆 0 否 1 是 */
	private Integer obfuscateFlag;
	/** 文件类型后缀 */
	private String type;
	/** 文件大小 */
	private Long size;
	/** 删除备份标志.1代表删除备份 */
	private Integer delFlag;
	/** 版本号 */
	private Long versionNum;
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
	/** 是否文件夹 */
	private boolean folderFlag;
	/** 备注 */
	private String remark;
	/** 操作人中文 */
	private String createUserName;
	private Date createStartDate;
	private Date createEndDate;
	/**是否需要按搜索条件筛选*/
	private String filterFlag;
	/**是否需要按搜索条件筛选*/
	private String sizeName;

	public Long getId() {
		return id;
	}

	public Integer getPlatform() {
		return platform;
	}

	public Long getJobId() {
		return jobId;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getBakPathPrefix() {
		return bakPathPrefix;
	}

	public String getBakName() {
		return bakName;
	}

	public String getType() {
		return type;
	}

	public Long getSize() {
		return size;
	}

	public Integer getDelFlag() {
		return delFlag;
	}

	public Long getVersionNum() {
		return versionNum;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public Long getUpdateUser() {
		return updateUser;
	}

	public boolean isFolderFlag() {
		return folderFlag;
	}

	@Override
	public String getRemark() {
		return remark;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public Date getCreateStartDate() {
		return createStartDate;
	}

	public Date getCreateEndDate() {
		return createEndDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBakPathPrefix(String bakPathPrefix) {
		this.bakPathPrefix = bakPathPrefix;
	}

	public void setBakName(String bakName) {
		this.bakName = bakName;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public void setVersionNum(Long versionNum) {
		this.versionNum = versionNum;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public void setFolderFlag(boolean folderFlag) {
		this.folderFlag = folderFlag;
	}

	public String getObfuscateSourceName() {
		return obfuscateSourceName;
	}

	public void setObfuscateSourceName(String obfuscateSourceName) {
		this.obfuscateSourceName = obfuscateSourceName;
	}

	public Integer getObfuscateFlag() {
		return obfuscateFlag;
	}

	public void setObfuscateFlag(Integer obfuscateFlag) {
		this.obfuscateFlag = obfuscateFlag;
	}

	public String getFilterFlag() {
		return filterFlag;
	}

	public void setFilterFlag(String filterFlag) {
		this.filterFlag = filterFlag;
	}

	@Override
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public void setCreateStartDate(Date createStartDate) {
		this.createStartDate = createStartDate;
	}

	public void setCreateEndDate(Date createEndDate) {
		this.createEndDate = createEndDate;
	}

	public String getSizeName() {
		return sizeName;
	}

	public void setSizeName(String sizeName) {
		this.sizeName = sizeName;
	}
}
