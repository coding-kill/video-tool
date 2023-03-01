package com.huadongfeng.project.siftjobdemo;

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
@Gecco(matchUrl="https://www.dongao.com/{a}/{b}/{c}/index_{page}.shtml", pipelines={"consolePipeline", "siftListPipeline"})
public class SiftList implements HtmlBean {
	
	private static final long serialVersionUID = 4369792078959596706L;

	@Request
	private HttpRequest request;

	@RequestParameter
	private int page;
	@RequestParameter
	private String a;
	@RequestParameter
	private String b;
	@RequestParameter
	private String c;

	/**
	 * 抓取列表项的详细内容，包括titile，价格，详情页地址等
	 */
	@HtmlField(cssPath="#top > div.wrap.theme_cpa > div > div.content > div.container > div.content_main > div.column_list > ul > li > a")
	private List<HrefBean> details;
	/**
	 * 获得商品列表的总页数
	 */
	@Text
	@HtmlField(cssPath="#top > div.wrap.theme_cpa > div > div.content > div.container > div.content_main > div:nth-child(2) > div > ul > li:nth-last-child(2) > a")
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

	public String getA() {
		return a;
	}

	public String getB() {
		return b;
	}

	public String getC() {
		return c;
	}

	public void setA(String a) {
		this.a = a;
	}

	public void setB(String b) {
		this.b = b;
	}

	public void setC(String c) {
		this.c = c;
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
