package com.labs.lab4;

import java.util.ArrayList;
import java.util.List;



public class RegExParser {

    public List<Token> parse(String regex) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            if (c == '(') {
                int closingParenIndex = findClosingParen(regex, i);
                String group = regex.substring(i + 1, closingParenIndex);
                String[] options = group.split("\\|");

                // Check if there's a quantifier after the closing parenthesis
                if (closingParenIndex + 1 < regex.length()) {
                    char nextChar = regex.charAt(closingParenIndex + 1);
                    if (nextChar == '*') {
                        tokens.add(new Token(TokenType.GROUP_ZERO_OR_MORE, options));
                        i = closingParenIndex + 1;
                    } else if (nextChar == '+') {
                        tokens.add(new Token(TokenType.GROUP_ONE_OR_MORE, options));
                        i = closingParenIndex + 1;
                    } else if (nextChar == '^' && closingParenIndex + 2 < regex.length() &&
                            Character.isDigit(regex.charAt(closingParenIndex + 2))) {
                        int count = Character.getNumericValue(regex.charAt(closingParenIndex + 2));
                        tokens.add(new Token(TokenType.GROUP_POWER, options, count));
                        i = closingParenIndex + 2;
                    } else {
                        tokens.add(new Token(TokenType.ALTERNATION, options));
                        i = closingParenIndex;
                    }
                } else {
                    tokens.add(new Token(TokenType.ALTERNATION, options));
                    i = closingParenIndex;
                }
            } else if (i < regex.length() - 1) {
                char nextChar = regex.charAt(i + 1);
                if (nextChar == '*') {
                    tokens.add(new Token(TokenType.ZERO_OR_MORE, new String[]{String.valueOf(c)}));
                    i++;
                } else if (nextChar == '+') {
                    tokens.add(new Token(TokenType.ONE_OR_MORE, new String[]{String.valueOf(c)}));
                    i++;
                } else if (nextChar == '^' && i + 2 < regex.length() && Character.isDigit(regex.charAt(i + 2))) {
                    int count = Character.getNumericValue(regex.charAt(i + 2));
                    tokens.add(new Token(TokenType.POWER, new String[]{String.valueOf(c)}, count));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.LITERAL, new String[]{String.valueOf(c)}));
                }
            } else {
                tokens.add(new Token(TokenType.LITERAL, new String[]{String.valueOf(c)}));
            }
        }

        return tokens;
    }


    private int findClosingParen(String regex, int openingParenIndex) {
        int count = 1;
        for (int i = openingParenIndex + 1; i < regex.length(); i++) {
            if (regex.charAt(i) == '(') {
                count++;
            } else if (regex.charAt(i) == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Mismatched parentheses in regular expression: " + regex);
    }
}