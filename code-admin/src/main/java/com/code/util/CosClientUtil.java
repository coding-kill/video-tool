package com.code.util;

import com.code.config.properties.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.*;
import com.qcloud.cos.utils.IOUtils;
import com.ruoyi.common.utils.spring.SpringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上传工具类
 * @author： dongao
 * @create: 2019/10/16
 */
public class CosClientUtil {

    private static CosProperties cosProperties = SpringUtils.getBean(CosProperties.class);

    //private int circle = 0;

    /**
     * 获取cos目录层级
     * @return
     */
    public List<ObjectListing> getContentList(String prefix) {
        COSClient cosClient = null;
        List<ObjectListing> list = new ArrayList<ObjectListing>();
        try {
            // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
            // 详细代码参见本页：简单操作 -> 创建 COSClient
            cosClient = createCOSClient();

            // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
            String bucketName = cosProperties.getBucketName();

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            // 设置 bucket 名称
            listObjectsRequest.setBucketName(bucketName);
            // prefix 表示列出的对象名以 prefix 为前缀
            // 这里填要列出的目录的相对 bucket 的路径
            listObjectsRequest.setPrefix(prefix);
            // delimiter 表示目录的截断符, 例如：设置为 / 则表示对象名遇到 / 就当做一级目录）
            listObjectsRequest.setDelimiter("/");
            // 设置最大遍历出多少个对象, 一次 listobject 最大支持1000
            listObjectsRequest.setMaxKeys(1000);

            // 保存每次列出的结果
            ObjectListing objectListing = null;
            do {
                objectListing = cosClient.listObjects(listObjectsRequest);
                list.add(objectListing);
                // 标记下一次开始的位置
                String nextMarker = objectListing.getNextMarker();
                listObjectsRequest.setMarker(nextMarker);
            } while (objectListing.isTruncated());
        }catch (Exception e) {
            e.printStackTrace();
        }

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();
        return list;
    }

    // 创建 COSClient 实例，这个实例用来后续调用请求
    public COSClient createCOSClient() {
        // 这里需要已经获取到临时密钥的结果。
        // 临时密钥的生成参考 https://cloud.tencent.com/document/product/436/14048#cos-sts-sdk
        String tmpSecretId = cosProperties.getSecretId();
        String tmpSecretKey = cosProperties.getSecretKey();
        //String sessionToken = "SESSIONTOKEN";

        //COSCredentials cred = new BasicSessionCredentials(tmpSecretId, tmpSecretKey, sessionToken);
        COSCredentials cred = new BasicCOSCredentials(tmpSecretId, tmpSecretKey);

        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION 请参照 https://cloud.tencent.com/document/product/436/6224
        clientConfig.setRegion(new Region(cosProperties.getRegion()));

        // 设置请求协议, http 或者 https
        // 5.6.53 及更低的版本，建议设置使用 https 协议
        // 5.6.54 及更高版本，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.http);

        // 以下的设置，是可选的：

        // 设置 socket 读取超时，默认 30s
        clientConfig.setSocketTimeout(30*1000);
        // 设置建立连接超时，默认 30s
        clientConfig.setConnectionTimeout(30*1000);

        // 如果需要的话，设置 http 代理，ip 以及 port
        /*clientConfig.setHttpProxyIp("httpProxyIp");
        clientConfig.setHttpProxyPort(80);*/

        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }

    /**
     * 删除指定文件
     * @param key
     */
    public void deleteObject(String key) throws CosClientException, CosServiceException{
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        //folder/picture.jpg
        //String key = "exampleobject";

        cosClient.deleteObject(bucketName, key);

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();
    }

    /**
     * 批量删除指定key
     * @param keys
     * @return
     */
    public void deleteObjects(List<String> keys) throws MultiObjectDeleteException, CosClientException, CosServiceException{
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String bucketName = "examplebucket-1250000000";

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosProperties.getBucketName());
        // 设置要删除的key列表, 最多一次删除1000个
        ArrayList<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        // 传入要删除的文件名
        for (String key : keys) {
            keyList.add(new DeleteObjectsRequest.KeyVersion(key));
        }
        deleteObjectsRequest.setKeys(keyList);

        DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
        //List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
        // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
        //List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
        //List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();
    }

    /**
     * 第一次删除部分成功部分失败，再次尝试删除失败的
     * @param deleteErrors
     * @return
     */
    public void deleteErrorsObjects(List<MultiObjectDeleteException.DeleteError> deleteErrors ){
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String bucketName = "examplebucket-1250000000";

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosProperties.getBucketName());
        // 设置要删除的key列表, 最多一次删除1000个
        ArrayList<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        // 传入要删除的文件名
        for (MultiObjectDeleteException.DeleteError deleteError : deleteErrors) {
            keyList.add(new DeleteObjectsRequest.KeyVersion(deleteError.getKey()));
        }
        deleteObjectsRequest.setKeys(keyList);

        try {
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
        } catch (MultiObjectDeleteException mde) {
            // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
            //circle++;
            //List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
            /*List<MultiObjectDeleteException.DeleteError> errors = mde.getErrors();
            if (circle < 5) {
                deleteErrorsObjects(errors);
            }*/
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();
    }

    /**
     * 删除目录下对象
     * @param delDir
     */
    public void deleteObjects(String delDir) throws MultiObjectDeleteException, CosClientException, CosServiceException{
        // 列目录实现参考：列出对象 -> 简单操作 -> 列出目录下的对象和子目录
        // 批量删除实现参考本页：批量删除对象

        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String bucketName = "examplebucket-1250000000";

        // 要删除的目录，这里是相对于 bucket 的路径
        //String delDir = "exampledir";

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置 bucket 名称
        listObjectsRequest.setBucketName(cosProperties.getBucketName());
        // prefix 表示列出的对象名以 prefix 为前缀
        // 这里填要列出的目录的相对 bucket 的路径
        listObjectsRequest.setPrefix(delDir);
        // 设置最大遍历出多少个对象, 一次 listobject 最大支持1000
        listObjectsRequest.setMaxKeys(1000);

        // 保存每次列出的结果
        ObjectListing objectListing = null;

        do {
            objectListing = cosClient.listObjects(listObjectsRequest);

            // 这里保存列出的对象列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();

            ArrayList<DeleteObjectsRequest.KeyVersion> delObjects = new ArrayList<DeleteObjectsRequest.KeyVersion>();

            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                delObjects.add(new DeleteObjectsRequest.KeyVersion(cosObjectSummary.getKey()));
            }

            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosProperties.getBucketName());

            deleteObjectsRequest.setKeys(delObjects);

            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
            // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
            //List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
            //List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();

            // 标记下一次开始的位置
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());
    }

    // 创建 TransferManager 实例，这个实例用来后续调用高级接口
    private TransferManager createTransferManager() {
        // 创建一个 COSClient 实例，这是访问 COS 服务的基础实例。
        // 这里创建的 cosClient 是以复制的目的端信息为基础的
        // 详细代码参见本页: 简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);

        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);

        // 设置高级接口的配置项
        // 分块复制阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartCopyThreshold(5*1024*1024);
        transferManagerConfiguration.setMultipartCopyPartSize(1*1024*1024);
        transferManager.setConfiguration(transferManagerConfiguration);

        return transferManager;
    }

    private void shutdownTransferManager(TransferManager transferManager) {
        // 指定参数为 true, 则同时会关闭 transferManager 内部的 COSClient 实例。
        // 指定参数为 false, 则不会关闭 transferManager 内部的 COSClient 实例。
        transferManager.shutdownNow(true);
    }

    /**
     * 同地域复制
     * @param srcBucketName
     * @param srcKey
     * @param destBucketName
     * @param destKey
     * @return
     */
    public CopyResult copy(String srcBucketName,String srcKey,String destBucketName,String destKey){
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();
        // 复制的源桶所在的地域
        Region srcBucketRegion = new Region(cosProperties.getRegion());
        // 复制的源桶，命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String srcBucketName = "srcbucket-1250000000";
        // 复制的源文件路径
        //String srcKey = "path/srckey";

        // 复制的目的桶名, 命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String destBucketName = "destbucket-1250000000";
        // 复制的目的文件路径
        //String destKey = "path/destkey";

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(srcBucketRegion, srcBucketName,
                srcKey, destBucketName, destKey);
        CopyResult copyResult = null;
        try {
            Copy copy = transferManager.copy(copyObjectRequest);
            // 高级接口会返回一个异步结果 Copy
            // 可同步的调用 waitForCopyResult 等待复制结束, 成功返回 CopyResult, 失败抛出异常
            copyResult = copy.waitForCopyResult();
            //System.out.println(copyResult.getRequestId());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);
        return copyResult;
    }

    /**
     * 跨地域复制
     * @param srcBucketName
     * @param srcKey
     * @param destBucketName
     * @param destKey
     * @return
     */
    public Copy copyRegion(String srcBucketName,String srcKey,String destBucketName,String destKey){
        // 这里创建以复制的目的端信息为基础
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();
        // 复制的源桶所在的地域
        Region srcBucketRegion = new Region(cosProperties.getRegion());
        // 复制的源桶，命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String srcBucketName = "srcbucket-1250000000";
        // 复制的源文件路径
        //String srcKey = "path/srckey";

        // 复制的目的桶名, 命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        //String destBucketName = "destbucket-1250000000";
        // 复制的目的文件路径
        //String destKey = "path/destkey";

        // 这里创建的 cosClient 是以复制的源端信息为基础的
        // 详细代码参见本页: 简单操作 -> 创建 COSClient
        COSClient srcCOSClient = createCOSClient();

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(srcBucketRegion, srcBucketName,
                srcKey, destBucketName, destKey);

        Copy copy = null;
        try {
            copy = transferManager.copy(copyObjectRequest, srcCOSClient, null);
            // 高级接口会返回一个异步结果 Copy
            // 可同步的调用 waitForCopyResult 等待复制结束, 成功返回 CopyResult, 失败抛出异常
            CopyResult copyResult = copy.waitForCopyResult();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);
        return copy;
    }

    /**
     * 下载文件
     * @param key
     * @param localFilePath
     * @return
     */
    public void download(String key,String localFilePath) throws CosServiceException,CosClientException,InterruptedException{
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        //String key = "exampleobject";
        // 本地文件路径
        //String localFilePath = "/path/to/localFile";
        File downloadFile = new File(localFilePath);

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);

        // 返回一个异步结果 Donload, 可同步的调用 waitForCompletion 等待下载结束, 成功返回 void, 失败抛出异常
        Download download = transferManager.download(getObjectRequest, downloadFile);
        download.waitForCompletion();

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);

    }

    /**
     * 下载目录
     * @param cos_path
     * @param localFilePath
     * @return
     */
    public MultipleFileDownload downloadDirectory(String cos_path,
                                                  String localFilePath) {
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 示例代码：创建 TransferManager
        TransferManager transferManager = createTransferManager();
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();

        // 设置要下载的对象的前缀（相当于cos上的一个目录），如果设置成 ""，则下载整个 bucket。
        //String cos_path = "/prefix";
        // 要保存下载的文件的文件夹的绝对路径 localFilePath
        //String dir_path = "/to/mydir";

        MultipleFileDownload download = null;
        try {
            // 返回一个异步结果download, 可同步的调用waitForUploadResult等待download结束.
            download = transferManager.downloadDirectory(bucketName, cos_path, new File(localFilePath));

            // 可以选择查看下载进度
            showTransferProgress(download);

            // 或者阻塞等待完成
            download.waitForCompletion();

            //System.out.println("download directory done.");
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 示例代码：关闭 TransferManager
        shutdownTransferManager(transferManager);

        return download;
    }

    /**
     * 上传本地文件到腾讯云
     * @param key
     * @param localFilePath
     * @return
     */
    public Upload uploadFile(String key,String localFilePath){
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。
        //String key = "exampleobject";
        // 本地文件路径
        //String localFilePath = "/path/to/localFile";
        File localFile = new File(localFilePath);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);

        Upload upload = null;
        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回UploadResult, 失败抛出异常
            upload = transferManager.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);
        return upload;
    }

    /**
     * 上传流文件到腾讯云
     * @param key
     * @param inputStream
     * @return
     */
    public UploadResult uploadStream(InputStream inputStream,String key,long inputStreamLength){
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。
        //String key = "exampleobject";

        // 这里创建一个 ByteArrayInputStream 来作为示例，实际中这里应该是您要上传的 InputStream 类型的流
        //long inputStreamLength = 1024 * 1024;
        //byte data[] = new byte[inputStreamLength];
        //InputStream inputStream = new ByteArrayInputStream(data);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        // 上传的流如果能够获取准确的流长度，则推荐一定填写 content-length
        // 如果确实没办法获取到，则下面这行可以省略，但同时高级接口也没办法使用分块上传了
        objectMetadata.setContentLength(inputStreamLength);
        // 设置 Content type
        objectMetadata.setContentType("application/octet-stream");
        if (key.contains(".")) {
            String filetype = key.substring(key.lastIndexOf(".") + 1);
            String getcontentType = StreamCosClientUtil.getcontentType(filetype);
            objectMetadata.setContentType(getcontentType);
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);

        UploadResult uploadResult = null;
        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            uploadResult = upload.waitForUploadResult();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);

        return uploadResult;
    }

    /**
     * 上传文件夹
     * @param virtualDirectoryKeyPrefix
     * @param directory
     * @param includeSubdirectories
     * @return
     */
    public MultipleFileUpload uploadDirectory(String virtualDirectoryKeyPrefix,String directory,boolean includeSubdirectories){
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 示例代码：创建 TransferManager
        TransferManager transferManager = createTransferManager();
        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();

        // 设置文件上传到 bucket 之后的前缀目录，设置为 “”，表示上传到 bucket 的根目录
        //String cos_path = "/prefix";
        // 要上传的文件夹的绝对路径
        //String dir_path = "/path/to/localdir";
        // 是否递归上传目录下的子目录，如果是 true，子目录下的文件也会上传，且cos上会保持目录结构
        Boolean recursive = includeSubdirectories;

        MultipleFileUpload upload = null;
        try {
            // 返回一个异步结果Upload, 可同步的调用waitForUploadResult等待upload结束, 成功返回UploadResult, 失败抛出异常.
            upload = transferManager.uploadDirectory(bucketName, virtualDirectoryKeyPrefix, new File(directory), recursive);

            // 可以选择查看上传进度，这个函数参见 高级接口 -> 上传文件 -> 显示上传进度
            showTransferProgress(upload);

            // 或者阻塞等待完成
            upload.waitForCompletion();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 确定本进程不再使用 transferManager 实例之后，关闭之
        // 详细代码参见本页：高级接口 -> 示例代码：关闭 TransferManager
        shutdownTransferManager(transferManager);

        return upload;
    }

    // 可以参考下面的例子，结合实际情况做调整
    private void showTransferProgress(Transfer transfer) {
        // 这里的 Transfer 是异步上传结果 Upload 的父类
        System.out.println(transfer.getDescription());

        // transfer.isDone() 查询上传是否已经完成
        while (transfer.isDone() == false) {
            try {
                // 每 2 秒获取一次进度
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return;
            }

            TransferProgress progress = transfer.getProgress();
            long sofar = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            System.out.printf("upload progress: [%d / %d] = %.02f%%\n", sofar, total, pct);
        }

        // 完成了 Completed，或者失败了 Failed
        System.out.println(transfer.getState());
    }

    /**
     * 以流形式下载文件
     * @param key
     * @return
     */
    public byte[] getObject(String key) throws IOException {
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        //String key = "exampleobject";

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        COSObjectInputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 处理下载到的流
        // 这里是直接读取，按实际情况来处理
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(cosObjectInput);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 用完流之后一定要调用 close()
            cosObjectInput.close();
            // 在流没有处理完之前，不能关闭 cosClient
            // 确认本进程不再使用 cosClient 实例之后，关闭之
            cosClient.shutdown();
        }
        return bytes;
    }

    /**
     * 检查对象是否存在
     * @param key
     * @return
     */
    public boolean doesObjectExist(String key){
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        //String key = "exampleobject";

        boolean objectExists = false;
        try {
            objectExists = cosClient.doesObjectExist(bucketName, key);

        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();

        return objectExists;
    }

    /**
     * 查询对象元数据
     * @param key
     * @return
     */
    public ObjectMetadata getObjectMetadata(String key){
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
        //String key = "exampleobject";

        ObjectMetadata objectMetadata = null;
        try {
            objectMetadata = cosClient.getObjectMetadata(bucketName, key);
            //System.out.println(objectMetadata.getInstanceLength());
            //System.out.println(objectMetadata.getLastModified());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();

        return objectMetadata;
    }

    /**
     * 创建文件夹
     * @param filePath
     * @return
     */
    public PutObjectResult createDirectory(String filePath) {
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();
        // 在这里指定要创建的目录的路径
        //String key = "/example/dir/";

        // 这里创建一个空的 ByteArrayInputStream 来作为示例
        byte data[] = new byte[0];
        InputStream inputStream = new ByteArrayInputStream(data);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0);

        String fileName = "placeholder-file";

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath+fileName, inputStream, objectMetadata);
        PutObjectResult putObjectResult = null;
        try {
            putObjectResult = cosClient.putObject(putObjectRequest);
            //System.out.println(putObjectResult.getRequestId());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();

        return putObjectResult;
    }

    /**
     * 判断当前文件夹是否存在 true 表示存在该文件夹
     * @param filePath
     * @return
     */
    public boolean doesDirectoryExist(String filePath) {
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosProperties.getBucketName();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置 bucket 名称
        listObjectsRequest.setBucketName(bucketName);
        // 设置列出的对象名以 prefix 为前缀
        listObjectsRequest.setPrefix(filePath);
        // 设置最大列出多少个对象, 一次 listobject 最大支持1000
        listObjectsRequest.setMaxKeys(1);

        // 保存列出的结果
        ObjectListing objectListing = null;

        try {
            objectListing = cosClient.listObjects(listObjectsRequest);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 这里保存列出来的子目录
        List<String> commonPrefixes = objectListing.getCommonPrefixes();
        // object summary 表示此次列出的对象列表
        List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();

        // 确认本进程不再使用 cosClient 实例之后，关闭之
        cosClient.shutdown();
        if (commonPrefixes.size() == 0 && cosObjectSummaries.size() == 0) {
            //当前文件夹下无子项
            return false;
        }else {
            return true;
        }
    }
}
