package com.code.siftjobdemo;

import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

/**
 * 抓取京东的某个商品列表页
 * 
 * @author memory
 *
 */
@Gecco(matchUrl="https://www.dongao.com/c/{d}/{id}.shtml", pipelines={"consolePipeline", "siftDetailPipeline"})
public class SiftDetail implements HtmlBean {
	
	private static final long serialVersionUID = 4369792078959596706L;

	@Request
	private HttpRequest request;

	@RequestParameter
	private int id;
	
//	/**
//	 * 抓取列表项的详细内容，包括titile，价格，详情页地址等
//	 * body > div.wrap.clearfix > div.main_left > div.latest_answer > div > ul > li:nth-child(1) > div > a
//	 */
//	@HtmlField(cssPath="body > div.wrap.clearfix > div.main_left > div.latest_answer > div > ul > li > div > a")
//	private List<HrefBean> details;
	/**
	 * 获得商品列表的总页数
	 *
	 */
	@Html
	@HtmlField(cssPath="html")
	private String answer;

	public void setRequest(HttpRequest request) {
		this.request = request;
	}


//	public void setDetails(List<HrefBean> details) {
//		this.details = details;
//	}




	public HttpRequest getRequest() {
		return request;
	}


//	public List<HrefBean> getDetails() {
//		return details;
//	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
