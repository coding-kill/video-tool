/**
 * Copyright (C), 2018-2022, XXX有限公司
 * FileName: Starta
 * Author:   coding
 * Date:     2022/8/28 9:37
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.code.siftjobdemo;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.request.HttpGetRequest;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author coding
 * @create 2022/8/28
 * @since 1.0.0
 */
public class Starta {


    public static void main(String[] args) {
//        HttpGetRequest start = new HttpGetRequest("https://www.dongao.com/dy/zckjs_sj_43343_list1/");
        HttpGetRequest start = new HttpGetRequest("https://www.dongao.com/zckjs/cf/cxsj/index_1.shtml");

        start.setCharset("GBK");
        GeccoEngine.create()
                .classpath("com.dongao.mysift")
                //开始抓取的页面地址
                .start(start)
                //开启几个爬虫线程
                .thread(15)
                //单个爬虫每次抓取完一个请求后的间隔时间
                .interval(0)
                .run();
    }
}
