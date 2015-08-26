/* Generated By:JavaCC: Do not edit this line. WQLParser.java */
package javax.wbem.query;

import java.io.*;

import javax.wbem.cim.UnsignedInt64;

/**
 * JavaCC specification for a WQL parser for a subset of ANSI SQL
 * 
 * @version	1.1 03/01/01
 * @author	Sun Microsystems, Inc.
 */

public class WQLParser implements WQLParserConstants {

  /* The WQLParser is based on the SQL92 grammar. The
   * non-terminals, which in the SQL grammar are of the form
   *
   *   <character value expression>
   *
   * are here rendered in the following form:
   *
   *   characterValueExpression
   *
   * Subsetting is indicated with comments.
   */

  public static void main(String args[]) {
    InputStream in = System.in;
    StringBuffer line;
    WQLParser parser = new WQLParser(System.in);
    System.out.println("Enter SQL one per line. End with <EOF>");
    try {
      while(true) {                     // get lines
        System.out.print("\nInputSQL:  ");
        line = new StringBuffer();
        int b;
        while((b = in.read()) != -1 && b != '\n') {             // get characters
          line.append((char)b);
        }
        line.append("\n");      // to test end-of-line comments
        System.out.println("ReadSQL: " + line.toString());
        ByteArrayInputStream lineStream =
          new ByteArrayInputStream(line.toString().getBytes());
        parser.ReInit(lineStream);
        WQLExp q;
        try {
          q = parser.querySpecification();
          System.out.println("toString: " + q);
        }
        catch (ParseException e) {
          System.out.println(e);
        }

        // Run it through again...
        lineStream = new ByteArrayInputStream(line.toString().getBytes());
        parser.ReInit(lineStream);
        try {
          q = parser.querySpecification();
          System.out.println("Repeated: " + q);
        }
        catch (ParseException e) {
          System.out.println(e);
        }
        if (b == -1) break;
      }
    }
    catch (java.io.IOException e) {
      System.err.println(e);
    }
  }

/* 
 * Non-terminal grammar symbols
 */
  final public boolean sign() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case plusSign:
      jj_consume_token(plusSign);
                {if (true) return false;}
      break;
    case minusSign:
      jj_consume_token(minusSign);
                 {if (true) return true;}
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public NumericValue unsignedNumericLiteral() throws ParseException {
  NumericValue exact;
  Token approx;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case unsignedInteger:
      exact = exactNumericLiteral();
       {if (true) return exact;}
      break;
    case approximateNumericLiteral:
      approx = jj_consume_token(approximateNumericLiteral);
       {if (true) return new NumericValue(new Double(approx.image));}
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public NumericValue exactNumericLiteral() throws ParseException {
  Token uinteger;
    uinteger = jj_consume_token(unsignedInteger);
       {if (true) return new NumericValue(new UnsignedInt64(uinteger.image));}
    throw new Error("Missing return statement in function");
  }

  final public String columnName() throws ParseException {
  Token id;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case columnidentifier:
      id = jj_consume_token(columnidentifier);
       {if (true) return id.image;}
      break;
    case identifier:
      id = jj_consume_token(identifier);
       {if (true) return id.image;}
      break;
    case asterisk:
      id = jj_consume_token(asterisk);
       {if (true) return id.image;}
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public NumericValue signedNumericLiteral() throws ParseException {
  NumericValue literal;
  boolean signed = false;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case plusSign:
    case minusSign:
      signed = sign();
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    literal = unsignedNumericLiteral();
        if (! signed)
          {if (true) return literal;}
        else {
          if (literal.isUint() || literal.isSint())
            {if (true) return new NumericValue(new Long(- literal.longValue()));}
          else
            {if (true) return new NumericValue(new Double(- literal.doubleValue()));}
        }
    throw new Error("Missing return statement in function");
  }

/*
 * Note: The full SQL definition for <general literal> includes
 * national character string, bit string, hex string, datetime and
 * interval literals.
 */
  final public StringValueExp generalLiteral() throws ParseException {
  Token literal;
    literal = jj_consume_token(characterStringLiteral);
        String s = literal.image;               // source
        StringBuffer t = new StringBuffer();    // target

        // remove surrounding quotes
        s = s.substring(1, s.length()-1);

        // convert each '' to '
        int i = 0;              // index of sequence of non-quotes
        int j;                  // index of ''
        while ( -1 != (j = s.indexOf("''", i))) {
          t.append(s.substring(i, j)).append("'");
          i = j + 2;
        }
        t.append(s.substring(i));

        {if (true) return new StringValueExp(t.toString());}
    throw new Error("Missing return statement in function");
  }

  final public ValueExp anyLiteral() throws ParseException {
    ValueExp exp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case plusSign:
    case minusSign:
    case approximateNumericLiteral:
    case unsignedInteger:
      exp = signedNumericLiteral();
      {if (true) return exp;}
      break;
    case characterStringLiteral:
      exp = generalLiteral();
      {if (true) return exp;}
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public String tableName() throws ParseException {
  Token id;
    id = jj_consume_token(identifier);
        {if (true) return id.image;}
    throw new Error("Missing return statement in function");
  }

/*
 * This rule is not part of the SQL grammar. It is provided to ensure
 * that there is no trailing garbage at the end of an otherwise legal
 * input. 
 */
  final public QueryExp searchConditionOnly() throws ParseException {
  QueryExp condition;
    condition = searchCondition();
    jj_consume_token(0);
      {if (true) return condition;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: this rule has been rewritten to avoid the left-recursion of
 * the SQL92 grammar.
 */
  final public QueryExp searchCondition() throws ParseException {
  QueryExp condition;
  QueryExp term;
    condition = booleanTerm();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case or:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_1;
      }
      jj_consume_token(or);
      term = booleanTerm();
          condition = new OrQueryExp(condition, term);
    }
     {if (true) return condition;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: this rule has been rewritten to avoid the left-recursion of
 * the SQL92 grammar.
 */
  final public QueryExp booleanTerm() throws ParseException {
  QueryExp term;
  QueryExp factor;
    term = booleanFactor();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case and:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_2;
      }
      jj_consume_token(and);
      factor = booleanFactor();
         term = new AndQueryExp(term, factor);
    }
     {if (true) return term;}
    throw new Error("Missing return statement in function");
  }

  final public QueryExp booleanFactor() throws ParseException {
  boolean isNot = false;
  QueryExp test;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case not:
      jj_consume_token(not);
             isNot = true;
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
    test = booleanTest();
        if (isNot)
          {if (true) return new NotQueryExp(test);}
        else
          {if (true) return test;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: The full SQL definition for <boolean test> includes 
 * [ IS [ NOT ] <truth value> ]
 */
  final public QueryExp booleanTest() throws ParseException {
  QueryExp primary;
    primary = booleanPrimary();
       {if (true) return primary;}
    throw new Error("Missing return statement in function");
  }

  final public QueryExp booleanPrimary() throws ParseException {
  QueryExp exp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case leftParen:
      jj_consume_token(leftParen);
      exp = searchCondition();
      jj_consume_token(rightParen);
        {if (true) return exp;}
      break;
    case plusSign:
    case minusSign:
    case asterisk:
    case approximateNumericLiteral:
    case unsignedInteger:
    case characterStringLiteral:
    case identifier:
    case columnidentifier:
      exp = relOperation();
        {if (true) return exp;}
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public BinaryRelQueryExp relOperation() throws ParseException {
  ValueExp lexp = null;
  ValueExp rexp = null;
  int op;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case asterisk:
    case identifier:
    case columnidentifier:
      lexp = columnReference();
      break;
    case plusSign:
    case minusSign:
    case approximateNumericLiteral:
    case unsignedInteger:
    case characterStringLiteral:
      lexp = anyLiteral();
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    op = compOp();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case asterisk:
    case identifier:
    case columnidentifier:
      rexp = columnReference();
      break;
    case plusSign:
    case minusSign:
    case approximateNumericLiteral:
    case unsignedInteger:
    case characterStringLiteral:
      rexp = anyLiteral();
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new BinaryRelQueryExp(op, lexp, rexp);}
    throw new Error("Missing return statement in function");
  }

  final public AttributeExp columnReference() throws ParseException {
  String attr = null;
    // Not doing lookahead for qualifying table for now.
          attr = columnName();
          {if (true) return new AttributeExp(attr);}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: The full SQL definition for <query specification> includes an
 * optional set quantifier.
 */
  final public WQLExp querySpecification() throws ParseException {
  SelectList list;
  Object [] fromWhere;
    jj_consume_token(select);
    list = selectList();
    fromWhere = tableExpression();
        {if (true) return new SelectExp(list, (FromExp)fromWhere[0],
        (QueryExp)fromWhere[1]);}
        {if (true) return null;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: The full SQL definition for <select list> includes the case of
 * asterisk and an actual list. JMAPI only uses a list of a single
 * item.
 */
  final public SelectList selectList() throws ParseException {
  SelectList list = null;
  AttributeExp exp;
    exp = selectSublist();
        list = new SelectList(exp);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case comma:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_3;
      }
      jj_consume_token(comma);
      exp = selectSublist();
            list.addElement(exp);
    }
        {if (true) return list;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: The full SQL definition for <select sublist> includes
 * qualifier.*.
 */
  final public AttributeExp selectSublist() throws ParseException {
  AttributeExp col;
    col = columnReference();
       {if (true) return col;}
    throw new Error("Missing return statement in function");
  }

  final public Object[] tableExpression() throws ParseException {
  Object [] fromWhere = new Object[2];
  fromWhere[1] = null;
    fromWhere[0] = fromClause();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case where:
      fromWhere[1] = whereClause();
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
        {if (true) return fromWhere;}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: the full SQL definition of <table reference> includes a joined
 * table
 */
  final public QualifiedAttributeExp tableReference() throws ParseException {
  String classname;
  String alias = null;
    // not handling table alias for now.
        classname = tableName();
       {if (true) return new QualifiedAttributeExp(classname, alias, null);}
    throw new Error("Missing return statement in function");
  }

/*
 * Note: the full SQL definition for <from clause> includes a list of
 * table references.
 */
  final public FromExp fromClause() throws ParseException {
  QualifiedAttributeExp ref;
  NonJoinExp exp;
    jj_consume_token(from);
    ref = tableReference();
       {if (true) return new NonJoinExp(ref);}
    throw new Error("Missing return statement in function");
  }

  final public QueryExp whereClause() throws ParseException {
  QueryExp cond;
    jj_consume_token(where);
    cond = searchCondition();
       {if (true) return cond;}
    throw new Error("Missing return statement in function");
  }

  final public int compOp() throws ParseException {
  boolean isNot = false;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case not:
    case like:
    case isa:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case not:
        jj_consume_token(not);
             isNot = true;
        break;
      default:
        jj_la1[13] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case like:
        jj_consume_token(like);
                if (isNot)
                    {if (true) return Query.NLIKE;}
                else
                    {if (true) return Query.LIKE;}
        break;
      case isa:
        jj_consume_token(isa);
                if (isNot)
                    {if (true) return Query.NISA;}
                else
                    {if (true) return Query.ISA;}
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case equalsOperator:
      jj_consume_token(equalsOperator);
                                         {if (true) return Query.EQ;}
      break;
    case notEqualsOperator:
      jj_consume_token(notEqualsOperator);
                                         {if (true) return Query.NE;}
      break;
    case lessThanOperator:
      jj_consume_token(lessThanOperator);
                                         {if (true) return Query.LT;}
      break;
    case greaterThanOperator:
      jj_consume_token(greaterThanOperator);
                                         {if (true) return Query.GT;}
      break;
    case lessThanOrEqualsOperator:
      jj_consume_token(lessThanOrEqualsOperator);
                                         {if (true) return Query.LE;}
      break;
    case greaterThanOrEqualsOperator:
      jj_consume_token(greaterThanOrEqualsOperator);
                                         {if (true) return Query.GE;}
      break;
    default:
      jj_la1[15] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  public WQLParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[16];
  final private int[] jj_la1_0 = {0xc000,0x80000000,0x10000,0xc000,0x8000c000,0x400000,0x800000,0x1000000,0x8005c000,0x8001c000,0x8001c000,0x200000,0x8000000,0x1000000,0x50000000,0x51003f00,};
  final private int[] jj_la1_1 = {0x0,0x2,0x18,0x0,0x6,0x0,0x0,0x0,0x1e,0x1e,0x1e,0x0,0x0,0x0,0x0,0x0,};

  public WQLParser(java.io.InputStream stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new WQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public WQLParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new WQLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public WQLParser(WQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  public void ReInit(WQLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 16; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[39];
    for (int i = 0; i < 39; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 16; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 39; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
