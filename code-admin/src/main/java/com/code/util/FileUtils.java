/**
 * Copyright 东奥
 * FileName: FileUtils
 * Author:   sangsk
 * Date:     2022/8/25 14:13
 * Description:
 * History:
 */
package com.code.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author dongao
 * @create 2022/8/25
 * @since 1.0.0
 */
public class FileUtils {

//  查询给定路径内的重复文件
    public static void main(String[] args) {
        Set<String> md5Set = new HashSet<>();
        Map<String,Object> md5Map = new HashMap<>();
        String path = "E:\\";
        File file = new File(path);
        if(file.isDirectory()){
            getDirectoryChildMd5(path, md5Set,md5Map);
        }else {
            try {
                String md5 = DigestUtils.md5Hex(new FileInputStream(file.getAbsolutePath()));
                if(md5Set.contains(md5)){
                    System.out.println("当前文件重复，文件名--》"+file.getAbsolutePath()+";已有文件路径--》"+md5Map.get(md5));
                }else {
                    md5Set.add(md5);
                    md5Map.put(md5,file.getAbsolutePath());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void getDirectoryChildMd5(String path,Set<String> md5Set,Map<String,Object> md5Map){
        File file = new File(path);
        File[] files = file.listFiles();
        for (File getFile : files) {
            String currentPath = getFile.toPath().toString();
            currentPath = currentPath.replaceAll("\\\\", "/");
            if(getFile.isDirectory()){
                getDirectoryChildMd5(currentPath, md5Set,md5Map);
            }else {
                try {
                    String md5 = DigestUtils.md5Hex(new FileInputStream(getFile.getAbsolutePath()));
                    if(md5Set.contains(md5)){
                        System.out.println("当前文件重复，文件名--》"+getFile.getAbsolutePath()+";已有文件路径--》"+md5Map.get(md5));
                    }else {
                        md5Set.add(md5);
                        md5Map.put(md5,getFile.getAbsolutePath());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
