package com.code.mydemo;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;

@PipelineName("myProductListPipeline")
public class ProductListPipeline implements Pipeline<ProductList> {

	@Override
	public void process(ProductList productList) {
		HttpRequest currRequest = productList.getRequest();

		int page = productList.getPage();
		int totalPage = productList.getTotalPage();
//		List<HrefBean> allList = productList.getAllList();

		if(page<totalPage){
			int i = page + 1;
			String url = "https://www.dongao.com/dy/zckjs_sj_43343_list" + i + "/";
			System.out.println(url);
			SchedulerContext.into(currRequest.subRequest(url));
		}
//		for (HrefBean bean:allList){
//			//进入祥情页面抓取
//			SchedulerContext.into(currRequest.subRequest(bean.getUrl()));
//		}

	}

}
