package com.labs.lab4;

import java.util.List;

public class RegExCombinationGenerator {
    private RegExParser parser;
    private CombinationGenerator generator;

    public RegExCombinationGenerator() {
        this.parser = new RegExParser();
        this.generator = new CombinationGenerator();
    }


    public List<String> generateCombinations(String regex) {
        List<Token> tokens = parser.parse(regex);

        System.out.println("Parsed tokens:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        return generator.generateCombinations(tokens);
    }
}