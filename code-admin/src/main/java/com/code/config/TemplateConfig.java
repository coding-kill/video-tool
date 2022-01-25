/**
 * Copyright 东奥
 * FileName: TemplateConfig
 * Author:   sangsk
 * Date:     2022/1/24 16:51
 * Description:
 * History:
 */
package com.code.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author dongao
 * @create 2022/1/24
 * @since 1.0.0
 */
@Configuration
public class TemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}
