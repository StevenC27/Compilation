public class SyntaxNode {
	public static final int PROGRAM = 1;
	public static final int STATEMENT_LIST = 2;
	public static final int STATEMENT = 3;
	public static final int IF = 4;
	public static final int WHILE = 5;
	public static final int SCOPE = 6;
	public static final int DECLARE = 7;
	public static final int ASSIGN = 8;
	public static final int EXPRESSION = 9;
	public static final int MATH = 10;
	public static final int MATH_ADD = 11;
	public static final int MATH_SUBTRACT = 12;
	public static final int TERM_MULTIPLY = 13;
	public static final int TERM_DIVIDE = 14;
	public static final int COMPARISON = 15;
	public static final int TERM = 16;
	public static final int FACTOR = 17;
	public static final int IDENTIFIER = 18;
	public static final int TYPE_NAME = 19;
	public static final int NUMBER = 20;
	public static final int STRING = 21;

	public int type;
	public int lineNumber;
	public int characterOffset;
	public String token;
	SyntaxNode children[];

	public SyntaxNode(int type, int lineNumber, int characterOffset, String token) {
		this.type = type;
		this.lineNumber = lineNumber;
		this.characterOffset = characterOffset;
		this.token = token;
	}

	public String toString() {
		if (type == IDENTIFIER)
			return "IDENTIFIER [" + token + "]";
		
		return "UNKNOWN";
	}
}