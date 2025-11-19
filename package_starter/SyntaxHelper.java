import java.util.*;

public class SyntaxHelper {
    LexToken[] lexTokens;
    List<SyntaxError> syntaxErrors;
    int position;

    public SyntaxHelper(LexToken[] lexTokens){
        this.lexTokens = lexTokens;
        this.syntaxErrors = new ArrayList<>();
        this.position = 0;
    }

    public SyntaxNode parseProgram(){
        // <program> ::= <statementList>
        SyntaxNode programNode = new SyntaxNode(SyntaxNode.PROGRAM, line(), offset(), "");
        programNode.addChild(parseStatementList());
        return programNode;
    }

    public SyntaxNode parseStatementList(){
        // <statementList> ::= <statement> ;
        // <statementList> ::= <statement> ; <statementList> ;

        SyntaxNode statementListNode = new SyntaxNode(SyntaxNode.STATEMENT_LIST, line(), offset(), "");
        String token = currentToken().token;

        if (endOfTokens()) {
            endOfTokenError();
        } else {
            SyntaxNode statementNode = parseStatement();
            if (statementNode.children.length > 0){
                statementListNode.addChild(statementNode);
                if (endOfTokens()){
                    endOfTokenError();
                } else if (compareToken(";", nextToken().token)){
                    advance();
                    if (endOfTokens()){
                        endOfTokenError();
                    } else {
                        statementListNode.addChild(parseStatementList());
                        if (endOfTokens()){
                            endOfTokenError();
                        } else if (compareToken(";", nextToken().token)){
                            advance();
                        } else {
                            unknownSyntaxError("Expected ';' but received unknown syntax.");
                        }
                    }
                } else {
                    unknownSyntaxError("Expected ';' but received unknown syntax.");
                }
            }
        }
        return statementListNode;
    }

    public SyntaxNode parseStatement(){
        // <statement> ::= <declaration> | <assignment> | <if> | <while>
        SyntaxNode statementNode = new SyntaxNode(SyntaxNode.STATEMENT, line(), offset(), "");
        if (compareToken("if", currentToken().token)){
            // Enter parseIf() with the current token being "if" keyword.
            statementNode.addChild(parseIf());
        } else if (compareToken("while", currentToken().token)){
            // Enter parseWhile() with the current token being "while" keyword.
            statementNode.addChild(parseWhile());
        } else if (currentToken().type == LexToken.TYPE_NAME){
            // Enter parseDeclare() with the current token being a type name.
            statementNode.addChild(parseDeclare());
        } else if (currentToken().type == LexToken.IDENTIFIER){
            // Enter parseAssign() with the current token being an identifier.
            statementNode.addChild(parseAssign());
        } else {
            unknownSyntaxError("Expected <declaration>, <expression>, <if> or <while> but received unknown syntax.");
        }
        return statementNode;
    }

    public SyntaxNode parseIf(){
        // <if> ::= if ( <expression> ) <scope> | if ( <expression> ) <scope> else <scope>

        SyntaxNode ifNode = new SyntaxNode(SyntaxNode.IF, line(), offset(), "");
        advance();
        String token;
        if (compareToken("(", currentToken().token) && !endOfTokens()){
            advance();
            token = currentToken().token;
            ifNode.addChild(parseExpression());
            token = currentToken().token;
            if (compareToken(")", currentToken().token) && !endOfTokens()){
                advance();
                token = currentToken().token;
                ifNode.addChild(parseScope());
                token = currentToken().token;
                if (compareToken("else", currentToken().token) && !endOfTokens()){
                    advance();
                    token = currentToken().token;
                    ifNode.addChild(parseScope());
                    token = currentToken().token;
                }
            } else if (!endOfTokens()){
                unknownSyntaxError("Expected ')' but received unknown syntax");
            }
            return ifNode;
        }
        endOfTokenError();
        return ifNode;
    }

    public SyntaxNode parseWhile(){
        // <while> ::= while ( <expression> ) <scope>
        SyntaxNode whileNode = new SyntaxNode(SyntaxNode.WHILE, line(), offset(), "");
        advance();
        advance();
        whileNode.addChild(parseExpression());
        advance();
        whileNode.addChild(parseScope());
        return whileNode;
    }

    public SyntaxNode parseScope(){
        // <scope> ::= { <statementList> }
        SyntaxNode scopeNode = new SyntaxNode(SyntaxNode.SCOPE, line(), offset(), "");
        advance();
        String token = currentToken().token;
        if (!endOfTokens()){
            scopeNode.addChild(parseStatementList());
            token = currentToken().token;
            if (compareToken("}", currentToken().token)) {
                advance();
            }
        } else {
            //add error.
        }

        return scopeNode;
    }

    public SyntaxNode parseDeclare(){
        // <declaration> --- typeName identifier
        // <declaration> --- typeName <assignment>

        // current token is a type name.
        // creates a declareNode for the children nodes to be stored.
        SyntaxNode declareNode = new SyntaxNode(SyntaxNode.DECLARE, line(), offset(), "");

        // parses the typeName and adds it to the declareNode.
        declareNode.addChild(parseTerminal(SyntaxNode.TYPE_NAME));

        // in both cases of the declareNode, the next token is an identifier.
        advance(); // advances the token so that the current token should be an identifier.

        // checks the current token is now an identifier.
        if (compareType(LexToken.IDENTIFIER, currentToken().type)){
            // checks the next token is the '=' token.
            if (compareToken("=", nextToken().token)){
                // adds assign to declareNode.
                // enters parseAssign() with current token as identifier.
                declareNode.addChild(parseAssign());
            } else {
                // if the token is not '=' then adds identifier to declareNode.
                declareNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));
            }
        } else {
            // if the token was not an identifier which we expected then call unknownSyntaxError.
            unknownSyntaxError("Expected identifier but received unknown syntax.");
        }

        // returns the declareNode.
        return declareNode;
    }

    public SyntaxNode parseAssign(){
        // <assignment> ::= identifier = <expression> | identifier = string

        // initialises the assignNode.
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.ASSIGN, line(), offset(), "");

        // the current token when entering parseAssign() is an identifier.
        // adds the identifier to assignNode.
        assignNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));

        // checks if the next token is '='.
        if (compareToken("=", nextToken().token)){
            advance(); // advances to the next token.
            // checks if there is no more tokens.
            if (endOfTokens()){
                // adds the end of tokens error.
                endOfTokenError();
            } else if (compareType(LexToken.LITERAL_STRING, nextToken().type)) {
                // checks if the next token is a string and adds it to assignNode if it is.
                assignNode.addChild(parseTerminal(SyntaxNode.STRING));
            } else if (compareType(LexToken.LITERAL_NUMBER, nextToken().type)
                    || compareType(LexToken.IDENTIFIER, nextToken().type)
                    || compareToken("(", nextToken().token)
                    || compareToken("==", nextToken().token)){
                // checks if the type is either a number or identifier since an expression can start with these types.
                // checks if the next token is either a '(' and '==' since an expression can start with these tokens.
                // if it does then add an expression to the assignNode.
                // enters parseExpression() with current token as '='.
                assignNode.addChild(parseExpression());
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
            expressionNode.addChild(parseComparison());
        } else if (compareType(LexToken.LITERAL_NUMBER, nextToken().type)
                || compareType(LexToken.IDENTIFIER, nextToken().type)
                || compareToken("(", nextToken().token)){
            // checks if the next token is '(' or the next type is a number or identifier.
            // if true then add math to the expressionNode.
            expressionNode.addChild(parseMath());
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
        String token = currentToken().token;
        // parse term and store it in the termNode.
        SyntaxNode termNode = parseTerm();
        token = currentToken().token;
        // checks if the next token is '+'.
        if (compareToken("+", nextToken().token)) {
            // if the token is '+' then create a tempMathNode to add the termNode to.
            SyntaxNode tempMathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
            tempMathNode.addChild(termNode);

            // then parse the math addition operation with the tempMathNode and add it to the mathNode.
            // enter parseMathOperation() with current token '+'/.
            mathNode.addChild(parseMathOperation(tempMathNode, SyntaxNode.MATH_ADD));
        } else if (compareToken("-", nextToken().token)) {
            // if the token is '-' then create a tempMathNode to add the termNode to.
            SyntaxNode tempMathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
            tempMathNode.addChild(termNode);

            // then parse the math subtraction operation with the tempMathNode and add it to the mathNode.
            // enter parseMathOperation() with current token '-'/.
            mathNode.addChild(parseMathOperation(tempMathNode, SyntaxNode.MATH_SUBTRACT));
        } else {
            // if there is no operation then add termNode to mathNode.
            mathNode.addChild(termNode);
        }
        return mathNode; // return mathNode.
    }

    public SyntaxNode parseMathOperation(SyntaxNode mathNode, int operationType){
        // initialises the mathOperationNode.
        SyntaxNode mathOperationNode = new SyntaxNode(operationType, line(), offset(), "");

        // current token is '+' at start of the function.
        advance(); // advances past '+' token.

        // parse term and store it in the termNode.
        if (endOfTokens()) {
            endOfTokens();
        } else {
            SyntaxNode termNode = parseTerm();

            // checks for nested operations.
            // checks if the next token is '+'.
            if (compareToken("+", currentToken().token)){
                // if the token is '+' then create innerMathAddNode to store the mathNode and termNode.
                SyntaxNode innerMathAddNode = new SyntaxNode(operationType, line(), offset(), "");
                innerMathAddNode.addChild(mathNode);
                innerMathAddNode.addChild(termNode);

                // then parse the math addition operation with innerMathAddNode.
                return parseMathOperation(innerMathAddNode, SyntaxNode.MATH_ADD);
            } else if (compareToken("-", currentToken().token)){
                // if the token is '-' then create innerMathSubtractNode to store the mathNode and termNode.
                SyntaxNode innerMathSubtractNode = new SyntaxNode(operationType, line(), offset(), "");
                innerMathSubtractNode.addChild(mathNode);
                innerMathSubtractNode.addChild(termNode);

                // then parse the math subtraction operation with innerMathSubtractNode.
                return parseMathOperation(innerMathSubtractNode, SyntaxNode.MATH_SUBTRACT);
            } else {
                // if there is no nested operations then add the mathNode and termNode to mathOperationNode
                mathOperationNode.addChild(mathNode);
                mathOperationNode.addChild(termNode);

            }
        }
        return mathOperationNode; // return mathOperationNode.
    }

    public SyntaxNode parseTermOperation(SyntaxNode termNode, int operationType){
        // initialises the termOperationNode.
        SyntaxNode termOperationNode = new SyntaxNode(operationType, line(), offset(), "");

        // Adds the termNode to the termOperationNode.
        advance(); // advances the token.

        // parse term and store it in the termNode.
        if (endOfTokens()) {
            endOfTokens();
        } else {
            SyntaxNode factorNode = parseFactor();

            // checks for nested operations.
            // checks if the next token is '*' or '/'.
            if (compareToken("*", currentToken().token)){
                // if the token is '*' then create innerTermMultiplyNode to store the termNode and factorNode.
                SyntaxNode innerTermMultiplyNode = new SyntaxNode(operationType, line(), offset(), "");
                innerTermMultiplyNode.addChild(termNode);
                innerTermMultiplyNode.addChild(factorNode);

                // then parse the math multiplication operation with innerTermMultiplyNode.
                return parseTermOperation(innerTermMultiplyNode, SyntaxNode.TERM_MULTIPLY);
            } else if (compareToken("/", currentToken().token)){
                // if the token is '/' then create innerTermDivideNode to store the termNode and factorNode.
                SyntaxNode innerTermDivideNode = new SyntaxNode(operationType, line(), offset(), "");
                innerTermDivideNode.addChild(termNode);
                innerTermDivideNode.addChild(factorNode);

                // then parse the math division operation with innerTermDivideNode.
                return parseTermOperation(innerTermDivideNode, SyntaxNode.TERM_DIVIDE);
            } else {
                // if there is no nested operations then add the termNode and factorNode to termOperationNode.
                termOperationNode.addChild(termNode);
                termOperationNode.addChild(factorNode);
            }
        }
        return termOperationNode;
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
                comparisonNode.addChild(parseExpression());

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
                        comparisonNode.addChild(parseExpression());

                        // checks if there is no more tokens.
                        if (endOfTokens()){
                            // adds the end of tokens error.
                            endOfTokenError();
                        } else if (compareToken(")", nextToken().token)){
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
        String token = currentToken().token;
        // checks if the next node is an identifier or a number.
        if (compareType(LexToken.IDENTIFIER, nextToken().type)
                || compareType(LexToken.LITERAL_NUMBER, nextToken().type)){
            advance(); // advances the token.

            // initialises the factorNode and adds the current token to the current.
            factorNode = new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), "");

            if (compareType(LexToken.IDENTIFIER, nextToken().type)){
                factorNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));
            } else {
                factorNode.addChild(parseTerminal(SyntaxNode.NUMBER));
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
                factorNode.addChild(parseExpression());
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
            tempTermNode.addChild(factorNode);
            // if the token is '*' then returns parseTermMultiply.
            return parseTermOperation(tempTermNode, SyntaxNode.TERM_MULTIPLY);
        } else if (compareToken("/", nextToken().token)) {
            SyntaxNode tempTermNode = new SyntaxNode(SyntaxNode.TERM, line(), offset(), "");
            tempTermNode.addChild(tempTermNode);
            // if the token is '/' then returns parseTermDivide.
            return parseTermOperation(tempTermNode, SyntaxNode.TERM_DIVIDE);
        } else {
            // if the token is not either '*' or '/' then add the factorNode to the termNode.
            termNode.addChild(factorNode);
        }
        return termNode; // return termNode.
    }

    public SyntaxNode parseTerminal(int type){
        return new SyntaxNode(type, line(), offset(), currentToken().token);
    }

    public void advance(){
        this.position++;
    }

    public boolean endOfTokens(){
        return nextToken() == null;
    }

    public int line(){
        return currentToken().lineNumber;
    }

    public int offset(){
        return currentToken().characterOffset;
    }

    public int lastLine(){
        return lexTokens[lexTokens.length-1].lineNumber;
    }

    public int lastOffset(){
        LexToken lastToken = lexTokens[lexTokens.length-1];
        return lastToken.characterOffset + lastToken.token.length();
    }

    public LexToken currentToken(){
        if (position >= lexTokens.length) return null;
        return lexTokens[position];
    }

    public LexToken nextToken(){
        if (position >= lexTokens.length - 1) return null;
        return lexTokens[position + 1];
    }

    public boolean compareType(int expectedType, int realType){
        return expectedType == realType;
    }

    public boolean compareToken(String expectedToken, String realToken){
        return Objects.equals(expectedToken, realToken);
    }

    public void endOfTokenError(){
        syntaxErrors.add(new SyntaxError(SyntaxError.OTHER, lastLine(), lastOffset(), "End of tokens."));
    }

    public void unknownSyntaxError(String message){
        syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), message));
    }
}
