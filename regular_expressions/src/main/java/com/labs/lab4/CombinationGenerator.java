package com.labs.lab4;

import java.util.ArrayList;
import java.util.List;

public class CombinationGenerator {
    private static final int MAX_REPETITIONS = 5;

    public List<String> generateCombinations(List<Token> tokens) {
        List<List<String>> allPossibilities = new ArrayList<>();

        for (Token token : tokens) {
            List<String> possibilities = new ArrayList<>();

            switch (token.getType()) {
                case LITERAL:
                    possibilities.add(token.getValues()[0]);
                    break;

                case ALTERNATION:
                    for (String option : token.getValues()) {
                        possibilities.add(option);
                    }
                    break;

                case ZERO_OR_MORE:
                    possibilities.add(""); // Zero occurrences
                    for (int i = 1; i <= MAX_REPETITIONS; i++) {
                        possibilities.add(repeat(token.getValues()[0], i));
                    }
                    break;

                case ONE_OR_MORE:
                    for (int i = 1; i <= MAX_REPETITIONS; i++) {
                        possibilities.add(repeat(token.getValues()[0], i));
                    }
                    break;

                case POWER:
                    // For power operator, generate exactly the specified count
                    possibilities.add(repeat(token.getValues()[0], token.getCount()));
                    break;

                case GROUP_ZERO_OR_MORE:
                    possibilities.add(""); // Zero occurrences
                    List<String> baseOptions = new ArrayList<>();
                    for (String option : token.getValues()) {
                        baseOptions.add(option);
                    }

                    // Add 1 to MAX_REPETITIONS repetitions
                    for (int rep = 1; rep <= MAX_REPETITIONS; rep++) {
                        List<String> currentRep = generateRepeatedOptions(baseOptions, rep);
                        possibilities.addAll(currentRep);
                    }
                    break;

                case GROUP_ONE_OR_MORE:
                    baseOptions = new ArrayList<>();
                    for (String option : token.getValues()) {
                        baseOptions.add(option);
                    }

                    // Add 1 to MAX_REPETITIONS repetitions
                    for (int rep = 1; rep <= MAX_REPETITIONS; rep++) {
                        List<String> currentRep = generateRepeatedOptions(baseOptions, rep);
                        possibilities.addAll(currentRep);
                    }
                    break;

                case GROUP_POWER:
                    baseOptions = new ArrayList<>();
                    for (String option : token.getValues()) {
                        baseOptions.add(option);
                    }

                    // Add exactly count repetitions
                    List<String> exactRep = generateRepeatedOptions(baseOptions, token.getCount());
                    possibilities.addAll(exactRep);
                    break;
            }

            allPossibilities.add(possibilities);
        }

        return combineAllPossibilities(allPossibilities);
    }


    private List<String> generateRepeatedOptions(List<String> options, int count) {
        List<String> result = new ArrayList<>();
        generateRepeatedOptionsHelper(options, count, "", result);
        return result;
    }

    private void generateRepeatedOptionsHelper(List<String> options, int remainingCount,
                                               String current, List<String> result) {
        if (remainingCount == 0) {
            result.add(current);
            return;
        }

        for (String option : options) {
            generateRepeatedOptionsHelper(options, remainingCount - 1, current + option, result);
        }
    }

    private String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private List<String> combineAllPossibilities(List<List<String>> allPossibilities) {
        List<String> result = new ArrayList<>();
        combineHelper(allPossibilities, 0, "", result);
        return result;
    }

    private void combineHelper(List<List<String>> allPossibilities, int index, String current, List<String> result) {
        if (index == allPossibilities.size()) {
            result.add(current);
            return;
        }

        for (String possibility : allPossibilities.get(index)) {
            combineHelper(allPossibilities, index + 1, current + possibility, result);
        }
    }
}