import java.util.*;

public class SemanticHelper {
    private final List<Symbol> symbolTable;
    private final Stack<Map<String, Integer>> scopeStack;
    private final List<SemanticError> semanticErrors;

    public SemanticHelper(SyntaxNode tree){
        // initialises the symbolTable, scopeStack, isNoErrors and semanticErrors.
        this.symbolTable = new ArrayList<>();
        this.scopeStack = new Stack<>();
        this.semanticErrors = new ArrayList<>();

        this.scopeStack.push(new HashMap<>()); // pushes a new hashmap onto the scopeStack to represent global scope.
        parseStatementList(tree.children[0]); // parses the tree.
    }

    public void parseStatementList(SyntaxNode statementListNode){
        // loops through each statement in the statementList and parses them.
        for (SyntaxNode child : statementListNode.children){
            parseStatement(child);
        }
    }

    public void parseStatement(SyntaxNode statementNode){
        int childNodeType = statementNode.children[0].type; // gets the node type of the statementNode's childNode.

        if (childNodeType == SyntaxNode.IF){
            // parses the if statement if the node type is IF.
            parseIf(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.WHILE){
            // parses the while statement if the node type is WHILE.
            parseWhile(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.DECLARE){
            // parses the declare statement if the node is type DECLARE.
            parseDeclare(statementNode.children[0]);
        } else if (childNodeType == SyntaxNode.ASSIGN){
            // parses the assign statement if the node is type ASSIGN.
            parseAssign(statementNode.children[0]);
        }
    }

    public void parseIf(SyntaxNode ifNode){
        // <if> ::= if ( <expression> ) <scope>
        // <if> ::= if ( <expression> ) <scope> else <scope>

        // add scope to scopeStack and remove one scope is parsed.
        parseScope(ifNode.children[1]);
        if (ifNode.children.length == 3){
            // if the ifNode has 3 children then it is an if else statement so parse the second scope.
            parseScope(ifNode.children[2]);
        }
    }

    public void parseWhile(SyntaxNode whileNode){
        // <while> ::= while ( <expression> ) <scope>
        parseScope(whileNode.children[1]); // parses the child scope.
    }

    public void parseDeclare(SyntaxNode declareNode){
        // <declaration> ::= typename identifier
        // <declaration> ::= typename <assignment>
        String identifierType = declareNode.children[0].token; // gets the typename of the identifier being declared.

        // checks if the second child node of the declareNode is of type IDENTIFIER.
        if (declareNode.children[1].type == SyntaxNode.IDENTIFIER){
            // if true store the node in identifierNode.
            SyntaxNode identifierNode = declareNode.children[1];

            // creates a new symbol for the symbol table.
            Symbol symbol = new Symbol(identifierType, identifierNode.token, identifierNode.lineNumber, identifierNode.characterOffset);

            addIdentifier(symbol); // calls addIdentifier passing in the symbol.
        } else {
            // otherwise the second child is an assign node which is stored in assignNode.
            SyntaxNode assignNode = declareNode.children[1];
            String identifierName = assignNode.children[0].token; // gets the typename of the identifier being assigned.

            // creates a new symbol for the symbol table.
            Symbol symbol = new Symbol(identifierType, identifierName, assignNode.lineNumber, assignNode.characterOffset);

            addIdentifier(symbol); // calls addIdentifier passing in the symbol.
            parseAssign(assignNode); // parses the assignNode.
        }
    }

    public void parseAssign(SyntaxNode assignNode){
        // <assignment> ::= identifier = <expression>
        // <assignment> ::= identifier = string

        String identifierName = assignNode.children[0].token; // stores the identifierName childNode in identifier.
        Symbol symbol = findAllScopeSymbol(identifierName); // finds the symbol related to the identifier.

        // checks if symbol is not null.
        if (symbol != null){
            String symbolType = symbol.getType();
            if (!((Objects.equals(symbolType, "int") && Objects.equals(getType(assignNode.children[1]), "bool"))
                    || Objects.equals(symbolType, getType(assignNode.children[1])))
                    && getType(assignNode.children[1]) != null){
                semanticErrors.add(new SemanticError(SemanticError.TYPE_MISMATCH, assignNode.lineNumber, assignNode.characterOffset, "Type mismatch for assign."));
            }
        } else {
            // otherwise symbol is null meaning it hasn't been declared.
            // this is an error since an undeclared symbol cannot be assigned.
            // add error to semanticErrors.
            semanticErrors.add(new SemanticError(SemanticError.UNDECLARED_VARIABLE, assignNode.lineNumber, assignNode.characterOffset, "Assigning undeclared variable."));
        }

        // checks if the second child is type EXPRESSION and parses expression if true.
        if (assignNode.children[1].type == SyntaxNode.EXPRESSION){
            parseExpression(assignNode.children[1]);
        }
    }

    public void parseExpression(SyntaxNode expressionNode){
        // gets the childNode of the expressionNode.
        SyntaxNode childNode = expressionNode.children[0];

        if (childNode.type == SyntaxNode.COMPARISON){
            // if the child is a comparisonNode then parse expressions of the comparisonNode.
            parseExpression(childNode.children[0]);
            parseExpression(childNode.children[1]);
        } else {
            // otherwise the childNode is a math node so parse math.
            parseMath(childNode);
        }
    }

    public void parseMath(SyntaxNode mathNode){
        // if the mathNode is length 1 then its child is a term node so parseTerm.
        if (mathNode.children.length == 1){
            parseTerm(mathNode.children[0]);
        } else {
            // otherwise it is an operation so parseMath on the first child and parseTerm on the second.
            parseMath(mathNode.children[0]);
            parseTerm(mathNode.children[1]);
        }
    }

    public void parseTerm(SyntaxNode termNode){
        // if the termNode is length 1 then its child is a factor node so parseFactor.
        if (termNode.children.length == 1){
            parseFactor(termNode.children[0]);
        } else {
            // otherwise it is an operation so parseTerm on the first child and parseFactor on the second.
            parseTerm(termNode.children[0]);
            parseFactor(termNode.children[1]);
        }
    }

    public void parseFactor(SyntaxNode factorNode){
        SyntaxNode childNode = factorNode.children[0]; // gets the childNode of the factorNode.

        // checks if the childNode is an IDENTIFIER type.
        if (childNode.type == SyntaxNode.IDENTIFIER){
            // if true then check if the childNode's type is null.
            if (getType(childNode) == null){
                // this only happens if the identifier is not declared in the scope so add error to semanticErrors.
                semanticErrors.add(new SemanticError(SemanticError.UNDECLARED_VARIABLE, childNode.lineNumber, childNode.characterOffset, "Undeclared variable in expression."));
            }
        } else if (childNode.type == SyntaxNode.EXPRESSION){
            // if the childNode is an expression then parseExpression.
            parseExpression(childNode);
        }
    }

    public void parseScope(SyntaxNode scope){
        scopeStack.push(new HashMap<>()); // creates a hashmap representing a new scope and pushes onto the scopeStack.
        parseStatementList(scope.children[0]); // parses the statementList child node.
        scopeStack.pop(); // pops the scope of the scopeStack since it ends.
    }

    public String getType(SyntaxNode node){
        if (node.type == SyntaxNode.IDENTIFIER){
            // if the node type is IDENTIFIER then check it exists in the scopeStack.
            Symbol symbol = findAllScopeSymbol(node.token);

            // if the symbol is not null then return its type otherwise return null.
            if (symbol != null){
                return symbol.getType();
            } else {
                return null;
            }
        } else if (node.type == SyntaxNode.STRING){
            // return "string" if the type is STRING.
            return "string";
        } else if (node.type == SyntaxNode.NUMBER){
            // return "int" if the type is NUMBER.
            return "int";
        } else if (node.type == SyntaxNode.COMPARISON){
            // if the type is COMPARISON then checks the comparison types are valid and returns "bool".
            checkComparisonTypes(node);
            return "bool";
        } else if (node.type == SyntaxNode.FACTOR
                || node.type == SyntaxNode.TERM
                || node.type == SyntaxNode.MATH
                || node.type == SyntaxNode.EXPRESSION){
            // if the type is FACTOR, TERM, MATH or EXPRESSION then return the type of its child node.
            return getType(node.children[0]);
        } else if (node.type == SyntaxNode.TERM_MULTIPLY || node.type == SyntaxNode.TERM_DIVIDE
                || node.type == SyntaxNode.MATH_ADD || node.type == SyntaxNode.MATH_SUBTRACT){
            // if the type is an operation type then check operations are valid and return "int".
            checkOperationTypes(node);
            return "int";
        }
        return null; // otherwise return null.
    }

    public void checkComparisonTypes(SyntaxNode comparisonNode){
        // <comparison> ::= ( <expression> , <expression> )

        // gets reference to the types of the children nodes.
        String firstExpressionType = getType(comparisonNode.children[0]);
        String secondExpressionType = getType(comparisonNode.children[1]);

        // checks if the types are not equal.
        if (!Objects.equals(firstExpressionType, secondExpressionType)){
            // if they are not then add error to semanticErrors.
            semanticErrors.add(new SemanticError(SemanticError.TYPE_MISMATCH, comparisonNode.lineNumber, comparisonNode.characterOffset, "Type mismatch for comparison."));
        };
    }

    public void checkOperationTypes(SyntaxNode operationNode){
        // checks if the child nodes of the operationNode are both not of type "int".
        if (!(Objects.equals(getType(operationNode.children[0]), "int")
                && Objects.equals(getType(operationNode.children[1]), "int"))){
            // if one or both of them are not then add error to semanticErrors.
            semanticErrors.add(new SemanticError(SemanticError.TYPE_MISMATCH, operationNode.lineNumber, operationNode.characterOffset, "Type mismatch for operation."));
        }
    }

    public void addIdentifier(Symbol symbol){
        // checks if the symbol exists in the scopeStack.
        if (findAllScopeSymbol(symbol.getIdentifier()) != null){
            // if it does exist then add error to semanticErrors.
            semanticErrors.add(new SemanticError(SemanticError.UNDECLARED_VARIABLE, symbol.getLineNumber(), symbol.getCharacterOffset(), "Expected existing symbol."));
        } else {
            // otherwise add the symbol to the symbolTable.
            // and add symbol to the scopeStack in the latest scope.
            symbolTable.add(symbol);
            scopeStack.peek().put(symbol.getIdentifier(), symbolTable.size() - 1);
        }
    }

    public Symbol findScopeSymbol(Map<String, Integer> scope, String identifier){
        Symbol targetSymbol = null; // sets the targetSymbol to null.

        // loops through the symbols in the scope.
        for (String symbol : scope.keySet()){
            // if the symbol exists then get index and set targetSymbol.
            if (Objects.equals(symbol, identifier)){
                int symbolIndex = scope.get(symbol);
                targetSymbol = symbolTable.get(symbolIndex);
                break;
            }
        }
        return targetSymbol; // return targetSymbol.
    }

    public Symbol findAllScopeSymbol(String identifier){
        Symbol targetSymbol = null; // sets the targetSymbol to null.

        // loops through the all the scopes in the scopeStack.
        for (Map<String, Integer> scope : scopeStack){
            Symbol symbol = findScopeSymbol(scope, identifier); // finds symbol in current scope.

            // checks if the symbol was found.
            if (symbol != null){
                // if it was then set targetSymbol to symbol and break loop.
                targetSymbol = symbol;
                break;
            }
        }
        return targetSymbol; // returns targetSymbol.
    }

    public boolean isErrors(){
        return !semanticErrors.isEmpty(); // returns true if semanticErrors is not empty.
    }

    public List<SemanticError> getErrors(){
        return semanticErrors; // returns the semanticErrors.
    }
}
