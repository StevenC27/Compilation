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
        updateCurrentChar();

        // loops through the characters in the input string.
        while (charExists(charIndex)){
            updateCurrentChar(); // updates the currentChar variable.

            // checks if currentChar is not a valid character.
            if (isNotValidChar(currentChar)){
                // if true then creates an error of type UNKNOWN_ENTITY and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_ENTITY, lineNumber, characterOffset + currentToken.length(), "Unknown entity"));
            } else if (currentChar == '"'){
                // checks if currentChar is '"' and if it is then enter tokeniseString() where a string is tokenised.
                tokeniseString();

                // checks if the currentToken is empty.
                // if it is that means a new token was created meaning a new token needs to be tokenised.
                // so continue to the beginning of the loop.
                if (currentToken.isEmpty()){
                    continue;
                }
            } else if (currentToken.isEmpty()){
                // checks if the currentToken is empty.

                // checks that the currentChar is not whitespace.
                if (!isWhitespace(currentChar)){
                    updateToken(); // updates the token.

                    // checks if the next char doesn't exist.
                    if (!charExists(charIndex + 1)){
                        // if it doesn't then update the tokenType and end token.
                        tokenType = getTokenType();
                        endToken();
                    }
                }
            } else {
                // if none of the above occurs then check the current token type.
                tokenType = getTokenType();

                // checks the cases of tokenType.
                switch (tokenType){
                    case LexToken.KEYWORD, LexToken.TYPE_NAME, LexToken.IDENTIFIER, LexToken.LITERAL_NUMBER:
                        // if tokenType is a keyword, typename, identifier or literal number then
                        // call tokeniseNonSyntax().
                        tokeniseNonSyntax();
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
            advanceChar(); // advances charIndex.
        }
    }

    public void tokeniseString(){
        tokenType = LexToken.LITERAL_STRING;
        advanceChar(); // advances the charIndex.
        characterOffset++; // increments characterOffset by 1;
        while (charExists(charIndex)){
            updateCurrentChar(); // updates the currentChar variable.

            if (currentChar == '"') {
                // checks if the currentChar is '"' and breaks out of the loop is true.
                characterOffset++;
                break;
            } else if (currentChar == '\n' || currentChar == '\r' || currentChar == '\t'){
                // checks if the currentChar is '\n', '\r' or '\t'.
                // if true the creates an error of type UNKNOWN_STRING_FORMAT and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_STRING_FORMAT, lineNumber, characterOffset + currentToken.length(), "Unknown string format"));
            } else if (currentChar == '\\'){
                // checks the currentChar is '\' and if true then checks the next character exists.
                if (charExists(charIndex)){
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
        if (charExists(charIndex)){
            characterOffset++; // increments characterOffset by 1 for the closing '"'.
            advanceChar(); // advances the charIndex.
        } else {
            // if the next character doesn't exist then
            // creates an error of type OTHER and adds it to the lexErrors arraylist.
            lexErrors.add(new LexError(LexError.OTHER, lineNumber, characterOffset + currentToken.length(), "End of file, expected closing \""));
        }
    }

    public void tokeniseNonSyntax(){
        // checks if the currentChar is a terminating char or an operation.
        if (isTerminatingChar(currentChar) || isOperationChar(currentChar)){
            endToken(); // ends the key token.
        } else {
            // if the currentChar is not a terminating character or an operation then the token shouldn't end.
            // so the currentToken is updated.
            updateToken();
        }
    }

    public void tokeniseSyntax(){
        // checks for repeating operations by checking if the currentChar is an operation
        // and the last character of currentToken is an operation.
        // this is to pick up the token "==" and read error tokens such as "===" or "--".
        if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            // if true then add currentChar to the token.
            updateToken();
        } else {
            // if the above doesn't occur then end the token.
            endToken();
        }
    }

    public void tokeniseUnknown(){
        // checks for repeating operations by checking if the currentChar is an operation
        // and the last character of currentToken is an operation.
        // this is to pick up the more unknown tokens such as "====" or "---".
        if (isOperationChar(currentChar) && isOperationChar(currentToken.charAt(currentToken.length() - 1))){
            // if true then add currentChar to the token.
            updateToken();
        } else {
            // if the above doesn't occur then create the error and end token.

            // checks if the error is of type UNKNOWN_SYNTAX.
            if (isInTokenArray(SYNTAX_TOKENS, currentToken.substring(0, 1))){
                // if true the creates an error of type UNKNOWN_SYNTAX and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_SYNTAX, lineNumber, characterOffset, "Unknown syntax."));
            } else if (isNumber(currentToken.substring(0, 1))){
                // checks if the error is of type UNKNOWN_NUMBER_FORMAT.
                // if true the creates an error of type UNKNOWN_NUMBER_FORMAT and adds it to the lexErrors arraylist.
                lexErrors.add(new LexError(LexError.UNKNOWN_NUMBER_FORMAT, lineNumber, characterOffset, "Unknown number"));
            }
            endToken(); // ends the key token.
        }
    }

    public void endToken(){
        // adds the currentToken to the lexTokens.
        if (tokenType != LexToken.UNKNOWN){
            lexTokens.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
        }

        // updates the characterOffset to the start of the nextToken and resets currentToken.
        characterOffset += currentToken.length();
        currentToken = "";
    }

    public int getTokenType(){
        // checks the token type.
        // checks keyword before identifier since tokens like "if" would meet identifier criteria but needs to be keyword.
        // likewise with typename, we check it before identifier to stop tokens like "int" being classified incorrectly.
        // if the currentToken is not recognised as any of the token types we want then return UNKNOWN type.
        if(isInTokenArray(KEYWORDS, currentToken)) return LexToken.KEYWORD;
        else if(isInTokenArray(SYNTAX_TOKENS, currentToken)) return LexToken.SYNTAX_TOKEN;
        else if(isInTokenArray(TYPE_NAMES, currentToken)) return LexToken.TYPE_NAME;
        else if(isIdentifier()) return LexToken.IDENTIFIER;
        else if(isNumber(currentToken)) return LexToken.LITERAL_NUMBER;
        return LexToken.UNKNOWN;
    }

    public boolean isInTokenArray(String[] array, String token){
        boolean isElement = false; // initialises isElement to false.

        // checks each element in array.
        for (String element : array){
            // if the element is equal to the token then isElement is set to true and the loop is broken.
            if (token.equals(element)) {
                isElement = true;
                break;
            }
        }

        return isElement; // returns isElement.
    }

    public boolean isIdentifier(){
        // an identifier cannot start with number and cannot contain any whitespace or syntax characters.

        if (currentToken.isEmpty()) return false;

        // checks if the first character of currentToken is a number and returns false if it is.
        if(Character.isDigit(currentToken.charAt(0))) return false;

        // checks if the currentToken contains any whitespace characters and returns false if it does.
        if(currentToken.contains(" ")
                || currentToken.contains("\n")
                || currentToken.contains("\r")
                || currentToken.contains("\t"))
            return false;


        boolean isIdentifier = true; // initialises isIdentifier.

        // loops through the possible syntax tokens.
        for(String syntaxToken : SYNTAX_TOKENS){
            // if the currentToken contains syntaxToken then isIdentifier is set to false and the loop is broken.
            if(currentToken.contains(syntaxToken)){
                isIdentifier = false;
                break;
            }
        }

        return isIdentifier; // returns isIdentifier.
    }

    public boolean isNumber(String string){
        // this fragment is used to find out if a string is numeric.
        // found at https://sentry.io/answers/how-to-check-if-a-string-is-numeric-in-java/
        try{
            // tries to parse string into an integer.
            Integer.parseInt(string);

            // if it works then return true since string is a number.
            // numbers begin with 0's so return false if the first char of string is 0 and true otherwise.
            return string.charAt(0) != '0';
        } catch (NumberFormatException e){
            // if there is an exception then returns false since string cannot be a number.
            return false;
        }
    }

    public boolean isOperationChar(char character){
        // returns true if character is '+', '-', '*', '/' or '=' and false otherwise.
        return (character == '+' || character == '-' || character == '*' || character == '/' || character == '=');
    }

    public boolean isNotValidChar(char character){
        // returns true if character is not a terminating char, operation, digit, letter or escape char.
        return !(isTerminatingChar(character) || isOperationChar(character)
                || isValidDigitChar(character) || isValidEscapeChar(character)
                || isValidLetterChar(character));
    }

    public boolean isValidEscapeChar(char character){
        // returns true if character is 'r', 'n', 't', '"' or '\\' and false otherwise.
        return character == 'r' || character == 'n' || character == 't' || character == '"' || character == '\\';
    }

    public boolean isValidLetterChar(char character){
        // returns true if character is alphabetic and false otherwise.
        return Character.isAlphabetic(character);
    }

    public boolean isValidDigitChar(char character){
        // returns true if character is a digit and false otherwise.
        return Character.isDigit(character);
    }

    public boolean isWhitespace(char character){
        // returns true if character is '\r', '\n', '\t' or ' ' and false otherwise.
        return character == '\r' || character == '\n' || character == '\t' || character == ' ';
    }

    public boolean isTerminatingChar(char character){
        // returns true if character is whitespace, ',', ';', '(', ')', '{', '}', '[' or ']' and false otherwise.
        return (isWhitespace(character) || character == ',' || character == ';'
                || character == '(' || character == ')' || character == '{'
                || character == '}' || character == '[' || character == ']');
    }

    public void advanceChar(){
        // increments charIndex by 1.
        charIndex++;
    }

    public void updateToken(){
        // adds currentChar to currentToken.
        currentToken += currentChar;
    }

    public void updateCurrentChar(){
        // sets currentChar equal to the character at charIndex in input.
        currentChar = input.charAt(charIndex);
    }

    public boolean charExists(int index){
        // returns true if the charIndex is strictly smaller than the length of input and false otherwise.
        return index < input.length();
    }

    public List<LexToken> getLexTokens(){
        return lexTokens; // returns lexTokens.
    }

    public List<LexError> getLexErrors(){
        return lexErrors; // returns lexErrors.
    }
}
