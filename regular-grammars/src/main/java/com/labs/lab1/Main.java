package com.labs.lab1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        List<String> terminals = Arrays.asList("a", "b");
        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("bA"));
        rules.put("A", Arrays.asList("b", "aB", "bA"));
        rules.put("C", Arrays.asList("cA"));
        rules.put("B", Arrays.asList("bC", "aB"));

        Grammar grammar = new Grammar(terminals, nonTerminals, rules, "S");
        System.out.println(grammar.generateString());
        System.out.println(grammar.generateString());
        System.out.println(grammar.generateString());
        System.out.println(grammar.generateString());
        System.out.println(grammar.generateString());
        FiniteAutomaton automaton = grammar.toFiniteAutomaton();
        System.out.println(automaton.stringBelongToLanguage("bbbaabcabcbabcabcbbabcabcabcaabcabcbabcbaaabcbbabcb"));
    }
}
