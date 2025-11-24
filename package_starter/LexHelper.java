import java.util.*;

public class LexHelper {
    public static final String[] KEYWORDS = {"if", "else", "while", "return"};
    public static final String[] SYNTAX_TOKENS = {"=", "==", "*", "/", "+", "-", "(", ")", "[", "]", "{", "}", ";", ","};
    public static final String[] TYPE_NAMES = {"int", "float", "string", "bool"};
    private final String input;
    private final List<LexToken> lexTokens;
    private final List<LexError> lexErrors;
    private int lineNumber;
    private int characterOffset;
    private int charIndex;
    private int tokenType;
    private String currentToken;
    private char currentChar;

    public LexHelper(String input){
        this.input = input;
        lexTokens = new ArrayList<>();
        lexErrors = new ArrayList<>();
        currentToken = "";
        tokenType = LexToken.UNKNOWN;
        lineNumber = 1;
        characterOffset = 0;
        charIndex = 0;
        tokenise();
    }

    public void tokenise(){
        while (nextCharExists()){
            currentChar = updateCurrentChar();
            if (currentChar == '"'){
                stringMode();
            } else if (Objects.equals(currentToken, "")){
                if (isValidChar(currentChar) && !isWhitespace(currentChar)){
                    updateToken();
                } else if (!isWhitespace(currentChar)){
                    addError(LexError.OTHER, lineNumber, characterOffset, "Unknown syntax");
                    characterOffset++;
                    advanceChar();
                }
            } else {
                tokenType = getTokenType();
                switch (tokenType){
                    case LexToken.KEYWORD, LexToken.TYPE_NAME:
                        tokeniseKeyType();
                        break;
                    case LexToken.IDENTIFIER:
                        tokeniseIdentifier();
                        break;
                    case LexToken.SYNTAX_TOKEN:
                        tokeniseSyntax();
                        break;
                    case LexToken.LITERAL_NUMBER:
                        tokeniseNumber();
                        break;
                    case LexToken.UNKNOWN:
                        tokeniseUnknown();
                        break;
                }
                if (Objects.equals(currentToken, "")){
                    continue;
                }
            }
            updateLineAndOffset();
            advanceChar();
        }
    }

    public void stringMode(){
        tokenType = LexToken.LITERAL_STRING;
        advanceChar();
        characterOffset++;
        while (nextCharExists()){
            currentChar = updateCurrentChar();
            if (currentChar == '"') {
                characterOffset++;
                break;
            } else if (currentChar == '\\'){
                updateToken();
                if (nextCharExists()){
                    advanceChar();
                    currentChar = updateCurrentChar();
                    if (isValidEscapeChar(currentChar)){
                        updateToken();
                    } else {
                        addError(LexError.UNKNOWN_ESCAPE_CHARACTER, lineNumber, characterOffset, "Invalid escape character : " + currentChar);
                    }
                } else {
                    addError(LexError.OTHER, lineNumber, characterOffset, "End of file");
                }
            } else if (!isValidChar(currentChar)){
                // do something
            } else {
                updateToken();
            }
            advanceChar();
        }
        endToken();
        if (nextCharExists()) advanceChar();

    }

    public void tokeniseKeyType(){
        // check if the next char is whitespace or a syntax
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken();
        } else {
            // add char to token
            updateCurrentChar();
        }
    }

    public void tokeniseIdentifier(){
        // check if the next char is whitespace or a syntax.
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken();
        } else if (isValidDigitChar(currentChar) || isValidLetterChar(currentChar)){
            // add char to token
            updateToken();
        } else {
            // do something.
        }
    }

    public void tokeniseSyntax(){
        if (isTerminatingChar(currentChar) || isValidDigitChar(currentChar) || isValidLetterChar(currentChar)
                || (isOperationChar(currentChar) && !isOperationChar(currentToken.charAt(currentToken.length() - 1)))){
            endToken();
        } else if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            updateToken();
        } else {
            addError(LexError.UNKNOWN_SYNTAX, lineNumber, characterOffset, "Unknown syntax.");
            // do something.
        }
    }

    public void tokeniseNumber(){
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken();
        } else if (isValidDigitChar(currentChar) || isValidLetterChar(currentChar)){
            updateToken();
        } else {
            // do something.
        }
    }

    public void tokeniseUnknown(){
        if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            updateToken();
        } else if (isTerminatingChar(currentChar)){
            if (!isInTokenArray(SYNTAX_TOKENS, currentToken.substring(0, 1))){

            }
            endToken();
        }
    }

    public void endToken(){
        lexTokens.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
        characterOffset += currentToken.length();
        currentToken = "";
    }

    public int getTokenType(){
        if(isInTokenArray(KEYWORDS, currentToken)) return LexToken.KEYWORD;
        else if(isInTokenArray(SYNTAX_TOKENS, currentToken)) return LexToken.SYNTAX_TOKEN;
        else if(isInTokenArray(TYPE_NAMES, currentToken)) return LexToken.TYPE_NAME;
        else if(isIdentifier()) return LexToken.IDENTIFIER;
        else if(isNumber()) return LexToken.LITERAL_NUMBER;
        return LexToken.UNKNOWN;
    }

    public boolean isInTokenArray(String[] array, String token){
        boolean isElement = false;
        for (String element : array){
            if (token.equals(element)) {
                isElement = true;
                break;
            }
        }
        return isElement;
    }

    public boolean isIdentifier(){
        if(currentToken.isEmpty()) return false;
        if(Character.isDigit(currentToken.charAt(0))) return false;
        if(currentToken.contains(" ")
                || currentToken.contains("\n")
                || currentToken.contains("\r")
                || currentToken.contains("\t"))
            return false;
        boolean isIdentifier = true;
        for(String syntax_token : SYNTAX_TOKENS){
            if(currentToken.contains(syntax_token)){
                isIdentifier = false;
                break;
            }
        }
        return isIdentifier;
    }

    public boolean isNumber(){
        // this fragment is used to find out if a string is numeric.
        // found at https://sentry.io/answers/how-to-check-if-a-string-is-numeric-in-java/
        try{
            Integer.parseInt(currentToken);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public boolean isOperationChar(char character){
        return (character == '+' || character == '-' || character == '*' || character == '/' || character == '=');
    }

    public boolean isValidChar(char character){
        return isTerminatingChar(character) || isOperationChar(character)
                || isValidDigitChar(character) || isValidEscapeChar(character)
                || isValidLetterChar(character);
    }

    public boolean isValidEscapeChar(char character){
        return character == 'r' || character == 'n' || character == 't' || character == '"' || character == '\\';
    }

    public boolean isValidLetterChar(char character){
        return Character.isAlphabetic(character);
    }

    public boolean isValidDigitChar(char character){
        return Character.isDigit(character);
    }

    public boolean isWhitespace(char character){
        return character == '\r' || character == '\n' || character == '\t' || character == ' ';
    }

    public boolean isTerminatingChar(char character){
        return (isWhitespace(character) || character == ',' || character == ';'
                || character == '(' || character == ')' || character == '{'
                || character == '}' || character == '[' || character == ']');
    }

    public void advanceChar(){
        charIndex++;
    }

    public void updateToken(){
        currentToken += currentChar;
    }

    public void addError(int type, int lineNumber, int characterOffset, String message){
        lexErrors.add(new LexError(type, lineNumber, characterOffset, message));
    }

    public char updateCurrentChar(){
        return input.charAt(charIndex);
    }

    public void updateLineAndOffset(){
        if (currentChar == '\r'){
            lineNumber++;
        } else if (currentChar == '\n'){
            characterOffset = 0;
        } else if (currentChar == '\t'){
            characterOffset += 4;
        } else if (currentChar == ' '){
            characterOffset++;
        }
    }

    public boolean nextCharExists(){
        return charIndex < input.length();
    }

    public List<LexToken> getLexTokens(){
        return lexTokens;
    }

    public List<LexError> getLexErrors(){
        return lexErrors;
    }
}
