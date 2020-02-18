package com.weishao.dbswitch.dbwriter.gpdb.copy.record;

import com.weishao.dbswitch.dbwriter.gpdb.copy.element.Column;

public interface Record {

	public void addColumn(Column column);

	public void setColumn(int i, final Column column);

	public Column getColumn(int i);

	public String toString();

	public int getColumnNumber();

	public int getByteSize();
}
