package com.code.util;



import com.code.config.properties.CosProperties;
import com.ruoyi.common.utils.spring.SpringUtils;

import java.io.IOException;

/**
 * Key Util: 1> according file name|size ..., generate a key;
 * 			 2> the key should be unique.
 */
public class TokenUtil {

    private static CosProperties cosProperties = SpringUtils.getBean(CosProperties.class);


    /**
	 * 20220322增加路径的传递
	 * 生成Token， A(hashcode>0)|B + |name的Hash值| +_+size的值
	 * @param name
	 * @param size
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String generateToken(String name, String size, String path)
			throws IOException {
		if (name == null || size == null){
			return "";
		}
		int code = name.hashCode();
		try {
			String token = "";
			//10M以下  无需分片
			if(Integer.valueOf(size.trim()) < 10485760){
				token = (code > 0 ? "A" : "B") + Math.abs(code) + "_" + size.trim();
			}else{
				String uploadId = StreamCosClientUtil.initiateMultipartUpload(cosProperties.getBucketName(), name, null);
				token = (code > 0 ? "A" : "B") + Math.abs(code)+ "_"+uploadId + "_" + size.trim();
			}
			/** TODO: store your token, here just create a file */
			IoUtil.storeToken(token,path);
			
			return token;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
