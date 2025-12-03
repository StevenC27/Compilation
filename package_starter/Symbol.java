public class Symbol {
    private String type;
    private String identifier;
    private int lineNumber;
    private int characterOffset;

    public Symbol(String type, String identifier, int lineNumber, int characterOffset){
        this.type = type;
        this.identifier = identifier;
    }

    public String getType() {
        return type; // gets the type of the symbol.
    }
    public String getIdentifier() {
        return identifier; // gets the identifier of the symbol.
    }
    public int getLineNumber() {
        return lineNumber; // gets the line of the symbol.
    }
    public int getCharacterOffset() {
        return characterOffset; // gets the offset of the symbol.
    }

}
