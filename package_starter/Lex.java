public class Lex {
	String stream;
	LexHelper lexHelper;

	Lex(String input) {
		stream = input;
		lexHelper = new LexHelper(stream);
	}
	
	LexToken[] getTokens() {
		return lexHelper.getLexTokens().toArray(new LexToken[0]);
	}
	
	LexError[] getErrors() {
		return lexHelper.getLexErrors().toArray(new LexError[0]);
	}
}
