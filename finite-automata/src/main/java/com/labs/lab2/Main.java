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

        System.out.println("The automaton is DFA: " + automaton.isDFA());

        System.out.println();
        System.out.println("Conversion to DFA:");
        System.out.println(automaton.toDFA());
        System.out.println("The automaton is DFA: " + automaton.toDFA().isDFA());

        System.out.println("Converted to Grammar:");
        Grammar grammar = automaton.toGrammar();
        System.out.println(grammar);

        System.out.println("Grammar type: " + grammar.getCategory());

        try {
            automaton.toImage("graph1.png");
            automaton.toDFA().toImage("graph2.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




        
    }
}
