public class Syntax {
	SyntaxHelper syntaxHelper;
  
	Syntax(LexToken[] tokens) {
		syntaxHelper = new SyntaxHelper(tokens); // creates a syntaxHelper reference.
	}
	
	SyntaxNode parse(){
		return syntaxHelper.parseProgram(); // Create root node for tree.
	}
	
	SyntaxError[] getErrors(){
		return syntaxHelper.syntaxErrors.toArray(new SyntaxError[0]); // gets the syntaxErrors from syntaxHelper.
	}
}
