package com.code.mydemo;

import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HrefBean;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.util.List;

/**
 * 抓取京东的某个商品列表页
 * 
 * @author memory
 *
 */
@Gecco(matchUrl="https://www.dongao.com/dy/zckjs_sj_43343_list{page}/", pipelines={"consolePipeline", "myProductListPipeline"})
public class ProductList implements HtmlBean {
	
	private static final long serialVersionUID = 4369792078959596706L;

	@Request
	private HttpRequest request;

	@RequestParameter
	private int page;
	
	/**
	 * 抓取列表项的详细内容，包括titile，价格，详情页地址等
	 * body > div.wrap.clearfix > div.main_left > div.latest_answer > div > ul > li:nth-child(1) > div > a
	 */
	@HtmlField(cssPath="body > div.wrap.clearfix > div.main_left > div.latest_answer > div > ul > li > div > a")
	private List<HrefBean> details;
	/**
	 * 获得商品列表的总页数
	 *
	 */
	@Text
	@HtmlField(cssPath="body > div.wrap.clearfix > div.main_left > div.latest_answer > div > div > ul > li:nth-last-child(2) > a")
	private int totalPage;

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setDetails(List<HrefBean> details) {
		this.details = details;
	}



	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public int getPage() {
		return page;
	}

	public List<HrefBean> getDetails() {
		return details;
	}


	public int getTotalPage() {
		return totalPage;
	}
}
