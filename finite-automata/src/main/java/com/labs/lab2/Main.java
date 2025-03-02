package com.labs.lab2;

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

        FiniteAutomaton automaton = new FiniteAutomaton(alphabet, states, "q0", "q2", transitions);
        System.out.println(automaton);
        Grammar grammar = automaton.toGrammar();
        System.out.println(grammar);

        String word = grammar.generateString();
        System.out.println(word);
        System.out.println(automaton.stringBelongToLanguage(word));
        System.out.println(automaton.isDFA());
        System.out.println(grammar.getCategory());

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
