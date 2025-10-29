import java.util.*;

public class Lex {
	String stream;

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
		boolean returnChar = false;
		boolean lineChar = false;
		boolean tabChar = false;
		boolean spaceChar = false;

		int tokenType = LexToken.UNKNOWN;

		for(int i = 0; i < stream.length(); i++) {
			char currentChar = stream.charAt(i);

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
				//STEP 1: check the type of the current token.
				tokenType = LexHelper.checkToken(currentToken);

				//STEP 2: check if the token should end.
				tokenEnd = LexHelper.isTokenEnd(currentToken, currentChar, tokenType);

				if (currentChar == '\r'){
					returnChar = true;
				} else if (currentChar == '\n'){
					lineChar = true;
				} else if (currentChar == '\t'){
					tabChar = true;
				} else if (currentChar == ' '){
					spaceChar = true;
				}
			}

			if (tokenEnd){
				if (currentToken.isEmpty()){
					tokenEnd = false;
				} else {
					tokenArrayList.add(new LexToken(tokenType, lineNumber, characterOffset, currentToken));
					characterOffset += currentToken.length();
					tokenEnd = false;
					currentToken = "";
				}

				if (tokenType == LexToken.LITERAL_STRING && !stringMode){
					characterOffset += 2;
				}

				if (returnChar){
					lineNumber++;
					returnChar = false;
				} else if (lineChar){
					characterOffset = 0;
					lineChar = false;
				} else if (tabChar){
					characterOffset += 4;
					tabChar = false;
				} else if (spaceChar){
					characterOffset++;
					spaceChar = false;
				}


				if (!(currentChar == '\r' || currentChar == '\n' || currentChar == '\t' || currentChar == ' ' || currentChar == '"')){
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
}
