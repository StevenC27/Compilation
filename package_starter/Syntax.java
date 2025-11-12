public class Syntax {
	SyntaxHelper syntaxHelper;
  
	Syntax(LexToken[] tokens) {
		syntaxHelper = new SyntaxHelper(tokens);
	}
	
	SyntaxNode parse(){
		// Create node for program.
		return syntaxHelper.parseProgram();
	}
	
	SyntaxError[] getErrors(){
		return null;
	}
}
