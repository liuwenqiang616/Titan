package com.wanfangdata.titan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @ClassName TitanApplication
 * @Author liuwq
 * @Date 2019/5/17 18:07
 * @Version 1.0
 **/
@SpringBootApplication
@ServletComponentScan
public class TitanApplication {
    public static void main(String[] args) {
        SpringApplication.run(TitanApplication.class, args);
        System.out.println("Titan 网关启动成功");
    }
}
