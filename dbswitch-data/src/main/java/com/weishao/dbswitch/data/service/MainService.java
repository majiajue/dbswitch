package com.weishao.dbswitch.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("MainService")
public class MainService {

	private static final Logger logger = LoggerFactory.getLogger(MainService.class);

	public void run() {
		logger.info("service is running....");
	}
}
