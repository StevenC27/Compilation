import java.util.*;

public class LexHelper {
    public static final String[] KEYWORDS = {"if", "else", "while", "return"};
    public static final String[] SYNTAX_TOKENS = {"=", "==", "*", "/", "+", "-", "(", ")", "[", "]", "{", "}", ";", ","};
    public static final String[] TYPE_NAMES = {"int", "float", "string", "bool"};
    private final String input;
    private List<LexToken> lexTokens;
    private List<LexError> lexErrors;
    private int lineNumber;
    private int characterOffset;
    private int charIndex;
    private int tokenType;
    private String currentToken;
    private char currentChar;

    public LexHelper(String input){
        this.input = input;
        tokenise();
    }

    public void tokenise(){
        // initialises the lexTokens and lexErrors arraylists.
        lexTokens = new ArrayList<>();
        lexErrors = new ArrayList<>();

        // initialises the other variables which need tracking.
        currentToken = "";
        tokenType = LexToken.UNKNOWN;
        lineNumber = 1;
        characterOffset = 0;
        charIndex = 0;

        // loops through the characters in the input string.
        while (nextCharExists()){
            updateCurrentChar(); // updates the currentChar variable.

            // checks if currentChar is not a valid character.
            if (isNotValidChar(currentChar)){
                // if true then creates an error of type UNKNOWN_ENTITY and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_ENTITY, lineNumber, characterOffset + currentToken.length(), "Unknown entity"));
            } else if (currentChar == '"'){
                // checks if currentChar is '"' and if it is then enter tokeniseString() where a string is tokenised.
                tokeniseString();
            } else if (Objects.equals(currentToken, "")){
                // checks if the current token is empty.
                // if so then checks that currentChar is not whitespace.
                if (!isWhitespace(currentChar)){
                    // if currentChar is not whitespace then calls updateToken() to add currentChar to the currentToken.
                    updateToken();
                }
            } else {
                // if none of the above occurs then check the current token type.
                tokenType = getTokenType();

                // checks the cases of tokenType.
                switch (tokenType){
                    case LexToken.KEYWORD, LexToken.TYPE_NAME:
                        // if tokenType is a keyword or a typename then enter tokeniseKeyType().
                        tokeniseKeyType();
                        break;
                    case LexToken.IDENTIFIER, LexToken.LITERAL_NUMBER:
                        // if tokenType is an identifier or a literal number then enter tokeniseNumberIdentifier().
                        tokeniseNumberIdentifier();
                        break;
                    case LexToken.SYNTAX_TOKEN:
                        // if tokenType is a syntax token then enter tokeniseSyntax();
                        tokeniseSyntax();
                        break;
                    case LexToken.UNKNOWN:
                        // if tokenType is unknown then enter tokeniseUnknown();
                        tokeniseUnknown();
                        break;
                }

                // checks if the currentToken is empty.
                // if it is that means a new token was created meaning a new token needs to be tokenised.
                // so continue to the beginning of the loop.
                if (Objects.equals(currentToken, "")){
                    continue;
                }
            }

            // checks the cases for currentChar where it is whitespace.
            switch (currentChar){
                case '\r':
                    // if currentChar is '\r' then increment lineNumber by 1.
                    lineNumber++;
                    break;
                case '\n':
                    // if currentChar is '\n' then set characterOffset to 0.
                    characterOffset = 0;
                    break;
                case '\t':
                    // if currentChar is '\t' then increment characterOffset by 4.
                    characterOffset += 4;
                    break;
                case ' ':
                    // if currentChar is ' ' then increment characterOffset by 1.
                    characterOffset++;
                    break;
            }

            advanceChar(); // advances the charIndex.
        }
    }

    public void tokeniseString(){
        tokenType = LexToken.LITERAL_STRING;
        advanceChar(); // advances the charIndex.
        characterOffset++; // increments characterOffset by 1;
        while (nextCharExists()){
            updateCurrentChar(); // updates the currentChar variable.

            // checks if currentChar is not a valid character.
            if (isNotValidChar(currentChar)){
                // if true then creates an error of type UNKNOWN_ENTITY and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_ENTITY, lineNumber, characterOffset + currentToken.length(), "Unknown entity"));
            } else if (currentChar == '"') {
                // checks if the currentChar is '"' and breaks out of the loop is true.
                break;
            } else if (currentChar == '\n' || currentChar == '\r' || currentChar == '\t'){
                // checks if the currentChar is '\n', '\r' or '\t'.
                // if true the creates an error of type UNKNOWN_STRING_FORMAT and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_STRING_FORMAT, lineNumber, characterOffset + currentToken.length(), "Unknown string format"));
            } else if (currentChar == '\\'){
                // checks the currentChar is '\' and if true then checks the next character exists.
                if (nextCharExists()){
                    advanceChar(); // advances the charIndex.
                    updateCurrentChar(); // updates the currentChar variable.

                    // checks if the currentChar is a valid escape character.
                    if (isValidEscapeChar(currentChar)){
                        // adds '\' and the currentChar to the currentToken.
                        currentToken += '\\';
                        updateToken();
                    } else {
                        // if the currentChar is not a valid escape character then
                        // creates an error of type UNKNOWN_ESCAPE_CHARACTER and adds it to the lexErrors arraylist.
                        lexErrors.add(new LexError(LexError.UNKNOWN_ESCAPE_CHARACTER, lineNumber, characterOffset + currentToken.length(), "Invalid escape character : \\" + currentChar));
                    }
                } else {
                    // if the next character doesn't exist then
                    // creates an error of type OTHER and adds it to the lexErrors arraylist.
                    lexErrors.add(new LexError(LexError.OTHER, lineNumber, characterOffset + currentToken.length(), "End of file"));
                }
            } else {
                // if none of the above occurs then update the currentToken.
                updateToken();
            }
            advanceChar(); // advances the charIndex.
        }

        endToken(); // ends the string token.

        // checks if the next character exists
        if (nextCharExists()){
            characterOffset++; // increments characterOffset by 1 for the closing '"'.
            advanceChar(); // advances the charIndex.
        } else {
            // if the next character doesn't exist then
            // creates an error of type OTHER and adds it to the lexErrors arraylist.
            lexErrors.add(new LexError(LexError.OTHER, lineNumber, characterOffset + currentToken.length(), "End of file, expected closing \""));
        }
    }

    public void tokeniseKeyType(){
        // check if the next char is whitespace or a syntax.
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken(); // ends the key token.
        } else {
            updateToken(); // add char to token.
        }
    }

    public void tokeniseNumberIdentifier(){
        // check if the next char is whitespace or a syntax.
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken(); // ends the key token.
        } else if (isValidDigitChar(currentChar) || isValidLetterChar(currentChar)){
            updateToken(); // add char to token.
        }
    }

    public void tokeniseSyntax(){
        if (isTerminatingChar(currentChar) || isValidDigitChar(currentChar) || isValidLetterChar(currentChar)
                || (isOperationChar(currentChar) && !isOperationChar(currentToken.charAt(currentToken.length() - 1)))){
            endToken(); // ends the key token.
        } else if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            updateToken(); // add char to token.
        }
    }

    public void tokeniseUnknown(){
        if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            updateToken(); // add char to token.
        } else if (isTerminatingChar(currentChar)){
            if (isInTokenArray(SYNTAX_TOKENS, currentToken.substring(0, 1))){
                lexErrors.add(new LexError(LexError.UNKNOWN_SYNTAX, lineNumber, characterOffset, "Unknown syntax."));
            } else if (isNumber(currentToken.substring(0, 1))){
                lexErrors.add(new LexError(LexError.UNKNOWN_NUMBER_FORMAT, lineNumber, characterOffset, "Unknown number"));
            }
            endToken(); // ends the key token.
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
        else if(isNumber(currentToken)) return LexToken.LITERAL_NUMBER;
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

    public boolean isNumber(String string){
        // this fragment is used to find out if a string is numeric.
        // found at https://sentry.io/answers/how-to-check-if-a-string-is-numeric-in-java/
        try{
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public boolean isOperationChar(char character){
        return (character == '+' || character == '-' || character == '*' || character == '/' || character == '=');
    }

    public boolean isNotValidChar(char character){
        return !isTerminatingChar(character) && !isOperationChar(character)
                && !isValidDigitChar(character) && !isValidEscapeChar(character)
                && !isValidLetterChar(character);
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

    public void updateCurrentChar(){
        currentChar = input.charAt(charIndex);
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
