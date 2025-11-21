import java.util.*;

public class SyntaxHelper {
    LexToken[] lexTokens; // stores the lexTokens.
    List<SyntaxError> syntaxErrors; // stores the lexErrors.
    int position; // stores the position in the lexToken array.

    public SyntaxHelper(LexToken[] lexTokens){
        this.lexTokens = lexTokens; // iniailises the lexTokens.
        this.syntaxErrors = new ArrayList<>(); // initialises the syntaxErrors.
        this.position = 0;
    }

    public SyntaxNode parseProgram(){
        // <program> ::= <statementList>

        // initialises the programNode.
        SyntaxNode programNode = new SyntaxNode(SyntaxNode.PROGRAM, line(), offset(), "");

        // parses statementList and adds to the programNode.
        addChildNode(programNode, parseStatementList());
        return programNode; // returns the programNode.
    }

    public SyntaxNode parseStatementList(){
        SyntaxNode statementListNode = new SyntaxNode(SyntaxNode.STATEMENT_LIST, line(), offset(), "");
        int i = 0;
        while (true){
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                break;
            } else if (compareToken("}", currentToken().token)){
                advance();
                break;
            } else if (compareToken(";", currentToken().token)){
                // checks if the current token is ';'.

                // checks the next token is '}'
                if (compareToken("}", nextToken().token)){
                    // if the next token is '}' then return the statementList since the end of a scope has been reached.
                    advance();
                    break;
                } else {
                    // if the next token is not '}' then advance to the next token.
                    advance();
                }
            }

            // parses statement and stores it in statementNode.
            SyntaxNode statementNode = parseStatement();

            // checks if the statement is not empty.
            if (statementNode.children.length > 0){
                // if the statement is not empty then add it to statementListNode.
                addChildNode(statementListNode, statementNode);
            }
            i++;
        }
        return statementListNode;
    }

    public SyntaxNode parseStatement(){
        // <statement> ::= <declaration> | <assignment> | <if> | <while>

        // initialises the statementNode.
        SyntaxNode statementNode = new SyntaxNode(SyntaxNode.STATEMENT, line(), offset(), "");

        // checks if the token is either "if" or "while" or if the type is type name or identifier.
        if (compareToken("if", currentToken().token)){
            // if the token is "if" then parse if and add to statementNode.
            // Enter parseIf() with the current token being "if" keyword.
            addChildNode(statementNode, parseIf());
        } else if (compareToken("while", currentToken().token)){
            // if the token is "while" then parse while and add to statementNode.
            // Enter parseWhile() with the current token being "while" keyword.
            addChildNode(statementNode, parseWhile());
        } else if (currentToken().type == LexToken.TYPE_NAME){
            // if the type is type name then parse declare and add to statementNode.
            // Enter parseDeclare() with the current token being a type name.
            addChildNode(statementNode, parseDeclare());
        } else if (currentToken().type == LexToken.IDENTIFIER){
            // if the type is identifier then parse assign and add to statementNode.
            // Enter parseAssign() with the current token being an identifier.
            addChildNode(statementNode, parseAssign());
        } else {
            // if the next token does not meet these conditions then call unknown error.
            unknownSyntaxError("Expected <declaration>, <expression>, <if> or <while> but received unknown syntax.");
        }

        // checks if the nextToken is ';'.
        if (compareToken(";", nextToken().token)){
            // if the token is ';' then advance token.
            advance();
        }
        return statementNode; // return statementNode.
    }

    public SyntaxNode parseIf(){
        // <if> ::= if ( <expression> ) <scope> | if ( <expression> ) <scope> else <scope>

        // initialises the ifNode.
        SyntaxNode ifNode = new SyntaxNode(SyntaxNode.IF, line(), offset(), "");

        // checks if there is no more tokens.
        if (endOfTokens()){
            // adds the end of tokens error.
            endOfTokenError();
        } else if (compareToken("(", nextToken().token)){
            // if the next token is '(' then advance token.
            advance();
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else {
                // if end of tokens is not reached then attempt to parse expression and add to ifNode.
                addChildNode(ifNode, parseExpression());
                // checks if there is no more tokens.
                if (endOfTokens()){
                    // adds the end of tokens error.
                    endOfTokenError();
                } else if (compareToken(")", nextToken().token)){
                    // if the next token is ')' then advance token.
                    advance();
                    // checks if there is no more tokens.
                    if (endOfTokens()){
                        // adds the end of tokens error.
                        endOfTokenError();
                    } else if (compareToken("{", nextToken().token)){
                        // if end of tokens is not reached then attempt to parse scope and add to ifNode.
                        advance();
                        // checks if there is no more tokens.
                        if (endOfTokens()){
                            // adds the end of tokens error.
                            endOfTokenError();
                        } else {
                            // if the end of tokens is not reached then parse scope and adds it to ifNode.
                            addChildNode(ifNode, parseScope());
                            // checks if the current token is 'else'.
                            if (compareToken("else", currentToken().token)){
                                // checks if there is no more tokens.
                                if (endOfTokens()){
                                    // adds the end of tokens error.
                                    endOfTokenError();
                                } else {
                                    // if the end of tokens is not reached then advance token.
                                    advance();
                                    // checks if there is no more tokens.
                                    if (endOfTokens()){
                                        // adds the end of tokens error.
                                        endOfTokenError();
                                    } else {
                                        // if the end of tokens is not reached then parse scope and adds it to ifNode.
                                        addChildNode(ifNode, parseScope());
                                    }
                                }
                            }
                        }
                    } else {
                        // if the token is not '{' then call unknown error.
                        unknownSyntaxError("Expected '{' but received unknown syntax.");
                    }
                } else {
                    // if the token is not ')' then call unknown error.
                    unknownSyntaxError("Expected ')' but received unknown syntax.");
                }
            }
        } else {
            // if the token is not '(' then call unknown error.
            unknownSyntaxError("Expected '(' but received unknown syntax.");
        }
        return ifNode; // returns ifNode.
    }

    public SyntaxNode parseWhile(){
        // <while> ::= while ( <expression> ) <scope>
        SyntaxNode whileNode = new SyntaxNode(SyntaxNode.WHILE, line(), offset(), "");

        // checks if there is no more tokens.
        if (endOfTokens()){
            // adds the end of tokens error.
            endOfTokenError();
        } else if (compareToken("(", nextToken().token)){
            // if the next token is '(' then advance token.
            advance();
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else {
                // if end of tokens is not reached then attempt to parse expression and add to whileNode.
                addChildNode(whileNode, parseExpression());
                // checks if there is no more tokens.
                if (endOfTokens()){
                    // adds the end of tokens error.
                    endOfTokenError();
                } else if (compareToken(")", nextToken().token)){
                    // if the next token is ')' then advance token.
                    advance();
                    // checks if there is no more tokens.
                    if (endOfTokens()){
                        // adds the end of tokens error.
                        endOfTokenError();
                    } else if (compareToken("{", nextToken().token)){
                        // if end of tokens is not reached then attempt to parse scope and add to whileNode.
                        advance();
                        // checks if there is no more tokens.
                        if (endOfTokens()){
                            // adds the end of tokens error.
                            endOfTokenError();
                        } else {
                            // if the end of tokens is not reached then parse scope and adds it to whileNode.
                            addChildNode(whileNode, parseScope());
                        }
                    } else {
                        // if the token is not '{' then call unknown error.
                        unknownSyntaxError("Expected '{' but received unknown syntax.");
                    }
                } else {
                    // if the token is not ')' then call unknown error.
                    unknownSyntaxError("Expected ')' but received unknown syntax.");
                }
            }
        } else {
            // if the token is not '(' then call unknown error.
            unknownSyntaxError("Expected '(' but received unknown syntax.");
        }
        return whileNode; // returns whileNode.
    }

    public SyntaxNode parseScope(){
        // <scope> ::= { <statementList> }

        // initialises the scopeNode.
        SyntaxNode scopeNode = new SyntaxNode(SyntaxNode.SCOPE, line(), offset(), "");

        // advance token past the '{' token.
        advance();

        // check if the current token is '}'.
        if (compareToken("}", currentToken().token)){
            // if the token is '}' then advance token.
            advance();
        } else {
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else {
                // if the end of tokens is not reached then parse statementList and add to scopeNode.
                addChildNode(scopeNode, parseStatementList());
                // checks if there is no more tokens.
                if (!compareToken("}", currentToken().token)) {
                    // if the token is not '}' then call unknown error.
                    unknownSyntaxError("Expected } but received unknown syntax.");
                }
            }
        }
        return scopeNode; // returns scopeNode.
    }

    public SyntaxNode parseDeclare(){
        // <declaration> --- typeName identifier
        // <declaration> --- typeName <assignment>

        // current token is a type name.
        // creates a declareNode for the children nodes to be stored.
        SyntaxNode declareNode = new SyntaxNode(SyntaxNode.DECLARE, line(), offset(), "");

        // parses the typeName and adds it to the declareNode.
        addChildNode(declareNode, parseTerminal(SyntaxNode.TYPE_NAME));

        // in both cases of the declareNode, the next token is an identifier.
        advance(); // advances the token so that the current token should be an identifier.

        // checks the current token is now an identifier.
        if (compareType(LexToken.IDENTIFIER, currentToken().type)){
            // checks the next token is the '=' token.
            if (compareToken("=", nextToken().token)){
                // adds assign to declareNode.
                // enters parseAssign() with current token as identifier.
                addChildNode(declareNode, parseAssign());
            } else {
                // if the token is not '=' then adds identifier to declareNode.
                addChildNode(declareNode, parseTerminal(SyntaxNode.IDENTIFIER));
            }
        } else {
            // if the token was not an identifier which we expected then call unknownSyntaxError.
            unknownSyntaxError("Expected identifier but received unknown syntax.");
        }
        return declareNode; // returns the declareNode.
    }

    public SyntaxNode parseAssign(){
        // <assignment> ::= identifier = <expression> | identifier = string

        // initialises the assignNode.
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.ASSIGN, line(), offset(), "");

        // the current token when entering parseAssign() is an identifier.
        // adds the identifier to assignNode.
        addChildNode(assignNode, parseTerminal(SyntaxNode.IDENTIFIER));

        // checks if the next token is '='.
        if (compareToken("=", nextToken().token)){
            advance(); // advances to the next token.
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else if (compareType(LexToken.LITERAL_STRING, nextToken().type)) {
                // checks if the next token is a string and adds it to assignNode if it is.
                advance();
                addChildNode(assignNode, parseTerminal(SyntaxNode.STRING));
            } else if (compareType(LexToken.LITERAL_NUMBER, nextToken().type)
                    || compareType(LexToken.IDENTIFIER, nextToken().type)
                    || compareToken("(", nextToken().token)
                    || compareToken("==", nextToken().token)){
                // checks if the type is either a number or identifier since an expression can start with these types.
                // checks if the next token is either a '(' and '==' since an expression can start with these tokens.
                // if it does then add an expression to the assignNode.
                // enters parseExpression() with current token as '='.
                addChildNode(assignNode, parseExpression());
            } else {
                // if the syntax is not the expected syntax then add an unknown error.
                unknownSyntaxError("Unknown syntax, expected <Expression> or string");
            }
        }
        return assignNode; // returns the assignNode.
    }

    public SyntaxNode parseExpression(){
        // <expression> ::= <math>
        // <expression> ::= <comparison>

        // initialises the expressionNode.
        SyntaxNode expressionNode = new SyntaxNode(SyntaxNode.EXPRESSION, line(), offset(), "");
        // checks if the next token is '=='.
        if (compareToken("==", nextToken().token)){
            // if the token is '==' then add comparison to the expressionNode.
            addChildNode(expressionNode, parseComparison());
        } else if (compareType(LexToken.LITERAL_NUMBER, nextToken().type)
                || compareType(LexToken.IDENTIFIER, nextToken().type)
                || compareToken("(", nextToken().token)){
            // checks if the next token is '(' or the next type is a number or identifier.
            // if true then add math to the expressionNode.
            addChildNode(expressionNode, parseMath());
        } else {
            // if the syntax is not the expected syntax then add an unknown error.
            unknownSyntaxError("Expected <math> or <expression>, received unknown syntax.");
        }
        return expressionNode; // returns expressionNode.
    }

    public SyntaxNode parseMath(){
        // <math> ::= <term> || <math> + <term> || <math> - <term>

        // initialises the mathNode.
        SyntaxNode mathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
        // parse term and store it in the termNode.
        SyntaxNode termNode = parseTerm();

        // checks if the next token is '+'.
        if (compareToken("+", nextToken().token)) {
            // if the token is '+' then create a tempMathNode to add the termNode to.
            SyntaxNode tempMathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
            addChildNode(tempMathNode, termNode);

            // then parse the math addition operation with the tempMathNode and add it to the mathNode.
            // return parseMathOperation with tempMathNode as input node and subtract as the operation.
            return parseMathOperation(tempMathNode, SyntaxNode.MATH_ADD);
        } else if (compareToken("-", nextToken().token)) {
            // if the token is '-' then create a tempMathNode to add the termNode to.
            SyntaxNode tempMathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
            addChildNode(tempMathNode, termNode);

            // then parse the math subtraction operation with the tempMathNode and add it to the mathNode.
            // return parseMathOperation with tempMathNode as input node and subtract as the operation.
            return parseMathOperation(tempMathNode, SyntaxNode.MATH_SUBTRACT);
        } else {
            // if there is no operation then add termNode to mathNode.
            addChildNode(mathNode, termNode);
        }
        return mathNode; // return mathNode.
    }

    public SyntaxNode parseMathOperation(SyntaxNode mathNode, int operationType){
        // initialises the mathOperationNode.
        SyntaxNode mathOperationNode = new SyntaxNode(operationType, line(), offset(), "");

        // current token is '+' at start of the function.
        advance(); // advances past '+' token.

        // checks if there is no more tokens.
        if (endOfTokens()){
            // adds the end of tokens error.
            endOfTokenError();
        } else {
            // parses term and stores in termNode.
            SyntaxNode termNode = parseTerm();

            // checks for nested operations.
            // checks if the next token is '+'.
            if (compareToken("+", nextToken().token)){
                // if the token is '+' then create innerMathAddNode to store the mathNode and termNode.
                SyntaxNode innerMathAddNode = new SyntaxNode(operationType, line(), offset(), "");
                addChildNode(innerMathAddNode, mathNode);
                addChildNode(innerMathAddNode, termNode);
                // then parse the math addition operation with innerMathAddNode.
                return parseMathOperation(innerMathAddNode, SyntaxNode.MATH_ADD);
            } else if (compareToken("-", nextToken().token)){
                // if the token is '-' then create innerMathSubtractNode to store the mathNode and termNode.
                SyntaxNode innerMathSubtractNode = new SyntaxNode(operationType, line(), offset(), "");
                addChildNode(innerMathSubtractNode, mathNode);
                addChildNode(innerMathSubtractNode, termNode);
                // then parse the math subtraction operation with innerMathSubtractNode.
                return parseMathOperation(innerMathSubtractNode, SyntaxNode.MATH_SUBTRACT);
            } else {
                // if there is no nested operations then add the mathNode and termNode to mathOperationNode
                addChildNode(mathOperationNode, mathNode);
                addChildNode(mathOperationNode, termNode);
            }
        }
        return mathOperationNode; // return mathOperationNode.
    }

    public SyntaxNode parseTermOperation(SyntaxNode termNode, int operationType){
        // initialises the termOperationNode.
        SyntaxNode termOperationNode = new SyntaxNode(operationType, line(), offset(), "");

        // Adds the termNode to the termOperationNode.
        advance(); // advances the token.

        // checks if there is no more tokens.
        if (endOfTokens()){
            // adds the end of tokens error.
            endOfTokenError();
        } else {
            SyntaxNode factorNode = parseFactor();
            // checks for nested operations.
            // checks if the next token is '*' or '/'.
            if (compareToken("*", nextToken().token)){
                // if the token is '*' then create innerTermMultiplyNode to store the termNode and factorNode.
                SyntaxNode innerTermMultiplyNode = new SyntaxNode(operationType, line(), offset(), "");
                addChildNode(innerTermMultiplyNode, termNode);
                addChildNode(innerTermMultiplyNode, factorNode);

                // then parse the math multiplication operation with innerTermMultiplyNode.
                return parseTermOperation(innerTermMultiplyNode, SyntaxNode.TERM_MULTIPLY);
            } else if (compareToken("/", nextToken().token)){
                // if the token is '/' then create innerTermDivideNode to store the termNode and factorNode.
                SyntaxNode innerTermDivideNode = new SyntaxNode(operationType, line(), offset(), "");
                addChildNode(innerTermDivideNode, termNode);
                addChildNode(innerTermDivideNode, factorNode);

                // then parse the math division operation with innerTermDivideNode.
                return parseTermOperation(innerTermDivideNode, SyntaxNode.TERM_DIVIDE);
            } else {
                // if there is no nested operations then add the termNode and factorNode to termOperationNode.
                addChildNode(termOperationNode, termNode);
                addChildNode(termOperationNode, factorNode);
            }
        }
        return termOperationNode; // return termOperationNode.
    }

    public SyntaxNode parseComparison(){
        // <comparison> ::= ( <expression> , <expression> )

        // initialises the comparisonNode.
        SyntaxNode comparisonNode = new SyntaxNode(SyntaxNode.COMPARISON, 0, 0, "");
        advance(); // advances the token to '=='.

        // checks if there is no more tokens.
        if (endOfTokens()){
            // adds the end of tokens error.
            endOfTokenError();
        } else if (compareToken("(", nextToken().token)){
            // checks if the next token is '('.
            advance(); // advances the token.

            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else {
                // if the end of the tokens is not reached then parse expression and add to comparisonNode.
                addChildNode(comparisonNode, parseExpression());

                // checks if there is no more tokens.
                if (endOfTokens()){
                    // adds the end of tokens error.
                    endOfTokenError();
                } else if (compareToken(",", nextToken().token)){
                    // checks if the next token is ','.
                    advance(); // advances the token.

                    // checks if there is no more tokens.
                    if (endOfTokens()){
                        // adds the end of tokens error.
                        endOfTokenError();
                    } else {
                        // if the end of the tokens is not reached then parse expression and add to comparisonNode.
                        addChildNode(comparisonNode, parseExpression());

                        // checks if there is no more tokens.
                        if (endOfTokens()){
                            // adds the end of tokens error.
                            endOfTokenError();
                        } else if (compareToken(")", nextToken().token)){
                            advance();
                            // checks if the next token is ')'.
                            return comparisonNode; // returns comparisonNode.
                        } else {
                            // if token is not ')' then call unknownSyntaxError().
                            unknownSyntaxError("Expected ')', received unknown syntax.");
                        }
                    }
                } else {
                    // if token is not ',' then call unknownSyntaxError().
                    unknownSyntaxError("Expected ',', received unknown syntax.");
                }
            }
        } else {
            // if token is not '(' then call unknownSyntaxError().
            unknownSyntaxError("Expected '(', received unknown syntax.");
        }
        return comparisonNode; // returns comparisonNode.
    }

    public SyntaxNode parseFactor(){
        // <factor> ::= number | identifier | ( <expression> )

        // creates a container for the factorNode.
        SyntaxNode factorNode;
        // checks if the next node is an identifier or a number.
        if (compareType(LexToken.IDENTIFIER, nextToken().type)
                || compareType(LexToken.LITERAL_NUMBER, nextToken().type)){
            advance(); // advances the token.

            // initialises the factorNode and adds the current token to the current.
            factorNode = new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), "");

            if (compareType(LexToken.IDENTIFIER, nextToken().type)){
                addChildNode(factorNode, parseTerminal(SyntaxNode.IDENTIFIER));
            } else {
                addChildNode(factorNode, parseTerminal(SyntaxNode.NUMBER));
            }
        } else if (compareToken("(", nextToken().token)) {
            // initialises the factorNode.
            factorNode = new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), "");
            advance(); // advances the token.

            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else {
                // if the end of the tokens isn't reached then add parseExpression() to factorNode.
                addChildNode(factorNode, parseExpression());
                if (endOfTokens()){
                    endOfTokenError();
                } else if (compareToken(")", nextToken().token)){
                    advance();
                }
            }
        } else {
            // if none of the conditions are met for the factor. then create an empty factor.
            factorNode = new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), "");

            // call unknownSyntaxError().
            unknownSyntaxError("Expected identifier, number or <expression>");
        }
        return factorNode; // return factorNode.
    }

    public SyntaxNode parseTerm(){
        // <term> = <factor> | <term> * <factor> | <term> / <factor>

        // initialises the termNode.
        SyntaxNode termNode = new SyntaxNode(SyntaxNode.TERM, line(), offset(), "");

        // parses the factor and creates a reference for it.
        SyntaxNode factorNode = parseFactor();

        // checks if the token after the factor is the '*' or '/' token.
        if (compareToken("*", nextToken().token)) {
            SyntaxNode tempTermNode = new SyntaxNode(SyntaxNode.TERM, line(), offset(), "");
            addChildNode(tempTermNode, factorNode);
            // if the token is '*' then returns parseTermMultiply.
            return parseTermOperation(tempTermNode, SyntaxNode.TERM_MULTIPLY);
        } else if (compareToken("/", nextToken().token)) {
            SyntaxNode tempTermNode = new SyntaxNode(SyntaxNode.TERM, line(), offset(), "");
            addChildNode(tempTermNode, factorNode);
            // if the token is '/' then returns parseTermDivide.
            return parseTermOperation(tempTermNode, SyntaxNode.TERM_DIVIDE);
        } else {
            // if the token is not either '*' or '/' then add the factorNode to the termNode.
            addChildNode(termNode, factorNode);
        }
        return termNode; // return termNode.
    }

    public SyntaxNode parseTerminal(int type){
        // returns a new SyntaxNode for the type given.
        return new SyntaxNode(type, line(), offset(), currentToken().token);
    }

    public void advance(){
        this.position++; // increments the position.
    }

    public boolean endOfTokens(){
        return nextToken() == null; // returns true if next token is null and false otherwise.
    }

    public int line(){
        return currentToken().lineNumber; // returns the lineNumber of the current token.
    }

    public int offset(){
        return currentToken().characterOffset; // returns the characterOffset of the current token.
    }

    public LexToken currentToken(){
        return lexTokens[position]; // returns the current lexToken.
    }

    public LexToken nextToken(){
        if (position == lexTokens.length - 1) return null;
        return lexTokens[position + 1]; // returns the next lexToken.
    }

    public boolean compareType(int expectedType, int actualType){
        return expectedType == actualType; // returns true of the expected type is equal to the actual type.
    }

    public boolean compareToken(String expectedToken, String actualType){
        return Objects.equals(expectedToken, actualType); // returns true if the expectedToken is equal to the actual type.
    }

    public void addChildNode(SyntaxNode parent, SyntaxNode child) {
        // creates a copy of the current children array with a length of +1 and stores it as the new children array.
        parent.children = Arrays.copyOf(parent.children, parent.children.length + 1);
        // adds the childNode to the end of the children array.
        parent.children[parent.children.length-1] = child;
    }

    public void endOfTokenError(){
        // adds a new error to syntaxErrors of type OTHER for the end of tokens.
        syntaxErrors.add(new SyntaxError(SyntaxError.OTHER,
                lexTokens[lexTokens.length - 1].lineNumber,
                lexTokens[lexTokens.length - 1].characterOffset,
                "End of tokens."));
    }

    public void unknownSyntaxError(String message){
        // adds a new error to syntaxErrors of type UNKNOWN_SYNTAX for unknown syntax.
        syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), message));
    }
}
