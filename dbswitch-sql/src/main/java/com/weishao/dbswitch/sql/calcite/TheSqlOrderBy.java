package com.weishao.dbswitch.sql.calcite;

import java.util.List;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

public class TheSqlOrderBy extends SqlOrderBy {
	
	public static final SqlSpecialOperator OPERATOR = new Operator() {
		@Override
		public SqlCall createCall(SqlLiteral functionQualifier, SqlParserPos pos, SqlNode... operands) {
			return new TheSqlOrderBy(pos, operands[0], (SqlNodeList) operands[1], operands[2], operands[3]);
		}
	};

	public final SqlNode query;
	public final SqlNodeList orderList;
	public final SqlNode offset;
	public final SqlNode fetch;

	// ~ Constructors -----------------------------------------------------------

	public TheSqlOrderBy(SqlParserPos pos, SqlNode query, SqlNodeList orderList,
		      SqlNode offset, SqlNode fetch) {
		    super(pos,query,orderList,offset,fetch);
		    this.query = query;
		    this.orderList = orderList;
		    this.offset = offset;
		    this.fetch = fetch;
		  }
	
	// ~ Methods ----------------------------------------------------------------

	@Override
	public SqlKind getKind() {
		return SqlKind.ORDER_BY;
	}

	public SqlOperator getOperator() {
		return OPERATOR;
	}

	public List<SqlNode> getOperandList() {
		return ImmutableNullableList.of(query, orderList, offset, fetch);
	}

	/** Definition of {@code ORDER BY} operator. */
	private static class Operator extends SqlSpecialOperator {
		private Operator() {
			// NOTE: make precedence lower then SELECT to avoid extra parens
			super("ORDER BY", SqlKind.ORDER_BY, 0);
		}

		public SqlSyntax getSyntax() {
			return SqlSyntax.POSTFIX;
		}

		public void unparse(SqlWriter writer, SqlCall call, int leftPrec, int rightPrec) {
			SqlOrderBy orderBy = (SqlOrderBy) call;
			final SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.ORDER_BY);
			orderBy.query.unparse(writer, getLeftPrec(), getRightPrec());
			if (orderBy.orderList != SqlNodeList.EMPTY) {
				writer.sep(getName());
				final SqlWriter.Frame listFrame = writer.startList(SqlWriter.FrameTypeEnum.ORDER_BY_LIST);
				unparseListClause(writer, orderBy.orderList);
				writer.endList(listFrame);
			}

			if (orderBy.fetch != null) {
				final SqlWriter.Frame frame3 = writer.startList(SqlWriter.FrameTypeEnum.FETCH);
				writer.newlineAndIndent();
				//writer.keyword("FETCH");
				//writer.keyword("NEXT");
				writer.keyword("LIMIT");
				orderBy.fetch.unparse(writer, -1, -1);
				//writer.keyword("ROWS");
				//writer.keyword("ONLY");
				writer.endList(frame3);
			}
			
			if (orderBy.offset != null) {
				final SqlWriter.Frame frame2 = writer.startList(SqlWriter.FrameTypeEnum.OFFSET);
				//writer.newlineAndIndent();
				writer.keyword("OFFSET");
				orderBy.offset.unparse(writer, -1, -1);
				//writer.keyword("ROWS");
				writer.endList(frame2);
			}
			
			writer.endList(frame);
		}
	}
}
