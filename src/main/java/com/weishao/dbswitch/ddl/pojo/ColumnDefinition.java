package com.weishao.dbswitch.ddl.pojo;

import java.util.Objects;

public class ColumnDefinition {
	private String columnName;
	private String columnType;
	private String columnComment;
	private Integer lengthOrPrecision;
	private Integer scale;
	private boolean primaryKey;
	private boolean nullable;
	private String defaultValue;
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = Objects.requireNonNull(columnName);
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public void setColumnType(String columnType) {
		this.columnType = Objects.requireNonNull(columnType);
	}
	
	public String getColumnComment() {
		return columnComment;
	}
	
	public void setColumnComment(String columnComment) {
		this.columnComment = columnComment;
	}
	
	public Integer getLengthOrPrecision() {
		return lengthOrPrecision;
	}
	
	public void setLengthOrPrecision(Integer lenOrPre) {
		this.lengthOrPrecision = Objects.requireNonNull(lenOrPre);
	}
	
	public Integer getScale() {
		return scale;
	}
	
	public void setScale(Integer scale) {
		this.scale = scale;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public boolean isNullable() {
		return nullable;
	}
	
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return "ColumnDefinition [columnName=" + columnName + ", columnType=" + columnType + ", columnComment="
				+ columnComment + ", lengthOrPrecision=" + lengthOrPrecision + ", scale=" + scale + ", primaryKey="
				+ primaryKey + ", nullable=" + nullable + ", defaultValue=" + defaultValue + "]";
	}

	
}
