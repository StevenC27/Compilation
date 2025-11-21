import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	SyntaxNode[] children;

	public SyntaxNode(int type, int lineNumber, int characterOffset, String token) {
		this.type = type;
		this.lineNumber = lineNumber;
		this.characterOffset = characterOffset;
		this.token = token;
		this.children = new SyntaxNode[0];
	}

	public String toString() {
		if (type == PROGRAM)
			return "PROGRAM";
		else if (type == STATEMENT_LIST)
			return "STATEMENT_LIST";
		else if (type == STATEMENT)
			return "STATEMENT";
		else if (type == IF)
			return "IF";
		else if (type == WHILE)
			return "WHILE";
		else if (type == SCOPE)
			return "SCOPE";
		else if (type == DECLARE)
			return "DECLARE";
		else if (type == ASSIGN)
			return "ASSIGN";
		else if (type == EXPRESSION)
			return "EXPRESSION";
		else if (type == MATH)
			return "MATH";
		else if (type == MATH_ADD)
			return "MATH_ADD";
		else if (type == MATH_SUBTRACT)
			return "MATH_SUBTRACT";
		else if (type == TERM_MULTIPLY)
			return "TERM_MULTIPLY";
		else if (type == TERM_DIVIDE)
			return "TERM_DIVIDE";
		else if (type == COMPARISON)
			return "COMPARISON";
		else if (type == TERM)
			return "TERM";
		else if (type == FACTOR)
			return "FACTOR";
		else if (type == IDENTIFIER)
			return "IDENTIFIER [" + token + "]";
		else if (type == TYPE_NAME)
			return "TYPE_NAME [" + token + "]";
		else if (type == NUMBER)
			return "NUMBER [" + token + "]";
		else if (type == STRING)
			return "STRING [" + token + "]";
		else return "UNKNOWN";
	}
}