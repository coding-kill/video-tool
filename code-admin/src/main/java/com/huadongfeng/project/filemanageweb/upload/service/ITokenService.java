package com.huadongfeng.project.filemanageweb.upload.service;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户和角色关联 服务层
 * 
 * @author dongao
 * @date 2022-03-04
 */
public interface ITokenService
{
	/**
	 * 方法
	 * @param req 请求
	 * @param resp 返回
	 * @return 用户和角色关联信息
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException;


}
