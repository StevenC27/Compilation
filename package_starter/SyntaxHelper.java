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

        // FIRST: {$, }}
        if (position >= lexTokens.length) return statementListNode;

        statementListNode.addChild(parseStatement());
        advance();

        if (position >= lexTokens.length) return statementListNode;

        statementListNode.addChild(parseStatementList());
        if (position >= lexTokens.length){
            LexToken lastToken = lexTokens[lexTokens.length-1];
            syntaxErrors.add(new SyntaxError(SyntaxError.OTHER,
                    lastToken.lineNumber,
                    lastToken.characterOffset + lastToken.token.length(),
                    "End of file"));
        } else {
            advance();
        }

        return statementListNode;
    }

    public SyntaxNode parseStatement(){
        // <statement> ::= <declaration> | <assignment> | <if> | <while>
        SyntaxNode statementNode = new SyntaxNode(SyntaxNode.STATEMENT, line(), offset(), "");

        if (Objects.equals(currentToken().token, "if"))
            statementNode.addChild(parseIf());
        else if (Objects.equals(currentToken().token, "while"))
            statementNode.addChild(parseWhile());
        else if (currentToken().type == LexToken.TYPE_NAME)
            statementNode.addChild(parseDeclare());
        else if (currentToken().type == LexToken.IDENTIFIER)
            statementNode.addChild(parseAssign());
        else return null;
        return statementNode;
    }

    public SyntaxNode parseIf(){
        // <if> ::= if ( <expression> ) <scope> | if ( <expression> ) <scope> else <scope>

        // <if> --- <expression> <scope>    (for just if)
        // <if> --- <expression> <scope> <scope>    (for if-else)
        SyntaxNode ifNode = new SyntaxNode(SyntaxNode.IF, line(), offset(), "");

        ifNode.addChild(parseExpression());
        ifNode.addChild(parseScope());
        if (Objects.equals(nextToken().token, "else"))
            ifNode.addChild(parseScope());

        return ifNode;
    }

    public SyntaxNode parseWhile(){
        // <while> ::= while ( <expression> ) <scope>

        // <while> --- <expression> <scope>
        SyntaxNode whileNode = new SyntaxNode(SyntaxNode.WHILE, line(), offset(), "");
        return whileNode;
    }

    public SyntaxNode parseScope(){
        // <scope> ::= { <statementList> }

        // <scope> --- <statementList>
        SyntaxNode scopeNode = new SyntaxNode(SyntaxNode.SCOPE, line(), offset(), "");
        return scopeNode;
    }

    public SyntaxNode parseDeclare(){
        // <declaration> --- typeName identifier
        // <declaration> --- typeName <assignment>

        SyntaxNode declareNode = new SyntaxNode(SyntaxNode.DECLARE, line(), offset(), "");
        if (compareType(LexToken.TYPE_NAME, currentToken().type)){
            declareNode.addChild(parseTerminal(SyntaxNode.TYPE_NAME));
            if (compareType(LexToken.IDENTIFIER, currentToken().type)){
                if (compareToken("=", nextToken().token)){
                    declareNode.addChild(parseAssign());
                } else {
                    declareNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));
                }
            } else {
                syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), "Unknown syntax, expected Identifier"));
            }
        } else {
            syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), "Unknown syntax, expected TypeName"));
        }

        return declareNode;
    }

    public SyntaxNode parseAssign(){
        // <assignment> ::= identifier = <expression> | identifier = string

        // <assignment> ::= identifier <expression>
        // <assignment> ::= identifier string
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.ASSIGN, line(), offset(), "");
        if (compareType(LexToken.IDENTIFIER, currentToken().type)) {
            assignNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));
            advance();
            System.out.println(currentToken());
            if (compareType(LexToken.LITERAL_STRING, currentToken().type)) {
                assignNode.addChild(parseTerminal(SyntaxNode.STRING));
            } else if (compareType(LexToken.LITERAL_NUMBER, currentToken().type)
                    || compareType(LexToken.IDENTIFIER, currentToken().type)
                    || compareToken("(", currentToken().token)
                    || compareToken("==", currentToken().token)){
                assignNode.addChild(parseExpression());
            } else {
                syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), "Unknown syntax, expected <Expression> or String"));
            }
        } else {
            syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), "Unknown syntax, expected Identifier"));
        }



        return assignNode;
    }

    public SyntaxNode parseExpression(){
        // <expression> ::= <math>
        // <expression> ::= <comparison>
        SyntaxNode expressionNode = new SyntaxNode(SyntaxNode.EXPRESSION, line(), offset(), "");
        if (compareToken("==", currentToken().token)){
            expressionNode.addChild(parseComparison());
        } else if (compareType(LexToken.LITERAL_NUMBER, currentToken().type)
                || compareType(LexToken.IDENTIFIER, currentToken().type)
                || compareToken("(", currentToken().token)){
            expressionNode.addChild(parseMath());
        } else {
            syntaxErrors.add(new SyntaxError(SyntaxError.UNKNOWN_SYNTAX, line(), offset(), "Unknown syntax, expected <Math> or <Comparison>"));
        }

        return expressionNode;
    }

    public SyntaxNode parseMath(){
        // <math> ::= <term>
        // <math> ::= <math> + <term>
        // <math> ::= <math> - <term>
        SyntaxNode mathNode = new SyntaxNode(SyntaxNode.MATH, line(), offset(), "");
        return mathNode;
    }

    public SyntaxNode parseMathAdd(){
        // <-> ::= <math> + <term>
        SyntaxNode mathAddNode = new SyntaxNode(SyntaxNode.MATH_ADD,
                nextToken().lineNumber, nextToken().characterOffset, nextToken().token);
        mathAddNode.addChild(parseTerm());
        mathAddNode.addChild(parseFactor());
        return mathAddNode;
    }

    public SyntaxNode parseMathSubtract(){
        // <math> - <term>
        SyntaxNode mathSubtractNode = new SyntaxNode(SyntaxNode.MATH_SUBTRACT,
                nextToken().lineNumber, nextToken().characterOffset, nextToken().token);
        mathSubtractNode.addChild(parseTerm());
        mathSubtractNode.addChild(parseFactor());
        return mathSubtractNode;
    }

    public SyntaxNode parseTermMultiply(){
        SyntaxNode termMultiplyNode = new SyntaxNode(SyntaxNode.TERM_MULTIPLY, line(), offset(), "");
        termMultiplyNode.addChild(new SyntaxNode(SyntaxNode.TERM, line(), offset(), ""));
        termMultiplyNode.addChild(new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), ""));
        return termMultiplyNode;
    }

    public SyntaxNode parseTermDivide(){
        SyntaxNode termDivideNode = new SyntaxNode(SyntaxNode.TERM_DIVIDE, line(), offset(), "");
        termDivideNode.addChild(new SyntaxNode(SyntaxNode.TERM, line(), offset(), ""));
        termDivideNode.addChild(new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), ""));
        return termDivideNode;
    }

    public SyntaxNode parseComparison(){
        // <comparison> --- <expression> <expression>
        SyntaxNode comparisonNode = new SyntaxNode(SyntaxNode.COMPARISON, 0, 0, "");
        comparisonNode.addChild(parseExpression());
        comparisonNode.addChild(parseExpression());
        return comparisonNode;
    }

    public SyntaxNode parseFactor(){
        // <factor> ::= number | identifier | ( <expression> )
        SyntaxNode factorNode = new SyntaxNode(SyntaxNode.FACTOR, line(), offset(), "");
        int nextTokenType = nextToken().type;
        if (compareType(LexToken.IDENTIFIER, currentToken().type))
            factorNode.addChild(parseTerminal(SyntaxNode.IDENTIFIER));
        else if (compareType(LexToken.LITERAL_NUMBER, currentToken().type))
            factorNode.addChild(parseTerminal(SyntaxNode.NUMBER));
        else factorNode.addChild(parseExpression());
        return factorNode;
    }

    public SyntaxNode parseTerm(){
        // <term> = <factor> | <term> * <factor> | <term> / <factor>
        SyntaxNode termNode = new SyntaxNode(SyntaxNode.TERM, line(), offset(), "");
        if (compareToken("*", currentToken().token))
            termNode.addChild(parseTermMultiply());
        else if (compareToken("/", currentToken().token))
            termNode.addChild(parseTermDivide());
        else termNode.addChild(parseFactor());

        return termNode;
    }

    public SyntaxNode parseTerminal(int type){
        SyntaxNode node = new SyntaxNode(type, line(), offset(), currentToken().token);
        advance();
        return node;
    }

    public void advance(){
        this.position++;
    }

    public int line(){
        return currentToken().lineNumber;
    }

    public int offset(){
        return currentToken().characterOffset;
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
}
