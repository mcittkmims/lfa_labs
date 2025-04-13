package com.labs.lab5;
import java.util.*;


public class Main {

    public static void main(String[] args) {
        Set<String> nonTerminals = new HashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> terminals = new HashSet<>(Arrays.asList("a", "b"));

        Map<String, Set<String>> productions2 = new HashMap<>();
        productions2.put("S", new HashSet<>(Arrays.asList("A", "b A", "a B")));
        productions2.put("A", new HashSet<>(Arrays.asList("B", "A S", "b B A B", "b")));
        productions2.put("B", new HashSet<>(Arrays.asList("b", "b S", "a D", "ε")));
        productions2.put("C", new HashSet<>(Arrays.asList("B a")));
        productions2.put("D", new HashSet<>(Arrays.asList("A A")));

        Grammar grammar = new Grammar(nonTerminals, terminals, productions2, "S");

        System.out.println("Original Grammar:");
        System.out.println(grammar);

        Grammar newGrammar = grammar.removeNullProductions().removeUnitProductions().removeNonProductiveSymbols().removeInaccessibleSymbols().toChomskyNormalForm();


        System.out.println("\nGrammar in Chomsky Normal Form:");
        System.out.println(newGrammar);
    }

}

