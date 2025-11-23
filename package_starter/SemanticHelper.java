import java.util.*;

public class SemanticHelper {
    private List<Identifier> symbolTable;
    private Stack<Map<String, Integer>> scopeStack;
    private boolean isNoErrors;

    public SemanticHelper(SyntaxNode tree){
        this.symbolTable = new ArrayList<>();

        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>());

        this.isNoErrors = true;

        parseStatementList(tree.children[0]);
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

    public void parseStatementList(SyntaxNode statementListNode){
        for (SyntaxNode child : statementListNode.children){
            parseStatement(child);
        }
    }

    public void parseStatement(SyntaxNode statementNode){
        int childNodeType = statementNode.children[0].type;
        if (childNodeType == SyntaxNode.IF){
            parseIf(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.WHILE){
            parseWhile(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.DECLARE){
            parseDeclare(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.ASSIGN){
            parseAssign(statementNode.children[0]);
        }
    }

    public void parseIf(SyntaxNode ifNode){
        // <if> ::= if ( <expression> ) <scope>
        // <if> ::= if ( <expression> ) <scope> else <scope>

        // add scope to scopeStack and remove one scope is parsed.
        parseScope(ifNode.children[1]);
        if (ifNode.children.length == 3){
            parseScope(ifNode.children[2]);
        }
    }

    public void parseWhile(SyntaxNode whileNode){
        // <while> ::= while ( <expression> ) <scope>
        parseScope(whileNode.children[1]);
    }

    public void parseDeclare(SyntaxNode declareNode){
        // <declaration> ::= typename identifier
        // <declaration> ::= typename <assignment>
        String identifierType = declareNode.children[0].token;
        String identifierName;
        if (declareNode.children[1].type == SyntaxNode.IDENTIFIER){
            identifierName = declareNode.children[1].token;
        } else {
            identifierName = declareNode.children[1].children[0].token;
        }

        Identifier symbol = new Identifier(identifierType, identifierName);
        addIdentifier(symbol);
    }

    public void parseAssign(SyntaxNode assignNode){
        // <assignment> ::= identifier = <expression>
        // <assignment> ::= identifier = string

        // need to check the variable exists in the symbol table.
        String identifier = assignNode.children[0].token;
        if (checkScopeSymbolExists(identifier)){
            isNoErrors = false;
        }
    }

    public void parseScope(SyntaxNode scope){
        scopeStack.push(new HashMap<>());
        parseStatementList(scope.children[0]);
        scopeStack.pop();
    }

    public void addIdentifier(Identifier identifier){
        if (checkScopeSymbolExists(identifier.getIdentifier())){
            isNoErrors = false;
        } else {
            symbolTable.add(identifier);
            scopeStack.peek().put(identifier.getIdentifier(), symbolTable.size()-1);
        }
    }

    public boolean checkSymbolExists(Identifier identifier){
        boolean symbolExists = false;
        for (Identifier symbol : symbolTable){
            if (Objects.equals(symbol.getType(), identifier.getType())
                    || Objects.equals(symbol.getIdentifier(), identifier.getIdentifier())){
                symbolExists = true;
                break;
            }
        }
        return symbolExists;
    }

    public boolean checkScopeSymbolExists(String identifier){
        boolean symbolExists = false;
        Map<String, Integer> currentScope = scopeStack.peek();
        for (String symbol : currentScope.keySet()){
            if (Objects.equals(symbol, identifier)){
                symbolExists = true;
                break;
            }
        }
        return symbolExists;
    }

    public boolean isNoErrors(){
        return isNoErrors;
    }
}
