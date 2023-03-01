package com.huadongfeng.project.config.streamconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 添加注解@Component 使当前类成为一个bean对象。(@Controller,@service都行)
 * 定义static的变量
 * 添加@PostConstruct注解注解注释的方法，在这个方法里，将自动注入的值赋值给定义的static变量
 * static变量替代自动注入在static方法里面使用
 * @author dongao
 */
@Component
public class ConfigConstant {
    @Autowired
    private Environment env;

    /**Cos相关*/
    public static String fileRepository;
    public static String crossServer;
    public static String crossOrigins;
    public static boolean isDeleteFinished;
    public static boolean isCrossed;
    public static String mPath;
    public static String cosBakPath;
    public static String cosTempPath;
    public static String nodejsServer;
    public static String ztPath;
    public static String mZtPath;

    public static boolean licenseFlag;
    /**
     * 各个平台的根目录的集合
     */
    public static Map<Integer, String> allRootPath = new HashMap<Integer, String>();

    /**
     * 当DI容器实例化当前受管Bean时，@PostConstruct注解的方法会被自动触发，从而完成一些初始化工作
     * 只有一个方法可以使用此注释进行注解；
     * 被注解方法不得有任何参数
     * 被注解方法返回值为void
     * 被注解方法不得抛出已检查异常
     * 被注解方法需是非静态方法
     * 此方法只会被执行一次
     */
    @PostConstruct
    public void readConfig() {
//      默认的pc根目录path
        fileRepository = env.getProperty("STREAM_FILE_REPOSITORY");
        crossServer = env.getProperty("STREAM_CROSS_SERVER");
        crossOrigins = env.getProperty("STREAM_CROSS_ORIGIN");
        isDeleteFinished = Boolean.parseBoolean(env.getProperty("STREAM_DELETE_FINISH"));
        isCrossed = Boolean.parseBoolean(env.getProperty("STREAM_IS_CROSS"));
//      默认的m的根目录
        mPath = env.getProperty("M_STREAM_FILE_REPOSITORY");
//      腾讯云上传备份的地方
        cosBakPath = env.getProperty("COS_BAK_STREAM_FILE_REPOSITORY");
        // cos 服务器临时文件存放
        cosTempPath = env.getProperty("COS_TEMP_FILE_REPOSITORY");
        nodejsServer = env.getProperty("NODEJS_SERVER");


//      专题的PC和M根路径
        ztPath = env.getProperty("CMS_ZT_ROOT_PATH");
        mZtPath = env.getProperty("M_CMS_ZT_ROOT_PATH");
//      东奥在线pc根路径
        allRootPath.put(2,fileRepository);
//      东奥在线m根路径
        allRootPath.put(3,mPath);
//      东奥专题根路径
        allRootPath.put(4,ztPath);
//      东奥专题m根路径
        allRootPath.put(5,mZtPath);

        licenseFlag = Boolean.parseBoolean(env.getProperty("license.flag"));



    }

}
