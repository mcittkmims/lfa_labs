package com.labs.lab1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<String> terminals = Arrays.asList("a", "b");
        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("bA"));
        rules.put("A", Arrays.asList("b", "aB", "bA"));
        rules.put("C", Arrays.asList("cA"));
        rules.put("B", Arrays.asList("bC", "aB"));

        Grammar grammar = new Grammar(terminals, nonTerminals, rules, "S");
        System.out.println("\nGrammar: ");
        System.out.println(grammar);

        FiniteAutomaton automaton = grammar.toFiniteAutomaton();
        System.out.println("\nFinite Automaton: ");
        System.out.println(automaton);
        
        System.out.println("\n5 generated strings: ");
        for (int i = 0; i < 5; i++) {
            String generatedString = grammar.generateString();
            System.out.println("\n\tGenerated word: " + generatedString);
            System.out.println("\tBelongs to language: " + automaton.stringBelongToLanguage(generatedString));
        }

        System.out.println("\nDo you want to check a string? (y/n)");
        String input = scanner.nextLine();
        if (input.equals("y")){
            while (true) {
                System.out.print("Enter the word(empty line to exit): ");
                input = scanner.nextLine();
                if (input.isBlank()) {
                    break;
                }
                System.out.println("Belongs to language: " + automaton.stringBelongToLanguage(input));
            }
        }
        
    }
}
