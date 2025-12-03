public class Lex {
	String stream;
	LexHelper lexHelper;

	Lex(String input) {
		stream = input; // sets stream to input
		lexHelper = new LexHelper(stream); // creates lexHelper.
	}
	
	LexToken[] getTokens() {
		return lexHelper.getLexTokens().toArray(new LexToken[0]); // returns the lexTokens from lexHelper.
	}
	
	LexError[] getErrors() {
		return lexHelper.getLexErrors().toArray(new LexError[0]); // returns the lexErrors from lexHelper.
	}
}
