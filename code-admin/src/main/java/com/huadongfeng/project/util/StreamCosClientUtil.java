package com.huadongfeng.project.util;


import com.huadongfeng.project.config.properties.CosProperties;
import com.huadongfeng.project.filemanageweb.upload.domain.FileUploadConstants;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * @author hanzh
 */
public class StreamCosClientUtil {

    private static CosProperties cosProperties = SpringUtils.getBean(CosProperties.class);


    /**
     * @description:  上传文件到cos
     * @date: 2019/9/4 18:20
     * @author: fdh
     * @param: [files, fileName, businessName] [文件字节数组, 文件名, 业务系统标识]
     * @return: java.lang.String
     */
    public static String uploadFileToCos(byte[] files, String fileName, FileUploadConstants fileUploadConstants) throws Exception {
        if( null == fileUploadConstants ){
            fileUploadConstants = FileUploadConstants.FILE;
        }

//        if (files.length > Integer.parseInt(cosProperties.getLimitSize())) {
//            throw new Exception("文件大小达到上限！");
//        }

        //生成文件夹层级
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        String folderName = String.format("%s/%s/%s/%s/%s/", cosProperties.getProjectName(), fileUploadConstants.getValue(), year, month, day);
        //生成对象键
        String key = folderName+fileName;
        try {
            InputStream inputStream = new ByteArrayInputStream(files);
            uploadFileToCosV2(inputStream, key);
            return cosProperties.getPrefixDomain() + key;
        } catch (Exception e) {
            throw new Exception("文件上传失败");
        }
    }




    /**
     * 上传到COS服务器 如果同名文件会覆盖服务器上的
     *
     * @param instream
     * @param key
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public static String uploadFileToCos(InputStream instream,Long fileSize, String key,String fileType) {
        COSClient cosClient = new CosClientUtil().createCOSClient();
        String etag = "";
        try {
            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(fileSize);
            // 设置 Content type
            objectMetadata.setContentType("application/octet-stream");
            //此方案行不通，注释掉
            /*if (!StringUtils.isNullOrEmpty(fileType) && ("jpg".equals(fileType.toLowerCase()) || "png".equals(fileType.toLowerCase()) || "gif".equals(fileType.toLowerCase()) || "jpeg".equals(fileType.toLowerCase()))) {
                Map userMeta = new HashMap(8);
                BufferedImage read = ImageIO.read(instream);
                int height = read.getHeight();
                int width = read.getWidth();
                userMeta.put("x-cos-meta-width",String.valueOf(width));
                userMeta.put("x-cos-meta-height",String.valueOf(height));
                objectMetadata.setUserMetadata(userMeta);
            }*/
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucketName(), key, instream,objectMetadata);
            // 设置存储类型, 默认是标准(Standard), 低频(standard_ia),一般为标准的
            putObjectRequest.setStorageClass(StorageClass.Standard);
            PutObjectResult putResult = cosClient.putObject(putObjectRequest);
            etag = putResult.getETag();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (instream != null) {
                    //关闭输入流
                    instream.close();
                }
                // 关闭客户端(关闭后台线程)
                cosClient.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return etag;
    }

    /**
     * Description: 判断Cos服务文件上传时文件的contentType
     *
     * @param filenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String filenameExtension) {
        String bmp = "bmp";
        if (bmp.equalsIgnoreCase(filenameExtension)) {
            return "image/bmp";
        }
        String gif = "gif";
        if (gif.equalsIgnoreCase(filenameExtension)) {
            return "image/gif";
        }
        String jpeg = "jpeg";
        String jpg = "jpg";
        String png = "png";
        if (jpeg.equalsIgnoreCase(filenameExtension) || jpg.equalsIgnoreCase(filenameExtension)
                || png.equalsIgnoreCase(filenameExtension)) {
            return "image/jpeg";
        }
        String html = "html";
        if (html.equalsIgnoreCase(filenameExtension)) {
            return "text/html";
        }
        String txt = "txt";
        if (txt.equalsIgnoreCase(filenameExtension)) {
            return "text/plain";
        }
        String vsd = "vsd";
        if (vsd.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.visio";
        }
        String pptx = "pptx";
        String ppt = "ppt";
        if (pptx.equalsIgnoreCase(filenameExtension) || ppt.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        String docx = "docx";
        String doc = "doc";
        if (docx.equalsIgnoreCase(filenameExtension) || doc.equalsIgnoreCase(filenameExtension)) {
            return "application/msword";
        }
        String xml = "xml";
        if (xml.equalsIgnoreCase(filenameExtension)) {
            return "text/xml";
        }
        return "image/jpeg";
    }


    /**
     * @description:  上传文件到cos
     * @date: 2019/9/4 18:20
     * @author: fdh
     * @param: [files, fileName, businessName] [文件字节数组, 文件名, 业务系统标识]
     * @return: java.lang.String
     */
    public static String uploadFileToCosV2(byte[] files, String fileName, FileUploadConstants fileUploadConstants) throws Exception {
        if( null == fileUploadConstants ){
            fileUploadConstants = FileUploadConstants.FILE;
        }

//        if (files.length > Integer.parseInt(Configurations.getLimitSize())) {
//            throw new Exception("文件大小达到上限！");
//        }
        //生成文件夹层级
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        String folderName = String.format("%s/%s/%s/%s/%s/%s/", cosProperties.getProjectName(), "private",fileUploadConstants.getValue(), year, month, day);
        //生成对象键
        String key = folderName+fileName;
        try {
            InputStream inputStream = new ByteArrayInputStream(files);
            uploadFileToCosV2(inputStream, key);
            return cosProperties.getPrefixDomain() + key;
        } catch (Exception e) {
            throw new Exception("文件上传失败");
        }

    }

    /**
     * 上传到COS服务器 如果同名文件会覆盖服务器上的
     *
     * @param instream
     * @param key
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public static String uploadFileToCosV2(InputStream instream, String key) {
        COSClient cosClient = new CosClientUtil().createCOSClient();
        String etag = "";
        try {
            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            //设置私有读
            objectMetadata.setHeader("x-cos-acl", "private");
            // 设置输入流长度为500
            objectMetadata.setContentLength(instream.available());
            // 设置 Content type
            objectMetadata.setContentType("application/octet-stream");
            // 上传文件
            PutObjectResult putResult = cosClient.putObject(cosProperties.getBucketName(),  key, instream, objectMetadata);
            etag = putResult.getETag();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (instream != null) {
                    //关闭输入流
                    instream.close();
                }
                // 关闭客户端(关闭后台线程)
                cosClient.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return etag;
    }



    /**
     * 初始化分块上传
     *
     * @param bucketName 桶名
     * @param key        key
     * @return
     */
    public static String initiateMultipartUpload(String bucketName, String key, String storageClass) {
        COSClient cosClient = new CosClientUtil().createCOSClient();
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, key);
        // 设置存储类型：标准存储（Standard）， 低频存储存储（Standard_IA），归档存储（ARCHIVE）。默认是标准（Standard）
        if (StringUtils.isNullOrEmpty(storageClass)) {
            request.setStorageClass(StorageClass.Standard);
        } else {
            if (StorageClass.valueOf(storageClass) == StorageClass.Standard) {
                request.setStorageClass(StorageClass.Standard);
            } else {
                request.setStorageClass(StorageClass.Standard_IA);
            }
        }
        String uploadId = null;
        try {
            InitiateMultipartUploadResult initResult = cosClient.initiateMultipartUpload(request);
            // 获取uploadid
            uploadId = initResult.getUploadId();
        } catch (CosServiceException e) {
            e.printStackTrace();
        }
        return uploadId;
    }

    /**
     * 分块上传
     */
    public static String  batchUpload(String uploadId, InputStream inputStream, Long fileSize,
                                   Integer partNumber, String key, Boolean isLastPart) {
        try {
            COSClient cosClient = new CosClientUtil().createCOSClient();
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(cosProperties.getBucketName());
            uploadPartRequest.setKey(key);
            uploadPartRequest.setUploadId(uploadId);

            // 设置分块的数据来源输入流
            uploadPartRequest.setInputStream(inputStream);
            // 设置分块的长度
            uploadPartRequest.setPartSize(fileSize); // 设置数据长度
            uploadPartRequest.setPartNumber(partNumber);     // 假设要上传的part编号是10
            uploadPartRequest.setLastPart(isLastPart);

            UploadPartResult uploadPartResult = cosClient.uploadPart(uploadPartRequest);
            PartETag partETag = uploadPartResult.getPartETag();
            System.out.println(partETag.getPartNumber());
            System.out.println(partETag.getETag());
            return partETag.getETag();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static  COSObjectInputStream readFile(String key) throws Exception {
        // 生成cos客户端
        COSClient cosClient = new CosClientUtil().createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        COSObject object = cosClient.getObject(bucketName, key);
        COSObjectInputStream objectContent = object.getObjectContent();
        return objectContent;
    }

    public static void main(String[] args) throws Exception {
//        readRealFile("/pcdist/zckjs/2022-zsfa/images/popup/popup_wy.png");
//        Path path = Paths.get("/pcdist/zckjs/2022-zsfa/images/popup/popup_wy.js");
//        String contentType = null;
//        try {
//            contentType = Files.probeContentType(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("File content type is : " + contentType);

    }


}
