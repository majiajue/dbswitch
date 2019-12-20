package com.weishao.dbswitch.webapi;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebServiceApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(WebServiceApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

}
