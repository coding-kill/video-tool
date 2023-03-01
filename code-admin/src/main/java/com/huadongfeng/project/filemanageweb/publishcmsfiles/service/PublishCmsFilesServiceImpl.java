package com.huadongfeng.project.filemanageweb.publishcmsfiles.service;

import com.huadongfeng.project.config.constants.Constants;
import com.huadongfeng.project.config.streamconfig.ConfigConstant;
import com.huadongfeng.project.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.huadongfeng.project.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.huadongfeng.project.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.huadongfeng.project.filemanageweb.upload.service.IStreamService;
import com.huadongfeng.project.util.CommonUtil;
import com.huadongfeng.project.util.FileUtil;
import com.huadongfeng.project.util.IoUtil;
import com.qcloud.cos.utils.UrlEncoderUtils;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件版本 服务层实现
 * 
 * @author dongao
 * @date 2022-03-18
 */
@Service
public class PublishCmsFilesServiceImpl implements IPublishCmsFilesService
{


	@Autowired
	private IStreamService streamService;
	@Autowired
	private IPublishFileVersionService publishFileVersionService;
	@Autowired
	private IPublishOperationLogService publishOperationLogService;

	@Override
	public List<PublishFileVersion> getFilesTreeData(String name) {
		if(StringUtils.isEmpty(name)){
			name = ConfigConstant.fileRepository;
		}
		List<PublishFileVersion> allFileInfo = FileUtil.getAllDictInfo(name);

		return allFileInfo;
	}

	/**
	 * 根据条件获取文件内容
	 * @param publishFileVersion
	 * @return
	 */
	@Override
	public List<PublishFileVersion> getFilesByPath(PublishFileVersion publishFileVersion, PageDomain pageDomain) {
		String path = publishFileVersion.getPath();
		String name = publishFileVersion.getName();
		String filterFlag = publishFileVersion.getFilterFlag();
		if(StringUtils.isEmpty(path)){
			path = ConfigConstant.fileRepository;
		}
		List<PublishFileVersion> allFileInfo = getAllFileInfo(path,name,pageDomain,filterFlag);
		return allFileInfo;
	}

    @Override
    public AjaxResult createFolder(HttpServletRequest request) {
//		首先判断文件夹是否已经存在
		String path = request.getParameter("path");
		String name = request.getParameter("name");
		String platform = request.getParameter("platform");
		if(StringUtils.isNotEmpty(path)){
			try {
				path = java.net.URLDecoder.decode(path,"UTF-8");
				path = java.net.URLDecoder.decode(path,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return AjaxResult.error("解析路径异常");
			}
		}
		path += "/"+name;

		File f = new File(path);
		if (f.exists()){
			return AjaxResult.error("文件夹已存在，请更换名称");
		}
		f.mkdirs();
		publishOperationLogService.commonSaveOperationLog(Integer.parseInt(platform),Constants.OperationType.FOLDER.getValue(),null,null,Constants.OPERATION_DESCRIPTION.CREATE_DIRECTORY.getDescription(),path,null,0L);
		return AjaxResult.success("创建成功");

    }

	/**
	 * 批量下载
	 * @param filePath
	 * @param response
	 * @param platform
	 */
	@Override
	public void batchDownloadFiles(String[] filePath, HttpServletResponse response,Integer platform) throws IOException {
		//输出流

		String fileName = "batchDownload.zip";
//		20230128新增
		response.reset();
		response.setCharacterEncoding("utf-8");
		response.setContentType("multipart/form-data");

		//设置压缩流：直接写入response，实现边压缩边下载
		ZipOutputStream zipos = null;
		try {
			zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
			zipos.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法
		} catch (Exception e) {
			e.printStackTrace();
		}
		String s = filePath[0];
		File nameFile = new File(s);
		File nameParentFile = nameFile.getParentFile();
		fileName = nameParentFile.getName()+".zip";
		response.setHeader("Content-Disposition", "attachment; filename="+ UrlEncoderUtils.encode(fileName));

		//遍历进行判断
		for (String path : filePath) {
			path = path.replaceAll("\\\\", "/");
			File file = new File(path);
			File parentFile = file.getParentFile();
			String parentParentPath = parentFile.getParentFile().toPath().toString();
			if(file.isDirectory()){
				parentParentPath = parentParentPath.replaceAll("\\\\", "/");
                getDirectory(path, zipos,parentParentPath,platform);
			}else {
				String parentPath = file.getParentFile().toPath().toString();
				parentParentPath = parentParentPath.replaceAll("\\\\", "/");

				String replace = path.replace(parentParentPath+"/", "");
				zipos.putNextEntry(new ZipEntry(replace));
//				20220402增加判断当前的版本库最新的版本是否是混淆的
				String name = file.getName();

				String fileNameSuffix = "";
				//生成混淆前源文件名称
				boolean contains = name.contains(".");
				if(contains){
					fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
				}
				if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//					当结尾是js或css的时候去查询数据版本
					PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(platform,parentPath,name);
					if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
//						混淆的时候下载去拿源文件
						String sourcePath = lastFileVersion.getBakPathPrefix() + File.separator + lastFileVersion.getObfuscateSourceName();
						file = new File(sourcePath);
					}
				}

				FileInputStream fin =new FileInputStream(file);
				byte[] bytes = new byte[1024*8];
				int len=fin.read(bytes);
				while(len!=-1){
					zipos.write(bytes,0,len);
					len=fin.read(bytes);
				}
				zipos.closeEntry();
				IOUtils.closeQuietly(fin);
			}
		}
		IOUtils.closeQuietly(zipos);
	}

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response)throws Exception {
        String path = request.getParameter("path");
        String platformStr = request.getParameter("platform");
		int platform = Integer.parseInt(platformStr);
		File file = new File(path);

        if (file.isDirectory()) {
//         文件夹的时候下载zip压缩文件
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ZipOutputStream zip = new ZipOutputStream(outputStream);

			String fileName = file.getName()+".zip";
//			20230128新增
			response.reset();
			response.setCharacterEncoding("utf-8");
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment; filename="+ UrlEncoderUtils.encode(fileName));

			String parentFile = file.getParentFile().toPath().toString();
			parentFile = parentFile.replaceAll("\\\\", "/");

			//设置压缩流：直接写入response，实现边压缩边下载
			ZipOutputStream zipos = null;
			try {
				zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
				zipos.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法
			} catch (Exception e) {
				e.printStackTrace();
			}
			getDirectory(path, zipos, parentFile,platform);
			IOUtils.closeQuietly(zipos);

        }else {
//          单个文件的时候直接下载流
//			20220402增加判断当前的版本库最新的版本是否是混淆的
			String parentPath = file.getParentFile().toPath().toString();
			parentPath = parentPath.replaceAll("\\\\", "/");
			String name = file.getName();

			String fileNameSuffix = "";
			//生成混淆前源文件名称
			boolean contains = name.contains(".");
			if(contains){
				fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
			}
			if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//					当结尾是js或css的时候去查询数据版本
				PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(platform,parentPath,name);
				if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
//						混淆的时候下载去拿源文件
					String sourcePath = lastFileVersion.getBakPathPrefix() + File.separator + lastFileVersion.getObfuscateSourceName();
					file = new File(sourcePath);
				}
			}

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis, 1024);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] cache = new byte[1024];
            int length = 0;
            while ((length = bis.read(cache)) != -1) {
                bos.write(cache, 0, length);
            }
            byte[] data = bos.toByteArray();
            genCode(response, data, name);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }
    }

	/**
	 * 删除文件
	 * @param filePath
	 * @param platform
	 * @return
	 */
	@Override
	public AjaxResult deleteFile(String[] filePath,String platform) {

		String rootPath = "";
		String bakRootPath = "";
		int logPlatform = Integer.parseInt(platform);
//		遍历获取当前平台的根路径及备份路径
		Set<Map.Entry<Integer, String>> entries = ConfigConstant.allRootPath.entrySet();
		for(Map.Entry<Integer, String> entry : entries){
			if(entry.getKey()==1){
//				跳过腾讯云获取其他本地平台
				continue;
			}
			if(entry.getKey()==logPlatform){
				rootPath = entry.getValue();
				bakRootPath = entry.getValue()+Constants.CMS_BAK_PATH_SUFFIX;
				break;
			}
		}

		StringBuffer sb = new StringBuffer();
		for(String path : filePath){
			File file = new File(path);
			if(file.isDirectory()){
//				增加删除的最外层路径是目录的时候直接记录删除日志。
				publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FOLDER.getValue(),null,null,Constants.OPERATION_DESCRIPTION.DIRECTORY_DELETE.getDescription(), path,null,0L);
				delDir(file,rootPath,bakRootPath,logPlatform,sb);
			}else {
				String floderPath = file.getParentFile().toPath().toString();
				floderPath = floderPath.replaceAll("\\\\", "/");
				String bakPath = floderPath.replaceAll(rootPath,bakRootPath);
//				判断后缀，判断是否备份混淆文件等
				String name = file.getName();

				String fileNamePre = name;
				String fileNameSuffix = "";
				//生成混淆前源文件名称
				boolean contains = name.contains(".");
				if(contains){
					fileNamePre = name.substring(0,name.lastIndexOf("."));
					fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
				}

				if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//					当结尾是js或css的时候去查询数据版本
					PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(logPlatform,floderPath,name);
					if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
						boolean flag = delCmsObfBakSave(logPlatform, path, file, floderPath, bakPath, name, fileNamePre, fileNameSuffix, lastFileVersion);
						if(!flag){
//							删除失败的时候加入失败信息
							sb.append(file.toPath().toString());
							sb.append(";");
						}
						continue;
					}
				}
				Long id = streamService.saveCmsBak(file.getName(), floderPath, bakPath, 1, logPlatform,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription(),null);
				boolean delete = file.delete();
				if(!delete){
//						删除失败的时候将版本库的备份版本日志删除
					publishFileVersionService.deletePublishFileVersionByIds(id.toString());
					sb.append(file.toPath().toString());
					sb.append(";");

				}
			}
		}
		if(sb.length()>0){
			return AjaxResult.error("部分文件删除失败："+sb.toString());
		}
		return AjaxResult.success("删除成功");
	}

	/**
	 * 删除的时候增加是否混淆等规则判断备份公共方法
	 * @param logPlatform
	 * @param path
	 * @param file
	 * @param floderPath
	 * @param bakPath
	 * @param name
	 * @param fileNamePre
	 * @param fileNameSuffix
	 * @param lastFileVersion
	 * @return
	 */
	private boolean delCmsObfBakSave(int logPlatform, String path, File file, String floderPath, String bakPath, String name, String fileNamePre, String fileNameSuffix, PublishFileVersion lastFileVersion) {
		try {
//			混淆的时候删除需要备份当前文件和混淆文件。
			Long maxVersionNum = publishFileVersionService.getMaxVersionNum(logPlatform, floderPath, name);
			//生成混淆前源文件名称
			String obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
			String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
			if(StringUtils.isNotEmpty(fileNameSuffix)){
				obfuscateSourceName = obfuscateSourceName+"."+fileNameSuffix;
				bakName = bakName+"."+fileNameSuffix;
			}

			//判断当前备份文件是否已经存在
			File bakFile = new File(bakPath + File.separator + bakName);
			if (!bakFile.getParentFile().exists()){
				bakFile.getParentFile().mkdirs();
			}
			while (bakFile.exists()){
//				判断如果文件已经存在，则版本号在加1
				maxVersionNum = maxVersionNum+1;
				obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
				bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
				if(StringUtils.isNotEmpty(fileNameSuffix)){
					obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
					bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
				}
				bakFile = new File(bakPath + File.separator + bakName);
			}
//			新的混淆前文件备份file
			File obfuscateSourceBakFile = new File(bakPath + File.separator + obfuscateSourceName);

//			复制当前线上文件版本混淆前文件到最新的备份混淆前文件
			String oldSourcePath = lastFileVersion.getBakPathPrefix() +File.separator+ lastFileVersion.getObfuscateSourceName();
			File oldSourceFile = new File(oldSourcePath);
			if(oldSourceFile.exists()){
//				线上版本的混淆文件存在的时候，复制
				Files.copy(oldSourceFile.toPath(), obfuscateSourceBakFile.toPath());
			}
            long fileSize = file.length();
//			移动当前线上文件到备份
			Files.move(file.toPath(), bakFile.toPath());
//			删除现有文件
//			记录版本日志和操作日志
			Long i = publishFileVersionService.commonSavePublishFileVersion(logPlatform, 0, floderPath, name, bakPath, bakName, fileNameSuffix, fileSize, 1, maxVersionNum, null,obfuscateSourceName,1);
			publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription(), path,name,i);

		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 文件重命名
	 * @param request
	 * @return
	 */
	@Override
	public AjaxResult renameFileSave(HttpServletRequest request) {
		String path = request.getParameter("path");
		String name = request.getParameter("name");
		String pastName = request.getParameter("pastName");
		String platformStr = request.getParameter("platform");
		int platform = Integer.parseInt(platformStr);
		path = path.replaceAll("\\\\", "/");
//		先校验文件是否已经存在
		File laterFile = new File( path + File.separator + name);
		if(laterFile.exists()){
			return AjaxResult.error("重命名的文件已存在，请重新修改名称");
		}
		File pastFile = new File( path + File.separator + pastName);
		String rootPath = ConfigConstant.allRootPath.get(platform);
		String bakRootPath = rootPath + Constants.CMS_BAK_PATH_SUFFIX;
		String bakPath = path.replace(rootPath,bakRootPath);

//		20220402增加先判断后缀，判断原名称文件是否备份混淆文件等
		String fileNamePre = pastName;
		String fileNameSuffix = "";
		//生成混淆前源文件名称
		boolean contains = pastName.contains(".");
		if(contains){
			fileNamePre = pastName.substring(0,pastName.lastIndexOf("."));
			fileNameSuffix = pastName.substring(pastName.lastIndexOf(".")+1);
		}

		if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//			当结尾是js或css的时候去查询数据重命名前文件的最新版本
			PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(platform,path,pastName);
			if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
				try {
//					混淆的时候删除需要备份当前文件和混淆文件。
					Long maxVersionNum = publishFileVersionService.getMaxVersionNum(platform, path, pastName);
					//生成混淆前源文件名称
					String obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
					String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
					if(contains){
						obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
					}
					//判断当前备份文件是否已经存在
					File bakFile = new File(bakPath + File.separator + bakName);
					if (!bakFile.getParentFile().exists()){
						bakFile.getParentFile().mkdirs();
					}
					while (bakFile.exists()){
//						判断如果文件已经存在，则版本号在加1
						maxVersionNum = maxVersionNum+1;
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
						if(contains){
							obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
							bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
						}
						bakFile = new File(bakPath + File.separator + bakName);
					}
//					新的混淆前文件备份file
					File obfuscateSourceBakFile = new File(bakPath + File.separator + obfuscateSourceName);
//					复制当前线上文件版本混淆前文件到最新的备份混淆前文件
					String oldSourcePath = lastFileVersion.getBakPathPrefix() +File.separator+ lastFileVersion.getObfuscateSourceName();
					File oldSourceFile = new File(oldSourcePath);
					if(oldSourceFile.exists()){
//						线上版本的混淆文件存在的时候，复制混淆前文件
						Files.copy(oldSourceFile.toPath(), obfuscateSourceBakFile.toPath());
					}
//					复制当前线上文件到备份
					Files.copy(pastFile.toPath(), bakFile.toPath());

//					新增加命名后的文件和文件版本
					Long renameMaxVersionNum = publishFileVersionService.getMaxVersionNum(platform, path, name);
					String renameFileNamePre = name;
					String renameFileNameSuffix = "";
					//生成混淆前源文件名称
					String renameObfuscateSourceName = renameFileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+renameMaxVersionNum;
					String renameBakName = renameFileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+renameMaxVersionNum;
					boolean renameContains = name.contains(".");
					if(renameContains){
						renameFileNamePre = name.substring(0,name.lastIndexOf("."));
						renameFileNameSuffix = name.substring(name.lastIndexOf(".")+1);
						renameObfuscateSourceName = renameFileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+renameMaxVersionNum+"."+renameFileNameSuffix;
						renameBakName = renameFileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+renameMaxVersionNum+"."+renameFileNameSuffix;
					}

					//判断当前备份文件是否已经存在
					File renameBakFile = new File(bakPath + File.separator + renameBakName);
					if (!renameBakFile.getParentFile().exists()){
						renameBakFile.getParentFile().mkdirs();
					}
					while (renameBakFile.exists()){
//						判断如果文件已经存在，则版本号在加1
						renameMaxVersionNum = renameMaxVersionNum+1;
						renameObfuscateSourceName = renameFileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+renameMaxVersionNum;
						renameBakName = renameFileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+renameMaxVersionNum;
						if(renameContains){
							renameObfuscateSourceName = renameFileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+renameMaxVersionNum+"."+renameFileNameSuffix;
							renameBakName = renameFileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+renameMaxVersionNum+"."+renameFileNameSuffix;
						}
						renameBakFile = new File(bakPath + File.separator + renameBakName);
					}
//					复制一份上边备份的文件到当前的命名后的文件。然后再备份相关的。
					Files.copy(bakFile.toPath(), laterFile.toPath());
//					备份一份到命名后的备份文件
					Files.copy(bakFile.toPath(), renameBakFile.toPath());

					if(oldSourceFile.exists()){
						File renameObfuscateSourceFile = new File(bakPath + File.separator + renameObfuscateSourceName);
//						线上版本的混淆文件存在的时候，复制
						Files.copy(oldSourceFile.toPath(), renameObfuscateSourceFile.toPath());
					}
//					记录版本日志和操作日志
					Long i1 = publishFileVersionService.commonSavePublishFileVersion(platform, 0, path, name, bakPath, renameBakName, fileNameSuffix, laterFile.length(), 0, maxVersionNum, null,renameObfuscateSourceName,1);
					publishOperationLogService.commonSaveOperationLog(platform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_RENAME.getDescription(), path,name,i1);

//					删除原有文件
					pastFile.delete();
//					记录版本日志和操作日志
					Long i = publishFileVersionService.commonSavePublishFileVersion(platform, 0, path, pastName, bakPath, bakName, fileNameSuffix, pastFile.length(), 1, maxVersionNum, null,obfuscateSourceName,1);
					publishOperationLogService.commonSaveOperationLog(platform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription(), path,pastName,i);


				}catch (Exception e){
					e.printStackTrace();
					try {
//						以上步骤失败的话则回滚文件
						IoUtil.getFile(name,path).delete();
//						记录失败日志到数据库
						publishOperationLogService.commonSaveOperationLog(platform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_RENAME.getDescription()+Constants.OPERATION_DESCRIPTION.OPERATION_FAILED.getDescription(),
								path,pastName,0L);
					}catch (Exception ee){
						ee.printStackTrace();
						return AjaxResult.error("重命名异常："+e.getMessage()+ee.getMessage());
					}
					return AjaxResult.error("重命名异常："+e.getMessage());
				}
                return AjaxResult.success("重命名成功");
            }
		}
//			先创建新的文件并备份，然后删除旧的文件。
        try {
//			备份现有文件打
            streamService.saveCmsBak(pastName,path,bakPath,1,platform,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription(),null);
//			复制文件到重命名之后的文件
            Files.move(IoUtil.getFile(pastName,path).toPath(), laterFile.toPath());
//			备份重命名后的文件
            streamService.saveCmsBak(name,path,bakPath,0,platform,Constants.OPERATION_DESCRIPTION.FILE_RENAME.getDescription(),null);
//			删除原有文件
        } catch (Exception e) {
            e.printStackTrace();
            try {
//				以上步骤失败的话则回滚文件
                IoUtil.getFile(name,path).delete();
//				记录失败日志到数据库
				publishOperationLogService.commonSaveOperationLog(platform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_RENAME.getDescription()+Constants.OPERATION_DESCRIPTION.OPERATION_FAILED.getDescription(),
                        path,pastName,0L);
            }catch (Exception ee){
                ee.printStackTrace();
                return AjaxResult.error("重命名异常："+e.getMessage()+ee.getMessage());
            }
            return AjaxResult.error("重命名异常："+e.getMessage());
		}

		return AjaxResult.success("重命名成功");
	}

	/**
	 * 删除文件夹的递归方法
	 * @param file
	 * @param rootPath
	 * @param bakRootPath
	 * @param logPlatform
	 */
	public void delDir(File file,String rootPath,String bakRootPath,int logPlatform,StringBuffer sb) {
		if (file.isDirectory()) {
			File childFiles[] = file.listFiles();
			for (File childFile : childFiles) {
				delDir(childFile,rootPath,bakRootPath,logPlatform,sb);
			}
//			所有的子文件夹及文件删除后删除该文件夹壳子
			boolean delete = file.delete();
			if(!delete){
				sb.append(file.toPath().toString());
				sb.append(";");
			}
		} else {
			String path = file.getParentFile().toPath().toString();
			path = path.replaceAll("\\\\", "/");
			String bakPath = path.replaceAll(rootPath,bakRootPath);

//			判断后缀，判断是否备份混淆文件等
			String name = file.getName();

			String fileNamePre = name;
			String fileNameSuffix = "";
			//生成混淆前源文件名称
			boolean contains = name.contains(".");
			if(contains){
				fileNamePre = name.substring(0,name.lastIndexOf("."));
				fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
			}
			if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//					当结尾是js或css的时候去查询数据版本
				PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(logPlatform,path,name);
				if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
					boolean flag = delCmsObfBakSave(logPlatform, path, file, path, bakPath, name, fileNamePre, fileNameSuffix, lastFileVersion);
					if(!flag){
//						删除失败的时候加入失败信息
						sb.append(file.toPath().toString());
						sb.append(";");
					}
				}
			}else {
				Long id = streamService.saveCmsBak(file.getName(), path, bakPath, 1, logPlatform,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription(),null);
				boolean delete = file.delete();
				if(!delete){
//					删除失败的时候将版本库的备份版本日志删除
					publishFileVersionService.deletePublishFileVersionByIds(id.toString());
					sb.append(file.toPath().toString());
					sb.append(";");
				}
			}
		}
	}

	public List<PublishFileVersion> getAllFileInfo(String path,String putName,PageDomain pageDomain,String filterFlag) {
		List<PublishFileVersion> allFileInfoList = new ArrayList<PublishFileVersion>();
		File file = new File(path);
		if(!file.exists()){
			return allFileInfoList;
		}

		File[] files = file.listFiles();
		List<PublishFileVersion> dirInfoList = new ArrayList<PublishFileVersion>();
		List<PublishFileVersion> fileInfoList = new ArrayList<PublishFileVersion>();
		boolean permissions = CommonUtil.getPermissions();
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			String getPath = files[i].toPath().toString();
			if (!permissions && getPath.contains("ei_bak")) {
				//不能展示备份目录
				continue;
			}
//			先判断名称是否包含搜索名称，不包含的时候直接跳过
			if(!"no".equals(filterFlag)&&StringUtils.isNotEmpty(putName)&&!fileName.toLowerCase().contains(putName.toLowerCase())){
				continue;
			}
			PublishFileVersion publishFileVersion = new PublishFileVersion();
			publishFileVersion.setName(fileName);
			publishFileVersion.setSize(files[i].length());
			publishFileVersion.setUpdateDate(new Date(files[i].lastModified()));
			getPath = getPath.replaceAll("\\\\", "/");
			publishFileVersion.setPath(getPath);
			publishFileVersion.setFolderFlag(false);
			publishFileVersion.setSizeName("");
			if (files[i].isDirectory()) {
				publishFileVersion.setFolderFlag(true);
				publishFileVersion.setType("文件夹");
				dirInfoList.add(publishFileVersion);
			}else {
				if(fileName.contains(".")){
					String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
					publishFileVersion.setType(suffix);
//					判断是图片的时候获取大小
					boolean imgFlag = "jpg".equals(suffix.toLowerCase()) || "png".equals(suffix.toLowerCase()) || "gif".equals(suffix.toLowerCase()) || "jpeg".equals(suffix.toLowerCase());
					if(imgFlag){
						FileInputStream fileInputStream = null;
						try {
							fileInputStream = new FileInputStream(files[i]);
							BufferedImage read = ImageIO.read(fileInputStream);
							String sizeName = "("+read.getWidth()+"*"+read.getHeight()+")";
							publishFileVersion.setSizeName(sizeName);
						}catch (Exception e){
							e.printStackTrace();
						}finally {
							IoUtil.close(fileInputStream);
						}
					}
				}else {
					publishFileVersion.setType("-");
				}

				fileInfoList.add(publishFileVersion);
			}
		}
//		文件夹排序
		List<PublishFileVersion> sortDirInfoList = dirInfoList.stream()
				.sorted(Comparator.comparing(PublishFileVersion::getName))
				.collect(Collectors.toList());
//		文件排序
		List<PublishFileVersion> sortFileInfoList = fileInfoList.stream()
				.sorted(Comparator.comparing(PublishFileVersion::getName))
				.collect(Collectors.toList());

		allFileInfoList.addAll(sortDirInfoList);
		allFileInfoList.addAll(sortFileInfoList);

		//排序操作
		String orderBy = pageDomain.getOrderByColumn();
		String isAsc = pageDomain.getIsAsc();
		if (StringUtils.isNotEmpty(orderBy) && CollectionUtils.isNotEmpty(allFileInfoList)) {
			if ("name".equals(orderBy)) {
				//按文件名称排序
				if ("asc".equals(isAsc)) {
					//升序
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getName)).collect(Collectors.toList());
				}else {
					//降序
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getName).reversed()).collect(Collectors.toList());
				}
			}else if ("type".equals(orderBy)) {
				//按文件类型排序
				if ("asc".equals(isAsc)) {
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getType)).collect(Collectors.toList());
				}else {
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getType).reversed()).collect(Collectors.toList());
				}
			}else if ("updateDate".equals(orderBy)) {
				//按更新时间排序
				if ("asc".equals(isAsc)) {
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getUpdateDate)).collect(Collectors.toList());
				}else {
					allFileInfoList = allFileInfoList.stream().sorted(Comparator.comparing(PublishFileVersion::getUpdateDate).reversed()).collect(Collectors.toList());
				}
			}
		}


		return allFileInfoList;
	}

	/**
	 * 生成文件 or 生成zip文件
	 *
	 * @param response
	 * @param data
	 * @throws IOException
	 */
	private void genCode(HttpServletResponse response, byte[] data,String fileName) throws IOException
	{
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename="+ UrlEncoderUtils.encode(fileName));
		response.addHeader("Content-Length", "" + data.length);
		response.setContentType("application/octet-stream; charset=UTF-8");
		IOUtils.write(data, response.getOutputStream());
	}

	/**
	 * 获取腾讯云目录及文件
	 * @param path
	 * @param zip
	 * @throws IOException
	 */
	private void getDirectory(String path, ZipOutputStream zip,String prefix,Integer platform) throws IOException {
		File file = new File(path);
		File[] files = file.listFiles();
		for (File getFile : files) {
			String currentPath = getFile.toPath().toString();
			currentPath = currentPath.replaceAll("\\\\", "/");

			if(getFile.isDirectory()){
//				文件夹的时候组装里边继续递归
//				递归查询
				getDirectory(currentPath,zip,prefix,platform);
			}else {
//				文件的时候直接把流放入zip
				String replace = currentPath.replace(prefix+"/", "");
				zip.putNextEntry(new ZipEntry(replace));

//				20220402增加判断当前的版本库最新的版本是否是混淆的
				String parentPath = getFile.getParentFile().toPath().toString();
				parentPath = parentPath.replaceAll("\\\\", "/");
				String name = getFile.getName();

				String fileNameSuffix = "";
				boolean contains = name.contains(".");
				if(contains){
					fileNameSuffix = name.substring(name.lastIndexOf(".")+1);
				}
				if("js".equals(fileNameSuffix.toLowerCase())||"css".equals(fileNameSuffix.toLowerCase())){
//					当结尾是js或css的时候去查询数据版本
					PublishFileVersion lastFileVersion = publishFileVersionService.getLastFileVersion(platform,parentPath,name);
					if(lastFileVersion!=null&&lastFileVersion.getObfuscateFlag()==1){
//						混淆的时候下载去拿源文件
						String sourcePath = lastFileVersion.getBakPathPrefix() + File.separator + lastFileVersion.getObfuscateSourceName();
                        getFile = new File(sourcePath);
					}
				}

				FileInputStream fin =new FileInputStream(getFile);

				byte[] bytes = new byte[1024*8];
				int len=fin.read(bytes);
				while(len!=-1){
					zip.write(bytes,0,len);
					len=fin.read(bytes);
				}
				zip.closeEntry();
                IOUtils.closeQuietly(fin);
			}
		}

	}

}
