public class LexToken {
	public static final int IDENTIFIER = 1;
	public static final int TYPE_NAME = 2;
	public static final int LITERAL_NUMBER = 3;
	public static final int LITERAL_STRING = 4;
	public static final int SYNTAX_TOKEN = 5;
	public static final int KEYWORD = 6;
	public static final int UNKNOWN = 7;
	public int type;
	public int lineNumber;
	public int characterOffset;
	public String token;

	public LexToken() {

	}

	public LexToken(int type, int lineNumber, int characterOffset, String token) {
		this.type = type;
		this.lineNumber = lineNumber;
		this.characterOffset = characterOffset;
		this.token = token;
	}

	public String toString() {
		if (type == IDENTIFIER)
			return "IDENTIFIER [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		else if (type == KEYWORD)
			return "KEYWORD [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		else if (type == SYNTAX_TOKEN)
			return "SYNTAX_TOKEN [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		else if (type == TYPE_NAME)
			return "TYPE_NAME [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		else if (type == LITERAL_NUMBER)
			return "LITERAL_NUMBER [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		else if (type == LITERAL_STRING)
			return "LITERAL_STRING [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
		
		return "UNKNOWN [" + token + "] on line " + lineNumber + ", offset " + characterOffset + "\n";
	}
}