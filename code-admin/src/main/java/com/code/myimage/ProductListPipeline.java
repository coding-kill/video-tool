package com.code.myimage;

import com.dongao.project.utils.FileUtil;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HrefBean;
import com.geccocrawler.gecco.utils.DownloadImage;

import java.util.List;

@PipelineName("myProductListPipeline")
public class ProductListPipeline implements Pipeline<ProductList> {

	@Override
	public void process(ProductList productList) {
		HttpRequest currRequest = productList.getRequest();

		int page = productList.getPage();
		int totalPage = productList.getTotalPage();
		System.out.println(totalPage);
		if(page<20){
			int i = page + 1;
			String url = "https://www.dongao.com/zckjs/zy/index_" + i + ".shtml";
			System.out.println(url);
			SchedulerContext.into(currRequest.subRequest(url));
//			FileUtil.writeFile("进入列表页=="+url+System.lineSeparator(),"E:\\","answer.txt","UTF-8");
		}

		List<String> images = productList.getImages();
		for (String img : images){
			System.out.println("img循环详情页"+img);

			DownloadImage.download("E:\\img",img);
		}

		List<HrefBean> details = productList.getDetails();
		for (HrefBean bean:details){
			System.out.println("列表循环详情页"+bean.getTitle());
			//进入祥情页面抓取
//			SchedulerContext.into(currRequest.subRequest(bean.getUrl()));
			FileUtil.writeFile("进入详情页=="+bean.getUrl()+System.lineSeparator(),"E:\\","answer.txt","UTF-8");
		}

	}

}
