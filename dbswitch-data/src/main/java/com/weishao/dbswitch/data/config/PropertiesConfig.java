package com.weishao.dbswitch.data.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;

@Configuration
@PropertySource("classpath:config.properties")
public class PropertiesConfig {

	@Value("${source.datasource.jdbc-url}")
	public String dbSourceJdbcUrl;
	
	@Value("${source.datasource.driver-class-name}")
	public String dbSourceClassName;

	@Value("${source.datasource.username}")
	public String dbSourceUserName;

	@Value("${source.datasource.password}")
	public String dbSourcePassword;
	
	@Value("${target.datasource.jdbc-url}")
	public String dbTargetJdbcUrl;
	
	@Value("${target.datasource.driver-class-name}")
	public String dbTargetClassName;

	@Value("${target.datasource.username}")
	public String dbTargetUserName;

	@Value("${target.datasource.password}")
	public String dbTargetPassword;

	/////////////////////////////////////////////

	@Value("${source.datasource-fetch.size}")
	public int fetchSizeSource;

	@Value("${source.datasource-source.schema}")
	public String schemaNameSource;

	@Value("${source.datasource-source.excludes}")
	private String tableNameExcludesSource;
	
	public List<String> getSourceTableNameExcludes() {
		if (!Strings.isEmpty(tableNameExcludesSource)) {
			String[] strs = tableNameExcludesSource.split(",");
			if (strs.length > 0) {
				return Arrays.asList(strs);
			}
		}

		return new ArrayList<String>();
	}

	////////////////////////////////////////////

	@Value("${target.datasource-target.schema}")
	public String dbTargetSchema;

	@Value("${target.datasource-target.drop}")
	public Boolean dropTargetTable;

	////////////////////////////////////////////

	@Bean(name="sourceDataSource")
	@Qualifier("sourceDataSource")
	@ConfigurationProperties(prefix="source.datasource")
	public DataSource sourceDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="targetDataSource")
	@Qualifier("targetDataSource")
	@ConfigurationProperties(prefix="target.datasource")
	public DataSource targetDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "sourceJdbcTemplate")
	public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean(name = "targetJdbcTemplate")
	public JdbcTemplate target(@Qualifier("targetDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public IMetaDataService getMetaDataService() {
		return new MigrationMetaDataServiceImpl();
	}
	
}
