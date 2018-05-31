// Generated from FilterChain.g4 by ANTLR 4.7.1
package org.cmdb4j.core.ext.elasticsearch.filter.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterChainLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, WS=12, WHEN=13, THEN=14, ACCEPT=15, REJECT=16, NOT=17, 
		AND=18, OR=19, IDENT=20, STRING_LITERAL=21, INT=22, DECIMAL=23, DOUBLE=24, 
		NUMBER=25;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "WS", "WHEN", "THEN", "ACCEPT", "REJECT", "NOT", "AND", 
		"OR", "IDENT", "ECHAR", "STRING_LITERAL1", "STRING_LITERAL2", "STRING_LITERAL", 
		"DIGIT", "EXPONENT", "SIGN", "DOT", "INT", "DECIMAL", "DOUBLE", "NUMBER"
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


	public FilterChainLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FilterChain.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\33\u00ec\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3"+
		"\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3"+
		"\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\23"+
		"\3\23\3\23\3\23\3\23\5\23\u0085\n\23\3\24\3\24\3\24\3\24\5\24\u008b\n"+
		"\24\3\25\3\25\7\25\u008f\n\25\f\25\16\25\u0092\13\25\3\26\3\26\3\26\3"+
		"\27\3\27\3\27\7\27\u009a\n\27\f\27\16\27\u009d\13\27\3\27\3\27\3\30\3"+
		"\30\3\30\7\30\u00a4\n\30\f\30\16\30\u00a7\13\30\3\30\3\30\3\31\3\31\5"+
		"\31\u00ad\n\31\3\32\3\32\3\33\3\33\5\33\u00b3\n\33\3\33\6\33\u00b6\n\33"+
		"\r\33\16\33\u00b7\3\34\3\34\3\35\3\35\3\36\5\36\u00bf\n\36\3\36\6\36\u00c2"+
		"\n\36\r\36\16\36\u00c3\3\37\5\37\u00c7\n\37\3\37\7\37\u00ca\n\37\f\37"+
		"\16\37\u00cd\13\37\3\37\3\37\7\37\u00d1\n\37\f\37\16\37\u00d4\13\37\3"+
		" \5 \u00d7\n \3 \6 \u00da\n \r \16 \u00db\3 \3 \7 \u00e0\n \f \16 \u00e3"+
		"\13 \3 \5 \u00e6\n \3!\3!\3!\5!\u00eb\n!\2\2\"\3\3\5\4\7\5\t\6\13\7\r"+
		"\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25"+
		")\26+\2-\2/\2\61\27\63\2\65\2\67\29\2;\30=\31?\32A\33\3\2\n\5\2\13\f\17"+
		"\17\"\"\5\2C\\aac|\6\2\62;C\\aac|\n\2$$))^^ddhhppttvv\6\2\f\f\17\17))"+
		"^^\6\2\f\f\17\17$$^^\4\2GGgg\4\2--//\2\u00f9\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2\61\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2"+
		"\3C\3\2\2\2\5E\3\2\2\2\7G\3\2\2\2\tI\3\2\2\2\13L\3\2\2\2\rN\3\2\2\2\17"+
		"R\3\2\2\2\21U\3\2\2\2\23[\3\2\2\2\25]\3\2\2\2\27_\3\2\2\2\31a\3\2\2\2"+
		"\33e\3\2\2\2\35j\3\2\2\2\37o\3\2\2\2!v\3\2\2\2#}\3\2\2\2%\u0084\3\2\2"+
		"\2\'\u008a\3\2\2\2)\u008c\3\2\2\2+\u0093\3\2\2\2-\u0096\3\2\2\2/\u00a0"+
		"\3\2\2\2\61\u00ac\3\2\2\2\63\u00ae\3\2\2\2\65\u00b0\3\2\2\2\67\u00b9\3"+
		"\2\2\29\u00bb\3\2\2\2;\u00be\3\2\2\2=\u00c6\3\2\2\2?\u00d6\3\2\2\2A\u00ea"+
		"\3\2\2\2CD\7*\2\2D\4\3\2\2\2EF\7+\2\2F\6\3\2\2\2GH\7?\2\2H\b\3\2\2\2I"+
		"J\7#\2\2JK\7?\2\2K\n\3\2\2\2LM\7\u0080\2\2M\f\3\2\2\2NO\7\61\2\2OP\7\u0080"+
		"\2\2PQ\7\61\2\2Q\16\3\2\2\2RS\7k\2\2ST\7p\2\2T\20\3\2\2\2UV\7p\2\2VW\7"+
		"q\2\2WX\7v\2\2XY\7K\2\2YZ\7p\2\2Z\22\3\2\2\2[\\\7]\2\2\\\24\3\2\2\2]^"+
		"\7.\2\2^\26\3\2\2\2_`\7_\2\2`\30\3\2\2\2ab\t\2\2\2bc\3\2\2\2cd\b\r\2\2"+
		"d\32\3\2\2\2ef\7y\2\2fg\7j\2\2gh\7g\2\2hi\7p\2\2i\34\3\2\2\2jk\7v\2\2"+
		"kl\7j\2\2lm\7g\2\2mn\7p\2\2n\36\3\2\2\2op\7c\2\2pq\7e\2\2qr\7e\2\2rs\7"+
		"g\2\2st\7r\2\2tu\7v\2\2u \3\2\2\2vw\7t\2\2wx\7g\2\2xy\7l\2\2yz\7g\2\2"+
		"z{\7e\2\2{|\7v\2\2|\"\3\2\2\2}~\7#\2\2~$\3\2\2\2\177\u0080\7c\2\2\u0080"+
		"\u0081\7p\2\2\u0081\u0085\7f\2\2\u0082\u0083\7(\2\2\u0083\u0085\7(\2\2"+
		"\u0084\177\3\2\2\2\u0084\u0082\3\2\2\2\u0085&\3\2\2\2\u0086\u0087\7q\2"+
		"\2\u0087\u008b\7t\2\2\u0088\u0089\7~\2\2\u0089\u008b\7~\2\2\u008a\u0086"+
		"\3\2\2\2\u008a\u0088\3\2\2\2\u008b(\3\2\2\2\u008c\u0090\t\3\2\2\u008d"+
		"\u008f\t\4\2\2\u008e\u008d\3\2\2\2\u008f\u0092\3\2\2\2\u0090\u008e\3\2"+
		"\2\2\u0090\u0091\3\2\2\2\u0091*\3\2\2\2\u0092\u0090\3\2\2\2\u0093\u0094"+
		"\7^\2\2\u0094\u0095\t\5\2\2\u0095,\3\2\2\2\u0096\u009b\7)\2\2\u0097\u009a"+
		"\n\6\2\2\u0098\u009a\5+\26\2\u0099\u0097\3\2\2\2\u0099\u0098\3\2\2\2\u009a"+
		"\u009d\3\2\2\2\u009b\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\u009e\3\2"+
		"\2\2\u009d\u009b\3\2\2\2\u009e\u009f\7)\2\2\u009f.\3\2\2\2\u00a0\u00a5"+
		"\7$\2\2\u00a1\u00a4\n\7\2\2\u00a2\u00a4\5+\26\2\u00a3\u00a1\3\2\2\2\u00a3"+
		"\u00a2\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6\3\2"+
		"\2\2\u00a6\u00a8\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8\u00a9\7$\2\2\u00a9"+
		"\60\3\2\2\2\u00aa\u00ad\5-\27\2\u00ab\u00ad\5/\30\2\u00ac\u00aa\3\2\2"+
		"\2\u00ac\u00ab\3\2\2\2\u00ad\62\3\2\2\2\u00ae\u00af\4\62;\2\u00af\64\3"+
		"\2\2\2\u00b0\u00b2\t\b\2\2\u00b1\u00b3\5\67\34\2\u00b2\u00b1\3\2\2\2\u00b2"+
		"\u00b3\3\2\2\2\u00b3\u00b5\3\2\2\2\u00b4\u00b6\5\63\32\2\u00b5\u00b4\3"+
		"\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8"+
		"\66\3\2\2\2\u00b9\u00ba\t\t\2\2\u00ba8\3\2\2\2\u00bb\u00bc\7\60\2\2\u00bc"+
		":\3\2\2\2\u00bd\u00bf\5\67\34\2\u00be\u00bd\3\2\2\2\u00be\u00bf\3\2\2"+
		"\2\u00bf\u00c1\3\2\2\2\u00c0\u00c2\5\63\32\2\u00c1\u00c0\3\2\2\2\u00c2"+
		"\u00c3\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4<\3\2\2\2"+
		"\u00c5\u00c7\5\67\34\2\u00c6\u00c5\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00cb"+
		"\3\2\2\2\u00c8\u00ca\5\63\32\2\u00c9\u00c8\3\2\2\2\u00ca\u00cd\3\2\2\2"+
		"\u00cb\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ce\3\2\2\2\u00cd\u00cb"+
		"\3\2\2\2\u00ce\u00d2\59\35\2\u00cf\u00d1\5\63\32\2\u00d0\u00cf\3\2\2\2"+
		"\u00d1\u00d4\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3>\3"+
		"\2\2\2\u00d4\u00d2\3\2\2\2\u00d5\u00d7\5\67\34\2\u00d6\u00d5\3\2\2\2\u00d6"+
		"\u00d7\3\2\2\2\u00d7\u00d9\3\2\2\2\u00d8\u00da\5\63\32\2\u00d9\u00d8\3"+
		"\2\2\2\u00da\u00db\3\2\2\2\u00db\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc"+
		"\u00dd\3\2\2\2\u00dd\u00e1\59\35\2\u00de\u00e0\5\63\32\2\u00df\u00de\3"+
		"\2\2\2\u00e0\u00e3\3\2\2\2\u00e1\u00df\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2"+
		"\u00e5\3\2\2\2\u00e3\u00e1\3\2\2\2\u00e4\u00e6\5\65\33\2\u00e5\u00e4\3"+
		"\2\2\2\u00e5\u00e6\3\2\2\2\u00e6@\3\2\2\2\u00e7\u00eb\5;\36\2\u00e8\u00eb"+
		"\5=\37\2\u00e9\u00eb\5? \2\u00ea\u00e7\3\2\2\2\u00ea\u00e8\3\2\2\2\u00ea"+
		"\u00e9\3\2\2\2\u00ebB\3\2\2\2\27\2\u0084\u008a\u0090\u0099\u009b\u00a3"+
		"\u00a5\u00ac\u00b2\u00b7\u00be\u00c3\u00c6\u00cb\u00d2\u00d6\u00db\u00e1"+
		"\u00e5\u00ea\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}