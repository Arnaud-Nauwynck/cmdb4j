// cf "-package" in generator 
//@parser::header {
//package org.cmdb4j.core.ext.elasticsearch.filter.antlr4;
//}

grammar FilterChain;

WS : (' ' | '\t' | '\r' | '\n' ) -> skip;


WHEN: 'when';
THEN: 'then';
ACCEPT: 'accept';
REJECT: 'reject';

NOT: '!';
AND: 'and' | '&&';
OR: 'or' | '||';


IDENT : ([A-Za-z_][A-Za-z0-9_]*);

fragment ECHAR : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'');
fragment STRING_LITERAL1 : '\'' (~('\'' | '\\' | '\n' | '\r') | ECHAR)* '\'';
fragment STRING_LITERAL2 : '"' (~('"' | '\\' | '\n' | '\r') | ECHAR)* '"';
STRING_LITERAL: STRING_LITERAL1 | STRING_LITERAL2;

fragment DIGIT : '0'..'9';
fragment EXPONENT : ('e'|'E') SIGN? DIGIT+;
fragment SIGN : ('+'|'-');
fragment DOT : '.';


INT :   SIGN? DIGIT+ ;
DECIMAL : SIGN? DIGIT* DOT DIGIT*;
DOUBLE : SIGN? DIGIT+ DOT DIGIT* EXPONENT?;

NUMBER: INT | DECIMAL | DOUBLE;


filterChain: filterEntry*;

filterEntry: WHEN filterExpr=expr THEN decision=filterDecision;

filterDecision: decision=( ACCEPT |  REJECT);

expr: 
	'(' e=expr ')'						#ParenthesisedExpr
	| prefixOp=NOT e=expr				#UnaryOpExpr
	| lhs=expr op=binaryOp rhs=expr 	#BinaryOp_Expr	
	| field=IDENT fieldOp=fieldValueOperator fieldVal=value		#FieldValueExpr
	| listField=IDENT listOp=listOperator listVal=listValue		#FieldListExpr
	;

binaryOp: (AND | OR);	

fieldValueOperator: '=' | '!=' | '~' | '/~/';

value: stringValue | numberValue;

stringValue: STRING_LITERAL;
numberValue: NUMBER;

listOperator: 'in' | 'notIn';
listValue: '[' value (',' value)* ']'; 


