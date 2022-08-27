package com.code.filemanageweb.publishfileversion.controller;

import com.code.config.constants.Constants;
import com.code.config.properties.CosProperties;
import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.code.filemanageweb.publishfileversion.service.IPublishFileVersionService;
import com.code.util.CosClientUtil;
import com.code.util.DiffHandleUtils;
import com.code.util.IoUtil;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Calendar;
import java.util.List;

/**
 * 文件版本信息操作处理
 *
 * @author dongao
 * @date 2022-03-18
 */
@Controller
@RequestMapping("/project/publishFileVersion")
public class PublishFileVersionController extends BaseController
{
	private String prefix = "project/publishfileversion";

	@Autowired
	private IPublishFileVersionService publishFileVersionService;
	@Autowired
	private CosProperties cosProperties;

	@RequiresPermissions("project:publishFileVersion:view")
	@GetMapping()
	public String publishFileVersion()
	{
		return prefix + "/publishfileversion";
	}

	/**
	 * 查询文件版本列表
	 */
	@RequiresPermissions("project:publishFileVersion:list")
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(PublishFileVersion publishFileVersion)
	{
		startPage();
		List<PublishFileVersion> list = publishFileVersionService.selectPublishFileVersionList(publishFileVersion);
		return getDataTable(list);
	}


	/**
	 * 导出文件版本列表
	 */
	@RequiresPermissions("project:publishFileVersion:export")
	@PostMapping("/export")
	@ResponseBody
	public AjaxResult export(PublishFileVersion publishFileVersion)
	{
		List<PublishFileVersion> list = publishFileVersionService.selectPublishFileVersionList(publishFileVersion);
		ExcelUtil<PublishFileVersion> util = new ExcelUtil<PublishFileVersion>(PublishFileVersion.class);
		return util.exportExcel(list, "publishfileversion");
	}

	/**
	 * 新增文件版本
	 */
	@GetMapping("/add")
	public String add()
	{
		return prefix + "/add";
	}

	/**
	 * 新增保存文件版本
	 */
	@RequiresPermissions("project:publishFileVersion:add")
	@Log(title = "文件版本", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(PublishFileVersion publishFileVersion)
	{
		return toAjax(publishFileVersionService.insertPublishFileVersion(publishFileVersion));
	}

	/**
	 * 修改文件版本
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap)
	{
		PublishFileVersion publishFileVersion = publishFileVersionService.selectPublishFileVersionById(id);
		mmap.put("publishfileversion", publishFileVersion);
		return prefix + "/edit";
	}

	/**
	 * 修改保存文件版本
	 */
	@RequiresPermissions("project:publishFileVersion:edit")
	@Log(title = "文件版本", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(PublishFileVersion publishFileVersion)
	{
		return toAjax(publishFileVersionService.updatePublishFileVersion(publishFileVersion));
	}

	/**
	 * 删除文件版本
	 */
	@RequiresPermissions("project:publishFileVersion:remove")
	@Log(title = "文件版本", businessType = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(String ids)
	{
		return toAjax(publishFileVersionService.deletePublishFileVersionByIds(ids));
	}



	/**
	 * 查询文件版本列表
	 */
	@RequiresPermissions("project:publishFileVersion:cmsFileVersionList")
	@PostMapping("/cmsFileVersionList")
	@ResponseBody
	public TableDataInfo cmsFileVersionList(PublishFileVersion publishFileVersion)
	{
		PageDomain pageDomain = TableSupport.buildPageRequest();
		String orderBy = pageDomain.getOrderBy();
		PageHelper.orderBy("temp_table."+orderBy);
		List<PublishFileVersion> list = publishFileVersionService.selectCmsFileVersionList(publishFileVersion);
		return getDataTable(list);
	}


	/**
	 * 查询子类试题列表
	 */
	@RequiresPermissions("project:publishFileVersion:getOneFileHistory")
	@PostMapping("/getOneFileHistory")
	@ResponseBody
	public AjaxResult getOneFileHistory(PublishFileVersion publishFileVersion)
	{
//      查询子类试题的时候只用父id就可以，不用其他条件
		List<PublishFileVersion> list = publishFileVersionService.getOneFileHistory(publishFileVersion);
		return AjaxResult.success(list);
	}


	/**
	 * 修改文件版本
	 */
	@GetMapping("/selectCompared")
	public String selectCompared(Long id, ModelMap mmap)
	{
		PublishFileVersion publishFileVersion = publishFileVersionService.selectPublishFileVersionById(id);
		mmap.put("publishfileversion", publishFileVersion);
		return prefix + "/selectCompared";
	}



	/**
	 * 查询文件版本列表
	 */
	@PostMapping("/getFileAllVersion")
	@ResponseBody
	public TableDataInfo getFileAllVersion(PublishFileVersion publishFileVersion)
	{
		List<PublishFileVersion> list = publishFileVersionService.getOneFileHistory(publishFileVersion);
		return getDataTable(list);
	}


	/**
	 * 还原文件
	 * @param id
	 * @return
	 */
	@RequiresPermissions("project:publishCosFiles:rollbackFile")
	@RequestMapping("/rollbackFile")
	@ResponseBody
	public AjaxResult rollbackFile(Long id) {
		try {
			if(id==null||id==0L){
				return error("参数为空，请刷新页面重试");
			}
			return publishFileVersionService.rollbackFile(id);
		} catch (Exception e) {
			return error(e.getMessage());
		}
	}

	@RequestMapping("/comparedThisFile")
	public String comparedThisFile(HttpServletRequest request, ModelMap mmap)
	{
		String text1path = null;
		String text2path = null;
		//String htmlPath = null;
		String platform = request.getParameter("platform");
		try {
			String leftId = request.getParameter("leftId");
			String rightId = request.getParameter("rightId");
			String profile = ConfigConstant.cosTempPath;
			PublishFileVersion left = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(leftId));
//			获取左边文件路径
			String bakpath = left.getBakPathPrefix();
			String bakname = left.getBakName();
			//file.text_0
			String filename = left.getName();
			String downname = filename +"_"+left.getVersionNum();
			if (filename.contains(".")) {
				downname = filename.replace(".","_"+left.getVersionNum()+".");
			}
			if(left.getObfuscateFlag()==1){
//				混淆的时候文件名用混淆的那个名字
				bakname = left.getObfuscateSourceName();
			}
			String leftkey = bakpath + bakname;

			PublishFileVersion right = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(rightId));
//			获取右边文件路径
			String bakpath2 = right.getBakPathPrefix();
			String bakname2 = right.getBakName();
			//file.text_0
			String filename2 = right.getName();
			String downname2 = filename2 +"_"+right.getVersionNum();
			if (filename2.contains(".")) {
				downname2 = filename2.replace(".","_"+right.getVersionNum()+".");
			}
			if(right.getObfuscateFlag()==1){
//				混淆的时候文件名用混淆的那个名字
				bakname2 = right.getObfuscateSourceName();
			}
			String rightkey = bakpath2 + bakname2;

			if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
				CosClientUtil cosClientUtil = new CosClientUtil();
				try {
					//下载到服务器
					text1path = profile + downname;
					cosClientUtil.download(leftkey, text1path);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					//下载到服务器
					text2path = profile + downname2;
					cosClientUtil.download(rightkey, text2path);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				text1path = bakpath+File.separator+bakname;
				text2path = bakpath2+File.separator+bakname2;
			}
			List<String> diffString = DiffHandleUtils.diffString(text1path,text2path,downname,downname2);
			//System.out.println("======diff::::::"+diffString);
			//在服务器生成一个diff.html文件，打开便可看到两个文件的对比
			//htmlPath = profile + "compareresult.html";
			String string = DiffHandleUtils.generateDiffString(diffString);
			//System.out.println("======res::::::"+string);
			mmap.put("right", string);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
				//删除下载的临时文件
				if (StringUtils.isNotEmpty(text1path)) {
					boolean delete = new File(text1path).delete();
				}
				if (StringUtils.isNotEmpty(text2path)) {
					boolean delete = new File(text2path).delete();
				}
			}
		}
		return  prefix+"/newComparedFile";
	}

	@RequestMapping("/comparedThisFile2")
	@ResponseBody
	public AjaxResult comparedThisFile2(HttpServletRequest request, ModelMap mmap)
	{
		String text1path = null;
		String text2path = null;
		String htmlPath = null;
		String key = null;
		FileInputStream inputStream = null;
		String platform = request.getParameter("platform");
		try {
			String leftId = request.getParameter("leftId");
			String rightId = request.getParameter("rightId");
			String profile = ConfigConstant.cosTempPath;
			PublishFileVersion left = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(leftId));
//			获取左边文件路径
			String bakpath = left.getBakPathPrefix();
			String bakname = left.getBakName();
			//file.text_0
			String filename = left.getName();
			String downname = filename +"_"+left.getVersionNum();
			if (filename.contains(".")) {
				downname = filename.replace(".","_"+left.getVersionNum()+".");
			}
			if(left.getObfuscateFlag()==1){
//				混淆的时候文件名用混淆的那个名字
				bakname = left.getObfuscateSourceName();
			}
			String leftkey = bakpath + bakname;

			PublishFileVersion right = publishFileVersionService.selectPublishFileVersionById(Long.parseLong(rightId));
//			获取右边文件路径
			String bakpath2 = right.getBakPathPrefix();
			String bakname2 = right.getBakName();
			//file.text_0
			String filename2 = right.getName();
			String downname2 = filename2 +"_"+right.getVersionNum();
			if (filename2.contains(".")) {
				downname2 = filename2.replace(".","_"+right.getVersionNum()+".");
			}
			if(right.getObfuscateFlag()==1){
//				混淆的时候文件名用混淆的那个名字
				bakname2 = right.getObfuscateSourceName();
			}
			String rightkey = bakpath2 + bakname2;

			if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
				CosClientUtil cosClientUtil = new CosClientUtil();
				try {
					//下载到服务器
					text1path = profile + downname;
					cosClientUtil.download(leftkey, text1path);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					//下载到服务器
					text2path = profile + downname2;
					cosClientUtil.download(rightkey, text2path);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				text1path = bakpath+File.separator+bakname;
				text2path = bakpath2+File.separator+bakname2;
			}
			List<String> diffString = DiffHandleUtils.diffString(text1path,text2path,downname,downname2);
			//在服务器生成一个diff.html文件，打开便可看到两个文件的对比
			String fileName = "compareresult_"+System.currentTimeMillis()+".html";
			htmlPath = profile + fileName;
			DiffHandleUtils.generateDiffHtml(diffString, htmlPath);

			//生成文件夹层级
			Calendar now = Calendar.getInstance();
			int year = now.get(Calendar.YEAR);
			int month = now.get(Calendar.MONTH) + 1;
			int day = now.get(Calendar.DAY_OF_MONTH);

			String folderName = String.format("%s%s/%s/%s/%s/", ConfigConstant.cosBakPath, "costemp", year, month, day);

			key = folderName + fileName;
			inputStream = new FileInputStream(htmlPath);
			CosClientUtil cosClientUtil = new CosClientUtil();
			cosClientUtil.uploadStream(inputStream,key,inputStream.available());
			key = cosProperties.getPrefixDomain()+key;
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			//删除下载的临时文件
			if (Constants.Platform.COS.getValue().equals(Integer.valueOf(platform))) {
				if (StringUtils.isNotEmpty(text1path)) {
					boolean delete = new File(text1path).delete();
				}
				if (StringUtils.isNotEmpty(text2path)) {
					boolean delete = new File(text2path).delete();
				}
			}
			if (inputStream != null) {
				IoUtil.close(inputStream);
			}
			if (StringUtils.isNotEmpty(htmlPath)) {
				boolean delete = new File(htmlPath).delete();
			}
		}
		return  AjaxResult.success(key);
	}

	public void getFileByBytes(byte[] bytes, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			// 判断文件目录是否存在
			if (!dir.exists() && dir.isDirectory()) {
				dir.mkdirs();
			}
			file = new File(filePath + "\\" + fileName);

			//输出流
			fos = new FileOutputStream(file);

			//缓冲流
			bos = new BufferedOutputStream(fos);

			//将字节数组写出
			bos.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
