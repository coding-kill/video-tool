/**
 * Copyright 东奥
 * FileName: SiftJob
 * Author:   sangsk
 * Date:     2022/9/1 15:17
 * Description:
 * History:
 */
package com.code.siftjobdemo;

import com.code.config.geccoconfig.SpringPipelineFactory;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.ruoyi.common.utils.spring.SpringUtils;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author dongao
 * @create 2022/9/1
 * @since 1.0.0
 */
@Component("siftJob")
public class SiftJob {

    public SiftJob() {
    }

    public void ryParams(String params) {
        System.out.println("执行有参方法：" + params);
        SpringPipelineFactory springPipelineFactory = SpringUtils.getBean("springPipelineFactory");

        HttpGetRequest start = new HttpGetRequest("https://www.dongao.com/zckjs/cf/cxsj/index_1.shtml");
        start.setCharset("GBK");
        GeccoEngine.create()
                .pipelineFactory(springPipelineFactory)
                .classpath("com.code.siftjobdemo")
                .start(start)
                .thread(10)
                .interval(0)
                .run();
    }

    public void ryNoParams() {
        System.out.println("执行无参方法");
    }

}
