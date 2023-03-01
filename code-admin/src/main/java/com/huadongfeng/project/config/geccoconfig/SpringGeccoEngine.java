package com.huadongfeng.project.config.geccoconfig;

import com.geccocrawler.gecco.pipeline.PipelineFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;

public abstract class SpringGeccoEngine implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    protected PipelineFactory springPipelineFactory;

    public abstract void init();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null){
            init();
        }
    }
}
