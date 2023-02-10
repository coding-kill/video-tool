package com.code.filemanageweb.publishfileversion.service;

import com.code.config.constants.Constants;
import com.code.config.properties.CosProperties;
import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.code.filemanageweb.publishfileversion.mapper.PublishFileVersionMapper;
import com.code.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.code.filemanageweb.upload.service.IStreamService;
import com.code.util.CosClientUtil;
import com.code.util.IoUtil;
import com.github.pagehelper.PageHelper;
import com.qcloud.cos.model.CopyResult;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.system.service.ISysUserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

/**
 * 文件版本 服务层实现
 * 
 * @author dongao
 * @date 2022-03-18
 */
@Service
public class PublishFileVersionServiceImpl implements IPublishFileVersionService 
{
	@Autowired
	private PublishFileVersionMapper publishFileVersionMapper;
	@Autowired
	private ISysUserService userService;

	@Autowired
	private IPublishOperationLogService publishOperationLogService;
	@Autowired
	private IStreamService streamService;
	@Autowired
	private CosProperties cosProperties;

	/**
     * 查询文件版本信息
     * 
     * @param id 文件版本ID
     * @return 文件版本信息
     */
    @Override
	public PublishFileVersion selectPublishFileVersionById(Long id)
	{
	    return publishFileVersionMapper.selectPublishFileVersionById(id);
	}
	
	/**
     * 查询文件版本列表
     * 
     * @param publishFileVersion 文件版本信息
     * @return 文件版本集合
     */
	@Override
	public List<PublishFileVersion> selectPublishFileVersionList(PublishFileVersion publishFileVersion)
	{
	    return publishFileVersionMapper.selectPublishFileVersionList(publishFileVersion);
	}
	
    /**
     * 新增文件版本
     * 
     * @param publishFileVersion 文件版本信息
     * @return 结果
     */
	@Override
	public int insertPublishFileVersion(PublishFileVersion publishFileVersion)
	{
	    return publishFileVersionMapper.insertPublishFileVersion(publishFileVersion);
	}
	
	/**
     * 修改文件版本
     * 
     * @param publishFileVersion 文件版本信息
     * @return 结果
     */
	@Override
	public int updatePublishFileVersion(PublishFileVersion publishFileVersion)
	{
	    return publishFileVersionMapper.updatePublishFileVersion(publishFileVersion);
	}

	/**
     * 删除文件版本对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	@Override
	public int deletePublishFileVersionByIds(String ids)
	{
		return publishFileVersionMapper.deletePublishFileVersionByIds(Convert.toStrArray(ids));
	}

	/**
	 * 根据条件查询最大的版本号
	 * @param platform
	 * @param path
	 * @param name
	 * @return
	 */
	@Override
	public Long getMaxVersionNum(int platform, String path, String name) {

		PublishFileVersion selectPublishFileVersion = new PublishFileVersion();
		selectPublishFileVersion.setPlatform(platform);
		selectPublishFileVersion.setPath(path);
		selectPublishFileVersion.setName(name);

		PageHelper.startPage(1,1);
		PageHelper.orderBy("version_num desc");
		List<PublishFileVersion> publishFileVersions = publishFileVersionMapper.selectPublishFileVersionList(selectPublishFileVersion);
		if(CollectionUtils.isEmpty(publishFileVersions)){
			return 0L;
		}
		PublishFileVersion publishFileVersion = publishFileVersions.get(0);
		return publishFileVersion.getVersionNum()+1;
	}

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
	@Override
	public Long commonSavePublishFileVersion(int platform, long jobId, String path, String name, String bakPath, String bakName, String type, long size, int delFlag, long versionNum, String remark,String obfuscateSourceName,Integer obfuscateFlag) {
		PublishFileVersion publishFileVersion = new PublishFileVersion();
		publishFileVersion.setPlatform(platform);
		publishFileVersion.setJobId(jobId);
		publishFileVersion.setPath(path);
		publishFileVersion.setName(name);
		publishFileVersion.setBakPathPrefix(bakPath);
		publishFileVersion.setBakName(bakName);
		publishFileVersion.setType(type);
		publishFileVersion.setSize(size);
		publishFileVersion.setDelFlag(delFlag);
		publishFileVersion.setVersionNum(versionNum);
		publishFileVersion.setCreateDate(new Date());
		publishFileVersion.setCreateUser(ShiroUtils.getUserId());
		publishFileVersion.setRemark(remark);
		publishFileVersion.setObfuscateFlag(obfuscateFlag);
		publishFileVersion.setObfuscateSourceName(obfuscateSourceName);

		int i = publishFileVersionMapper.insertPublishFileVersion(publishFileVersion);
		if(i>0){
			return publishFileVersion.getId();
		}
		return 0L;
	}

	/**
	 * cms查询版本list
	 * @param publishFileVersion
	 * @return
	 */
	@Override
	public List<PublishFileVersion> selectCmsFileVersionList(PublishFileVersion publishFileVersion) {
		List<PublishFileVersion> list = publishFileVersionMapper.selectCmsFileVersionList(publishFileVersion);
		for(PublishFileVersion getPublishFileVersion : list){
//			创建人名称
			if(getPublishFileVersion.getCreateUser()==null||getPublishFileVersion.getCreateUser()==0L){
				continue;
			}
			SysUser createUser = userService.selectUserById(getPublishFileVersion.getCreateUser());
			getPublishFileVersion.setCreateUserName(createUser.getUserName());
		}
		return list;
	}

	/**
	 * 根据条件获取单个文件的所有历史版本
	 * @param publishFileVersion
	 * @return
	 */
	@Override
	public List<PublishFileVersion> getOneFileHistory(PublishFileVersion publishFileVersion) {
		List<PublishFileVersion> oneFileHistory = publishFileVersionMapper.getOneFileHistory(publishFileVersion);
		for(PublishFileVersion getPublishFileVersion : oneFileHistory){
//			创建人名称
			if(getPublishFileVersion.getCreateUser()==null||getPublishFileVersion.getCreateUser()==0L){
				continue;
			}
			SysUser createUser = userService.selectUserById(getPublishFileVersion.getCreateUser());
			getPublishFileVersion.setCreateUserName(createUser.getUserName());
		}
		return oneFileHistory;
	}

	/**
	 * 还原文件的方法
	 * @param id
	 * @return
	 */
	@Override
	public AjaxResult rollbackFile(Long id) {
		PublishFileVersion publishFileVersion = publishFileVersionMapper.selectPublishFileVersionById(id);
		if(publishFileVersion==null){
			return AjaxResult.error("查询为空，请刷新重试");
		}
		if(Constants.Platform.COS.getValue().equals(publishFileVersion.getPlatform())){
//			平台为腾讯云的时候，走腾讯云相关的逻辑
			try {
				//获取当前选中版本备份文件的路径
				String bakPath = publishFileVersion.getBakPathPrefix();
				String bakName = publishFileVersion.getBakName();

				Long filesize = publishFileVersion.getSize();
				String obfuscateSourceName = null;
				Integer obfuscateFlag = null;
				//复制当前版本的备份为最新文件 ei_cos_bak/ei-dongao/image/common/2019/  23d5808-current-bak-1.jpg
				String filePath = publishFileVersion.getPath();
				String fileName = publishFileVersion.getName();
				String fileNamePre;
				String type = null;
				if (fileName.contains(".")) {
					fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
					type = publishFileVersion.getType();
				}else {
					fileNamePre = fileName;
				}
				CosClientUtil cosClientUtil = new CosClientUtil();
				String bucketName = cosProperties.getBucketName();
				CopyResult copyResult = cosClientUtil.copy(bucketName, bakPath + bakName, bucketName, filePath + fileName);
				if (copyResult != null) {
					//还原成功，同时对当前文件进行备份，获取新的备份名称
					//获取当前版本库中的最大版本号
					long maxVersionNum = getMaxVersionNum(Constants.Platform.COS.getValue(), filePath, fileName);
					if (type != null) {
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+type;
					}else {
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
					}

					obfuscateFlag = publishFileVersion.getObfuscateFlag();
					//如果是js css 文件需要同时备份混淆前源文件
					if (type != null && ("js".equals(type.toLowerCase()) || "css".equals(type.toLowerCase()))) {
						if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
							obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+type;
							//备份混淆前源文件
							cosClientUtil.copy(bucketName,bakPath+publishFileVersion.getObfuscateSourceName(),bucketName,bakPath+obfuscateSourceName);
						}
					}
					streamService.saveCosBak(fileName,filePath,filesize,filePath+fileName,
							ConfigConstant.cosBakPath+filePath,bakName,0,null,type,maxVersionNum,obfuscateSourceName,
							obfuscateFlag,Constants.OPERATION_DESCRIPTION.FILE_ROLLBACK.getDescription());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			try {
				String rootPath = ConfigConstant.allRootPath.get(publishFileVersion.getPlatform());
				String bakRootPath = rootPath + Constants.CMS_BAK_PATH_SUFFIX;

				// 先备份 在删除。之后成功的话删除备份，失败则回滚备份
				File currentFile = new File( publishFileVersion.getPath() + File.separator + publishFileVersion.getName());
				if (currentFile.exists()){
//					判断文件存在的时候进行备份
					File tempBakFile = IoUtil.getFile(publishFileVersion.getName()+"ei_temp_bak", publishFileVersion.getPath());
					tempBakFile.delete();
					Files.copy(IoUtil.getFile(publishFileVersion.getName(),publishFileVersion.getPath()).toPath(), tempBakFile.toPath());
				}
//				删除现有文件
				IoUtil.getFile(publishFileVersion.getName(),publishFileVersion.getPath()).delete();
//				复制需要还原的版本为最新版本并备份
				Files.copy(IoUtil.getFile(publishFileVersion.getBakName(),publishFileVersion.getBakPathPrefix()).toPath(), currentFile.toPath());
//				判断是否是混淆的，需要多备份版本等
				String bakPath = publishFileVersion.getPath().replace(rootPath,bakRootPath);

				if(publishFileVersion.getObfuscateFlag()==1){
//					混淆的时候走混淆的，获取原混淆文件备份
					String name = publishFileVersion.getName();
					Long maxVersionNum = getMaxVersionNum(publishFileVersion.getPlatform(), publishFileVersion.getPath(), name);

					String fileNamePre = name;
					String fileNameSuffix = "";
					//生成混淆前源文件名称
					String obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
					String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
					boolean contains = name.contains(".");
					if(contains){
						fileNamePre = name.substring(0,name.lastIndexOf("."));
						fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum + "." + fileNameSuffix;
						obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+ "." + fileNameSuffix;
					}

					//判断当前备份文件是否已经存在
					File bakFile = new File(bakPath + File.separator + bakName);
					if (!bakFile.getParentFile().exists()){
						bakFile.getParentFile().mkdirs();
					}
					while (bakFile.exists()){
//						判断如果文件已经存在，则版本号在加1
						maxVersionNum = maxVersionNum+1;
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
						if(contains){
							bakName = bakName + "."+fileNameSuffix;
						}
						bakFile = new File(bakPath + File.separator + bakName);
					}
//					新的混淆前文件备份file
					File obfuscateSourceBakFile = new File(bakPath + File.separator + obfuscateSourceName);
//					复制当前线上文件版本混淆前文件到最新的备份混淆前文件
					String oldSourcePath = publishFileVersion.getBakPathPrefix() +File.separator+ publishFileVersion.getObfuscateSourceName();
					File oldSourceFile = new File(oldSourcePath);
					if(oldSourceFile.exists()){
//						线上版本的混淆文件存在的时候，复制混淆前文件
						Files.copy(oldSourceFile.toPath(), obfuscateSourceBakFile.toPath());
					}
//					备份现有文件
					File oldBakFile = IoUtil.getFile(publishFileVersion.getBakName(), publishFileVersion.getBakPathPrefix());
					Files.copy(oldBakFile.toPath(), bakFile.toPath());

//					记录版本日志到数据库，记录操作日志到数据库
					Long i = commonSavePublishFileVersion(publishFileVersion.getPlatform(), 0, publishFileVersion.getPath(), publishFileVersion.getName(), bakPath, bakName, fileNameSuffix, oldBakFile.length(), 0, maxVersionNum, null,obfuscateSourceName,1);
					publishOperationLogService.commonSaveOperationLog(publishFileVersion.getPlatform(),Constants.OperationType.FILE.getValue(),null,null,"还原备份增加新版本",
							publishFileVersion.getPath(),publishFileVersion.getName(),i);

				}else {
//					备份现有文件
					streamService.saveCmsBak(publishFileVersion.getName(),publishFileVersion.getPath(),bakPath,0,publishFileVersion.getPlatform(),"还原备份增加新版本",null);
				}
//				备份完成删除临时备份文件
				IoUtil.getFile(publishFileVersion.getName()+"ei_temp_bak",publishFileVersion.getPath()).delete();

			} catch (Exception e) {
				try {
//					以上步骤失败的话则回滚文件
					IoUtil.getFile(publishFileVersion.getName(),publishFileVersion.getPath()).delete();
					Files.copy(IoUtil.getFile(publishFileVersion.getName()+"ei_temp_bak",publishFileVersion.getPath()).toPath(), new File( publishFileVersion.getPath() + File.separator + publishFileVersion.getName()).toPath());
					IoUtil.getFile(publishFileVersion.getName()+"ei_temp_bak",publishFileVersion.getPath()).delete();
//					记录失败日志到数据库
					publishOperationLogService.commonSaveOperationLog(publishFileVersion.getPlatform(),Constants.OperationType.FILE.getValue(),null,null,"还原失败",
							publishFileVersion.getPath(),publishFileVersion.getName(),0L);
				}catch (Exception ee){
					ee.printStackTrace();
					return AjaxResult.error("还原失败："+ee.getMessage());
				}
			}
		}

		return AjaxResult.success("还原成功");
	}

	/**
	 * 根据path name 获取最后一条记录
	 * @param path
	 * @param name
	 * @return
	 */
	@Override
	public PublishFileVersion getLastFileVersion(Integer platform,String path, String name) {
		return publishFileVersionMapper.getLastFileVersion(platform,path,name);
	}

}
