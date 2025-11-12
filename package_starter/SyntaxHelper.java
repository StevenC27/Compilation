import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;

public class SyntaxHelper {
    LexToken[] lexTokens;
    Hashtable<Integer, HashSet<String>> firsts = initFirsts();
    int position;
    public SyntaxHelper(LexToken[] lexTokens){
        this.lexTokens = lexTokens;
        this.position = 0;
    }

    public SyntaxNode parseProgram(){
        // <program> ::= <statementList>
        SyntaxNode programNode = new SyntaxNode(SyntaxNode.PROGRAM, 0, 0, "");
        programNode.addChild(parseStatementList());
        return programNode;
    }

    public SyntaxNode parseStatementList(){
        // <statementList> ::= <statement> ; | <statementList> ; <statement> ;

        // <statementList> --- <statement>

        SyntaxNode statementList = new SyntaxNode(SyntaxNode.STATEMENT_LIST, 0, 0, "");
        while(position != lexTokens.length - 1){
            SyntaxNode statement = parseStatement();
            if (statement != null){
                statementList.addChild(statement);
            }
        }
        return statementList;
    }

    public SyntaxNode parseStatement(){
        // <statement> ::= <declaration> | <assignment> | <if> | <while>
        SyntaxNode statementNode = new SyntaxNode(SyntaxNode.STATEMENT, 0, 0, "");

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
        SyntaxNode ifNode = new SyntaxNode(SyntaxNode.IF, 0, 0, "");

        ifNode.addChild(parseExpression());
        ifNode.addChild(parseScope());
        if (Objects.equals(nextToken().token, "else"))
            ifNode.addChild(parseScope());

        return ifNode;
    }

    public SyntaxNode parseWhile(){
        // <while> ::= while ( <expression> ) <scope>

        // <while> --- <expression> <scope>
        SyntaxNode whileNode = new SyntaxNode(SyntaxNode.WHILE, 0, 0, "");
        return whileNode;
    }

    public SyntaxNode parseScope(){
        // <scope> ::= { <statementList> }

        // <scope> --- <statementList>
        SyntaxNode scopeNode = new SyntaxNode(SyntaxNode.SCOPE, 0, 0, "");
        return scopeNode;
    }

    public SyntaxNode parseDeclare(){
        // <declaration> --- typeName identifier
        // <declaration> --- typeName <assignment>

        SyntaxNode declareNode = new SyntaxNode(SyntaxNode.DECLARE, 0, 0, "");
        declareNode.addChild(parseTypeName());
        if (expectType(LexToken.IDENTIFIER)){
            if ()
        }

        return declareNode;
    }

    public SyntaxNode parseAssign(){
        // <assignment> ::= identifier = <expression> | identifier = string

        // <assignment> --- identifier <expression>
        // <assignment> --- identifier string
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.ASSIGN, 0, 0, "");
        if (expectType(LexToken.IDENTIFIER))
            assignNode.addChild(parseIdentifier());
        else return null;

        if (expectType(LexToken.LITERAL_STRING)){
            assignNode.addChild(parseString());
        } else {
            assignNode.addChild(parseExpression());
        }

        return assignNode;
    }

    public SyntaxNode parseExpression(){
        // <expression> --- <math>
        // <expression> --- <comparison>
        SyntaxNode expressionNode = new SyntaxNode(SyntaxNode.EXPRESSION, 0, 0, "");

        return expressionNode;
    }

    public SyntaxNode parseMath(){
        // <math> ::= <term>
        //| <math> + <term>
        //| <math> - <term>
        SyntaxNode mathNode = new SyntaxNode(SyntaxNode.MATH, 0, 0, "");
        if (Objects.equals(nextToken().token, "+"))
            mathNode.addChild(parseMathAdd());
        else if (Objects.equals(nextToken().token, "-"))
            mathNode.addChild(parseMathSubtract());
        else mathNode.addChild(parseTerm());
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
        SyntaxNode termMultiplyNode = new SyntaxNode(SyntaxNode.TERM_MULTIPLY, 0, 0, "");
        termMultiplyNode.addChild(new SyntaxNode(SyntaxNode.TERM, 0, 0, ""));
        termMultiplyNode.addChild(new SyntaxNode(SyntaxNode.FACTOR, 0, 0, ""));
        return termMultiplyNode;
    }

    public SyntaxNode parseTermDivide(){
        SyntaxNode termDivideNode = new SyntaxNode(SyntaxNode.TERM_DIVIDE, 0, 0, "");
        termDivideNode.addChild(new SyntaxNode(SyntaxNode.TERM, 0, 0, ""));
        termDivideNode.addChild(new SyntaxNode(SyntaxNode.FACTOR, 0, 0, ""));
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
        SyntaxNode factorNode = new SyntaxNode(SyntaxNode.FACTOR, 0, 0, "");
        int nextTokenType = nextToken().type;
        if (expectType(LexToken.IDENTIFIER))
            factorNode.addChild(parseIdentifier());
        else if (expectType(LexToken.LITERAL_NUMBER))
            factorNode.addChild(parseNumber());
        else factorNode.addChild(parseExpression());
        return factorNode;
    }

    public SyntaxNode parseTerm(){
        // <term> = <factor> | <term> * <factor> | <term> / <factor>
        SyntaxNode termNode = new SyntaxNode(SyntaxNode.TERM, 0, 0, "");
        if (expectToken("*"))
            termNode.addChild(parseTermMultiply());
        else if (expectToken("/"))
            termNode.addChild(parseTermDivide());
        else termNode.addChild(parseFactor());

        return termNode;
    }

    public SyntaxNode parseIdentifier(){
        return new SyntaxNode(SyntaxNode.IDENTIFIER, currentToken().lineNumber, currentToken().characterOffset, currentToken().token);
    }

    public SyntaxNode parseTypeName(){
        return new SyntaxNode(SyntaxNode.TYPE_NAME, currentToken().lineNumber, currentToken().characterOffset, currentToken().token);
    }

    public SyntaxNode parseNumber(){
        return new SyntaxNode(SyntaxNode.NUMBER, currentToken().lineNumber, currentToken().characterOffset, currentToken().token);
    }

    public SyntaxNode parseString(){
        return new SyntaxNode(SyntaxNode.STRING, currentToken().lineNumber, currentToken().characterOffset, currentToken().token);
    }

    public void advance(){
        this.position++;
    }

    public LexToken currentToken(){
        if (position >= lexTokens.length) return null;
        return lexTokens[position];
    }

    public LexToken nextToken(){
        if (position >= lexTokens.length - 1) return null;
        return lexTokens[position + 1];
    }

    public boolean currentExpectType(int tokenType) {
        return currentToken().type == tokenType;
    }

    public boolean expectToken(String token){
        return Objects.equals(currentToken().token, token);
    }

    public Hashtable<Integer, HashSet<String>> initFirsts(){
        Hashtable<Integer, HashSet<String>> tempFirsts = new Hashtable<>();

        HashSet<String> ifFirst = new HashSet<>();
        HashSet<String> whileFirst = new HashSet<>();
        HashSet<String> scopeFirst = new HashSet<>();
        HashSet<String> declareFirst = new HashSet<>();
        HashSet<String> assignFirst = new HashSet<>();
        HashSet<String> expressionFirst = new HashSet<>();
        HashSet<String> mathFirst = new HashSet<>();
        HashSet<String> compareFirst = new HashSet<>();
        HashSet<String> termFirst = new HashSet<>();
        HashSet<String> factorFirst = new HashSet<>();

        ifFirst.add("if");
        whileFirst.add("while");
        scopeFirst.add("{");
        declareFirst.add("typeName");
        assignFirst.add("identifier");
        expressionFirst.add("number");
        expressionFirst.add("identifier");
        expressionFirst.add("(");
        mathFirst.add("number");
        mathFirst.add("identifier");
        mathFirst.add("(");
        termFirst.add("number");
        termFirst.add("identifier");
        termFirst.add("(");
        factorFirst.add("number");
        factorFirst.add("identifier");
        factorFirst.add("(");
        compareFirst.add("==");

        tempFirsts.put(SyntaxNode.IF, ifFirst);
        tempFirsts.put(SyntaxNode.WHILE, whileFirst);
        tempFirsts.put(SyntaxNode.SCOPE, scopeFirst);
        tempFirsts.put(SyntaxNode.DECLARE, declareFirst);
        tempFirsts.put(SyntaxNode.ASSIGN, assignFirst);
        tempFirsts.put(SyntaxNode.EXPRESSION, expressionFirst);
        tempFirsts.put(SyntaxNode.MATH, mathFirst);
        tempFirsts.put(SyntaxNode.COMPARISON, compareFirst);
        tempFirsts.put(SyntaxNode.TERM, termFirst);
        tempFirsts.put(SyntaxNode.FACTOR, factorFirst);

        return tempFirsts;
    }
}
