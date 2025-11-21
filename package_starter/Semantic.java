public class Semantic {
	SemanticHelper helper;
	Semantic(SyntaxNode tree){
		helper = new SemanticHelper(tree);
	}
	
	Boolean parse() {
		return false;
	}
	
	SemanticError[] getErrors()
		{
		return null;
		}
}
