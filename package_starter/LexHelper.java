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
    private String currentToken;
    private char currentChar;
    private boolean tokenEnd;

    public LexHelper(String input){
        this.input = input;
        lexTokens = new ArrayList<>();
        lexErrors = new ArrayList<>();
        tokenEnd = false;
        currentToken = "";
        lineNumber = 1;
        characterOffset = 0;
        charIndex = 0;
        tokenise();
    }

    public void tokenise(){
        // check if in string mode.

        // ---String Mode---
        // set the line and offset to the location of the first ".
        // check for the closing " character.
        // add characters to the string token.

        // ---Not String Mode---
        // check what the current character is and if it is valid.
        // if the character is whitespace then change offset and line and check next character.
        // if the character is not valid then add to errors.
        // check if the current token can be a type.
        // based on the next character, decide if the current token ends
        // if it ends then add the token to the token list.

        int tokenType = LexToken.UNKNOWN;

        for(int i = 0; i < input.length(); i++) {
            currentChar = input.charAt(i);

            if (currentChar == '"' && !stringMode){
                stringMode = true;
                tokenType = LexToken.LITERAL_STRING;
                tokenEnd = true;
            } else if (stringMode){
                if (currentChar == '"'){
                    stringMode = false;
                    tokenEnd = true;
                }
            } else {
                tokenType = getTokenType(currentToken);

                if (tokenType == LexToken.KEYWORD || tokenType == LexToken.TYPE_NAME){
                    if (!(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar))){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.SYNTAX_TOKEN){
                    if (!(currentToken.endsWith(Character.toString(currentChar)) && isOperation(currentChar))){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.IDENTIFIER){
                    if (checkTokenArray(LexHelper.SYNTAX_TOKENS, Character.toString(currentChar))){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.LITERAL_NUMBER){
                    if (!Character.isDigit(currentChar)){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.UNKNOWN){
                    tokenEnd = true;
                }

                if (currentChar == '\r'){
                    tokenEnd = true;
                } else if (currentChar == '\n'){
                    tokenEnd = true;
                } else if (currentChar == '\t'){
                    tokenEnd = true;
                } else if (currentChar == ' '){
                    tokenEnd = true;
                }
            }

            if (tokenEnd){
                if (currentToken.isEmpty()){
                    tokenEnd = false;
                } else {
                    lexTokens.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
                    characterOffset += currentToken.length();
                    tokenEnd = false;
                    currentToken = "";
                }

                if (tokenType == LexToken.LITERAL_STRING && !stringMode){
                    characterOffset += 2;
                }

                if (currentChar == '\r'){
                    lineNumber++;
                } else if (currentChar == '\n'){
                    characterOffset = 0;
                } else if (currentChar == '\t'){
                    characterOffset += 4;
                } else if (currentChar == ' '){
                    characterOffset++;
                } else if (!(currentChar == '"')){
                    currentToken += currentChar;
                }

                if (i == input.length() - 1) {
                    tokenType = getTokenType(currentToken);
                    lexTokens.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
                }

            } else {
                currentToken += currentChar;
            }
        }
    }

    public void tokenise2(){
        while (nextCharExists()){
            currentChar = updateCurrentChar();
            if (currentChar == '"'){
                stringMode();
            } else {
                int tokenType = getTokenType();
                if (tokenType == LexToken.KEYWORD || tokenType == LexToken.TYPE_NAME){
                    if (!(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar))){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.SYNTAX_TOKEN){
                    if (!(currentToken.endsWith(Character.toString(currentChar)) && isOperation(currentChar))){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.IDENTIFIER){
                    if (isInTokenArray(LexHelper.SYNTAX_TOKENS)){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.LITERAL_NUMBER){
                    if (!Character.isDigit(currentChar)){
                        tokenEnd = true;
                    }
                } else if (tokenType == LexToken.UNKNOWN){
                    tokenEnd = true;
                }
            }
        }
    }

    public void stringMode(){
        advanceChar();
        while (nextCharExists()){
            currentChar = updateCurrentChar();
            if (currentChar == '"') break;
            else if (!isValidEscapeChar(currentChar)){
                // add error.
            } else if (!isValidDigitChar(currentChar)){
                // add error.
            } else if (!isValidLetterChar(currentChar)){
                // add error.
            } else {
                updateToken();
            }
            advanceChar();
        }
        if (nextCharExists()) advanceChar();

    }

    public boolean isOperation(char character){
        return (character == '+' || character == '-' || character == '*' || character == '/' || character == '=');
    }

    public int getTokenType(){
        if(isInTokenArray(KEYWORDS)) return LexToken.KEYWORD;
        else if(isInTokenArray(SYNTAX_TOKENS)) return LexToken.SYNTAX_TOKEN;
        else if(isInTokenArray(TYPE_NAMES)) return LexToken.TYPE_NAME;
        else if(isIdentifier()) return LexToken.IDENTIFIER;
        else if(isDigit()) return LexToken.LITERAL_NUMBER;
        return LexToken.UNKNOWN;
    }

    public boolean isInTokenArray(String[] array){
        boolean isElement = false;
        for (String element : array){
            if (currentToken.equals(element)) {
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

    public boolean isDigit(){
        boolean isDigit = true;
        if (currentToken.isEmpty()) return false;
        for (char character : currentToken.toCharArray()){
            if (!Character.isDigit(character)){
                isDigit = false;
                break;
            }
        }
        return isDigit;
    }

    public boolean isValidEscapeChar(char character){
        return character == '\r' || character == '\n' || character == '\t' || character == '\"' || character == '\\';
    }

    public boolean isValidLetterChar(char character){
        return Character.isAlphabetic(character);
    }

    public boolean isValidDigitChar(char character){
        return Character.isDigit(character);
    }

    public void advanceChar(){
        charIndex++;
    }

    public void updateToken(){
        currentToken += currentChar;
    }

    public void addToken(int type, int lineNumber, int characterOffset){
        lexTokens.add(new LexToken(type, lineNumber, characterOffset, currentToken));
    }

    public void addError(int type, int lineNumber, int characterOffset, String message){
        lexErrors.add(new LexError(type, lineNumber, characterOffset, message));
    }

    public char updateCurrentChar(){
        return input.charAt(charIndex);
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
