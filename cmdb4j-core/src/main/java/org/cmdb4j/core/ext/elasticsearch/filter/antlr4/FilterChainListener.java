// Generated from FilterChain.g4 by ANTLR 4.7.1
package org.cmdb4j.core.ext.elasticsearch.filter.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FilterChainParser}.
 */
public interface FilterChainListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#filterChain}.
	 * @param ctx the parse tree
	 */
	void enterFilterChain(FilterChainParser.FilterChainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#filterChain}.
	 * @param ctx the parse tree
	 */
	void exitFilterChain(FilterChainParser.FilterChainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#filterEntry}.
	 * @param ctx the parse tree
	 */
	void enterFilterEntry(FilterChainParser.FilterEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#filterEntry}.
	 * @param ctx the parse tree
	 */
	void exitFilterEntry(FilterChainParser.FilterEntryContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#filterDecision}.
	 * @param ctx the parse tree
	 */
	void enterFilterDecision(FilterChainParser.FilterDecisionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#filterDecision}.
	 * @param ctx the parse tree
	 */
	void exitFilterDecision(FilterChainParser.FilterDecisionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldValueExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFieldValueExpr(FilterChainParser.FieldValueExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldValueExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFieldValueExpr(FilterChainParser.FieldValueExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenthesisedExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisedExpr(FilterChainParser.ParenthesisedExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenthesisedExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisedExpr(FilterChainParser.ParenthesisedExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldListExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFieldListExpr(FilterChainParser.FieldListExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldListExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFieldListExpr(FilterChainParser.FieldListExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BinaryOp_Expr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOp_Expr(FilterChainParser.BinaryOp_ExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BinaryOp_Expr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOp_Expr(FilterChainParser.BinaryOp_ExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOpExpr(FilterChainParser.UnaryOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOpExpr(FilterChainParser.UnaryOpExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#binaryOp}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOp(FilterChainParser.BinaryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#binaryOp}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOp(FilterChainParser.BinaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#fieldValueOperator}.
	 * @param ctx the parse tree
	 */
	void enterFieldValueOperator(FilterChainParser.FieldValueOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#fieldValueOperator}.
	 * @param ctx the parse tree
	 */
	void exitFieldValueOperator(FilterChainParser.FieldValueOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(FilterChainParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(FilterChainParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#stringValue}.
	 * @param ctx the parse tree
	 */
	void enterStringValue(FilterChainParser.StringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#stringValue}.
	 * @param ctx the parse tree
	 */
	void exitStringValue(FilterChainParser.StringValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#numberValue}.
	 * @param ctx the parse tree
	 */
	void enterNumberValue(FilterChainParser.NumberValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#numberValue}.
	 * @param ctx the parse tree
	 */
	void exitNumberValue(FilterChainParser.NumberValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#listOperator}.
	 * @param ctx the parse tree
	 */
	void enterListOperator(FilterChainParser.ListOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#listOperator}.
	 * @param ctx the parse tree
	 */
	void exitListOperator(FilterChainParser.ListOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterChainParser#listValue}.
	 * @param ctx the parse tree
	 */
	void enterListValue(FilterChainParser.ListValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterChainParser#listValue}.
	 * @param ctx the parse tree
	 */
	void exitListValue(FilterChainParser.ListValueContext ctx);
}