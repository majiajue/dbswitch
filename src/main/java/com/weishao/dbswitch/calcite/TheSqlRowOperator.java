package com.weishao.dbswitch.calcite;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperatorBinding;
import org.apache.calcite.sql.SqlSpecialOperator;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.SqlUtil;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.type.InferTypes;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.util.Pair;
import java.util.AbstractList;
import java.util.Map;

/**
 * 代码来源于org.apache.calcite.sql.fun.SqlRowOperator的代码，这里
 * 重写了unparse()方法，以处理INSERT语句的ROW问题。
 * 
 *  @author tang
 *  
 * SqlRowOperator represents the special ROW constructor.
 *
 * <p>TODO: describe usage for row-value construction and row-type construction
 * (SQL supports both).
 */
public class TheSqlRowOperator extends SqlSpecialOperator {
  //~ Constructors -----------------------------------------------------------

  public TheSqlRowOperator() {
    super("",
        SqlKind.ROW, MDX_PRECEDENCE,
        false,
        null,
        InferTypes.RETURN_TYPE,
        OperandTypes.VARIADIC);
  }

  //~ Methods ----------------------------------------------------------------

  // implement SqlOperator
  public SqlSyntax getSyntax() {
    // Function syntax would work too.
    return SqlSyntax.SPECIAL;
  }

  public RelDataType inferReturnType(
      final SqlOperatorBinding opBinding) {
    // The type of a ROW(e1,e2) expression is a record with the types
    // {e1type,e2type}.  According to the standard, field names are
    // implementation-defined.
    return opBinding.getTypeFactory().createStructType(
        new AbstractList<Map.Entry<String, RelDataType>>() {
          public Map.Entry<String, RelDataType> get(int index) {
            return Pair.of(
                SqlUtil.deriveAliasFromOrdinal(index),
                opBinding.getOperandType(index));
          }

          public int size() {
            return opBinding.getOperandCount();
          }
        });
  }

  public void unparse(
      SqlWriter writer,
      SqlCall call,
      int leftPrec,
      int rightPrec) {
    SqlUtil.unparseFunctionSyntax(this, writer, call);
  }

  // override SqlOperator
  public boolean requiresDecimalExpansion() {
    return false;
  }
}

// End TheSqlRowOperator.java

