package com.weishao.dbswitch.core.constant;

/**
 * 数据库表类型:视图表、物理表
 * @author tang
 *
 */
public enum DBTableType {
	TABLE(0), //物理表
	VIEW(1);//视图表

	private int index;

	DBTableType(int idx) {
		this.index = idx;
	}

	public int getIndex() {
		return index;
	}
}
