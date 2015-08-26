/* Generated By:JavaCC: Do not edit this line. CIM_PrepTokenManager.java */
package org.wbemservices.wbem.compiler.prep;

public class CIM_PrepTokenManager implements CIM_PrepConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjMoveStringLiteralDfa0_0()
{
   return jjMoveNfa_0(0, 0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 37;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (curChar == 47)
                     jjAddStates(0, 1);
                  else if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 7:
                  if ((0x100000600L & l) != 0L)
                     jjAddStates(2, 3);
                  break;
               case 15:
                  if ((0x100000600L & l) != 0L)
                     jjAddStates(4, 5);
                  break;
               case 16:
                  if (curChar == 40)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 17:
                  if ((0x100000600L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 18:
               case 19:
                  if (curChar == 34)
                     jjCheckNAddStates(6, 8);
                  break;
               case 21:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 22:
                  if (curChar == 34)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 23:
                  if ((0x100000600L & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 24:
                  if (curChar == 41 && kind > 4)
                     kind = 4;
                  break;
               case 25:
                  if (curChar == 47)
                     jjAddStates(0, 1);
                  break;
               case 26:
                  if (curChar == 47)
                     jjCheckNAddStates(9, 11);
                  break;
               case 27:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(9, 11);
                  break;
               case 28:
                  if ((0x2400L & l) != 0L && kind > 1)
                     kind = 1;
                  break;
               case 29:
                  if (curChar == 10 && kind > 1)
                     kind = 1;
                  break;
               case 30:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 31:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 32:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 33:
                  if (curChar == 42)
                     jjCheckNAddStates(12, 14);
                  break;
               case 34:
                  if ((0xffff7bffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(35, 33);
                  break;
               case 35:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(35, 33);
                  break;
               case 36:
                  if (curChar == 47 && kind > 2)
                     kind = 2;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x1000000010000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 2;
                  break;
               case 2:
                  if ((0x4000000040000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 3:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 4:
                  if ((0x8000000080L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 5:
                  if ((0x200000002000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 6:
                  if ((0x200000002L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 8:
                  if ((0x20000000200L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 9:
                  if ((0x400000004000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 10:
                  if ((0x800000008L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 11:
                  if ((0x100000001000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 12:
                  if ((0x20000000200000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 13:
                  if ((0x1000000010L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 14:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(4, 5);
                  break;
               case 20:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 19;
                  break;
               case 21:
                  jjAddStates(6, 8);
                  break;
               case 27:
                  jjAddStates(9, 11);
                  break;
               case 32:
                  jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
               case 35:
                  jjCheckNAddTwoStates(35, 33);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 21:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(6, 8);
                  break;
               case 27:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(9, 11);
                  break;
               case 32:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
               case 35:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(35, 33);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 37 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   26, 31, 7, 8, 15, 16, 20, 21, 22, 27, 28, 30, 33, 34, 36, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
private JavaCharStream input_stream;
private final int[] jjrounds = new int[37];
private final int[] jjstateSet = new int[74];
protected char curChar;
public CIM_PrepTokenManager(JavaCharStream stream)
{
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public CIM_PrepTokenManager(JavaCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 37; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedPos == 0 && jjmatchedKind > 3)
   {
      jjmatchedKind = 3;
   }
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
         matchedToken = jjFillToken();
         return matchedToken;
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}