package com.labs.lab1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grammar {

    private List<String> terminals;
    private List<String> nonTerminals;
    private Map<String, List<String>> rules;
    private String start;
    private Random random = new Random();

    public Grammar(List<String> terminals, List<String> nonTerminals,
            Map<String, List<String>> rules, String start) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.rules = rules;
        this.start = start;
    }

    public String generateString() {
        String result = start;
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

    public FiniteAutomaton toFiniteAutomaton() {
        List<String> alphabet = new ArrayList<>(this.terminals);
        List<String> states = new ArrayList<>(this.nonTerminals);
        states.add("X");
        String initialState = this.start;
        String finalState = "X";
        Map<Transition, List<String>> transitions = new HashMap<>();

        for (String nonTerminal : rules.keySet()) {
            for (String result : rules.get(nonTerminal)) {
                Transition transition = new Transition(nonTerminal, Character.toString(result.charAt(0)));
                transitions.putIfAbsent(transition, new ArrayList<>());
                transitions.get(transition).add(Character.toString((result.length() > 1) ? result.charAt(1) : 'X'));
            }
        }
        
        return new FiniteAutomaton(alphabet, states, initialState, finalState, transitions);

        
    }

    private boolean hasNonTerminals(String string) {

        return nonTerminals.stream().anyMatch(string::contains);
    }


}
