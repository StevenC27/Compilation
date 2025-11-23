import java.util.*;

public class SemanticHelper {
    private List<Symbol> symbolTable;
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
        if (declareNode.children[1].type == SyntaxNode.IDENTIFIER){
            String identifierName = declareNode.children[1].token;
            Symbol symbol = new Symbol(identifierType, identifierName);
            addIdentifier(symbol);
        } else {
            SyntaxNode assignNode = declareNode.children[1];
            String identifierName = assignNode.children[0].token;
            Symbol symbol = new Symbol(identifierType, identifierName);
            addIdentifier(symbol);
            parseAssign(assignNode);
        }
    }

    public void parseAssign(SyntaxNode assignNode){
        // <assignment> ::= identifier = <expression>
        // <assignment> ::= identifier = string

        // need to check the variable exists in the symbol table.
        String identifier = assignNode.children[0].token;
        Symbol symbol = findAllScopeSymbol(identifier);
        if (symbol != null){
            if (!((Objects.equals(symbol.getType(), "int") && Objects.equals(getType(assignNode.children[1]), "bool"))
                    || Objects.equals(symbol.getType(), getType(assignNode.children[1])))){
                isNoErrors = false;
            }
        } else {
            isNoErrors = false;
        }
    }

    public void parseScope(SyntaxNode scope){
        scopeStack.push(new HashMap<>());
        parseStatementList(scope.children[0]);
        scopeStack.pop();
    }

    public String getType(SyntaxNode node){
        if (node.type == SyntaxNode.IDENTIFIER){
            Symbol symbol = findAllScopeSymbol(node.token);
            if (symbol != null){
                return symbol.getType();
            } else {
                return null;
            }
        } else if (node.type == SyntaxNode.STRING){
            return "string";
        } else if (node.type == SyntaxNode.NUMBER){
            return "int";
        } else if (node.type == SyntaxNode.COMPARISON){
            checkComparisonTypes(node);
            return "bool";
        } else if (node.type == SyntaxNode.FACTOR
                || node.type == SyntaxNode.TERM
                || node.type == SyntaxNode.MATH){
            return getType(node.children[0]);
        } else if (node.type == SyntaxNode.TERM_MULTIPLY || node.type == SyntaxNode.TERM_DIVIDE
                || node.type == SyntaxNode.MATH_ADD || node.type == SyntaxNode.MATH_SUBTRACT){
            checkOperationTypes(node);
            return "int";
        } else if (node.type == SyntaxNode.EXPRESSION){
            if (node.children[0].type == SyntaxNode.COMPARISON){
                checkComparisonTypes(node.children[0]);
                return "bool";
            } else {
                return getType(node.children[0]);
            }
        }
        return null;
    }

    public void checkComparisonTypes(SyntaxNode comparisonNode){
        // <comparison> ::= ( <expression> , <expression> )
        String firstExpressionType = getType(comparisonNode.children[0]);
        String secondExpressionType = getType(comparisonNode.children[1]);
        if (!Objects.equals(firstExpressionType, secondExpressionType)){
            isNoErrors = false;
        };
    }

    public void checkOperationTypes(SyntaxNode operationNode){
        SyntaxNode childTermNode = operationNode.children[0];
        SyntaxNode childFactorNode = operationNode.children[1];

        if (!(Objects.equals(getType(childTermNode), "int")
                && Objects.equals(getType(childFactorNode), "int"))){
            isNoErrors = false;
        }
    }

    public void addIdentifier(Symbol symbol){
        if (findAllScopeSymbol(symbol.getIdentifier()) != null){
            isNoErrors = false;
        } else {
            symbolTable.add(symbol);
            scopeStack.peek().put(symbol.getIdentifier(), symbolTable.size() - 1);
        }
    }

    public Symbol findScopeSymbol(Map<String, Integer> scope, String identifier){
        Symbol targetSymbol = null;
        for (String symbol : scope.keySet()){
            if (Objects.equals(symbol, identifier)){
                int symbolIndex = scope.get(symbol);
                targetSymbol = symbolTable.get(symbolIndex);
                break;
            }
        }
        return targetSymbol;
    }

    public Symbol findAllScopeSymbol(String identifier){
        Symbol targetSymbol = null;
        for (Map<String, Integer> scope : scopeStack){
            Symbol symbol = findScopeSymbol(scope, identifier);
            if (symbol != null){
                targetSymbol = symbol;
            }
        }
        return targetSymbol;
    }

    public boolean isNoErrors(){
        return isNoErrors;
    }
}
