package com.weishao.dbswitch;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SwitchApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(SwitchApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

}
