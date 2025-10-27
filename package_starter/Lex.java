import java.util.*;

public class Lex {
	String stream;
	private static final String[] keywords = {"if", "else", "while", "return"};
	private static final String[] syntax_tokens = {"=", "==", "*", "/", "+", "-", "(", ")", "[", "]", "{", "}", ";", ","};
	private static final String[] types = {"int", "float", "string", "bool"};

	Lex(String input) {
		stream = input;
	}
	
	LexToken[] getTokens() {
		ArrayList<LexToken> tokenArrayList = new ArrayList<>();
		String currentToken = "";
		int lineNumber = 1;
		int characterOffset = 0;
		int currentOffset = 0;
		boolean stringMode = false;

		for(int i = 0; i < stream.length(); i++) {
			char currentChar = stream.charAt(i);
			int tokenType = checkToken(currentToken);
			if (currentChar == '"'){
				if(stringMode){
					tokenArrayList.add(new LexToken(LexToken.LITERAL_STRING, lineNumber, characterOffset, currentToken));
					currentOffset++;
					characterOffset = currentOffset;
					currentToken = "";
				} else {
					currentOffset++;
				}
				stringMode = !stringMode;
			} else if (stringMode){
				currentToken += currentChar;
				currentOffset++;
			}

            if (tokenType == LexToken.IDENTIFIER) {
				if (!(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar))){
					tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
					currentOffset++;
					characterOffset = currentOffset;
					currentToken = "";
				} else {
					currentToken += currentChar;
					currentOffset++;
				}
			} else if (tokenType == LexToken.SYNTAX_TOKEN){
				tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
				currentOffset++;
				characterOffset = currentOffset;
				currentToken = "";
			} else if (tokenType == LexToken.KEYWORD || tokenType == LexToken.TYPE_NAME){
				if (Character.isDigit(currentChar) || Character.isAlphabetic(currentChar)){
					currentToken += currentChar;
					currentOffset++;
				} else {
					tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
					currentOffset++;
					characterOffset = currentOffset;
					currentToken = "";
				}
			} else if (currentToken.isEmpty()){
				if (Character.isDigit(currentChar)){
					currentToken += currentChar;
					tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
					currentOffset++;
					characterOffset = currentOffset;
					currentToken = "";
				}else{
					currentToken += currentChar;
					currentOffset++;
				}
			} else {
				currentToken += currentChar;
				currentOffset++;
			}

            if (currentChar == '\r' || currentChar == '\n' || currentChar == '\t' || currentChar == ' ') {
                if (currentChar == '\r') currentOffset = 0;
                if (currentChar == '\n') lineNumber++;
                if (currentChar == '\t') currentOffset += 4;
                if (currentChar == ' ') currentOffset++;
                currentToken = "";
            }
		}
		LexToken[] tokens = new LexToken[tokenArrayList.size()];
		tokens = tokenArrayList.toArray(tokens);
		return tokens;
	}
	
	LexError[] getErrors() {
		return null;
	}

	boolean checkIdentifier(String token){
		if(token.isEmpty()) return false;
		if(Character.isDigit(token.charAt(0))) return false;
		if(token.contains(" ") || token.contains("\n") || token.contains("\r") || token.contains("\t"))
			return false;
		boolean isIdentifier = true;
		for(String syntax_token : syntax_tokens){
			if(token.contains(syntax_token)){
				isIdentifier = false;
				break;
			}
		}
		return isIdentifier;
	}

	boolean checkTokenArray(String[] array, String token){
		boolean isSyntax = false;
		for (String element : array){
			if (token.equals(element)) {
				isSyntax = true;
				break;
			}
		}
		return isSyntax;
	}

	int checkToken(String token){
		if(checkTokenArray(keywords, token)) return LexToken.KEYWORD;
		else if(checkTokenArray(syntax_tokens, token)) return LexToken.SYNTAX_TOKEN;
		else if(checkTokenArray(types, token)) return LexToken.TYPE_NAME;
		else if(checkIdentifier(token)) return LexToken.IDENTIFIER;
		return LexToken.UNKNOWN;
	}
}
