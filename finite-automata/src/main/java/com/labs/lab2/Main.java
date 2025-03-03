package com.labs.lab2;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<String> alphabet = Arrays.asList("a", "b");
        List<String> states = Arrays.asList("q0", "q1", "q2");
        Map<Transition, List<String>> transitions = new HashMap<>();
        transitions.put(new Transition("q0", "a"), Arrays.asList("q1", "q0"));
        transitions.put(new Transition("q1", "b"), Arrays.asList("q1"));
        transitions.put(new Transition("q1", "a"), Arrays.asList("q2"));
        transitions.put(new Transition("q2", "b"), Arrays.asList("q2"));
        transitions.put(new Transition("q2", "a"), Arrays.asList("q0"));

        FiniteAutomaton automaton = new FiniteAutomaton(alphabet, states, "q0", Set.of("q2"), transitions);
        System.out.println(automaton);
        Grammar grammar = automaton.toGrammar();
        System.out.println(grammar);

        String word = grammar.generateString();
        System.out.println(word);
        System.out.println(automaton.stringBelongToLanguage(word));
        System.out.println(automaton.isDFA());
        System.out.println(grammar.getCategory());
        System.out.println(automaton.toDFA());
        try {
            automaton.toDFA().toImage("graph.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        List<String> terminals = Arrays.asList("a", "b", "c");
//        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
//        Map<String, List<String>> rules = new HashMap<>();
//        rules.put("S", Arrays.asList("aA"));
//        rules.put("A", Arrays.asList("b", "aB", "aA"));
//        rules.put("C", Arrays.asList("cA"));
//        rules.put("B", Arrays.asList("aC", "bB"));
//
//        Grammar grammar2 = new Grammar(terminals, nonTerminals, rules, "S");
//        System.out.println(grammar2);
//
//
//        FiniteAutomaton automaton2 = grammar2.toFiniteAutomaton();
//        System.out.println(automaton2);
//
//        Grammar grammar3 = automaton2.toGrammar();
//        System.out.println(grammar3);
//
//        FiniteAutomaton automaton = automaton2.toDFA();
//        System.out.println(automaton);

//        System.out.println("\nDo you want to check a string? (y/n)");
//        String input = scanner.nextLine();
//        if (input.equals("y")){
//            while (true) {
//                System.out.print("Enter the word(empty line to exit): ");
//                input = scanner.nextLine();
//                if (input.isBlank()) {
//                    break;
//                }
//                System.out.println("Belongs to language: " + automaton.stringBelongToLanguage(input));
//            }
//        }
        
    }
}
