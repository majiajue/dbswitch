package com.weishao.dbswitch.data;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import com.weishao.dbswitch.data.service.MainService;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DataSyncApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(DataSyncApplication.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		ApplicationContext context=springApplication.run(args);
		MainService service=context.getBean("MainService",MainService.class);
		service.run();
	}

}
