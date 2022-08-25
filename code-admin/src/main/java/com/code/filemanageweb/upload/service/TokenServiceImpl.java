package com.code.filemanageweb.upload.service;

import com.code.config.streamconfig.ConfigConstant;
import com.code.util.TokenUtil;
import com.ruoyi.common.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * According the file name and its size, generate a unique token. And this
 * token will be refer to user's file.
 * @author dongao
 */
@Service
public class TokenServiceImpl implements ITokenService {
	private static final long serialVersionUID = 2650340991003623753L;
	static final String FILE_NAME_FIELD = "name";
	static final String FILE_SIZE_FIELD = "size";
	static final String TOKEN_FIELD = "token";
	static final String SERVER_FIELD = "server";
	static final String SUCCESS = "success";
	static final String MESSAGE = "message";
	

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doOptions(req,resp);
		String name = req.getParameter(FILE_NAME_FIELD);
		String size = req.getParameter(FILE_SIZE_FIELD);
//		20220322增加路径的传递
		String param2 = req.getParameter("param2");
		if(StringUtils.isNotEmpty(param2)){
			try {
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
				param2 = java.net.URLDecoder.decode(param2,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			}
		}

		String token = TokenUtil.generateToken(name, size, param2);
		PrintWriter writer = resp.getWriter();

		JSONObject json = new JSONObject();
		try {
			json.put(TOKEN_FIELD, token);
			if (ConfigConstant.isCrossed){
				json.put(SERVER_FIELD, ConfigConstant.crossServer);
			}
			json.put(SUCCESS, true);
			json.put(MESSAGE, "");
		} catch (JSONException e) {
		}
		/** TODO: save the token. */
		writer.write(json.toString());
	}


	public static void doOptions(HttpServletRequest req, HttpServletResponse resp){
		resp.setContentType("application/json;charset=utf-8");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Range,Content-Type");
		resp.setHeader("Access-Control-Allow-Origin", ConfigConstant.crossOrigins);
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	}


}
