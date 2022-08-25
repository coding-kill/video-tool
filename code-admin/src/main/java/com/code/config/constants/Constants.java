package com.code.config.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:Constants
 * @author:dongao
 * @date 2022/1/27 10:37
 */
public class Constants {

    //东奥短信平台密钥
    public static final String DASMS_KEY = "DAGX#1571ecba5ce5a4c65e07c1d928c6e927";

    //主要人员
    public static final String ONE_KEY = "1962550931785042";
    public static final String TWO_KEY = "21664809241244222";

    //校验是否唯一
    public final static String UNIQUE = "0";
    public final static String NOT_UNIQUE = "1";

    /**
     * 钉钉消息类型
     */
    public static enum MsgType{
        TEXT("text"),IMAGE("image"),FILE("file"),LINK("link"),MARKDOWN("markdown");
        private String value;
        MsgType(String value) {
            this.value = value;
        }
        public String getValue() {return value;}
    }

    /**
     * 文件上传平台
     */
    public static enum Platform{
        COS(1,"腾讯云"),
        CMS_DAZX_PC(2,"东奥在线PC"),
        CMS_DAZX_M(3,"东奥在线H5"),
        CMS_DAZT_PC(4,"东奥专题PC"),
        CMS_DAZT_M(5,"东奥专题H5");

        private Integer value;
        private String description;

        public static Map<Integer, String> typeMap = new HashMap<>();

        static {
            Platform[] types = Platform.values();
            for (Platform type : types) {
                typeMap.put(type.value, type.description);
            }
        }

        private Platform(Integer value, String description) {
            this.value = value;
            this.description = description;
        }
        public Integer getValue() {
            return this.value;
        }
        public String getDescription() {
            return this.description;
        }

        public static String getDescByValue(Integer value) {
            Platform[] platforms = Platform.values();
            for (Platform sta : platforms) {
                if (sta.getValue().equals(value)) {
                    return sta.getDescription();
                }
            }
            return "";
        }

    }

    /**
     * 操作类型，文件夹或者文件
     */
    public static enum OperationType{
        FOLDER(1,"文件夹"),
        FILE(2,"文件");

        private Integer value;
        private String description;
        private OperationType(Integer value, String description) {
            this.value = value;
            this.description = description;
        }
        public Integer getValue() {
            return this.value;
        }
        public String getDescription() {
            return this.description;
        }
    }

    /**显示文件备份*/
    public final static String CURRENT_UPLOAD_BAK_PREFIX = "-current-bak-";
    /**混淆前源文件备份*/
    public final static String CURRENT_UPLOAD_CONFUSED_PREFIX = "-current-confused-bak-";

    /**
     * cms不同的平台备份项目在根路径的后缀
     */
    public final static String CMS_BAK_PATH_SUFFIX = "/ei_bak";
    /**
     * 是否混淆 0 否 1 是
     */
    public static enum OBFUSCATE_FLAG{
        YES(1,"是"),
        NO(0,"否");

        private Integer value;
        private String description;
        private OBFUSCATE_FLAG(Integer value, String description) {
            this.value = value;
            this.description = description;
        }
        public Integer getValue() {
            return this.value;
        }
        public String getDescription() {
            return this.description;
        }
    }

    /**
     * 操作说明
     */
    public static enum OPERATION_DESCRIPTION{
        FILE_UPLOAD(1,"文件上传"),
        FILE_DELETE(2,"文件删除"),
        FILE_RENAME(3,"文件重命名"),
        DIRECTORY_DELETE(4,"文件夹删除"),
        FILE_ROLLBACK(5,"文件还原"),
        CREATE_DIRECTORY(6,"文件夹创建"),
        OPERATION_FAILED(7,"操作失败");

        private Integer value;
        private String description;
        private OPERATION_DESCRIPTION(Integer value, String description) {
            this.value = value;
            this.description = description;
        }
        public Integer getValue() {
            return this.value;
        }
        public String getDescription() {
            return this.description;
        }
    }

}
