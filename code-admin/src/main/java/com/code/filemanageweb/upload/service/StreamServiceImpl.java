package com.code.filemanageweb.upload.service;

import com.alibaba.fastjson.JSON;
import com.code.config.constants.Constants;
import com.code.config.properties.CosProperties;
import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.code.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.code.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.code.filemanageweb.upload.domain.Range;
import com.code.util.CosClientUtil;
import com.code.util.IoUtil;
import com.code.util.StreamCosClientUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Upload;
import com.qcloud.cos.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.*;

/**
 * File reserved servlet, mainly reading the request parameter and its file
 * part, stored it.
 * @author dongao
 */
@Service
public class StreamServiceImpl implements IStreamService{
	private static final long serialVersionUID = -8619685235661387895L;
	/** when the has increased to 10kb, then flush it to the hard-disk. */
	static final int BUFFER_LENGTH = 10240;
	static final String START_FIELD = "start";
	public static final String CONTENT_RANGE_HEADER = "content-range";

	private static CosProperties cosProperties = SpringUtils.getBean(CosProperties.class);
	public static final Logger logger = LoggerFactory.getLogger(StreamServiceImpl.class);



	@Autowired
	private IPublishOperationLogService publishOperationLogService;

	@Autowired
	private IPublishFileVersionService publishFileVersionService;

	/**
	 * Lookup where's the position of this file?
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doOptions(req, resp);

		final String token = req.getParameter(TokenServiceImpl.TOKEN_FIELD);
		final String size = req.getParameter(TokenServiceImpl.FILE_SIZE_FIELD);
		final String fileName = req.getParameter(TokenServiceImpl.FILE_NAME_FIELD);
		final String fileDownLoad = req.getParameter("param1");
		final PrintWriter writer = resp.getWriter();

		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";

//		20220322增加路径的传递获取
		String param2 = req.getParameter("param2");
		if(!StringUtils.isNullOrEmpty(param2)){
			try {
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			}
		}

		/** TODO: validate your token. */
		//本地上传
		if(StringUtils.isNullOrEmpty(fileDownLoad)||fileDownLoad.equals("local")){
			try {
				File f = IoUtil.getTokenedFile(token,param2);
				start = f.length();
				/** file size is 0 bytes. */
				if (token.endsWith("_0") && "0".equals(size) && 0 == start){
					f.renameTo(IoUtil.getFile(fileName,param2));
				}
			} catch (FileNotFoundException fne) {
				message = "Error: " + fne.getMessage();
				success = false;
			} finally {
				try {
					if (success)
						json.put(START_FIELD, start);
					json.put(TokenServiceImpl.SUCCESS, success);
					json.put(TokenServiceImpl.MESSAGE, message);
				} catch (JSONException e) {}
				writer.write(json.toString());
				IoUtil.close(writer);
			}
		}
		//腾讯云上传
		else {
			try {
				File f = IoUtil.getTokenedFile(token,param2);
				start = f.length();

			} catch (FileNotFoundException fne) {
				message = "Error: " + fne.getMessage();
				success = false;
			} finally {
				/** file size is 0 bytes. */
				if (token.endsWith("_0") && "0".equals(size) && 0 == start){
					String filetype = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
					StreamCosClientUtil.uploadFileToCos(req.getInputStream(),0L, fileName,filetype);
					System.out.println("TK: `" + token + "`, NE: `" + fileName + "`");
					/** if `STREAM_DELETE_FINISH`, then delete it. */
					if (ConfigConstant.isDeleteFinished) {
						IoUtil.getFile(token,param2).delete();
					}
				}
				try {
					if (success)
						json.put(START_FIELD, start);
					json.put(TokenServiceImpl.SUCCESS, success);
					json.put(TokenServiceImpl.MESSAGE, message);
				} catch (JSONException e) {}
				writer.write(json.toString());
				IoUtil.close(writer);
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doOptions(req, resp);
		String token = req.getParameter(TokenServiceImpl.TOKEN_FIELD);
		String fileName = req.getParameter(TokenServiceImpl.FILE_NAME_FIELD);
		String fileDownLoad = req.getParameter("param1");
		String param3 = req.getParameter("param3");
		String remark = req.getParameter("remark");
		String jsparam = req.getParameter("jsparam");
		String cssparam = req.getParameter("cssparam");

//		20220322增加路径的传递获取
		String param2 = req.getParameter("param2");
		if(!StringUtils.isNullOrEmpty(param2)){
			try {
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			}
		}

		Range range = IoUtil.parseRange(req);
		OutputStream out = null;
		InputStream content = null;
		final PrintWriter writer = resp.getWriter();

		/** TODO: validate your token. */
		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";
		File f = IoUtil.getTokenedFile(token,param2);

		if(StringUtils.isNullOrEmpty(fileDownLoad)||fileDownLoad.equals("local")){
			try {
				if (f.length() != range.getFrom()) {
					/** drop this uploaded data */
					throw new StreamException(StreamException.ERROR_FILE_RANGE_START);
				}
				out = new FileOutputStream(f, true);
				content = req.getInputStream();
				int read = 0;
				final byte[] bytes = new byte[BUFFER_LENGTH];
				while ((read = content.read(bytes)) != -1){
					out.write(bytes, 0, read);
				}
				start = f.length();
			} catch (StreamException se) {
				success = StreamException.ERROR_FILE_RANGE_START == se.getCode();
				message = "Code: " + se.getCode();
			} catch (FileNotFoundException fne) {
				message = "Code: " + StreamException.ERROR_FILE_NOT_EXIST;
				success = false;
			} catch (IOException io) {
				message = "IO Error: " + io.getMessage();
				success = false;
			} finally {
				IoUtil.close(out);
				IoUtil.close(content);

				String bakPath = "";
				param2 = param2.replaceAll("\\\\", "/");
				int logPlatform = Integer.parseInt(param3);
//				遍历获取当前平台的根路径及备份路径
				Set<Map.Entry<Integer, String>> entries = ConfigConstant.allRootPath.entrySet();
				for(Map.Entry<Integer, String> entry : entries){
					if(entry.getKey()==1){
//						跳过腾讯云获取其他本地平台
						continue;
					}
					if(entry.getKey()==logPlatform){
						bakPath = param2.replace(entry.getValue(),entry.getValue()+Constants.CMS_BAK_PATH_SUFFIX);
						break;
					}
				}

//				上传的时候增加文件夹的时候文件名称带路径问题统一处理
				fileName = fileName.replaceAll("\\\\", "/");
				//文件夹上传时会出现 filename 有文件夹的情况 metisMenu/jquery.metisMenu.js
				if (fileName.contains("/")) {
					String temp = fileName.substring(0,fileName.lastIndexOf("/"));
					fileName = fileName.substring(fileName.lastIndexOf("/")+1);
					param2 = param2 + "/" + temp;
					bakPath = bakPath + "/" + temp;
				}

				/** rename the file */
				if (range.getSize() == start) {
					// TODO: f.renameTo(dst); 重命名在Windows平台下可能会失败，stackoverflow建议使用下面这句
					try {
// 						先备份 在删除。之后成功的话删除备份，失败则回滚备份
						File tempf = new File( param2 + File.separator + fileName);
						if (tempf.exists()){
//								判断文件存在的时候进行备份,每次先将临时文件删除一次
							File tempBakFile = IoUtil.getFile(fileName+"ei_temp_bak", param2);
							tempBakFile.delete();
							Files.copy(IoUtil.getFile(fileName,param2).toPath(), tempBakFile.toPath());
						}

//						20220401增加js和css判断混淆的逻辑
						String fileNamePre = fileName;
						String fileNameSuffix = "";
						//生成混淆前源文件名称
						boolean contains = fileName.contains(".");
						if(contains){
							fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
							fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
						}

						boolean jsObfFlag = "1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase());
						boolean cssObfFlag = "1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase());
						if (jsObfFlag||cssObfFlag) {
							cmsObfFile(fileName, remark, param2, f, bakPath, logPlatform, fileNamePre, fileNameSuffix);
						}else {
//							不是混淆的时候
//							删除现有文件
							IoUtil.getFile(fileName,param2).delete();
//							移动token为现有文件
							Files.move(f.toPath(), tempf.toPath());
//							备份现有文件
							saveCmsBak(fileName,param2,bakPath,0,logPlatform,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),remark);
						}
					} catch (Exception e) {
						success = false;
						message = "Rename file error: " + e.getMessage();
//						以上步骤失败的话则回滚文件
						File tempFile = IoUtil.getFile(fileName + "ei_temp_bak", param2);
						if(tempFile.exists()){
							File nowFile = new File(param2 + File.separator + fileName);
							nowFile.delete();
							Files.move(tempFile.toPath(), nowFile.toPath());
						}

//						记录失败日志到数据库
						publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription()+Constants.OPERATION_DESCRIPTION.OPERATION_FAILED.getDescription(),
								param2,fileName,0L);

					}
				}
				try {
					if (success){
						json.put(START_FIELD, start);
//						成功则删除临时备份
						IoUtil.getFile(fileName+"ei_temp_bak",param2).delete();
					}
					json.put(TokenServiceImpl.SUCCESS, success);
					json.put(TokenServiceImpl.MESSAGE, message);
				} catch (JSONException e) {}
				writer.write(json.toString());
				IoUtil.close(writer);
			}
		}else{
			try {
				content = req.getInputStream();
				long fileSize = range.getTo() - range.getFrom();
				//腾讯云文件名需要追加具体路径
				String uploadFileName = param2 + fileName;
				//备份路径
				String bakPath = ConfigConstant.cosBakPath+param2;
				//如果是js css 且需要混淆的话，则需要先上传到备份文件夹，混淆文件名
				Integer platform = Constants.Platform.COS.getValue();
				//获取当前版本库中的最大版本号
				Long maxVersionNum = publishFileVersionService.getMaxVersionNum(platform, param2, fileName);
				//生成混淆前源文件名称
				String fileNamePre;
				String fileNameSuffix;
				String obfuscateSourceName;
				String bakName;
				if (fileName.contains(".")) {
					fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
					fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
					obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
					bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
				}else {
					fileNamePre = fileName;
					fileNameSuffix = "";
					obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
					bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
				}
				//判断当前备份文件是否已经存在
				CosClientUtil cosClientUtil = new CosClientUtil();
				boolean exist = cosClientUtil.doesObjectExist(bakPath+obfuscateSourceName);
				if (exist) {
					//判断如果文件已经存在，则版本号在加1
					maxVersionNum = maxVersionNum+1;
					if (StringUtils.isNullOrEmpty(fileNameSuffix)) {
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
					}else {
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
					}
				}
				boolean isconfused = false;
				Integer obfuscateFlag = 0;
				if ("1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase())) {
					uploadFileName = bakPath + obfuscateSourceName;
					isconfused = true;
					obfuscateFlag = 1;
				}
				if ("1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase())) {
					uploadFileName = bakPath + obfuscateSourceName;
					isconfused = true;
					obfuscateFlag = 1;
				}

				//说明是10M以下文件 直接上传 无需分片
				if(range.getFrom() == 0 && range.getTo()==range.getSize()){
					StreamCosClientUtil.uploadFileToCos(content,range.getSize(),uploadFileName,fileNameSuffix);
					cosObscured(fileName, remark, param2, range.getSize(), uploadFileName, bakPath, maxVersionNum,
							fileNameSuffix, obfuscateSourceName, bakName, cosClientUtil, isconfused, obfuscateFlag);
				}else{
					boolean flag = true;
					String[] tokenSplit = token.split("_");
					if(range.getTo() == range.getSize()){
						flag = continueUpload(content,tokenSplit[1],uploadFileName,fileSize,true);
					}else{
						flag = continueUpload(content, tokenSplit[1], uploadFileName, fileSize, false);
					}
					//true标识上传成功 false表示上传存在问题 需重新上传
					if(flag){
						cosObscured(fileName, remark, param2, range.getSize(), uploadFileName, bakPath, maxVersionNum,
								fileNameSuffix, obfuscateSourceName, bakName, cosClientUtil, isconfused, obfuscateFlag);
					}else {
						throw new StreamException(StreamException.ERROR_FILE_RANGE_START);
					}
				}
				start = range.getTo();
			} catch (StreamException se) {
				success = StreamException.ERROR_FILE_RANGE_START == se.getCode();
				message = "Code: " + se.getCode();
			}  catch (FileNotFoundException fne) {
				message = "Code: " + StreamException.ERROR_FILE_NOT_EXIST;
				success = false;
			} catch (IOException io) {
				message = "IO Error: " + io.getMessage();
				success = false;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtil.close(content);
				/** rename the file */
				if (range.getSize() == start) {
					// TODO: f.renameTo(dst); 重命名在Windows平台下可能会失败，stackoverflow建议使用下面这句
					System.out.println("TK: `" + token + "`, NE: `" + fileName + "`");
					/** if `STREAM_DELETE_FINISH`, then delete it. */
					if (ConfigConstant.isDeleteFinished) {
						IoUtil.getFile(token,param2).delete();
					}
				}
				try {
					if (success)
						json.put(START_FIELD, start);
					json.put(TokenServiceImpl.SUCCESS, success);
					json.put(TokenServiceImpl.MESSAGE, message);
				} catch (JSONException e) {}

				writer.write(json.toString());
				IoUtil.close(writer);
			}
		}
	}

	/**
	 * cms上传混淆操作
	 * @param fileName
	 * @param remark
	 * @param param2
	 * @param f
	 * @param bakPath
	 * @param logPlatform
	 * @param fileNamePre
	 * @param fileNameSuffix
	 * @throws Exception
	 */
	private void cmsObfFile(String fileName, String remark, String param2, File f, String bakPath, int logPlatform, String fileNamePre, String fileNameSuffix) throws Exception {
		//获取当前版本库中的最大版本号
		Long maxVersionNum = publishFileVersionService.getMaxVersionNum(logPlatform, param2, fileName);
		//生成混淆前源文件名称
		String obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
		String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
		if(com.ruoyi.common.utils.StringUtils.isNotEmpty(fileNameSuffix)){
			obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
		}
		//判断当前备份文件是否已经存在
		File bakFile = new File(bakPath + File.separator + bakName);
		if (!bakFile.getParentFile().exists()){
			bakFile.getParentFile().mkdirs();
		}
		while (bakFile.exists()){
//			判断如果文件已经存在，则版本号在加1
			maxVersionNum = maxVersionNum+1;
			obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
			if(com.ruoyi.common.utils.StringUtils.isNotEmpty(fileNameSuffix)){
				obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
				bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
			}
			bakFile = new File(bakPath + File.separator + bakName);
		}
		File obfuscateSourceBakFile = new File(bakPath + File.separator + obfuscateSourceName);

//							先复制token一份到当前文件
		Files.move(f.toPath(), obfuscateSourceBakFile.toPath());
//							调用接口生成混淆文件
//							当js或css开启混淆的时候，当前路径上传混淆后的。备份路径要备份两个文件一个是混淆前的，一个是混淆后的
        String oldEncode = URLEncoder.encode(bakPath + File.separator + obfuscateSourceName, "UTF-8");
        oldEncode = oldEncode.replace("+","%20");
        String newEncode = URLEncoder.encode( bakPath + File.separator + bakName,"UTF-8");
        newEncode = newEncode.replace("+","%20");
        String param = "oldFilePath=" + oldEncode + "&newFilePath=" +newEncode;

		String sendPost = HttpUtils.sendGet(ConfigConstant.nodejsServer, param);
		com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(sendPost);
		Integer code = (Integer) jsonObject.get("code");
		if (code != 200) {
			//混淆失败的时候直接
			throw new Exception("调用文件混淆失败");
		}
//							混淆成功的时候，移动替换文件并
//							删除现有文件
		File nowFile = IoUtil.getFile(fileName, param2);
		nowFile.delete();
//							复制混淆后的文件一份为现有文件
		Files.copy(bakFile.toPath(), nowFile.toPath());
//							记录版本日志和操作日志
		Long i = publishFileVersionService.commonSavePublishFileVersion(logPlatform, 0, param2, fileName, bakPath, bakName, fileNameSuffix, nowFile.length(), 0, maxVersionNum, remark,obfuscateSourceName,1);
		publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),
				param2,fileName,i);
	}

	/**
	 * cms上传混淆操作
	 * @param fileName
	 * @param remark
	 * @param path
	 * @param f
	 * @param bakPath
	 * @param logPlatform
	 * @param fileNamePre
	 * @param fileNameSuffix
	 * @throws Exception
	 */
	private void cmsObfFileNew(String fileName, String remark, String path, MultipartFile f, String bakPath, int logPlatform, String fileNamePre, String fileNameSuffix) throws Exception {
		//获取当前版本库中的最大版本号
		Long maxVersionNum = publishFileVersionService.getMaxVersionNum(logPlatform, path, fileName);
		//生成混淆前源文件名称
		String obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
		String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
		if(com.ruoyi.common.utils.StringUtils.isNotEmpty(fileNameSuffix)){
			obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
		}
		//判断当前备份文件是否已经存在
		File bakFile = new File(bakPath + File.separator + bakName);
		if (!bakFile.getParentFile().exists()){
			bakFile.getParentFile().mkdirs();
		}
		while (bakFile.exists()){
//			判断如果文件已经存在，则版本号在加1
			maxVersionNum = maxVersionNum+1;
			obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
			if(com.ruoyi.common.utils.StringUtils.isNotEmpty(fileNameSuffix)){
				obfuscateSourceName = fileNamePre+ Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
				bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
			}
			bakFile = new File(bakPath + File.separator + bakName);
		}
		File obfuscateSourceBakFile = new File(bakPath + File.separator + obfuscateSourceName);

//		先复制token一份到当前文件
		FileOutputStream out = null;
		InputStream content = null;
		try {
			out = new FileOutputStream(obfuscateSourceBakFile, true);
			content = f.getInputStream();
			int read = 0;
			final byte[] bytes = new byte[BUFFER_LENGTH];
			while ((read = content.read(bytes)) != -1){
				out.write(bytes, 0, read);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			IoUtil.close(out);
			IoUtil.close(content);
		}

//	    调用接口生成混淆文件
//		当js或css开启混淆的时候，当前路径上传混淆后的。备份路径要备份两个文件一个是混淆前的，一个是混淆后的
        String oldEncode = URLEncoder.encode(bakPath + File.separator + obfuscateSourceName, "UTF-8");
        oldEncode = oldEncode.replace("+","%20");
        String newEncode = URLEncoder.encode( bakPath + File.separator + bakName,"UTF-8");
        newEncode = newEncode.replace("+","%20");
        String param = "oldFilePath=" + oldEncode + "&newFilePath=" +newEncode;

		String sendPost = HttpUtils.sendGet(ConfigConstant.nodejsServer, param);
		com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(sendPost);
		Integer code = (Integer) jsonObject.get("code");
		if (code != 200) {
			//混淆失败的时候直接
			throw new Exception("调用文件混淆失败");
		}
//		混淆成功的时候，移动替换文件并
//		删除现有文件
		File nowFile = IoUtil.getFile(fileName, path);
		nowFile.delete();
//		复制混淆后的文件一份为现有文件
		Files.copy(bakFile.toPath(), nowFile.toPath());
//		记录版本日志和操作日志
		Long i = publishFileVersionService.commonSavePublishFileVersion(logPlatform, 0, path, fileName, bakPath, bakName, fileNameSuffix, nowFile.length(), 0, maxVersionNum, remark,obfuscateSourceName,1);
		publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),
				path,fileName,i);
	}

	//混淆操作
	private void cosObscured(String fileName, String remark, String param2, Long size, String uploadFileName,
							 String bakPath, Long maxVersionNum, String fileNameSuffix, String obfuscateSourceName, String bakName, CosClientUtil cosClientUtil, boolean isconfused, Integer obfuscateFlag) throws Exception {
		if (isconfused) {
            File file = null;
            FileInputStream inputStream = null;
			String cosTempPath = null;
            try {
                //混淆前源文件上传完成后，需要混淆并上传到资源文件夹下
                //临时文件夹
                cosTempPath = ConfigConstant.cosTempPath;
                cosClientUtil.download(uploadFileName, cosTempPath+fileName);

				String oldEncode = URLEncoder.encode(cosTempPath+fileName, "UTF-8");
				oldEncode = oldEncode.replace("+","%20");
				String newEncode = URLEncoder.encode( cosTempPath + "hunxiao_" + fileName,"UTF-8");
				newEncode = newEncode.replace("+","%20");
				String param = "oldFilePath=" + oldEncode + "&newFilePath=" +newEncode;
                logger.info("======请求远程接口进行混淆开始======");
                String sendPost = HttpUtils.sendGet(ConfigConstant.nodejsServer, param);
				logger.info("======请求远程接口进行混淆结束======【{}】",sendPost);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(sendPost);
                Integer code = (Integer) jsonObject.get("code");
                if (code == 200) {
                    //混淆成功后继续上传混淆后文件到目标文件夹下
                    String tempFileName = cosTempPath + "hunxiao_" + fileName;
                    file = new File(tempFileName);
                    inputStream = new FileInputStream(file);
                    StreamCosClientUtil.uploadFileToCos(inputStream,file.length(),param2+fileName,fileNameSuffix);
                    saveCosBak(fileName, param2, size, param2+fileName, bakPath,bakName,0,remark,
							fileNameSuffix,maxVersionNum,obfuscateSourceName,obfuscateFlag,
							Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription());
                }else {
                	throw new RuntimeException("压缩文件失败！");
				}
            }finally {
                if (inputStream != null) {
                    IoUtil.close(inputStream);
                }
                if (file != null) {
                    file.delete();
                }
            }
        }else {
			//不用混淆
			saveCosBak(fileName, param2, size, param2+fileName, bakPath,bakName,0,remark,fileNameSuffix,
					maxVersionNum,null,0,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription());
		}
	}

	/**
	 * 备份当前文件并记录备份版本和日志
	 * @param fileName 只是文件名，不包含文件路径
	 * @param filePath
	 * @param fileSize
	 * @param srcPath 源文件上传路径
	 * @param bakPath
	 * @throws StreamException
	 */
	@Override
	public void saveCosBak(String fileName, String filePath, long fileSize, String srcPath, String bakPath,
						   String bakName,int delFlag,String remark,String fileNameSuffix,Long maxVersionNum,
						   String obfuscateSourceName,Integer obfuscateFlag,String logName) throws StreamException {
		//文件夹上传时会出现 filename 有文件夹的情况 metisMenu/jquery.metisMenu.js
		if (fileName.contains("/")) {
			String temp = fileName.substring(0,fileName.lastIndexOf("/")+1);
			fileName = fileName.replace(temp,"");
			filePath += temp;
		}
		if (bakName != null && bakName.contains("/")) {
			String temp = bakName.substring(0,bakName.lastIndexOf("/")+1);
			bakName = bakName.replace(temp,"");
			bakPath += temp;
		}
		Integer platform = Constants.Platform.COS.getValue();
		//上传完成之后，需要对当前文件进行备份
		CosClientUtil cosClientUtil = new CosClientUtil();
		if (com.ruoyi.common.utils.StringUtils.isEmpty(bakName)) {
			//获取当前版本库中的最大版本号
			maxVersionNum = publishFileVersionService.getMaxVersionNum(platform, filePath, fileName);
			//备份文件路径
			String fileNamePre;
			if (fileName.contains(".")) {
				fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
				fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
				bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
			}else {
				fileNamePre = fileName;
				fileNameSuffix = "";
				bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
			}
			//判断当前备份文件是否已经存在
			boolean exist = cosClientUtil.doesObjectExist(bakPath+bakName);
			if (exist) {
				//判断如果文件已经存在，则版本号在加1
				maxVersionNum = maxVersionNum+1;
				if (StringUtils.isNullOrEmpty(fileNameSuffix)) {
					bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
				}else {
					bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
				}
			}
		}
		//复制当前上传文件到备份库中
		String bucketName = cosProperties.getBucketName();
		CopyResult copyResult = cosClientUtil.copy(bucketName, srcPath, bucketName, bakPath+bakName);
		if (copyResult != null) {
            //复制成功，保存版本信息
            //记录版本日志到数据库，记录操作日志到数据库
			Long aLong = publishFileVersionService.commonSavePublishFileVersion(platform, 0, filePath, fileName, bakPath, bakName, fileNameSuffix, fileSize, delFlag, maxVersionNum, remark, obfuscateSourceName, obfuscateFlag);
			publishOperationLogService.commonSaveOperationLog(platform,Constants.OperationType.FILE.getValue(),null, null,logName, filePath,fileName,aLong);
        }else {
            throw new StreamException(StreamException.ERROR_FILE_RANGE_START);
        }
	}

	protected static void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json;charset=utf-8");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Range,Content-Type");
		resp.setHeader("Access-Control-Allow-Origin", ConfigConstant.crossOrigins);
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	}

	/**
	 * 断点续传
	 */
	public static boolean continueUpload(InputStream inputStream, String uploadId, String key,Long fileSize,boolean isLastPart) {
//		获取腾讯云对象
		COSClient cosClient = new CosClientUtil().createCOSClient();
		boolean flag = true;
		try {
			int num = 1;
			// 查询已上传的分块
			ListPartsRequest listPartsRequest = new ListPartsRequest(cosProperties.getBucketName(), key, uploadId);
			PartListing partListing = cosClient.listParts(listPartsRequest);
			if(partListing.getParts() ==null || partListing.getParts().size() ==0){
				StreamCosClientUtil.batchUpload(uploadId, inputStream, fileSize, num, key, isLastPart);
			}else{
				for (PartSummary partSummary : partListing.getParts()) {
					if(partSummary.getPartNumber()>num){
						num = partSummary.getPartNumber();
					}
				}
				num = num +1;
				StreamCosClientUtil.batchUpload(uploadId, inputStream, fileSize, num, key, isLastPart);
			}
			List<PartETag> partETagList = new ArrayList<>();
			ListPartsRequest endPartsRequest = new ListPartsRequest(cosProperties.getBucketName(), key, uploadId);
			PartListing endPartListing = cosClient.listParts(listPartsRequest);
			for (PartSummary partSummary : endPartListing.getParts()) {
				PartETag partETag = new PartETag(partSummary.getPartNumber(),partSummary.getETag());
				partETagList.add(partETag);
				if(partSummary.getPartNumber() == num){
					if(partSummary.getSize() != fileSize){
						flag = false;
					}else{
						flag = true;
					}
				}
			}
			if(flag && isLastPart){
				CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest();
				completeMultipartUploadRequest.setBucketName(cosProperties.getBucketName());
				completeMultipartUploadRequest.setUploadId(uploadId);
				completeMultipartUploadRequest.setKey(key);
				completeMultipartUploadRequest.setPartETags(partETagList);
				CompleteMultipartUploadResult completeMultipartUploadResult = cosClient.completeMultipartUpload(completeMultipartUploadRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cosClient.shutdown();
		return flag;
	}


	/**
	 * 版本备份及记录日志
	 * @param fileName
	 * @param filePath
	 * @param bakPath
	 * @param delFlag
	 * @param logPlatform
	 * @param logName
	 * @return
	 */
	@Override
	public Long saveCmsBak(String fileName, String filePath, String bakPath,int delFlag,int logPlatform,String logName,String remark) {

//		获取当前版本库中的最大版本号
		Long maxVersionNum = publishFileVersionService.getMaxVersionNum(logPlatform, filePath, fileName);

		String fileNamePre = fileName;
		String fileNameSuffix = "";
		String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
		boolean contains = fileName.contains(".");
		if(contains){
			fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
			fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum + "." + fileNameSuffix;
		}

		File bakFile = new File(bakPath + File.separator + bakName);
		if (!bakFile.getParentFile().exists()){
			bakFile.getParentFile().mkdirs();
		}
		while (bakFile.exists()){
//			判断如果文件已经存在，则版本号在加1
			maxVersionNum = maxVersionNum+1;
			bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
			if(contains){
				bakName = bakName + "." + fileNameSuffix;
			}
			bakFile = new File(bakPath + File.separator + bakName);
		}
		File lastFile;
		try {
			lastFile = IoUtil.getFile(fileName, filePath);
			Files.copy(lastFile.toPath(), bakFile.toPath());
		}catch (Exception e){
			e.printStackTrace();
			throw new ServiceException("备份文件发生异常："+e.toString());
		}
//		记录版本日志到数据库，记录操作日志到数据库 TODO 最后参数根据情况修改
		Long i = publishFileVersionService.commonSavePublishFileVersion(logPlatform, 0, filePath, fileName, bakPath, bakName, fileNameSuffix, lastFile.length(), delFlag, maxVersionNum, remark,null,0);
		publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,logName,
				filePath,fileName,i);
		return i;
	}

	/**
	 * 新增加的上传插件的公共方法
	 * @param request
	 * @param response
	 * @param upfile
	 */
	@Override
	public void webUploadPost(HttpServletRequest request, HttpServletResponse response, MultipartFile upfile)throws ServletException, IOException  {

		doOptions(request, response);
		String fileName = request.getParameter("fileName");
		String action = request.getParameter("action");
		String hash = request.getParameter("hash");

		String platformStr = request.getParameter("platform");
		Integer platform = Integer.parseInt(platformStr);

		String remark = request.getParameter("remark");
		String jsparam = request.getParameter("jsparam");
		String cssparam = request.getParameter("cssparam");

		String fileSize = request.getParameter("fileSize");

//		20220322增加路径的传递获取
		String path = request.getParameter("path");
		if(!StringUtils.isNullOrEmpty(path)){
			try {
				path = java.net.URLDecoder.decode(path,"UTF-8");
				path = java.net.URLDecoder.decode(path,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			}
		}

		OutputStream out = null;
		InputStream content = null;
		final PrintWriter writer = response.getWriter();

		/** TODO: validate your token. */
		JSONObject json = new JSONObject();
		boolean success = true;

		if(!Constants.Platform.COS.getValue().equals(platform)){

			//普通上传
			String bakPath = "";
			path = path.replaceAll("\\\\", "/");
			int logPlatform = platform;
//				遍历获取当前平台的根路径及备份路径
			Set<Map.Entry<Integer, String>> entries = ConfigConstant.allRootPath.entrySet();
			for(Map.Entry<Integer, String> entry : entries){
				if(entry.getKey()==1){
//						跳过腾讯云获取其他本地平台
					continue;
				}
				if(entry.getKey()==logPlatform){
					bakPath = path.replace(entry.getValue(),entry.getValue()+Constants.CMS_BAK_PATH_SUFFIX);
					break;
				}
			}

//				上传的时候增加文件夹的时候文件名称带路径问题统一处理
			fileName = fileName.replaceAll("\\\\", "/");
			//文件夹上传时会出现 filename 有文件夹的情况 metisMenu/jquery.metisMenu.js
			if (fileName.contains("/")) {
				String temp = fileName.substring(0,fileName.lastIndexOf("/"));
				fileName = fileName.substring(fileName.lastIndexOf("/")+1);
				path = path + "/" + temp;
				bakPath = bakPath + "/" + temp;
			}

			File pathFile = new File(path);
			if(!pathFile.exists()){
				pathFile.mkdirs();
			}

			if (com.ruoyi.common.utils.StringUtils.isEmpty(hash)) {

				/** rename the file */
				try {
// 					判断上传的文件是否存在，存在的话先备份。之后成功的话删除备份，失败则回滚备份
					File currentFile = new File( path + File.separator + fileName);
					if (currentFile.exists()){
//						判断文件存在的时候进行备份,每次先将临时文件删除一次
						File tempBakFile = IoUtil.getFile(fileName+"ei_temp_bak", path);
						tempBakFile.delete();
						Files.copy(currentFile.toPath(), tempBakFile.toPath());
					}

//					20220401增加js和css判断混淆的逻辑
					String fileNamePre = fileName;
					String fileNameSuffix = "";
//					生成混淆前源文件名称
					boolean contains = fileName.contains(".");
					if(contains){
						fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
						fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
					}

					boolean jsObfFlag = "1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase());
					boolean cssObfFlag = "1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase());
					if (jsObfFlag||cssObfFlag) {
//						cmsObfFile(fileName, remark, path, f, bakPath, logPlatform, fileNamePre, fileNameSuffix);
						cmsObfFileNew(fileName, remark, path, upfile, bakPath, logPlatform, fileNamePre, fileNameSuffix);
					}else {
						out = new FileOutputStream(currentFile, true);
						content = upfile.getInputStream();
						int read = 0;
						final byte[] bytes = new byte[BUFFER_LENGTH];
						while ((read = content.read(bytes)) != -1){
							out.write(bytes, 0, read);
						}
//						备份现有文件
						saveCmsBak(fileName,path,bakPath,0,logPlatform,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),remark);
					}
				} catch (Exception e) {
					e.printStackTrace();
					success = false;
//					以上步骤失败的话则回滚文件
					File tempFile = IoUtil.getFile(fileName + "ei_temp_bak", path);
					if(tempFile.exists()){
						File nowFile = new File(path + File.separator + fileName);
						nowFile.delete();
						Files.move(tempFile.toPath(), nowFile.toPath());
					}

//					记录失败日志到数据库
					publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription()+Constants.OPERATION_DESCRIPTION.OPERATION_FAILED.getDescription(),
							path,fileName,0L);
				}finally {
					IoUtil.close(out);
					IoUtil.close(content);
				}
				try {
					if (success){
//						成功则删除临时备份
						IoUtil.getFile(fileName+"ei_temp_bak",path).delete();
						json = getResponseJSON(request, json);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				writer.write(json.toString());
				IoUtil.close(writer);
				return;
			}else {
//				秒传或分片上传

				//秒传或断点续传/20220419增加name的传递
				String hashPath = path +"/"+ hash+"-"+fileName;
				String hashPathOk = hashPath + ".ok";
				File hashPathFile = new File(hashPath);
				File hashPathOkFile = new File(hashPathOk);

				//状态查询
				if ("query".equals(action)) {
					try {
						if (hashPathOkFile.exists()){
							json.put("ret",1);
							json = getResponseJSON(request, json);
							writer.write(json.toString());
							IoUtil.close(writer);
							return;
						} else if (hashPathFile.exists()){
							hashPathFile.delete();
							writer.write("0");
							IoUtil.close(writer);
							return;
						} else{
							writer.write("0");
							IoUtil.close(writer);
							return;
						}
					} catch (JSONException e) {
						writer.write("0");
						IoUtil.close(writer);
						return;
					}
				} else {
//					逻辑处理
					try {
						if (upfile!=null&&!upfile.isEmpty()) {
							out = new FileOutputStream(hashPathFile, true);
							content = upfile.getInputStream();
							int read = 0;
							final byte[] bytes = new byte[BUFFER_LENGTH];
							while ((read = content.read(bytes)) != -1){
								out.write(bytes, 0, read);
							}
						}
						String okStr = request.getParameter("ok");
						if (!"1".equals(okStr)){
							writer.write("1");
							IoUtil.close(writer);
							return;
						}
						IoUtil.close(out);
						IoUtil.close(content);

						if (hashPathFile.exists()){
							if(hashPathOkFile.exists()){
								hashPathOkFile.delete();
							}
							Files.move(hashPathFile.toPath(), hashPathOkFile.toPath());
						}else {
							writer.write("0");
							IoUtil.close(writer);
							return;
						}
					} catch (Exception se) {
						se.printStackTrace();
						success = false;
						writer.write("0");
						IoUtil.close(writer);
						return;
					} finally {
						IoUtil.close(out);
						IoUtil.close(content);
					}

					try {
// 						先备份 在删除。之后成功的话删除备份，失败则回滚备份
						File currentFile = new File( path + File.separator + fileName);
						if (currentFile.exists()){
//								判断文件存在的时候进行备份,每次先将临时文件删除一次
							File tempBakFile = IoUtil.getFile(fileName+"ei_temp_bak", path);
							tempBakFile.delete();
							Files.copy(IoUtil.getFile(fileName,path).toPath(), tempBakFile.toPath());
						}

//						20220401增加js和css判断混淆的逻辑
						String fileNamePre = fileName;
						String fileNameSuffix = "";
						//生成混淆前源文件名称
						boolean contains = fileName.contains(".");
						if(contains){
							fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
							fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
						}

						boolean jsObfFlag = "1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase());
						boolean cssObfFlag = "1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase());
						if (jsObfFlag||cssObfFlag) {
							cmsObfFile(fileName, remark, path, hashPathOkFile, bakPath, logPlatform, fileNamePre, fileNameSuffix);
						}else {
//							不是混淆的时候
//							删除现有文件
							IoUtil.getFile(fileName,path).delete();
//							移动token为现有文件
							Files.move(hashPathOkFile.toPath(), currentFile.toPath());
//							备份现有文件
							saveCmsBak(fileName,path,bakPath,0,logPlatform,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),remark);
						}
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
//						以上步骤失败的话则回滚文件
                        File tempFile = new File(path + File.separator + fileName + "ei_temp_bak");
                        if(tempFile.exists()){
							File nowFile = new File(path + File.separator + fileName);
							nowFile.delete();
							Files.move(tempFile.toPath(), nowFile.toPath());
						}
//						记录失败日志到数据库
						publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription()+Constants.OPERATION_DESCRIPTION.OPERATION_FAILED.getDescription(),
								path,fileName,0L);

						writer.write("0");
						IoUtil.close(writer);
						return;
					}
					if (success){
//						成功则删除临时备份
						IoUtil.getFile(fileName+"ei_temp_bak",path).delete();
					}
					try {
						json = getResponseJSON(request, json);
						writer.write(json.toString());
						IoUtil.close(writer);
						return;
					}catch (Exception e){
						e.printStackTrace();
						writer.write("0");
						IoUtil.close(writer);
						return;
					}
				}
			}
		}else {
			//腾讯云平台
			path = path.replaceAll("\\\\", "/");
			//上传的时候增加文件夹的时候文件名称带路径问题统一处理
			fileName = fileName.replaceAll("\\\\", "/");
			//文件夹上传时会出现 filename 有文件夹的情况 metisMenu/jquery.metisMenu.js
			if (fileName.contains("/")) {
				String temp = fileName.substring(0,fileName.lastIndexOf("/")+1);
				fileName = fileName.substring(fileName.lastIndexOf("/")+1);
				path += temp;
			}
			//备份路径
			String bakPath = ConfigConstant.cosBakPath+path;

			if (com.ruoyi.common.utils.StringUtils.isEmpty(hash)) {

				/** rename the file */
				/*try {
// 					判断上传的文件是否存在，存在的话先备份。之后成功的话删除备份，失败则回滚备份
					File currentFile = new File( path + File.separator + fileName);
					if (currentFile.exists()){
//						判断文件存在的时候进行备份,每次先将临时文件删除一次
						File tempBakFile = IoUtil.getFile(fileName+"ei_temp_bak", path);
						tempBakFile.delete();
						Files.copy(currentFile.toPath(), tempBakFile.toPath());
					}

//					20220401增加js和css判断混淆的逻辑
					String fileNamePre = fileName;
					String fileNameSuffix = "";
//					生成混淆前源文件名称
					boolean contains = fileName.contains(".");
					if(contains){
						fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
						fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
					}

					boolean jsObfFlag = "1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase());
					boolean cssObfFlag = "1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase());
					if (jsObfFlag||cssObfFlag) {
//						cmsObfFile(fileName, remark, path, f, bakPath, logPlatform, fileNamePre, fileNameSuffix);
						cmsObfFileNew(fileName, remark, path, upfile, bakPath, logPlatform, fileNamePre, fileNameSuffix);
					}else {
						out = new FileOutputStream(currentFile, true);
						content = upfile.getInputStream();
						int read = 0;
						final byte[] bytes = new byte[BUFFER_LENGTH];
						while ((read = content.read(bytes)) != -1){
							out.write(bytes, 0, read);
						}
						IoUtil.close(out);
						IoUtil.close(content);

//						备份现有文件
						saveCmsBak(fileName,path,bakPath,0,logPlatform,"文件实时上传备份",remark);
					}
				} catch (Exception e) {
					success = false;
//					以上步骤失败的话则回滚文件
					File tempFile = IoUtil.getFile(fileName + "ei_temp_bak", path);
					if(tempFile.exists()){
						File nowFile = new File(path + File.separator + fileName);
						nowFile.delete();
						Files.move(tempFile.toPath(), nowFile.toPath());
					}

//					记录失败日志到数据库
					publishOperationLogService.commonSaveOperationLog(logPlatform,Constants.OperationType.FILE.getValue(),null,null,"实时文件上传失败，自动回滚",
							path,fileName);
				}
				try {
					if (success){
//						成功则删除临时备份
						IoUtil.getFile(fileName+"ei_temp_bak",path).delete();
						json = getResponseJSON(request, json);
					}
				} catch (JSONException e) {

				}*/
				try {
					json.put("msg","上传无效");
					writer.write(json.toString());
					IoUtil.close(writer);
					return;
				}catch (Exception e) {
					e.printStackTrace();
				}
			}else {

				//秒传或断点续传
				String hashPath = ConfigConstant.cosTempPath+path + hash+"-"+fileName;
				File pathFile = new File(ConfigConstant.cosTempPath+path);
				if(!pathFile.exists()){
					pathFile.mkdirs();
				}
				//腾讯云文件名需要追加具体路径
				String uploadFileName = path + fileName;
				CosClientUtil cosClientUtil = new CosClientUtil();
				//状态查询
				if ("query".equals(action)) {
					try {
						//判断当前文件夹下是否有.ok文件，有的话就删除
						File pathflag = new File(hashPath);
						if (pathflag.exists()){
							pathflag.delete();
							writer.write("0");
							IoUtil.close(writer);
							return;
						} else{
							writer.write("0");
							IoUtil.close(writer);
							return;
						}
					} catch (Exception e) {
						writer.write("0");
						IoUtil.close(writer);
						return;
					}
				} else {
					String uploadId = null;
					//根据size判断一下当前的是不是需要分片
					if (Integer.valueOf(fileSize) < 4194304) {
						//无需分片
					}else{
						File file = new File(hashPath);
						if (!file.exists()) {
							//第一次上传
							uploadId = StreamCosClientUtil.initiateMultipartUpload(cosProperties.getBucketName(),uploadFileName,
									null);
							uploadId = uploadId + "_0_"+upfile.getSize();
							out = new FileOutputStream(hashPath);
							out.write(uploadId.getBytes("UTF-8"));
							IoUtil.close(out);
						}else {
							BufferedReader in = new BufferedReader(new FileReader(hashPath));
							StringBuffer sb = null;
							while (in.ready()) {
								sb = (new StringBuffer(in.readLine()));
							}
							in.close();
							uploadId = sb.toString();
							String[] split = uploadId.split("_");
							if (Long.parseLong(split[2]) < Long.parseLong(fileSize)) {
								long l = Long.parseLong(split[2]) + upfile.getSize();
								uploadId = split[0]+"_"+split[2]+"_"+l;
								out = new FileOutputStream(hashPath);
								out.write(uploadId.getBytes("UTF-8"));
								IoUtil.close(out);
							}
						}
					}
//					逻辑处理
					Long maxVersionNum = 0L;
					String fileNameSuffix = null;
					String obfuscateSourceName = null;
					String bakName = null;
					boolean isconfused = false;
					Integer obfuscateFlag = 0;
					try {
						if (upfile != null && !upfile.isEmpty()) {
							content = upfile.getInputStream();
							//获取当前版本库中的最大版本号
							maxVersionNum = publishFileVersionService.getMaxVersionNum(platform, path, fileName);
							//生成混淆前源文件名称
							String fileNamePre;
							if (fileName.contains(".")) {
								fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
								fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
								obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
								bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
							}else {
								fileNamePre = fileName;
								fileNameSuffix = "";
								obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
								bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
							}
							//判断当前备份文件是否已经存在
							boolean exist = cosClientUtil.doesObjectExist(bakPath+obfuscateSourceName);
							if (exist) {
								//判断如果文件已经存在，则版本号在加1
								maxVersionNum = maxVersionNum+1;
								if (StringUtils.isNullOrEmpty(fileNameSuffix)) {
									obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
									bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
								}else {
									obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
									bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
								}
							}
							if ("1".equals(jsparam) && "js".equals(fileNameSuffix.toLowerCase())) {
								uploadFileName = bakPath + obfuscateSourceName;
								isconfused = true;
								obfuscateFlag = 1;
							}
							if ("1".equals(cssparam) && "css".equals(fileNameSuffix.toLowerCase())) {
								uploadFileName = bakPath + obfuscateSourceName;
								isconfused = true;
								obfuscateFlag = 1;
							}
						}
						if (uploadId == null) {
							StreamCosClientUtil.uploadFileToCos(content,upfile.getSize(),uploadFileName,
									null);
						}else {
							String[] split = uploadId.split("_");
							long l = Long.parseLong(split[2]);
							boolean flag;
							if (l < Long.parseLong(fileSize)) {
								flag = continueUpload(content, split[0], uploadFileName, upfile.getSize(), false);
							}else {
								flag = continueUpload(content, split[0], uploadFileName, upfile.getSize(), true);
								if (flag) {
									File file = new File(hashPath);
									if (file.exists()) {
										file.delete();
									}
								}
							}
						}
						String okStr = request.getParameter("ok");
						if (!"1".equals(okStr)){
							writer.write("1");
							IoUtil.close(writer);
							return;
						}
					} catch (Exception se) {
						se.printStackTrace();
						writer.write("0");
						IoUtil.close(writer);
						return;
					} finally {
						IoUtil.close(content);
					}
					//上传完成之后需要进行后续备份等操作
					try {
						cosObscured(fileName, remark, path, Long.parseLong(fileSize), uploadFileName, bakPath,
								maxVersionNum,
								fileNameSuffix, obfuscateSourceName, bakName, cosClientUtil, isconfused, obfuscateFlag);
					} catch (Exception e) {
						e.printStackTrace();
						writer.write("0");
						IoUtil.close(writer);
						return;
					}
					writer.write(json.toString());
					IoUtil.close(writer);
				}
			}
		}
		try {
			JSONObject responseJSON = getResponseJSON(request, json);
			writer.write(responseJSON.toString());
			IoUtil.close(writer);
			return;
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private JSONObject getResponseJSON(HttpServletRequest request, JSONObject json) throws JSONException{
		String type = request.getParameter("type");
		String user = request.getParameter("user");
		String name = request.getParameter("name");

		json.put("time",DateUtils.parseDateToStr("yyyy/MM/dd HH:mm:ss",new Date()));

		if(type != null){
			json.put("type",type);
		}
		if(user != null){
			json.put("user",user);
		}
		if(name != null){
			json.put("name",name);
		}

		return json;
	}

	/**
	 * 编辑后执行腾讯云COS上传
	 * @param filearea  文件内容字符串
	 * @param fileName   文件名称
	 * @param versionId 最后一版文件版本的版本id
	 */
	@Override
	public void editUpload(String filearea, String fileName, String versionId,String platform,String path,
						   String remark) throws Exception{
		//获取最后一版文件版本信息
		PublishFileVersion fileVersion = null;
		if (com.ruoyi.common.utils.StringUtils.isNotEmpty(versionId)) {
			fileVersion = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(versionId));
		}
		String key = null;
		try {
			//临时文件存放目录  写入改写后的文件
			String profile = ConfigConstant.cosTempPath;
			//临时文件全路径
			key = profile + fileName;
			FileWriter writer = new FileWriter(key);
			writer.write(filearea);
			writer.flush();
			writer.close();

			if("1".equals(platform)){
				//腾讯云
				String bakPath = ConfigConstant.cosBakPath+path;
				//腾讯云文件名需要追加具体路径
				String uploadFileName = path + fileName;
				//腾讯云平台
				CosClientUtil cosClientUtil = new CosClientUtil();

				//临时文件写入成功之后进入后续上传文件到腾讯云操作
				Long maxVersionNum = 0L;
				if (Objects.nonNull(fileVersion)) {
					maxVersionNum = fileVersion.getVersionNum()+1;
				}else {
					maxVersionNum = publishFileVersionService.getMaxVersionNum(Integer.parseInt(platform), path, fileName);
				}
				String fileNameSuffix = "";
				String obfuscateSourceName = "";
				String bakName = "";
				boolean isconfused = false;
				Integer obfuscateFlag = 0;
				try {
					//生成混淆前源文件名称
					String fileNamePre;
					if (fileName.contains(".")) {
						fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
						fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
					}else {
						fileNamePre = fileName;
						fileNameSuffix = "";
						obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
						bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
					}
					//判断当前备份文件是否已经存在
					boolean exist = cosClientUtil.doesObjectExist(bakPath+obfuscateSourceName);
					if (exist) {
						//判断如果文件已经存在，则版本号在加1
						maxVersionNum = maxVersionNum+1;
						if (StringUtils.isNullOrEmpty(fileNameSuffix)) {
							obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum;
							bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum;
						}else {
							obfuscateSourceName = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+fileNameSuffix;
							bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+fileNameSuffix;
						}
					}
					if (Objects.nonNull(fileVersion)) {
						//最后的版本存在的时候，需要判断是否混淆，如果没有最后的版本则不用混淆
						if (fileVersion.getObfuscateFlag() == 1 && "js".equals(fileNameSuffix.toLowerCase())) {
							uploadFileName = bakPath + obfuscateSourceName;
							isconfused = true;
							obfuscateFlag = 1;
						}
						if (fileVersion.getObfuscateFlag() == 1 && "css".equals(fileNameSuffix.toLowerCase())) {
							uploadFileName = bakPath + obfuscateSourceName;
							isconfused = true;
							obfuscateFlag = 1;
						}
					}
					//上传本地临时文件到腾讯云
					cosClientUtil.uploadFile(uploadFileName, key);
				} catch (Exception se) {
					se.printStackTrace();
				}
				//上传完成之后需要进行后续备份等操作
				try {
					File file = new File(key);
					cosObscured(fileName, remark, path, file.length(), uploadFileName, bakPath, maxVersionNum,
							fileNameSuffix, obfuscateSourceName, bakName, cosClientUtil, isconfused, obfuscateFlag);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}else {
				//新编辑的内容已经写到文件里了，后续需要执行混淆及备份了
				String fileNamePre = fileName;
				String fileNameSuffix = "";
				String bakPath;
				if (Objects.nonNull(fileVersion)) {
					bakPath = fileVersion.getBakPathPrefix();
				}else {
					bakPath = getCmsBakPath(path, platform);
				}
				Integer logPlatform = Integer.parseInt(platform);
				//生成混淆前源文件名称
				boolean contains = fileName.contains(".");
				if(contains){
					fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
					fileNameSuffix = fileName.substring(fileName.lastIndexOf(".")+1);
				}
				boolean jsObfFlag = false;
				boolean cssObfFlag = false;
				if (Objects.nonNull(fileVersion)) {
					//最后的版本存在的时候，需要判断是否混淆，如果没有最后的版本则不用混淆
					jsObfFlag = fileVersion.getObfuscateFlag() == 1 && "js".equals(fileNameSuffix.toLowerCase());
					cssObfFlag = fileVersion.getObfuscateFlag() == 1 && "css".equals(fileNameSuffix.toLowerCase());
				}
				if (jsObfFlag||cssObfFlag) {
					File file = new File(key);
					FileInputStream fileInputStream = new FileInputStream(file);
					MultipartFile upfile = new MockMultipartFile(fileName,fileInputStream);
					cmsObfFileNew(fileName, remark, path, upfile, bakPath, logPlatform, fileNamePre, fileNameSuffix);
				}else {
//					移动token为现有文件
					File file = new File(key);
					File currentFile = new File( path + "/" + fileName);
//					删除现有文件
					IoUtil.getFile(fileName,path).delete();
					Files.copy(file.toPath(), currentFile.toPath());
					//备份现有文件
					saveCmsBak(fileName,path,bakPath,0,logPlatform,Constants.OPERATION_DESCRIPTION.FILE_UPLOAD.getDescription(),remark);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			//由于此处文件内容写在临时文件中，所以使用完之后需要删除下载的临时文件
			if (com.ruoyi.common.utils.StringUtils.isNotEmpty(key)) {
				boolean delete = new File(key).delete();
			}
		}
	}

	public String getCmsBakPath(String path,String platform) {
		//遍历获取当前平台的根路径及备份路径
		String bakPath = "";
		int logplatform = Integer.parseInt(platform);
		Set<Map.Entry<Integer, String>> entries = ConfigConstant.allRootPath.entrySet();
		for(Map.Entry<Integer, String> entry : entries){
			if(entry.getKey()==1){
				//	跳过腾讯云获取其他本地平台
				continue;
			}
			if(entry.getKey()==logplatform){
				bakPath = path.replace(entry.getValue(),entry.getValue()+Constants.CMS_BAK_PATH_SUFFIX);
				break;
			}
		}
		return bakPath;
	}

}
