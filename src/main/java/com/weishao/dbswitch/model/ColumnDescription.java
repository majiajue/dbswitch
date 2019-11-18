package com.weishao.dbswitch.model;

import com.weishao.dbswitch.constant.DatabaseType;

/**
 * 数据库列描述符信息定义(Column Description)
 * @author tang
 *
 */
public class ColumnDescription {

	private String fieldName;
	private String labelName;
	private String fieldTypeName;
	private String filedTypeClassName;
	private int fieldType;
	private int displaySize;
	private int scaleSize;
	private int precisionSize;
	private boolean isAutoIncrement;
	private boolean isNullable;
	private String remarks;
	private boolean signed = false;
	private DatabaseType dbtype;

	public String getFieldName() {
		if (null != this.fieldName) {
			return fieldName;
		}

		return this.labelName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getLabelName() {
		if (null != labelName) {
			return labelName;
		}

		return this.fieldName;
	}

	public void setLabelName(String labalName) {
		this.labelName = labalName;
	}

	public String getFieldTypeName() {
		return fieldTypeName;
	}

	public void setFieldTypeName(String fieldTypeName) {
		this.fieldTypeName = fieldTypeName;
	}

	public String getFiledTypeClassName() {
		return filedTypeClassName;
	}

	public void setFiledTypeClassName(String filedTypeClassName) {
		this.filedTypeClassName = filedTypeClassName;
	}

	public int getFieldType() {
		return fieldType;
	}

	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}

	public int getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
	}

	public int getScaleSize() {
		return scaleSize;
	}

	public void setScaleSize(int scaleSize) {
		this.scaleSize = scaleSize;
	}

	public int getPrecisionSize() {
		return precisionSize;
	}

	public void setPrecisionSize(int precisionSize) {
		this.precisionSize = precisionSize;
	}

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	
	public boolean isSigned() {
		return signed;
	}
	
	public void setSigned(boolean signed) {
		this.signed=signed;
	}
	
	public DatabaseType getDbType() {
		return this.dbtype;
	}
	
	public void setDbType(DatabaseType dbtype) {
		this.dbtype=dbtype;
	}
	
	public String getRemarks() {
		return this.remarks;
	}
	
	public void setRemarks(String remarks) {
		this.remarks=remarks;
	}
	
	/////////////////////////////////////////////
	
	public ColumnMetaData getMetaData() {
		return new ColumnMetaData(this);
	}

}
