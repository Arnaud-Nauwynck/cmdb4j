// Generated from FilterChain.g4 by ANTLR 4.7.1
package org.cmdb4j.core.ext.elasticsearch.filter.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterChainParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, WS=12, WHEN=13, THEN=14, ACCEPT=15, REJECT=16, NOT=17, 
		AND=18, OR=19, IDENT=20, STRING_LITERAL=21, INT=22, DECIMAL=23, DOUBLE=24, 
		NUMBER=25;
	public static final int
		RULE_filterChain = 0, RULE_filterEntry = 1, RULE_filterDecision = 2, RULE_expr = 3, 
		RULE_binaryOp = 4, RULE_fieldValueOperator = 5, RULE_value = 6, RULE_stringValue = 7, 
		RULE_numberValue = 8, RULE_listOperator = 9, RULE_listValue = 10;
	public static final String[] ruleNames = {
		"filterChain", "filterEntry", "filterDecision", "expr", "binaryOp", "fieldValueOperator", 
		"value", "stringValue", "numberValue", "listOperator", "listValue"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'='", "'!='", "'~'", "'/~/'", "'in'", "'notIn'", 
		"'['", "','", "']'", null, "'when'", "'then'", "'accept'", "'reject'", 
		"'!'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		"WS", "WHEN", "THEN", "ACCEPT", "REJECT", "NOT", "AND", "OR", "IDENT", 
		"STRING_LITERAL", "INT", "DECIMAL", "DOUBLE", "NUMBER"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "FilterChain.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FilterChainParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class FilterChainContext extends ParserRuleContext {
		public List<FilterEntryContext> filterEntry() {
			return getRuleContexts(FilterEntryContext.class);
		}
		public FilterEntryContext filterEntry(int i) {
			return getRuleContext(FilterEntryContext.class,i);
		}
		public FilterChainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterChain; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFilterChain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFilterChain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFilterChain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterChainContext filterChain() throws RecognitionException {
		FilterChainContext _localctx = new FilterChainContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_filterChain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WHEN) {
				{
				{
				setState(22);
				filterEntry();
				}
				}
				setState(27);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterEntryContext extends ParserRuleContext {
		public ExprContext filterExpr;
		public FilterDecisionContext decision;
		public TerminalNode WHEN() { return getToken(FilterChainParser.WHEN, 0); }
		public TerminalNode THEN() { return getToken(FilterChainParser.THEN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FilterDecisionContext filterDecision() {
			return getRuleContext(FilterDecisionContext.class,0);
		}
		public FilterEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterEntry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFilterEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFilterEntry(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFilterEntry(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterEntryContext filterEntry() throws RecognitionException {
		FilterEntryContext _localctx = new FilterEntryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_filterEntry);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			match(WHEN);
			setState(29);
			((FilterEntryContext)_localctx).filterExpr = expr(0);
			setState(30);
			match(THEN);
			setState(31);
			((FilterEntryContext)_localctx).decision = filterDecision();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterDecisionContext extends ParserRuleContext {
		public Token decision;
		public TerminalNode ACCEPT() { return getToken(FilterChainParser.ACCEPT, 0); }
		public TerminalNode REJECT() { return getToken(FilterChainParser.REJECT, 0); }
		public FilterDecisionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterDecision; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFilterDecision(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFilterDecision(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFilterDecision(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterDecisionContext filterDecision() throws RecognitionException {
		FilterDecisionContext _localctx = new FilterDecisionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_filterDecision);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			((FilterDecisionContext)_localctx).decision = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==ACCEPT || _la==REJECT) ) {
				((FilterDecisionContext)_localctx).decision = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FieldValueExprContext extends ExprContext {
		public Token field;
		public FieldValueOperatorContext fieldOp;
		public ValueContext fieldVal;
		public TerminalNode IDENT() { return getToken(FilterChainParser.IDENT, 0); }
		public FieldValueOperatorContext fieldValueOperator() {
			return getRuleContext(FieldValueOperatorContext.class,0);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public FieldValueExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFieldValueExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFieldValueExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFieldValueExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesisedExprContext extends ExprContext {
		public ExprContext e;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParenthesisedExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterParenthesisedExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitParenthesisedExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitParenthesisedExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FieldListExprContext extends ExprContext {
		public Token listField;
		public ListOperatorContext listOp;
		public ListValueContext listVal;
		public TerminalNode IDENT() { return getToken(FilterChainParser.IDENT, 0); }
		public ListOperatorContext listOperator() {
			return getRuleContext(ListOperatorContext.class,0);
		}
		public ListValueContext listValue() {
			return getRuleContext(ListValueContext.class,0);
		}
		public FieldListExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFieldListExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFieldListExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFieldListExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BinaryOp_ExprContext extends ExprContext {
		public ExprContext lhs;
		public BinaryOpContext op;
		public ExprContext rhs;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public BinaryOpContext binaryOp() {
			return getRuleContext(BinaryOpContext.class,0);
		}
		public BinaryOp_ExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterBinaryOp_Expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitBinaryOp_Expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitBinaryOp_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryOpExprContext extends ExprContext {
		public Token prefixOp;
		public ExprContext e;
		public TerminalNode NOT() { return getToken(FilterChainParser.NOT, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public UnaryOpExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterUnaryOpExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitUnaryOpExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitUnaryOpExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				_localctx = new ParenthesisedExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(36);
				match(T__0);
				setState(37);
				((ParenthesisedExprContext)_localctx).e = expr(0);
				setState(38);
				match(T__1);
				}
				break;
			case 2:
				{
				_localctx = new UnaryOpExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(40);
				((UnaryOpExprContext)_localctx).prefixOp = match(NOT);
				setState(41);
				((UnaryOpExprContext)_localctx).e = expr(4);
				}
				break;
			case 3:
				{
				_localctx = new FieldValueExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(42);
				((FieldValueExprContext)_localctx).field = match(IDENT);
				setState(43);
				((FieldValueExprContext)_localctx).fieldOp = fieldValueOperator();
				setState(44);
				((FieldValueExprContext)_localctx).fieldVal = value();
				}
				break;
			case 4:
				{
				_localctx = new FieldListExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(46);
				((FieldListExprContext)_localctx).listField = match(IDENT);
				setState(47);
				((FieldListExprContext)_localctx).listOp = listOperator();
				setState(48);
				((FieldListExprContext)_localctx).listVal = listValue();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(58);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new BinaryOp_ExprContext(new ExprContext(_parentctx, _parentState));
					((BinaryOp_ExprContext)_localctx).lhs = _prevctx;
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(52);
					if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
					setState(53);
					((BinaryOp_ExprContext)_localctx).op = binaryOp();
					setState(54);
					((BinaryOp_ExprContext)_localctx).rhs = expr(4);
					}
					} 
				}
				setState(60);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class BinaryOpContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(FilterChainParser.AND, 0); }
		public TerminalNode OR() { return getToken(FilterChainParser.OR, 0); }
		public BinaryOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binaryOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterBinaryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitBinaryOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitBinaryOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BinaryOpContext binaryOp() throws RecognitionException {
		BinaryOpContext _localctx = new BinaryOpContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_binaryOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==OR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldValueOperatorContext extends ParserRuleContext {
		public FieldValueOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldValueOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterFieldValueOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitFieldValueOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitFieldValueOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldValueOperatorContext fieldValueOperator() throws RecognitionException {
		FieldValueOperatorContext _localctx = new FieldValueOperatorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fieldValueOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public StringValueContext stringValue() {
			return getRuleContext(StringValueContext.class,0);
		}
		public NumberValueContext numberValue() {
			return getRuleContext(NumberValueContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_value);
		try {
			setState(67);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(65);
				stringValue();
				}
				break;
			case NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(66);
				numberValue();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringValueContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(FilterChainParser.STRING_LITERAL, 0); }
		public StringValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterStringValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitStringValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitStringValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringValueContext stringValue() throws RecognitionException {
		StringValueContext _localctx = new StringValueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_stringValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberValueContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(FilterChainParser.NUMBER, 0); }
		public NumberValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterNumberValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitNumberValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitNumberValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberValueContext numberValue() throws RecognitionException {
		NumberValueContext _localctx = new NumberValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_numberValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListOperatorContext extends ParserRuleContext {
		public ListOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterListOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitListOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitListOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListOperatorContext listOperator() throws RecognitionException {
		ListOperatorContext _localctx = new ListOperatorContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_listOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_la = _input.LA(1);
			if ( !(_la==T__6 || _la==T__7) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListValueContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ListValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).enterListValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterChainListener ) ((FilterChainListener)listener).exitListValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FilterChainVisitor ) return ((FilterChainVisitor<? extends T>)visitor).visitListValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListValueContext listValue() throws RecognitionException {
		ListValueContext _localctx = new ListValueContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_listValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(T__8);
			setState(76);
			value();
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9) {
				{
				{
				setState(77);
				match(T__9);
				setState(78);
				value();
				}
				}
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(84);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 3:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\33Y\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\3\2\7\2\32\n\2\f\2\16\2\35\13\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\65\n\5\3"+
		"\5\3\5\3\5\3\5\7\5;\n\5\f\5\16\5>\13\5\3\6\3\6\3\7\3\7\3\b\3\b\5\bF\n"+
		"\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\7\fR\n\f\f\f\16\fU\13\f\3"+
		"\f\3\f\3\f\2\3\b\r\2\4\6\b\n\f\16\20\22\24\26\2\6\3\2\21\22\3\2\24\25"+
		"\3\2\5\b\3\2\t\n\2T\2\33\3\2\2\2\4\36\3\2\2\2\6#\3\2\2\2\b\64\3\2\2\2"+
		"\n?\3\2\2\2\fA\3\2\2\2\16E\3\2\2\2\20G\3\2\2\2\22I\3\2\2\2\24K\3\2\2\2"+
		"\26M\3\2\2\2\30\32\5\4\3\2\31\30\3\2\2\2\32\35\3\2\2\2\33\31\3\2\2\2\33"+
		"\34\3\2\2\2\34\3\3\2\2\2\35\33\3\2\2\2\36\37\7\17\2\2\37 \5\b\5\2 !\7"+
		"\20\2\2!\"\5\6\4\2\"\5\3\2\2\2#$\t\2\2\2$\7\3\2\2\2%&\b\5\1\2&\'\7\3\2"+
		"\2\'(\5\b\5\2()\7\4\2\2)\65\3\2\2\2*+\7\23\2\2+\65\5\b\5\6,-\7\26\2\2"+
		"-.\5\f\7\2./\5\16\b\2/\65\3\2\2\2\60\61\7\26\2\2\61\62\5\24\13\2\62\63"+
		"\5\26\f\2\63\65\3\2\2\2\64%\3\2\2\2\64*\3\2\2\2\64,\3\2\2\2\64\60\3\2"+
		"\2\2\65<\3\2\2\2\66\67\f\5\2\2\678\5\n\6\289\5\b\5\69;\3\2\2\2:\66\3\2"+
		"\2\2;>\3\2\2\2<:\3\2\2\2<=\3\2\2\2=\t\3\2\2\2><\3\2\2\2?@\t\3\2\2@\13"+
		"\3\2\2\2AB\t\4\2\2B\r\3\2\2\2CF\5\20\t\2DF\5\22\n\2EC\3\2\2\2ED\3\2\2"+
		"\2F\17\3\2\2\2GH\7\27\2\2H\21\3\2\2\2IJ\7\33\2\2J\23\3\2\2\2KL\t\5\2\2"+
		"L\25\3\2\2\2MN\7\13\2\2NS\5\16\b\2OP\7\f\2\2PR\5\16\b\2QO\3\2\2\2RU\3"+
		"\2\2\2SQ\3\2\2\2ST\3\2\2\2TV\3\2\2\2US\3\2\2\2VW\7\r\2\2W\27\3\2\2\2\7"+
		"\33\64<ES";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}