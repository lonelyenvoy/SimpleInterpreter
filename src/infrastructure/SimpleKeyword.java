package infrastructure;

public enum SimpleKeyword {
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    DEFINE("define"),
    DO("do"),
    FUNCTION("function"),
    LIST("list");

    private final String literal;

    public String getLiteral() {
        return literal;
    }

    SimpleKeyword(String literal) {
        this.literal = literal;
    }
}
