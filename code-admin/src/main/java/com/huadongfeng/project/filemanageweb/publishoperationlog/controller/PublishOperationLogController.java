package com.huadongfeng.project.filemanageweb.publishoperationlog.controller;

import com.huadongfeng.project.config.constants.Constants;
import com.huadongfeng.project.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.huadongfeng.project.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.huadongfeng.project.filemanageweb.publishoperationlog.domain.PublishOperationLog;
import com.huadongfeng.project.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.huadongfeng.project.util.CosClientUtil;
import com.qcloud.cos.utils.UrlEncoderUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 操作日志信息操作处理
 * 
 * @author dongao
 * @date 2022-03-18
 */
@Controller
@RequestMapping("/project/publishOperationLog")
public class PublishOperationLogController extends BaseController
{
    private String prefix = "project/publishoperationlog";
	
	@Autowired
	private IPublishOperationLogService publishOperationLogService;

	@Autowired
	private IPublishFileVersionService publishFileVersionService;

	
	@RequiresPermissions("project:publishOperationLog:view")
	@GetMapping()
	public String publishOperationLog(ModelMap mmap, HttpServletRequest request) {
		List<Map<String,Object>> platformList = new ArrayList<>();

		Set<Map.Entry<Integer, String>> entries = Constants.Platform.typeMap.entrySet();
		for(Map.Entry<Integer, String> entry : entries){
			Map<String,Object> pcMap = new HashMap<>(4);
			pcMap.put("name",entry.getValue());
			pcMap.put("platform",entry.getKey());
			platformList.add(pcMap);
		}
		mmap.put("platformList", platformList);

		String fileName = request.getParameter("fileName");
		String path = request.getParameter("path");
		if(StringUtils.isNotEmpty(path)){

			if(StringUtils.isNotEmpty(fileName)){
				request.setAttribute("fileName",fileName);
				request.setAttribute("nowPath",path);
			}else {
				request.setAttribute("path",path);
			}
		}
	    return prefix + "/publishOperationLog";
	}
	
	/**
	 * 查询操作日志列表
	 */
	@RequiresPermissions("project:publishOperationLog:list")
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(PublishOperationLog publishOperationLog)
	{
		startPage();
        List<PublishOperationLog> list = publishOperationLogService.fuzzySelectOperationLogList(publishOperationLog);
		return getDataTable(list);
	}
	
	
	/**
	 * 导出操作日志列表
	 */
	@RequiresPermissions("project:publishOperationLog:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(PublishOperationLog publishOperationLog)
    {
    	List<PublishOperationLog> list = publishOperationLogService.selectPublishOperationLogList(publishOperationLog);
        ExcelUtil<PublishOperationLog> util = new ExcelUtil<PublishOperationLog>(PublishOperationLog.class);
        return util.exportExcel(list, "publishoperationlog");
    }
	
	/**
	 * 新增操作日志
	 */
	@GetMapping("/add")
	public String add()
	{
	    return prefix + "/add";
	}
	
	/**
	 * 新增保存操作日志
	 */
	@RequiresPermissions("project:publishOperationLog:add")
	@Log(title = "操作日志", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(PublishOperationLog publishOperationLog)
	{		
		return toAjax(publishOperationLogService.insertPublishOperationLog(publishOperationLog));
	}

	/**
	 * 修改操作日志
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap)
	{
		PublishOperationLog publishOperationLog = publishOperationLogService.selectPublishOperationLogById(id);
		mmap.put("publishoperationlog", publishOperationLog);
	    return prefix + "/edit";
	}
	
	/**
	 * 修改保存操作日志
	 */
	@RequiresPermissions("project:publishOperationLog:edit")
	@Log(title = "操作日志", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(PublishOperationLog publishOperationLog)
	{		
		return toAjax(publishOperationLogService.updatePublishOperationLog(publishOperationLog));
	}
	
	/**
	 * 删除操作日志
	 */
	@RequiresPermissions("project:publishOperationLog:remove")
	@Log(title = "操作日志", businessType = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(String ids)
	{		
		return toAjax(publishOperationLogService.deletePublishOperationLogByIds(ids));
	}

	/**
	 * 下载文件
	 */
	@RequiresPermissions("project:publishOperationLog:download")
	@RequestMapping("/cmsDownloadFile")
	@ResponseBody
	public void cmsDownloadFile(HttpServletResponse response, HttpServletRequest request) throws Exception
	{
		String fileVersionId = request.getParameter("fileVersionId");

		PublishFileVersion publishFileVersion = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(fileVersionId));
		Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();

		String path = publishFileVersion.getBakPathPrefix()+"/"+publishFileVersion.getBakName();
		if(obfuscateFlag.equals(1)){
			path = publishFileVersion.getBakPathPrefix()+"/"+publishFileVersion.getObfuscateSourceName();
		}
		File file = new File(path);

//      单个文件的时候直接下载流
//		20220402增加判断当前的版本库最新的版本是否是混淆的
		String name = file.getName();

		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis, 1024);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		byte[] cache = new byte[1024];
		int length = 0;
		while ((length = bis.read(cache)) != -1) {
			bos.write(cache, 0, length);
		}
		byte[] data = bos.toByteArray();
		genCode(response, data, name);
		IOUtils.closeQuietly(bos);
		IOUtils.closeQuietly(bis);
		IOUtils.closeQuietly(fis);

	}

	/**
	 * 下载文件
	 */
	@RequiresPermissions("project:publishOperationLog:download")
	@RequestMapping("/cosDownloadFile")
	@ResponseBody
	public void cosDownloadFile(HttpServletResponse response, HttpServletRequest request) throws Exception
	{
		String fileVersionId = request.getParameter("fileVersionId");

		PublishFileVersion publishFileVersion = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(fileVersionId));
		Integer obfuscateFlag = publishFileVersion.getObfuscateFlag();

		String path = publishFileVersion.getBakPathPrefix()+"/"+publishFileVersion.getBakName();
		if(obfuscateFlag.equals(1)){
			path = publishFileVersion.getBakPathPrefix()+"/"+publishFileVersion.getObfuscateSourceName();
		}
		File file = new File(path);

//      单个文件的时候直接下载流
//		20220402增加判断当前的版本库最新的版本是否是混淆的
		String name = file.getName();

		CosClientUtil cosClientUtil = new CosClientUtil();
		byte[] data = cosClientUtil.getObject(path);
		genCode(response, data, name);

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
