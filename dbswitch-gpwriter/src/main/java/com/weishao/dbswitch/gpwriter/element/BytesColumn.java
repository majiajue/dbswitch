package com.weishao.dbswitch.gpwriter.element;

import java.util.Date;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;

public class BytesColumn extends Column {

	public BytesColumn() {
		this(null);
	}

	public BytesColumn(byte[] bytes) {
		super(ArrayUtils.clone(bytes), Column.Type.BYTES, null == bytes ? 0 : bytes.length);
	}

	@Override
	public byte[] asBytes() {
		if (null == this.getRawData()) {
			return null;
		}

		return (byte[]) this.getRawData();
	}

	@Override
	public String asString() {
		if (null == this.getRawData()) {
			return null;
		}

		try {
			return ColumnCast.bytes2String(this);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Bytes[%s]不能转为String .", this.toString()));
		}
	}

	@Override
	public Long asLong() {
		throw new RuntimeException("Bytes类型不能转为Long .");
	}

	@Override
	public BigDecimal asBigDecimal() {
		throw new RuntimeException("Bytes类型不能转为BigDecimal .");
	}

	@Override
	public BigInteger asBigInteger() {
		throw new RuntimeException("Bytes类型不能转为BigInteger .");
	}

	@Override
	public Double asDouble() {
		throw new RuntimeException("Bytes类型不能转为Long .");
	}

	@Override
	public Date asDate() {
		throw new RuntimeException("Bytes类型不能转为Date .");
	}

	@Override
	public Boolean asBoolean() {
		throw new RuntimeException("Bytes类型不能转为Boolean .");
	}
}
