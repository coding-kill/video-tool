package com.huadongfeng.project.filemanageweb.publishcosfiles.service;

import com.huadongfeng.project.config.constants.Constants;
import com.huadongfeng.project.config.properties.CosProperties;
import com.huadongfeng.project.config.streamconfig.ConfigConstant;
import com.huadongfeng.project.filemanageweb.publishcosfiles.domain.PublishFiles;
import com.huadongfeng.project.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.huadongfeng.project.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.huadongfeng.project.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.huadongfeng.project.filemanageweb.upload.service.IStreamService;
import com.huadongfeng.project.filemanageweb.upload.service.StreamException;
import com.huadongfeng.project.util.CommonUtil;
import com.huadongfeng.project.util.CosClientUtil;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.utils.UrlEncoderUtils;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @ClassName:ContentServiceImpl
 * @author:dongao
 * @date 2022/3/16 9:56
 */
@Service
public class PublishCosFilesServiceImpl implements IPublishCosFilesService {

    @Autowired
    private IStreamService streamService;
    @Autowired
    private CosProperties cosProperties;
    @Autowired
    private IPublishFileVersionService publishFileVersionService;
    @Autowired
    private IPublishOperationLogService publishOperationLogService;
    /**
     * 获取目录结构
     * @return
     */
    @Override
    public List<Map> getTreeData(String name) {
        List<Map> list = new ArrayList();
        CosClientUtil cosClientUtil = new CosClientUtil();
        List<ObjectListing> contentList;
        boolean permissions = CommonUtil.getPermissions();
        if (StringUtils.isNotEmpty(name)) {
            contentList = cosClientUtil.getContentList(name);
        }else {
            //默认查询根目录下文件夹
            contentList = cosClientUtil.getContentList("/");
        }
        for (ObjectListing objl : contentList) {
            //层级目录
            List<String> commonPrefixes = objl.getCommonPrefixes();
            for (String commonPrefix : commonPrefixes) {
                if (!permissions && commonPrefix.contains("ei_cos_bak")) {
                    //不能展示备份目录
                    continue;
                }
                Map map = new HashMap(2);
                map.put("name",commonPrefix);
                /*//查询当前目录下是否有子目录 即使没有子级目录 contentList1 的 size 也大于0 为1
                List<ObjectListing> contentList1 = cosClientUtil.getContentList(commonPrefix);
                //取index 0 的判断是否有子目录
                ObjectListing objectListing = contentList1.get(0);
                List<String> commonPrefixes1 = objectListing.getCommonPrefixes();
                if (commonPrefixes1.size() > 0) {
                    map.put("isParent",true);
                }else {
                    map.put("isParent",false);
                }*/
                map.put("isParent",true);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 获取当前选中目录下的子级目录及文件
     * @param publishFiles
     * @return
     */
    @Override
    public List<PublishFiles> selectContentList(PublishFiles publishFiles, PageDomain pageDomain) {
        List<PublishFiles> list = new ArrayList();
        CosClientUtil cosClientUtil = new CosClientUtil();
        List<ObjectListing> contentList;
        String title = publishFiles.getTitle();
        boolean permissions = CommonUtil.getPermissions();

        if (StringUtils.isNotEmpty(title)) {
            contentList = cosClientUtil.getContentList(title);
        }else {
            //默认查询根目录下文件夹
            contentList = cosClientUtil.getContentList("/");
        }
        for (ObjectListing objl : contentList) {
            PublishFiles cont;
            //层级目录
            List<String> commonPrefixes = objl.getCommonPrefixes();
            //对象集合
            List<COSObjectSummary> objectSummaries = objl.getObjectSummaries();
            //遍历组装返回值
            if (commonPrefixes.size() > 0) {
                for (String commonPrefix : commonPrefixes) {
                    if (!permissions && commonPrefix.contains("ei_cos_bak")) {
                        //不能展示备份目录
                        continue;
                    }
                    cont = new PublishFiles();
                    //fileName 需要截断至最后一层展示
                    if ("/".equals(title)) {
                        //加载根目录时无需踢掉/
                        cont.setFileName(commonPrefix);
                    }else {
                        cont.setFileName(commonPrefix.replace(title,""));
                    }
                    cont.setDirectory(true);
                    cont.setTitle(commonPrefix);
                    cont.setFileType("文件夹");
                    list.add(cont);
                }
            }
            if (objectSummaries.size() > 0) {
                for (COSObjectSummary cosobj : objectSummaries) {
                    String key = cosobj.getKey();
                    if (!permissions && key.contains("ei_cos_bak")) {
                        //不能展示备份目录
                        continue;
                    }
                    cont = new PublishFiles();
                    int length = key.length();
                    String fileName = key.substring(key.lastIndexOf("/") + 1, length);
                    cont.setFileType(null);
                    cont.setFilePath(key);
                    cont.setTitle(key);
                    if (key.contains(".")) {
                        String fileType = key.substring(key.lastIndexOf(".") + 1, length);
                        /*if ("jpg".equals(fileType.toLowerCase()) || "png".equals(fileType.toLowerCase()) || "gif".equals(fileType.toLowerCase()) || "jpeg".equals(fileType.toLowerCase())) {

                            *//*获取图片信息 共有读 http://examples-1251000004.cos.ap-shanghai.myqcloud.com/sample.jpeg?imageInfo
                            私有读 http://examples-1251000004.cos.ap-shanghai.myqcloud.com/sample.jpeg?q-sign-algorithm=<signature>&imageInfo
                            {"code": -2,"error": "Invalid image"}*//*
                            try {
                                key = URLEncoder.encode(key, "UTF-8");
                                key = key.replace("+", "%20");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            String url = cosProperties.getPrefixDomain() + key;
                            //url = UrlEncoderUtils.encodeEscapeDelimiter(url);
                            String imageInfo = HttpUtils.sendGet(url, "imageInfo");
                            if (StringUtils.isNotEmpty(imageInfo)) {
                                JSONObject jsonObject = JSON.parseObject(imageInfo);
                                String width = (String) jsonObject.get("width");
                                String height = (String) jsonObject.get("height");
                                String filepx = "("+width+"*"+height+")";
                                cont.setFilepx(filepx);
                            }
                        }*/
                        cont.setFileType(fileType);
                    }else {
                        if ("placeholder-file".equals(fileName)) {
                            //不展示
                            continue;
                        }
                        if(StringUtils.isEmpty(fileName)){
                            //线上空文件名的，不展示
                            continue;
                        }
                    }
                    cont.setFileName(fileName);
                    cont.setFileSize(cosobj.getSize());
                    cont.setUpdateTime(cosobj.getLastModified());
                    list.add(cont);
                }
            }
        }
        //返回给前端页面渲染之前，如果查询条件fileName有值，则需要对list结果进行模糊匹配
        if (StringUtils.isNotEmpty(publishFiles.getFileName()) && !"no".equals(publishFiles.getFilterFlag())) {
            if (CollectionUtils.isNotEmpty(list)) {
                Iterator<PublishFiles> iterator = list.iterator();
                while (iterator.hasNext()) {
                    PublishFiles next = iterator.next();
                    if (!next.getFileName().toLowerCase().contains(publishFiles.getFileName().toLowerCase())) {
                        iterator.remove();
                    }
                }
            }
        }
        //排序操作
        String orderBy = pageDomain.getOrderByColumn();
        String isAsc = pageDomain.getIsAsc();
        if (StringUtils.isNotEmpty(orderBy) && CollectionUtils.isNotEmpty(list)) {
            if ("fileName".equals(orderBy)) {
                //按文件名称排序
                if ("asc".equals(isAsc)) {
                    //升序
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getFileName)).collect(Collectors.toList());
                }else {
                    //降序
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getFileName).reversed()).collect(Collectors.toList());
                }
            }else if ("fileType".equals(orderBy)) {
                //按文件类型排序
                if ("asc".equals(isAsc)) {
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getFileTypeSort)).collect(Collectors.toList());
                }else {
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getFileTypeSort).reversed()).collect(Collectors.toList());
                }
            }else if ("updateTime".equals(orderBy)) {
                //按更新时间排序
                if ("asc".equals(isAsc)) {
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getUpdateTimeSort)).collect(Collectors.toList());
                }else {
                    list = list.stream().sorted(Comparator.comparing(PublishFiles::getUpdateTimeSort).reversed()).collect(Collectors.toList());
                }
            }
        }
        return list;
    }
    /**
     * 下载单个文件
     * @param request
     * @param response
     */
    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filePath = request.getParameter("filePath");
        String fileName = request.getParameter("fileName");
        String filetype = "";
        if (fileName.contains(".")) {
            filetype = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        }
        if ("js".equals(filetype.toLowerCase()) || "css".equals(filetype.toLowerCase())) {
            String path = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            PublishFileVersion publishFileVersion = publishFileVersionService.getLastFileVersion(Constants.Platform.COS.getValue(),path,fileName);
            //判断是否是混淆了的文件，混淆了的文件需要下载混淆前的源文件
            if (publishFileVersion != null && publishFileVersion.getObfuscateFlag() != null) {
                Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();
                if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
                    String bakPath = publishFileVersion.getBakPathPrefix();
                    String obfuscateSourceName = publishFileVersion.getObfuscateSourceName();
                    filePath = bakPath+obfuscateSourceName;
                }
            }
        }
        CosClientUtil cosClientUtil = new CosClientUtil();
        byte[] data = cosClientUtil.getObject(filePath);
        genCode(response, data, fileName);
    }

    /**
     * 下载文件夹及文件
     * @param request
     * @param response
     */
    @Override
    public void downloadDirectory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        //输出流
        String sub = title.substring(0, title.lastIndexOf("/"));
        String fileName = sub.substring(sub.lastIndexOf("/") + 1, sub.length())+".zip";


        response.reset();
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment; filename="+ UrlEncoderUtils.encode(fileName));

        CosClientUtil cosClientUtil = new CosClientUtil();
        String prefix = title;
        //目录前缀  prefix=a-2022-test/ABB/ 由于要保留选中文件名，所以前缀需要前移一步 prefix = a-2022-test/ABB
        prefix = prefix.substring(0, prefix.lastIndexOf("/"));
        //期望这样的前缀 prefix = a-2022-test/
        prefix = prefix.substring(0,prefix.lastIndexOf("/")+1);
        //获取腾讯云目录及文件

        //设置压缩流：直接写入response，实现边压缩边下载
        ZipOutputStream zipos = null;
        try {
            zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipos.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法
        } catch (Exception e) {
            e.printStackTrace();
        }
        getDirectory(title, zipos, cosClientUtil,prefix);
        IOUtils.closeQuietly(zipos);

    }

    /**
     * 批量下载
     * @param request
     * @param response
     */
    @Override
    public void batchDownloadFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long start = System.currentTimeMillis();
        System.out.println("======批量下载开始======"+ start);
        String filePaths = request.getParameter("filePaths");
        //ei-dongao/image/common/
        String oldprefix = request.getParameter("prefixPath");
        //目录前缀  prefix=ei-dongao/image/common/ 由于要保留选中文件名，所以前缀需要前移一步 prefix = ei-dongao/image/common
        String prefix = oldprefix.substring(0, oldprefix.lastIndexOf("/"));
        String fileName;
        if ("/".equals(oldprefix)) {
            fileName = cosProperties.getBucketName()+".zip";
        }else {
            //期望这样的前缀 prefix = ei-dongao/image/
            prefix = prefix.substring(0,prefix.lastIndexOf("/")+1);
            String replace1 = oldprefix.replace(prefix, "");
            fileName = replace1+".zip";
        }
        String[] filePath = Convert.toStrArray(filePaths);

//      20230128新增加
        response.reset();
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment; filename="+ UrlEncoderUtils.encode(fileName));
        //设置压缩流：直接写入response，实现边压缩边下载
        ZipOutputStream zipos = null;
        try {
            zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipos.setMethod(ZipOutputStream.DEFLATED); //设置压缩方法
        } catch (Exception e) {
            e.printStackTrace();
        }

        //输出流
        CosClientUtil cosClientUtil = new CosClientUtil();

        //遍历进行判断  ei-dongao/image/common/abc.jpg
        for (String title : filePath) {
            int length = title.length();
            int slash = title.lastIndexOf("/")+1;
            if (length == slash) {
                if ("/".equals(oldprefix)) {
                    title = cosProperties.getBucketName()+"/"+title;
                }
                //目录
                getDirectory(title, zipos, cosClientUtil,prefix);
            }else {
                //文件 ei-dongao/image/common
                String replace;
                if (slash == 0) {
                    //title abc.jpg
                    replace = cosProperties.getBucketName()+"/"+title;
                }else {
                    String substring = title.substring(0, slash - 1);
                    int i = substring.lastIndexOf("/") + 1;
                    replace = title.substring(i,length);
                }
                //下载前需要判断是否有混淆情况
                String filetype = "";
                if (replace.contains(".")) {
                    filetype = replace.substring(replace.lastIndexOf(".") + 1);
                }else {
                    //下载时排除默认文件
                    String substring = title.substring(slash);
                    if ("placeholder-file".equals(substring)) {
                        continue;
                    }
//                      20221206增加解决打包下载报错问题
                    if(StringUtils.isEmpty(substring)){
                        continue;
                    }
                }
                zipos.putNextEntry(new ZipEntry(replace));
                String filepath = title.substring(0, slash);
                if ("js".equals(filetype.toLowerCase()) || "css".equals(filetype.toLowerCase())) {
                    PublishFileVersion publishFileVersion = publishFileVersionService.getLastFileVersion(Constants.Platform.COS.getValue(),filepath,replace);
                    //判断是否是混淆了的文件，混淆了的文件需要下载混淆前的源文件
                    if (publishFileVersion != null && publishFileVersion.getObfuscateFlag() != null) {
                        Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();
                        if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
                            String bakPath = publishFileVersion.getBakPathPrefix();
                            String obfuscateSourceName = publishFileVersion.getObfuscateSourceName();
                            title = bakPath+obfuscateSourceName;
                        }
                    }
                }
                byte[] bytes = cosClientUtil.getObject(title);
                IOUtils.write(bytes, zipos);
                zipos.closeEntry();
            }
        }
        IOUtils.closeQuietly(zipos);

        long end = System.currentTimeMillis();
        System.out.println("======批量下载结束======"+ end);
        long res = (end - start)/1000;
        System.out.println("======批量下载耗时======"+res+"s");
    }

    /**
     * 删除单个文件
     * @param title
     * @return
     */
    @Override
    public int deleteFile(String title) {
        try {
            //删除文件之前需要先备份一份到备份库中
            CosClientUtil cosClientUtil = new CosClientUtil();
            //由于混淆的缘故，删除需要增加处理混淆前源文件的备份操作
            int i = copyFile(title, cosClientUtil,null,1,null,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription());
            if (i > 0) {
                //备份成功再删除
                cosClientUtil.deleteObject(title);
                return 1;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 复制文件到备份目录
     * @param title
     * @param cosClientUtil
     * @throws StreamException
     */
    private int copyFile(String title, CosClientUtil cosClientUtil,Long fileSize,int delFlag,String oldtitle,
                         String logName){
        try {
            String fileName = title.substring(title.lastIndexOf("/")+1, title.length());
            String filePath = title.substring(0, title.lastIndexOf("/") + 1);
            long filesize;
            if (fileSize != null) {
                filesize = fileSize;
            }else {
                //获取元文件详细信息
                ObjectMetadata objectMetadata = cosClientUtil.getObjectMetadata(title);
                filesize = objectMetadata.getContentLength();
            }
            //备份路径
            String bakPath = ConfigConstant.cosBakPath + filePath;
            String fileNamePre;
            String filetype;
            if (fileName.contains(".")) {
                fileNamePre = fileName.substring(0,fileName.lastIndexOf("."));
                filetype = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            }else {
                fileNamePre = fileName;
                filetype = "";
            }
            if ("js".equals(filetype.toLowerCase()) || "css".equals(filetype.toLowerCase())) {
                //这两种文件会有混淆逻辑参与 获取备份表中改文件的其他属性信息
                PublishFileVersion publishFileVersion = publishFileVersionService.getLastFileVersion(Constants.Platform.COS.getValue(),filePath,fileName);
                if (publishFileVersion != null) {
                    Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();
                    if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
                        //混淆项目会有混淆文件名,先备份混淆前源文件
                        Long maxVersionNum = publishFileVersion.getVersionNum()+1;
                        String oldname = publishFileVersion.getObfuscateSourceName();
                        String newname = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+filetype;

                        String bucketName = cosProperties.getBucketName();
                        CopyResult copyResult = cosClientUtil.copy(bucketName, bakPath+oldname, bucketName, bakPath+newname);
                        if (copyResult != null) {
                            //混淆前源文件备份完成，下面继续备份混淆后文件，交给saveCosBak去实现
                            String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+filetype;
                            streamService.saveCosBak(fileName,filePath,filesize,title, bakPath,bakName,delFlag,null,
                                    filetype,maxVersionNum,newname,obfuscateFlag,logName);
                        }
                    }
                }else {
                    //遇到重命名情况时，拿重命名后的路径是无法找到版本记录的，那就需要拿之前的版本去找版本记录
                    if (StringUtils.isNotEmpty(oldtitle)) {
                        String oldfilePath = oldtitle.substring(0, oldtitle.lastIndexOf("/") + 1);
                        String oldfileName = oldtitle.substring(oldtitle.lastIndexOf("/") + 1, oldtitle.length());
                        publishFileVersion = publishFileVersionService.getLastFileVersion(Constants.Platform.COS.getValue(),oldfilePath,oldfileName);
                        Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();
                        if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
                            //混淆项目会有混淆文件名,先备份混淆前源文件
                            Long maxVersionNum = 0L;
                            String oldname = publishFileVersion.getObfuscateSourceName();
                            String newname = fileNamePre+Constants.CURRENT_UPLOAD_CONFUSED_PREFIX+maxVersionNum+"."+filetype;

                            String bucketName = cosProperties.getBucketName();
                            CopyResult copyResult = cosClientUtil.copy(bucketName, bakPath+oldname, bucketName, bakPath+newname);
                            if (copyResult != null) {
                                //混淆前源文件备份完成，下面继续备份混淆后文件，交给saveCosBak去实现
                                String bakName = fileNamePre+Constants.CURRENT_UPLOAD_BAK_PREFIX+maxVersionNum+"."+filetype;
                                streamService.saveCosBak(fileName,filePath,filesize,title, bakPath,bakName,delFlag,
                                        null,filetype,maxVersionNum,newname,obfuscateFlag,logName);
                            }
                        }
                    }else {
                        //如果新老版本title都没有记录，则
                        streamService.saveCosBak(fileName,filePath,filesize,title, bakPath,null,delFlag,null,null,
                                null,null,null,logName);
                    }
                }
            }else {
                //备份文件到备份库中 正常备份即可
                streamService.saveCosBak(fileName,filePath,filesize,title, bakPath,null,delFlag,null,null,null,null,
                        null,logName);
            }
            return 1;
        } catch (StreamException e) {
            //备份失败
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 删除文件夹
     * @param title
     * @return
     */
    @Override
    public int deleteDirectory(String title) {
        try {
            //删除之前需要先备份文件夹下的文件
            CosClientUtil cosClientUtil = new CosClientUtil();
            //复制当前目录下的目录及文件
            copyDirectory(title, cosClientUtil);
            //删除目录
            try {
                publishOperationLogService.commonSaveOperationLog(Constants.Platform.COS.getValue(),
                        Constants.OperationType.FOLDER.getValue(),null,
                        null,Constants.OPERATION_DESCRIPTION.DIRECTORY_DELETE.getDescription(), title,null,0L);
                cosClientUtil.deleteObjects(title);
            }catch (MultiObjectDeleteException mde) {
                // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
                //删除成功的对象
                //List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
                //删除失败的对象
                List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
                //对于删除失败的文件再次尝试删除
                cosClientUtil.deleteErrorsObjects(deleteErrors);
            }catch (CosServiceException e) {
                e.printStackTrace();
            }catch (CosClientException e) {
                e.printStackTrace();
            }
            return 1;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 批量删除
     * @param request
     * @param response
     * @return
     */
    @Override
    public int batchRemoveFiles(HttpServletRequest request, HttpServletResponse response) {
        String filePaths = request.getParameter("filePaths");
        String[] filePath = Convert.toStrArray(filePaths);
        //遍历进行判断
        int size = 0;
        for (String title : filePath) {
            int length = title.length();
            int slash = title.lastIndexOf("/") + 1;
            if (length == slash) {
                //目录
                int i = deleteDirectory(title);
                if (i > 0) {
                    size += 1;
                }
            }else {
                //文件
                int j = deleteFile(title);
                if (j > 0) {
                    size += 1;
                }
            }
        }
        if (size == filePath.length) {
            return 1;
        }
        return 0;
    }

    /**
     * 重命名文件
     * @param publishFiles
     * @return
     */
    @Override
    public int renameFile(PublishFiles publishFiles) {
        //重命名文件需要 1 源文件备份 2 重命名为新文件并删除源文件 3 新文件备份
        CosClientUtil cosClientUtil = new CosClientUtil();
        //1 源文件备份  title 为源文件全路径
        String title = publishFiles.getTitle();
        int i = copyFile(title, cosClientUtil, null,1,null,Constants.OPERATION_DESCRIPTION.FILE_DELETE.getDescription());
        if (i > 0) {
            // 2 重命名为新文件
            String bucketName = cosProperties.getBucketName();
            //新文件全路径
            String fileName = publishFiles.getFileName();
            String newTitle = publishFiles.getFilePath()+ fileName;
            CopyResult copyResult = cosClientUtil.copy(bucketName, title, bucketName, newTitle);
            if (copyResult != null) {
                //删除源文件
                cosClientUtil.deleteObject(title);
                //3 新文件备份时，后续根据当前新文件去版本库无法找到对应记录，那么就需要重名之前的文件路径获取对应记录和混淆前源文件路径
                int j = copyFile(newTitle, cosClientUtil,null,0,title,Constants.OPERATION_DESCRIPTION.FILE_RENAME.getDescription());
                if (j > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * 校验文件名是否重复
     * @param title
     * @return
     */
    @Override
    public String checkFileNameUnique(String title) {
        CosClientUtil cosClientUtil = new CosClientUtil();
        boolean exist = cosClientUtil.doesObjectExist(title);
        if (exist) {
            return UserConstants.POST_NAME_NOT_UNIQUE;
        }
        return UserConstants.POST_NAME_UNIQUE;
    }

    /**
     * 创建文件夹
     * @param publishFiles
     * @return
     */
    @Override
    public int createFolder(PublishFiles publishFiles) {
        CosClientUtil cosClientUtil = new CosClientUtil();
        String title = publishFiles.getFilePath() + publishFiles.getFileName() + "/";
        PutObjectResult directory =
                cosClientUtil.createDirectory(title);
        if (StringUtils.isNotEmpty(directory.getRequestId())) {
            publishOperationLogService.commonSaveOperationLog(Constants.Platform.COS.getValue(),
                    Constants.OperationType.FOLDER.getValue(),
                    null,null,
                    Constants.OPERATION_DESCRIPTION.CREATE_DIRECTORY.getDescription(),title,null,0L);
            return 1;
        }
        return 0;
    }

    /**
     * 复制当前文件夹到备份目录
     * @param title
     * @param cosClientUtil
     */
    private void copyDirectory(String title, CosClientUtil cosClientUtil) {
        List<ObjectListing> contentList = cosClientUtil.getContentList(title);
        for (ObjectListing obj : contentList) {
            //获取目录
            List<String> commonPrefixes = obj.getCommonPrefixes();
            //获取文件
            List<COSObjectSummary> objectSummaries = obj.getObjectSummaries();
            //判断是否还有子级目录
            if (commonPrefixes.size() > 0) {
                //有子级目录，继续执行上述操作
                for (String commonPrefixe : commonPrefixes) {
                    copyDirectory(commonPrefixe, cosClientUtil);
                }
            }

            if (objectSummaries.size() > 0) {
                for (COSObjectSummary cosobj : objectSummaries) {
                    //逐个复制文件到备份目录
                    String key = cosobj.getKey();
                    long size = cosobj.getSize();
                    //System.out.println(key);
                    int i = copyFile(key, cosClientUtil,size,1,null,"目录删除备份");
                    if (i == 0) {
                        //备份失败再尝试一次
                        copyFile(key, cosClientUtil,size,1,null,"目录删除备份");
                    }
                }
            }
        }
    }

    /**
     * 获取腾讯云目录及文件
     * @param title
     * @param zip
     * @param cosClientUtil
     * @throws IOException
     */
    private void getDirectory(String title, ZipOutputStream zip, CosClientUtil cosClientUtil,String prefix) throws IOException {
        String bucket = cosProperties.getBucketName() + "/";
        boolean flag = false;
        if (title.contains(bucket)) {
            flag = true;
            title = title.replace(bucket,"");
        }
        List<ObjectListing> contentList = cosClientUtil.getContentList(title);
        for (ObjectListing objl : contentList) {
            //层级目录
            List<String> commonPrefixes = objl.getCommonPrefixes();
            //对象集合
            List<COSObjectSummary> objectSummaries = objl.getObjectSummaries();
            //遍历组装返回值
            if (commonPrefixes.size() > 0) {
                for (String commonPrefix : commonPrefixes) {
                    String replace;
                    if (flag) {
                        replace = bucket+commonPrefix;
                        commonPrefix = bucket+commonPrefix;
                    }else {
                        replace = commonPrefix.replace(prefix, "");
                    }
                    // 添加到zip
                    zip.putNextEntry(new ZipEntry(replace));
                    IOUtils.write(replace, zip, com.ruoyi.common.constant.Constants.UTF8);
                    zip.closeEntry();

                    getDirectory(commonPrefix,zip,cosClientUtil,prefix);
                }
            }

            if (objectSummaries.size() > 0) {
                for (COSObjectSummary cosobj : objectSummaries) {
                    // 添加到zip
                    String key = cosobj.getKey();
                    String replace;
                    if (flag) {
                        replace = bucket+key;
                    }else {
                        replace = key.replace(prefix, "");
                    }

                    //下载前需要判断是否有混淆情况
                    String filetype = "";
                    if (replace.contains(".")) {
                        filetype = replace.substring(replace.lastIndexOf(".") + 1);
                    }else {
                        //下载时排除默认文件
                        String substring = key.substring(key.lastIndexOf("/") + 1);
                        if ("placeholder-file".equals(substring)) {
                            continue;
                        }
//                      20221206增加解决打包下载报错问题
                        if(StringUtils.isEmpty(substring)){
                            continue;
                        }
                    }
                    if ("js".equals(filetype.toLowerCase()) || "css".equals(filetype.toLowerCase())) {
                        PublishFileVersion publishFileVersion = publishFileVersionService.getLastFileVersion(Constants.Platform.COS.getValue(),prefix,replace);
                        //判断是否是混淆了的文件，混淆了的文件需要下载混淆前的源文件
                        if (publishFileVersion != null && publishFileVersion.getObfuscateFlag() != null) {
                            Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();
                            if (Constants.OBFUSCATE_FLAG.YES.getValue().equals(obfuscateFlag)) {
                                String bakPath = publishFileVersion.getBakPathPrefix();
                                String obfuscateSourceName = publishFileVersion.getObfuscateSourceName();
                                key = bakPath+obfuscateSourceName;
                            }
                        }
                    }
                    zip.putNextEntry(new ZipEntry(replace));
                    byte[] bytes = cosClientUtil.getObject(key);
                    IOUtils.write(bytes, zip);
                    zip.closeEntry();
                }
            }
        }
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
}
