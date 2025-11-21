import java.util.Map;

public class SemanticHelper {
    SyntaxNode tree;
    Map<String, Integer> symbolTable;
    public SemanticHelper(SyntaxNode tree){
        this.tree = tree;
        navigateTree();
    }

    /*
    *   navigate the syntax tree and check for any declarations.
    *   add the unknown declared variables to the symbol table.
    *
    *   create a stack for the scopes in the program.
    *   in these scopes store the variables that are available in the scope.
    *   this includes the variables from an inner scope.
    *
    *   check types
    * */

    public void navigateTree(){
        // navigate to the child of <program> which is <statementList>
        SyntaxNode statementListNode = tree.children[0];

        // check the first statement of the statementListNode is a declaration node.
        // then get the symbol name and type from the declaration and add to the symbol table.
        // we need to check for any instances of the identifier and check to see what it is or if it exists.

        // then check if there is another child of statementListNode.
        // with this second child, it should be a statement list node so set statementNodeList equal to it and repeat the process.

    }

    public boolean isStatementDeclaration(SyntaxNode syntaxNode){
        return true;
    }
}
