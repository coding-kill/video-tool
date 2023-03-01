package com.huadongfeng.project;

import com.huadongfeng.project.config.streamconfig.ConfigConstant;
import com.huadongfeng.project.license.LicenseVerify;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@MapperScan(basePackages = {"com.huadongfeng.**.mapper"})
@ComponentScan(value = {"com.huadongfeng", "com.ruoyi"})
@ServletComponentScan(value = {"com.huadongfeng", "com.ruoyi"})
@EnableAsync
public class MyApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MyApplication.class);
    }

    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        ConfigurableApplicationContext run = SpringApplication.run(MyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  启动成功   ლ(´ڡ`ლ)ﾞ");

        if(ConfigConstant.licenseFlag){
            LicenseVerify licenseVerify = new LicenseVerify();
//        //校验证书是否有效
            boolean verify = licenseVerify.verify();
            if(!verify){
                System.out.println("++++++++ 证书认证失败 ++++++++");
                run.close();
                System.exit(1);
            }
            System.out.println("++++++++ 证书认证成功 ++++++++");
        }
    }
}