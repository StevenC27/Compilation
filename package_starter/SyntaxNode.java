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

	public void addChild(SyntaxNode child) {
		this.children = Arrays.copyOf(children, children.length + 1);
		this.children[this.children.length-1] = child;
	}

	public String toString() {
		if (type == PROGRAM)
			return "PROGRAM [" + token + "]";
		else if (type == STATEMENT_LIST)
			return "STATEMENT_LIST [" + token + "]";
		else if (type == STATEMENT)
			return "STATEMENT [" + token + "]";
		else if (type == IF)
			return "IF [" + token + "]";
		else if (type == WHILE)
			return "WHILE [" + token + "]";
		else if (type == SCOPE)
			return "SCOPE [" + token + "]";
		else if (type == DECLARE)
			return "DECLARE [" + token + "]";
		else if (type == ASSIGN)
			return "ASSIGN [" + token + "]";
		else if (type == EXPRESSION)
			return "EXPRESSION [" + token + "]";
		else if (type == MATH)
			return "MATH [" + token + "]";
		else if (type == MATH_ADD)
			return "MATH_ADD [" + token + "]";
		else if (type == MATH_SUBTRACT)
			return "MATH_SUBTRACT [" + token + "]";
		else if (type == TERM_MULTIPLY)
			return "TERM_MULTIPLY [" + token + "]";
		else if (type == TERM_DIVIDE)
			return "TERM_DIVIDE [" + token + "]";
		else if (type == COMPARISON)
			return "COMPARISON [" + token + "]";
		else if (type == TERM)
			return "TERM [" + token + "]";
		else if (type == FACTOR)
			return "FACTOR [" + token + "]";
		else if (type == IDENTIFIER)
			return "IDENTIFIER [" + token + "]";
		else if (type == TYPE_NAME)
			return "TYPE_NAME [" + token + "]";
		else if (type == NUMBER)
			return "NUMBER [" + token + "]";
		else if (type == STRING)
			return "STRING [" + token + "]";
		return "UNKNOWN";
	}
}