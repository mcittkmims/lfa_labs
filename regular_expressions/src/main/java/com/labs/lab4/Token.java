package com.labs.lab4;

public class Token {
    private TokenType type;
    private String[] values;
    private int count; // For '^' quantifier

    public Token(TokenType type, String[] values) {
        this.type = type;
        this.values = values;
        this.count = 1; // Default
    }

    public Token(TokenType type, String[] values, int count) {
        this.type = type;
        this.values = values;
        this.count = count;
    }

    public TokenType getType() {
        return type;
    }

    public String[] getValues() {
        return values;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token{type=").append(type);
        sb.append(", values=[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values[i]);
        }
        sb.append("], count=").append(count).append("}");
        return sb.toString();
    }
}
