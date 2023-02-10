package com.code.filemanageweb.publishcmsfiles.controller;

import com.code.config.constants.Constants;
import com.code.config.streamconfig.ConfigConstant;
import com.code.filemanageweb.publishcmsfiles.service.IPublishCmsFilesService;
import com.code.filemanageweb.publishfileversion.domain.PublishFileVersion;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * cms文件处理
 *
 * @author dongao
 * @date 2022-03-18
 */
@Controller
@RequestMapping("/project/publishCmsFiles")
public class PublishCmsFilesController extends BaseController
{
	private String prefix = "project/publishcmsfiles";

	@Autowired
	private IPublishCmsFilesService publishCmsFilesService;

	@RequiresPermissions("project:publishCmsFiles:view")
	@GetMapping()
	public String publishFileVersion(ModelMap mmap)
	{
//		默认的pc的根路径
		mmap.put("path", ConfigConstant.allRootPath.get(Constants.Platform.CMS_DAZX_PC.getValue()));
		mmap.put("platform", Constants.Platform.CMS_DAZX_PC.getValue());

		List<Map<String,Object>> platformList = new ArrayList<>();


		Set<Map.Entry<Integer, String>> entries = ConfigConstant.allRootPath.entrySet();
		for(Map.Entry<Integer, String> entry : entries){
			if(entry.getKey()==1){
//				跳过腾讯云获取其他本地平台
				continue;
			}
			Map<String,Object> pcMap = new HashMap<>(4);
			pcMap.put("name",Constants.Platform.getDescByValue(entry.getKey()));
			pcMap.put("path",entry.getValue());
			pcMap.put("platform",entry.getKey());
			platformList.add(pcMap);
		}
		mmap.put("platformList", platformList);

		return prefix + "/publishcmsfiles";
	}

	/**
	 * 查询文件版本列表
	 */
	@PostMapping("/treeData")
    @RequiresPermissions("project:publishCmsFiles:treeData")
	@ResponseBody
	public AjaxResult treeData(String name)
	{
		List<PublishFileVersion> list = publishCmsFilesService.getFilesTreeData(name);
		return AjaxResult.success(list);
	}

	/**
	 * 查询文件版本列表
	 */
	@RequiresPermissions("project:publishCmsFiles:list")
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(PublishFileVersion publishFileVersion)
	{
		PageDomain pageDomain = TableSupport.buildPageRequest();
		List<PublishFileVersion> list = publishCmsFilesService.getFilesByPath(publishFileVersion,pageDomain);
		return getDataTable(list);
	}



	/**
	 * 创建文件夹
	 */
	@GetMapping("/createFolder")
    @RequiresPermissions("project:publishCmsFiles:createFolder")
	public String createFolder(HttpServletRequest request)throws Exception {
		String path = request.getParameter("path");
		String platform = request.getParameter("platform");
		if(StringUtils.isNotEmpty(path)){
			try {
				path = java.net.URLDecoder.decode(path,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw e;
			}
		}else {
			throw new ServiceException("路径为空");
		}
		request.setAttribute("path",path);
		request.setAttribute("platform",platform);

		return prefix + "/create_folder";
	}

	/**
	 * 新增保存操作日志
	 */
	@RequiresPermissions("project:publishCmsFiles:createFolder")
	@Log(title = "创建文件夹", businessType = BusinessType.OTHER)
	@PostMapping("/createFolder")
	@ResponseBody
	public AjaxResult createFolderSave(HttpServletRequest request) {
		return publishCmsFilesService.createFolder(request);
	}

	/**
	 * 批量下载
	 */
	@RequiresPermissions("project:publishCmsFiles:batchDownload")
	@RequestMapping("/batchDownloadFiles")
	@ResponseBody
	public void batchDownloadFiles(HttpServletResponse response, String filePaths, Integer platform) throws IOException
	{
		String[] filePath = Convert.toStrArray(filePaths);
		publishCmsFilesService.batchDownloadFiles(filePath,response,platform);
	}


	/**
	 * 预览图片
	 * @param request
	 * @param mmap
	 * @return
	 */
	@RequiresPermissions("project:publishCmsFiles:viewImage")
	@RequestMapping("/viewImage")
	public String viewImage(HttpServletRequest request, ModelMap mmap)
	{
		String filePath = request.getParameter("filePath");
		mmap.put("filePath", filePath);
		return prefix + "/viewImage";
	}

	@RequestMapping("/getImageOutputStream")
    @RequiresPermissions("project:publishCmsFiles:viewImage")
	public void getImageOutputStream(HttpServletRequest request, HttpServletResponse response)throws Exception {
		String imgPath=request.getParameter("imgPath");
		// photoUrl为接收到的路径
		if(!StringUtils.isNotBlank(imgPath)){
			return;
		}
		File file = new File(imgPath);
		if (!file.exists()) {
			return;
		}
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis, 1024);
			bos = new ByteArrayOutputStream(1024);
			byte[] cache = new byte[1024];
			int length = 0;
			while ((length = bis.read(cache)) != -1) {
				bos.write(cache, 0, length);
			}
			response.getOutputStream().write(bos.toByteArray());
		}catch (Exception e){
			throw e;
		}finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(fis);
		}

	}


    /**
     * 下载文件
     */
    @RequiresPermissions("project:publishCmsFiles:download")
    @RequestMapping("/downloadFile")
    @ResponseBody
    public void downloadFile(HttpServletResponse response, HttpServletRequest request) throws Exception
    {
        publishCmsFilesService.downloadFile(request,response);
    }


	/**
	 * 删除单个文件
	 * @param path
	 * @return
	 */
	@RequiresPermissions("project:publishCosFiles:remove")
	@PostMapping("/delFile")
	@ResponseBody
	public AjaxResult delFile(String path,String platform) {
		try {
			String[] filePath = Convert.toStrArray(path);
			return publishCmsFilesService.deleteFile(filePath,platform);
		} catch (Exception e) {
			return error(e.getMessage());
		}
	}


    /**
     * 获取目录历史版本页面
     */
    @GetMapping("/getHistoricVersion")
    @RequiresPermissions("project:publishCmsFiles:getHistoricVersion")
    public String getHistoricVersion(HttpServletRequest request)throws Exception {
        String path = request.getParameter("path");
        String platform = request.getParameter("platform");
        request.setAttribute("path",path);
        request.setAttribute("platform",platform);

        String fileName = request.getParameter("fileName");
        if(StringUtils.isNotEmpty(fileName)){
            request.setAttribute("name",fileName);
        }

        return prefix + "/publishCmsFileVersion";
    }


	/**
	 * 重命名
	 */
	@GetMapping("/renameFile")
    @RequiresPermissions("project:publishCmsFiles:rename")
	public String renameFile(HttpServletRequest request) {
		String path = request.getParameter("path");
		String name = request.getParameter("name");
		String platform = request.getParameter("platform");
		request.setAttribute("path",path);
		request.setAttribute("name",name);
		request.setAttribute("platform",platform);

		return prefix + "/renameFile";
	}

	/**
	 * 文件重命名
	 */
	@RequiresPermissions("project:publishCmsFiles:rename")
	@Log(title = "重命名", businessType = BusinessType.OTHER)
	@PostMapping("/renameFile")
	@ResponseBody
	public AjaxResult renameFileSave(HttpServletRequest request) {
		return publishCmsFilesService.renameFileSave(request);
	}


    /**
    * 校验目录是否存在
    */
    @Log(title = "校验目录是否存在", businessType = BusinessType.OTHER)
    @PostMapping("/checkPathExist")
    @ResponseBody
    public AjaxResult checkPathExist(HttpServletRequest request) {
        String path = request.getParameter("path");
        File file = new File(path);
        if (!file.exists()) {
            return error("目录不存在");
        }

//      判断是否是目录
        if (!file.isDirectory()) {
            return error("目录不存在");
        }

        return success();
    }

	@RequiresPermissions("project:publishCmsFiles:kaoshiView")
	@GetMapping("/kaoshizhinan")
	public String kaoShiZhiNan(ModelMap mmap)
	{
//		默认的pc的根路径
		mmap.put("path","/data/webapps/wwwroot_release/dazx/shouye/online_www/zt");
		mmap.put("platform", Constants.Platform.CMS_DAZX_PC.getValue());

		List<Map<String,Object>> platformList = new ArrayList<>();
		Map<String,Object> pcMap = new HashMap<>(4);

		pcMap.put("name","东奥在线PC");
		pcMap.put("path","/data/webapps/wwwroot_release/dazx/shouye/online_www/zt");
		pcMap.put("platform",2);
		platformList.add(pcMap);

		mmap.put("platformList", platformList);

		return prefix + "/baokaozhinancmsfiles";
	}

	@GetMapping("/main")
	public String main(ModelMap mmap)
	{
//		默认的pc的根路径

		return prefix + "/main";
	}

}