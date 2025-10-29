public class LexHelper {
    public static final String[] KEYWORDS = {"if", "else", "while", "return"};
    public static final String[] SYNTAX_TOKENS = {"=", "==", "*", "/", "+", "-", "(", ")", "[", "]", "{", "}", ";", ","};
    public static final String[] TYPE_NAMES = {"int", "float", "string", "bool"};

    public static boolean isTokenEnd(String currentToken, char currentChar, int tokenType){
        if (tokenType == LexToken.KEYWORD || tokenType == LexToken.TYPE_NAME){
            if (!(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar))){
                return true;
            }
        } else if (tokenType == LexToken.SYNTAX_TOKEN){
            if (!(currentToken.endsWith(Character.toString(currentChar)) && isOperation(currentChar))){
                return true;
            }
        } else if (tokenType == LexToken.IDENTIFIER){
            if (LexHelper.checkTokenArray(LexHelper.SYNTAX_TOKENS, Character.toString(currentChar))){
                return true;
            }
        } else if (tokenType == LexToken.LITERAL_NUMBER){
            if (!Character.isDigit(currentChar)){
                return true;
            }
        } else if (tokenType == LexToken.UNKNOWN){
            return true;
        }

        return currentChar == '\r' || currentChar == '\n' || currentChar == '\t' || currentChar == ' ';
    }

    public static boolean isOperation(char character){
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

    public static int checkToken(String token){
        if(checkTokenArray(KEYWORDS, token)) return LexToken.KEYWORD;
        else if(checkTokenArray(SYNTAX_TOKENS, token)) return LexToken.SYNTAX_TOKEN;
        else if(checkTokenArray(TYPE_NAMES, token)) return LexToken.TYPE_NAME;
        else if(checkIdentifier(token)) return LexToken.IDENTIFIER;
        else if(checkDigit(token)) return LexToken.LITERAL_NUMBER;
        return LexToken.UNKNOWN;
    }

    public static boolean checkTokenArray(String[] array, String token){
        boolean isElement = false;
        for (String element : array){
            if (token.equals(element)) {
                isElement = true;
                break;
            }
        }
        return isElement;
    }

    public static boolean checkDigit(String token){
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
}
