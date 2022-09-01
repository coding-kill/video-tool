package com.code.siftjobdemo;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HrefBean;
import org.springframework.stereotype.Service;

import java.util.List;

@PipelineName("siftListPipeline")
@Service
public class SiftListPipeline implements Pipeline<SiftList> {
	int total = 0;

	@Override
	public void process(SiftList productList) {
		HttpRequest currRequest = productList.getRequest();
		String a = productList.getA();
		String b = productList.getB();
		String c = productList.getC();
		int page = productList.getPage();
		if(page==1){
			total = productList.getTotalPage();
		}

		if(page<total){
			int i = page + 1;
			String url = "https://www.dongao.com/"+a+"/"+b+"/"+c+"/index_"+i+".shtml";
			System.out.println(url);
			SchedulerContext.into(currRequest.subRequest(url));
//			FileUtil.writeFile("进入列表页=="+url+System.lineSeparator(),"E:\\","sift.txt","UTF-8");

		}
		List<HrefBean> details = productList.getDetails();
		for (HrefBean bean:details){
			System.out.println("列表循环详情页"+bean.getTitle());
			//进入祥情页面抓取
			SchedulerContext.into(currRequest.subRequest(bean.getUrl()));
//			FileUtil.writeFile("进入详情页=="+bean.getTitle()+"<>"+bean.getUrl()+System.lineSeparator(),"E:\\","sift.txt","UTF-8");
		}

	}

}
