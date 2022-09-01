package com.code.siftjobdemo;

import com.code.util.FileUtil;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@PipelineName("siftDetailPipeline")
@Service
public class SiftDetailPipeline implements Pipeline<SiftDetail> {

	@Override
	public void process(SiftDetail productDetail) {
		HttpRequest currRequest = productDetail.getRequest();
		Map<String, String> headers = currRequest.getHeaders();
		System.out.println("headers"+headers);
		String refere = headers.get("Referer");
		String url = currRequest.getUrl();
		int id = productDetail.getId();
		String answer = productDetail.getAnswer();
		if(answer.contains("window.location.href")){
			FileUtil.writeFile("url=="+url+";内容404,所在页面="+refere+System.lineSeparator(),"E:\\","sift.txt","UTF-8");
			System.out.println("id=="+url+";内容404==");
		}
//		for (HrefBean bean:allList){
//			//进入祥情页面抓取
//			SchedulerContext.into(currRequest.subRequest(bean.getUrl()));
//		}

	}

}
