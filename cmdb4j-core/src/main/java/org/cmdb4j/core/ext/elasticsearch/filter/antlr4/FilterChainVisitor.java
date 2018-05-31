// Generated from FilterChain.g4 by ANTLR 4.7.1
package org.cmdb4j.core.ext.elasticsearch.filter.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FilterChainParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FilterChainVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#filterChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterChain(FilterChainParser.FilterChainContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#filterEntry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterEntry(FilterChainParser.FilterEntryContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#filterDecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterDecision(FilterChainParser.FilterDecisionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldValueExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldValueExpr(FilterChainParser.FieldValueExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenthesisedExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesisedExpr(FilterChainParser.ParenthesisedExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldListExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldListExpr(FilterChainParser.FieldListExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BinaryOp_Expr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOp_Expr(FilterChainParser.BinaryOp_ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link FilterChainParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOpExpr(FilterChainParser.UnaryOpExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#binaryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOp(FilterChainParser.BinaryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#fieldValueOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldValueOperator(FilterChainParser.FieldValueOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(FilterChainParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#stringValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringValue(FilterChainParser.StringValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#numberValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberValue(FilterChainParser.NumberValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#listOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListOperator(FilterChainParser.ListOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link FilterChainParser#listValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListValue(FilterChainParser.ListValueContext ctx);
}