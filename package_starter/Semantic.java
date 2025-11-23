public class Semantic {
	SemanticHelper helper;
	Semantic(SyntaxNode tree){
		helper = new SemanticHelper(tree);
	}
	
	Boolean parse() {
		return helper.isNoErrors();
	}
	
	SemanticError[] getErrors()
		{
		return null;
		}
}
