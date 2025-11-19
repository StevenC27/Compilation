import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LexHelper {
    public static final String[] KEYWORDS = {"if", "else", "while", "return"};
    public static final String[] SYNTAX_TOKENS = {"=", "==", "*", "/", "+", "-", "(", ")", "[", "]", "{", "}", ";", ","};
    public static final String[] TYPE_NAMES = {"int", "float", "string", "bool"};
    private final String input;
    private List<LexToken> lexTokens;
    private List<LexError> lexErrors;

    public LexHelper(String input){
        this.input = input;
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

        lexTokens = new ArrayList<>();
        lexErrors = new ArrayList<>();
        String currentToken = "";
        int lineNumber = 1;
        int characterOffset = 0;
        boolean stringMode = false;

        boolean tokenEnd = false;

        int tokenType = LexToken.UNKNOWN;

        for(int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

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
                tokenType = checkToken(currentToken);

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
                    tokenType = checkToken(currentToken);
                    lexTokens.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
                }

            } else {
                currentToken += currentChar;
            }
        }
    }

    public boolean isOperation(char character){
        return (character == '+' || character == '-' || character == '*' || character == '/' || character == '=');
    }

    public static boolean checkIdentifier(String token){
        if(token.isEmpty()) return false;
        if(Character.isDigit(token.charAt(0))) return false;
        if(token.contains(" ") || token.contains("\n") || token.contains("\r") || token.contains("\t"))
            return false;
        boolean isIdentifier = true;
        for(String syntax_token : SYNTAX_TOKENS){
            if(token.contains(syntax_token)){
                isIdentifier = false;
                break;
            }
        }
        return isIdentifier;
    }

    public int checkToken(String token){
        if(checkTokenArray(KEYWORDS, token)) return LexToken.KEYWORD;
        else if(checkTokenArray(SYNTAX_TOKENS, token)) return LexToken.SYNTAX_TOKEN;
        else if(checkTokenArray(TYPE_NAMES, token)) return LexToken.TYPE_NAME;
        else if(checkIdentifier(token)) return LexToken.IDENTIFIER;
        else if(checkDigit(token)) return LexToken.LITERAL_NUMBER;
        return LexToken.UNKNOWN;
    }

    public boolean checkTokenArray(String[] array, String token){
        boolean isElement = false;
        for (String element : array){
            if (token.equals(element)) {
                isElement = true;
                break;
            }
        }
        return isElement;
    }

    public boolean checkDigit(String token){
        boolean isDigit = true;
        if (token.isEmpty()) return false;
        for (char character : token.toCharArray()){
            if (!Character.isDigit(character)){
                isDigit = false;
                break;
            }
        }
        return isDigit;
    }

    public boolean isValidCharacter(char character){
        if (Character.isAlphabetic(character)) return true;
        else if (Character.isDigit(character)) return true;
        else return false;
    }

    public List<LexToken> getLexTokens(){
        return lexTokens;
    }

    public List<LexError> getLexErrors(){
        return lexErrors;
    }
}
