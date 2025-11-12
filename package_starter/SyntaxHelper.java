import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyntaxHelper {
    LexToken[] lexTokens;
    int position = 0;
    public SyntaxHelper(LexToken[] lexTokens){
        this.lexTokens = lexTokens;
        this.position = 0;
    }

    public SyntaxNode parseProgram(){
        // <program> ::= <statementList>
        return parseStatementList();
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
        SyntaxNode statement = new SyntaxNode(SyntaxNode.STATEMENT, 0, 0, "");

        if (Objects.equals(currentToken().token, "if"))
            statement.addChild(parseIf());
        else if (Objects.equals(currentToken().token, "while"))
            statement.addChild(parseWhile());
        else if (currentToken().type == LexToken.TYPE_NAME)
            statement.addChild(parseDeclare());
        else if (currentToken().type == LexToken.IDENTIFIER)
            statement.addChild(parseAssign());
        else return null;
        return statement;
    }

    public SyntaxNode parseIf(){
        // <if> ::= if ( <expression> ) <scope> | if ( <expression> ) <scope> else <scope>

        // <if> --- <expression> <scope>    (for just if)
        // <if> --- <expression> <scope> <scope>    (for if-else)
        SyntaxNode ifNode = new SyntaxNode(SyntaxNode.IF, 0, 0, "");



        return null;
    }

    public SyntaxNode parseWhile(){
        // <while> ::= while ( <expression> ) <scope>

        // <while> --- <expression> <scope>

        return null;
    }

    public SyntaxNode parseScope(){
        // <scope> ::= { <statementList> }

        // <scope> --- <statementList>

        return null;
    }

    public SyntaxNode parseDeclare(){
        // <declaration> --- typeName identifier
        // <declaration> --- typeName <assignment>

        return null;
    }

    public SyntaxNode parseAssign(){
        // <assignment> ::= identifier = <expression> | identifier = string

        // <assignment> --- identifier <expression>
        // <assignment> --- identifier string

        return null;
    }

    public SyntaxNode parseExpression(){
        // <expression> --- <math>
        // <expression> --- <comparison>

        return null;
    }

    public SyntaxNode parseMath(){
        // <math> ::= <term>
        //| <math> + <term>
        //| <math> - <term>
        SyntaxNode math = new SyntaxNode(SyntaxNode.MATH, 0, 0, "");


        return null;
    }

    public SyntaxNode parseMathAdd(){
        // <-> ::= <math> + <term>
        return null;
    }

    public SyntaxNode parseMathSubtract(){
        // <math> - <term>
        return null;
    }

    public SyntaxNode parseTermMultiply(){
        SyntaxNode termMultiply = new SyntaxNode(SyntaxNode.TERM_MULTIPLY, 0, 0, "");
        termMultiply.addChild(new SyntaxNode(SyntaxNode.TERM, 0, 0, ""));
        termMultiply.addChild(new SyntaxNode(SyntaxNode.FACTOR, 0, 0, ""));
        return termMultiply;
    }

    public SyntaxNode parseTermDivide(){
        SyntaxNode termDivide = new SyntaxNode(SyntaxNode.TERM_DIVIDE, 0, 0, "");
        termDivide.addChild(new SyntaxNode(SyntaxNode.TERM, 0, 0, ""));
        termDivide.addChild(new SyntaxNode(SyntaxNode.FACTOR, 0, 0, ""));
        return termDivide;
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
        SyntaxNode factor = new SyntaxNode(SyntaxNode.FACTOR, 0, 0, "");
        int nextTokenType = nextToken().type;
        if (nextTokenType == LexToken.IDENTIFIER)
            factor.addChild(parseIdentifier());
        else if (nextTokenType == LexToken.LITERAL_NUMBER)
            factor.addChild(parseNumber());
        else factor.addChild(parseExpression());
        return factor;
    }

    public SyntaxNode parseTerm(){
        // <term> = <factor> | <term> * <factor> | <term> / <factor>
        SyntaxNode term = new SyntaxNode(SyntaxNode.TERM)
        if (nextToken().token == "*")

        return null;
    }

    public SyntaxNode parseIdentifier(){
        return null;
    }

    public SyntaxNode parseTypeName(){
        return null;
    }

    public SyntaxNode parseNumber(){
        return null;
    }

    public SyntaxNode parseString(){
        return null;
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
}
