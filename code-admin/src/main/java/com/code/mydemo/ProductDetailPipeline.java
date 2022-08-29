package com.code.mydemo;

import com.dongao.project.utils.FileUtil;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;

@PipelineName("myProductDetailPipeline")
public class ProductDetailPipeline implements Pipeline<ProductDetail> {

	@Override
	public void process(ProductDetail productDetail) {
		HttpRequest currRequest = productDetail.getRequest();

		int id = productDetail.getId();
		String answer = productDetail.getAnswer();
//		System.out.println("id=="+id+";内容=="+answer);
		FileUtil.writeFile("id=="+id+";内容=="+System.lineSeparator(),"E:\\","answer.txt","UTF-8");
//		for (HrefBean bean:allList){
//			//进入祥情页面抓取
//			SchedulerContext.into(currRequest.subRequest(bean.getUrl()));
//		}

	}

}
