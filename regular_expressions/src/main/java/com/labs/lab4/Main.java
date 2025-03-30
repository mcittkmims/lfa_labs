package com.labs.lab4;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        RegExCombinationGenerator generator = new RegExCombinationGenerator();


        String regex1 = "(S|T)(u|v)w*y+24";
        List<String> combinations1 = generator.generateCombinations(regex1);
        System.out.println("Combinations for " + regex1 + ":");
        printCombinations(combinations1);
        System.out.println();


        String regex2 = "L(M|N)O^3P*Q(2|3)";
        List<String> combinations2 = generator.generateCombinations(regex2);
        System.out.println("\nCombinations for " + regex2 + ":");
        printCombinations(combinations2);
        System.out.println();


        String regex3 = "R*s(T|U|V)W(x|y|z)^2";
        List<String> combinations3 = generator.generateCombinations(regex3);
        System.out.println("\nCombinations for " + regex3 + ":");
        printCombinations(combinations3);
        System.out.println();
    }


    private static void printCombinations(List<String> combinations) {
        int maxToPrint = Math.min(10, combinations.size());
        for (int i = 0; i < maxToPrint; i++) {
            System.out.println(combinations.get(i));
        }

        if (combinations.size() > maxToPrint) {
            System.out.println("... and " + (combinations.size() - maxToPrint) + " more combinations");
        }

        System.out.println("Total combinations: " + combinations.size());
    }
}