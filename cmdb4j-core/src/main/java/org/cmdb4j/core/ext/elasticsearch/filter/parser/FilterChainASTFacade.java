package org.cmdb4j.core.ext.elasticsearch.filter.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilterDecision;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.ChainSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.ChainSearchHitFilterElement;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainBaseVisitor;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainLexer;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.BinaryOpContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.BinaryOp_ExprContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FieldListExprContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FieldValueExprContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FieldValueOperatorContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FilterChainContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FilterDecisionContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.FilterEntryContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.ListOperatorContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.ListValueContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.NumberValueContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.ParenthesisedExprContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.StringValueContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.UnaryOpExprContext;
import org.cmdb4j.core.ext.elasticsearch.filter.antlr4.FilterChainParser.ValueContext;


public class FilterChainASTFacade {

	public static ChainSearchHitFilter parse(String inputText) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(inputText.getBytes())) {
			return parse(in);
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static ChainSearchHitFilter parse(InputStream input) throws IOException {
		FilterChainLexer lex = new FilterChainLexer(new ANTLRInputStream(input));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        FilterChainParser parser = new FilterChainParser(tokens);

        // parse antlr CST
        FilterChainContext cstTree = parser.filterChain();
        
        // convert antlr CST to AST
        FilterChainAstBuilder cstToAstBuilder = new FilterChainAstBuilder();
        ChainSearchHitFilter res = (ChainSearchHitFilter) cstToAstBuilder.visit(cstTree);
        
        return res;
	}
	
	
	static class FilterChainAstBuilder extends FilterChainBaseVisitor<Object> {

		@Override
		public ChainSearchHitFilter visitFilterChain(FilterChainContext ctx) {
			List<ChainSearchHitFilterElement> elts = new ArrayList<>();
			for(FilterEntryContext entryCtx : ctx.filterEntry()) {
				elts.add(visitFilterEntry(entryCtx));
			}
			return new ChainSearchHitFilter(elts);
		}

		@Override
		public ChainSearchHitFilterElement visitFilterEntry(FilterEntryContext ctx) {
			SearchHitFilter filter = (SearchHitFilter) visit(ctx.filterExpr); 
			SearchHitFilterDecision decision = (SearchHitFilterDecision) visitFilterDecision(ctx.decision);
			boolean include = decision == SearchHitFilterDecision.ACCEPT;
			return new ChainSearchHitFilterElement(include, filter);
		}

		
		@Override
		public SearchHitFilterDecision visitFilterDecision(FilterDecisionContext ctx) {
			String decisionText = ctx.decision.getText().toLowerCase();
			return SearchHitFilterDecision.parse(decisionText);
		}

		@Override
		public Object visitFieldValueExpr(FieldValueExprContext ctx) {
			String field = ctx.field.getText();
			String op = ctx.fieldOp.getText();
			Object value = visit(ctx.fieldVal);
			SearchHitFilter res;
			switch(op) {
			case "=": 
				res = SearchHitFilter.fieldEq(field, value);
				break;
			case "~": 
				res = SearchHitFilter.fieldTemplatePattern(field, (String) value);
				break;
			case "/~/": 
				Pattern valuePattern = Pattern.compile((String) value);
				res = SearchHitFilter.fieldMatch(field, valuePattern);
				break;
			default:
				throw new IllegalArgumentException("unrecognized field <op> value, op:" + op);
			}
			return res;
		}

		@Override
		public String visitFieldValueOperator(FieldValueOperatorContext ctx) {
			return ctx.getText();
		}

		@Override
		public Object visitParenthesisedExpr(ParenthesisedExprContext ctx) {
			SearchHitFilter expr = (SearchHitFilter) visit(ctx.e);
			return expr;
		}

		@Override
		public SearchHitFilter visitBinaryOp_Expr(BinaryOp_ExprContext ctx) {
			SearchHitFilter lhsExpr = (SearchHitFilter) visit(ctx.lhs);
			String opText = visitBinaryOp(ctx.op);
			SearchHitFilter rhsExpr = (SearchHitFilter) visit(ctx.rhs);
			return opText.equals("and")? SearchHitFilter.and(lhsExpr) : SearchHitFilter.or(rhsExpr);
		}


		@Override
		public String visitBinaryOp(BinaryOpContext ctx) {
			ParseTree token = ctx.getChild(0);
			TerminalNode andNode = ctx.AND();
			TerminalNode orNode = ctx.OR();
			return "and"; // TODO ..
		}
		
		
		@Override
		public Object visitUnaryOpExpr(UnaryOpExprContext ctx) {
			// Token prefixOp = ctx.prefixOp; //TODO only "not" implemented ... should add "strict accept", "strict reject", ..
			SearchHitFilter expr = (SearchHitFilter) visit(ctx.e);
			return SearchHitFilter.inverse(expr);
		}

		@Override
		public Object visitValue(ValueContext ctx) {
			return super.visitValue(ctx);
		}

		@Override
		public String visitStringValue(StringValueContext ctx) {
			ParseTree child = ctx.getChild(0);
			String textWithQuotes = child.getText();
			return textWithQuotes.substring(1, textWithQuotes.length() - 1);
		}

		@Override
		public String visitNumberValue(NumberValueContext ctx) {
			ParseTree child = ctx.getChild(0);
			String text = child.getText();
			return text;  // to change: parse as Number?
		}

		@Override
		public Object visitFieldListExpr(FieldListExprContext ctx) {
			// ctx.listField;
			return super.visitFieldListExpr(ctx);
		}

		@Override
		public Object visitListOperator(ListOperatorContext ctx) {
			return ctx.getText();
		}

		@Override
		public Object visitListValue(ListValueContext ctx) {
			// TODO Auto-generated method stub
			return super.visitListValue(ctx);
		}


	}
}
