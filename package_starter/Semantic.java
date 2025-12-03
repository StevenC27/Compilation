public class Semantic {
	SemanticHelper semanticHelper;
	Semantic(SyntaxNode tree){
		semanticHelper = new SemanticHelper(tree);
	}
	
	Boolean parse() {
		return !semanticHelper.isErrors(); // returns true if there are no errors and false otherwise.
	}
	
	SemanticError[] getErrors() {
		return semanticHelper.getErrors().toArray(new SemanticError[0]); // returns the semanticErrors from semanticHelper.
	}
}
