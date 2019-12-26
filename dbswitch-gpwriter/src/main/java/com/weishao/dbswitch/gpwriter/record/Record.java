package com.weishao.dbswitch.gpwriter.record;

import com.weishao.dbswitch.gpwriter.element.Column;

public interface Record {

	public void addColumn(Column column);

	public void setColumn(int i, final Column column);

	public Column getColumn(int i);

	public String toString();

	public int getColumnNumber();

	public int getByteSize();
}
