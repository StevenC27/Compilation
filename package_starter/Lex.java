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
		/*
			STRING LITERAL:
				- check for " and put into string mode.
				- append each character to the token.
				- check for closing " to end string mode and end the token.
			GENERAL:
				- if there is white space then end the token
				- if the token is empty then add the current char into the token.

			IDENTIFIER:
				- if there is syntax then end the token.

			+SYNTAX:
				- if the syntax is anything but = then it ends after the single character.
				- if the syntax is = then the next character is checked and must be another = to create == token.
				- if the syntax is == then the next character is checked and if it is another = then it is an unknown token.

			+KEYWORD:
				- if the current token is a keyword but the next token is another alphabetic or digit character then the token is not a keyword
					for example "ifg" is an identifier.

			NUMERIC LITERALS:
				- must be an uninterrupted sequence of numeric characters from 0...9.

			+TYPE NAMES:
				- if the current token is a keyword but the next token is another alphabetic or digit character then the token is not a keyword
					for example "intq" is an identifier.

			---- PLAN ----
			STEP 1: check the type of the current token.
			STEP 2: check that the token should end.
			STEP 3: create a new token of the correct type and add it to the list.
		*/

		ArrayList<LexToken> tokenArrayList = new ArrayList<>();
		String currentToken = "";
		int lineNumber = 1;
		int characterOffset = 0;
		boolean stringMode = false;
		boolean tokenEnd = false;
		int tokenType = LexToken.UNKNOWN;

		for(int i = 0; i < stream.length(); i++) {
			char currentChar = stream.charAt(i);

			if (currentChar == '"' && !stringMode){
				stringMode = true;
				tokenType = LexToken.LITERAL_STRING;
				tokenEnd = true;
			}

			if (stringMode){
				if (currentChar == '"'){
					tokenEnd = true;
				}
			} else{
				//STEP 1: check the type of the current token.
				tokenType = checkToken(currentToken);
				if (tokenType == LexToken.KEYWORD || tokenType == LexToken.TYPE_NAME){
					if (!(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar))){
						tokenEnd = true;
					}
				} else if (tokenType == LexToken.SYNTAX_TOKEN){
					if (!((currentToken == "=" || currentToken == "==") && currentChar == '=')){
						tokenEnd = true;
					}
				} else if (tokenType == LexToken.IDENTIFIER){
					if (checkTokenArray(syntax_tokens, Character.toString(currentChar))){
						tokenEnd = true;
					}
				} else if (tokenType == LexToken.LITERAL_NUMBER){
					if (!Character.isDigit(currentChar)){
						tokenEnd = true;
					}
				}

				if (currentChar == '\r'){
					lineNumber++;
					tokenEnd = true;
				} else if (currentChar == '\n'){
					characterOffset = 0;
					tokenEnd = true;
				} else if (currentChar == '\t'){
					characterOffset += 4;
					tokenEnd = true;
				} else if (currentChar == ' '){
					characterOffset++;
					tokenEnd = true;
				}
			}



			if (tokenEnd){
				tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
				characterOffset += currentToken.length();
				tokenEnd = false;
				currentToken = "";
				if (!Character.isSpaceChar(currentChar)){
					currentToken+=currentChar;
				}
			} else {
				currentToken += currentChar;
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

	boolean checkDigit(String token){
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

	boolean checkTokenArray(String[] array, String token){
		boolean isElement = false;
		for (String element : array){
			if (token.equals(element)) {
				isElement = true;
				break;
			}
		}
		return isElement;
	}

	int checkToken(String token){
		if(checkTokenArray(keywords, token)) return LexToken.KEYWORD;
		else if(checkTokenArray(syntax_tokens, token)) return LexToken.SYNTAX_TOKEN;
		else if(checkTokenArray(types, token)) return LexToken.TYPE_NAME;
		else if(checkIdentifier(token)) return LexToken.IDENTIFIER;
		else if(checkDigit(token)) return LexToken.LITERAL_NUMBER;
		return LexToken.UNKNOWN;
	}
}
