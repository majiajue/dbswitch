package com.weishao.dbswitch.webapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.weishao.dbswitch.core.service.IMigrationService;
import com.weishao.dbswitch.sql.service.ISqlConvertService;
import com.weishao.dbswitch.sql.service.ISqlGeneratorService;
import com.weishao.dbswitch.core.service.impl.MigrationConvertServiceImpl;
import com.weishao.dbswitch.sql.service.impl.CalciteSqlConvertServiceImpl;
import com.weishao.dbswitch.sql.service.impl.MyselfSqlGeneratorServiceImpl;

@Configuration
public class AdapterBeanConfiguration {
	
	@Bean("MigrationService")
	public IMigrationService getMigrationService() {
		return new MigrationConvertServiceImpl();
	}

	@Bean("SqlConvertService")
	public ISqlConvertService getSqlConvertService() {
		return new CalciteSqlConvertServiceImpl();
	}
	
	@Bean("SqlGeneratorService")
	public ISqlGeneratorService getSqlGeneratorService() {
		return new MyselfSqlGeneratorServiceImpl();
	}
	
}
