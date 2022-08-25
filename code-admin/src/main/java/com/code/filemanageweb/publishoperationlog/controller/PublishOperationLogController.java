package com.code.filemanageweb.publishoperationlog.controller;

import com.code.config.constants.Constants;
import com.code.filemanageweb.publishoperationlog.domain.PublishOperationLog;
import com.code.filemanageweb.publishoperationlog.service.IPublishOperationLogService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

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
	
	@RequiresPermissions("project:publishOperationLog:view")
	@GetMapping()
	public String publishOperationLog(ModelMap mmap) {
		List<Map<String,Object>> platformList = new ArrayList<>();

		Set<Map.Entry<Integer, String>> entries = Constants.Platform.typeMap.entrySet();
		for(Map.Entry<Integer, String> entry : entries){
			Map<String,Object> pcMap = new HashMap<>(4);
			pcMap.put("name",entry.getValue());
			pcMap.put("platform",entry.getKey());
			platformList.add(pcMap);
		}
		mmap.put("platformList", platformList);

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
	
}
