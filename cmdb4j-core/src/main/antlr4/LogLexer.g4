// @header {
// package org.cmdb4j.core.ext.patterns.tokentemplate; ... obsolete in antlr4 ... cf "-package" in generator 
// }

lexer grammar LogLexer;


IDENT : ([A-Za-z_][A-Za-z0-9_]*); // TODO... also accept all accents
TEXT : (IDENT | WS | PUNCTUATION)+;

NL : '\r'? '\n';
WS : (' ' | '\t' ) -> skip;

STRING_LITERAL1 : '\'' (~('\'' | '\\' | '\n' | '\r') | ECHAR)* '\'';
STRING_LITERAL2 : '"' (~('"' | '\\' | '\n' | '\r') | ECHAR)* '"';
STRING_LITERAL_LONG1 : '\'\'\'' (('\'' | '\'\'')? (~('\''|'\\') | ECHAR))* '\'\'\'';
STRING_LITERAL_LONG2 : '"""' (('"' | '""')? (~('"'|'\\') | ECHAR))* '"""';

fragment ECHAR : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'');



PUNCTUATION : ( '.' | ',' | ';' | ':' | '!' | '?');
ASSIGN_OP : (':=' | '=');

fragment DIGIT : '0'..'9';
fragment EXPONENT : ('e'|'E') SIGN? DIGIT+;
fragment SIGN : ('+'|'-');
fragment DOT : '.';

INT :   SIGN? DIGIT+ ;
DECIMAL : SIGN? DIGIT* DOT DIGIT*;
DOUBLE : SIGN? DIGIT+ DOT DIGIT* EXPONENT?;

fragment DATE_YYYYMMDD: DIGIT+ ('/' | '-') MONTH ('/' | '-') DAY;
fragment DATE_DDMMYYYY: DAY ('/' | '-') MONTH ('/' | '-') YEAR;
fragment DATE_DDMMMYYYY: DAY_NAME? DAY MONTH_NAME YEAR;
fragment DATE_HMS: DIGIT+ ':' DIGIT+ ':' DIGIT+; // TODO... millis, Timezone... 
fragment YEAR: DIGIT+; // ( DIGIT{2} | DIGIT{4} );
fragment MONTH_NAME: EN_MONTH | FR_MONTH;  
fragment EN_MONTH: ('Jan' | 'Feb' | 'Mar' | 'Apr' | 'May' | 'Jun' | 'Jul' | 'Aug' | 'Sep' | 'Oct' | 'Nov' | 'Dec' | 'January' | 'February' | 'March' | 'April' | 'May' | 'June' | 'July' | 'August' | 'September' | 'October' | 'November' | 'December');
fragment FR_MONTH: ('Jan' | 'Fev' | 'Mar' | 'Avr' | 'Mai' | 'Jui' | 'Jul' | 'Aou' | 'Sep' | 'Oct' | 'Nov' | 'Dec' | 'Janvier' | 'Fevrier' | 'Mars' | 'Avril' | 'Mai' | 'Juin' | 'Juillet' | 'Aout' | 'Septembre' | 'Octobre' | 'Novembre' | 'Decembre');
fragment MONTH: ( DIGIT+ | MONTH_NAME );
fragment DAY_NAME: EN_DAY | FR_DAY;
fragment EN_DAY: ('Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun' | 'Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday');
fragment FR_DAY: ('Lun' | 'Mar' | 'Mer' | 'Jeu' | 'Ven' | 'Sam' | 'Dim' | 'Lundi' | 'Mardi' | 'Mercredi' | 'Jeudi' | 'Vendredi' | 'Samedi' | 'Dimanche');
fragment DAY: ( DIGIT+ | DAY_NAME );
DATE: (DATE_YYYYMMDD | DATE_DDMMYYYY | DATE_DDMMMYYYY ) DATE_HMS?;

OPEN_BRACE : '(';
CLOSE_BRACE : ')';
OPEN_CURLY_BRACE : '{';
CLOSE_CURLY_BRACE : '}';
OPEN_SQUARE_BRACKET : '[';
CLOSE_SQUARE_BRACKET : ']';

OTHER: .;
