package com.labs.lab1;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grammar {

    private List<String> terminals;
    private List<String> nonTerminals;
    private Map<String, List<String>> rules;
    private Random random = new Random();

    public Grammar(List<String> terminals, List<String> nonTerminals,
            Map<String, List<String>> rules) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.rules = rules;
    }

    public String generateString() {
        String result = "S";
        while (hasNonTerminals(result)) {
            for (String nonTerminal : nonTerminals) {
                if (result.contains(nonTerminal)) {
                    result = result.replaceFirst(nonTerminal,
                            rules.get(nonTerminal).get(random.nextInt(rules.get(nonTerminal).size())));
                    continue;
                }
            }
        }
        return result;
    }

    private boolean hasNonTerminals(String string) {

        return nonTerminals.stream().anyMatch(string::contains);
    }

}
