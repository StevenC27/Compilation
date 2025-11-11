import java.util.ArrayList;
import java.util.List;

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

        List<SyntaxNode> statementNodes = new ArrayList<>();
        while(position != lexTokens.length - 1){
            SyntaxNode statement = parseStatement();
            if (statement != null){
                statementNodes.add(statement);
            }
        }
        SyntaxNode statementList = new SyntaxNode(SyntaxNode.STATEMENT_LIST, 0, 0, "");
        statementList.setChildren(statementNodes.toArray(new SyntaxNode[0]));
        return statementList;
    }

    public SyntaxNode parseStatement(){
        // <statement> ::= <declaration> | <assignment> | <if> | <while>

        // <statement> :== <if>
        if (currentToken().type == LexToken.KEYWORD)

        // <statement> --- <while>
        // <statement> --- <declare>
        // <statement> --- <assign>

        return null;
    }

    public SyntaxNode parseIf(){
        // <if> ::= if ( <expression> ) <scope> | if ( <expression> ) <scope> else <scope>

        // <if> --- <expression> <scope>    (for just if)
        // <if> --- <expression> <scope> <scope>    (for if-else)

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
        return null;
    }
    public SyntaxNode parseTermDivide(){
        return null;
    }
    public SyntaxNode parseComparison(){
        // <comparison> --- <expression> <expression>

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

    public SyntaxNode parseFactor(){
        // <factor> ::= number | identifier | ( <expression> )
        return null;
    }

    public SyntaxNode parseTerm(){
        // <term> = <factor> | <term> * <factor> | <term> / <factor>
        return null;
    }

    public void advance(){
        this.position++;
    }

    public LexToken currentToken(){
        if (position >= lexTokens.length) return null;
        return lexTokens[position];
    }
}
