package com.labs.lab1;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FiniteAutomaton {
    private List<String> alphabet;
    private List<String> states;
    private String initialState;
    private String finalState;
    private Map<Transition, List<String>> transitions;

    public FiniteAutomaton(List<String> alphabet, List<String> states, String initialState, String finalState,
            Map<Transition, List<String>> transitions) {
        this.alphabet = alphabet;
        this.states = states;
        this.initialState = initialState;
        this.finalState = finalState;
        this.transitions = transitions;
    }

    public boolean stringBelongToLanguage(final String inputString) {
        List<String> possibleCurrentStates = Arrays.asList(initialState);
        boolean stateFound;

        for (int i = 0; i < inputString.length(); i++) {
            stateFound = false;
            for (String possibleCurrentState : possibleCurrentStates) {
                Transition transition = new Transition(possibleCurrentState, Character.toString(inputString.charAt(i)));
                if (transitions.get(transition) != null) {
                    possibleCurrentStates = transitions.get(transition);
                    stateFound = true;
                    break;
                }

            }
            if (!stateFound) {
                return false;
            }
        }

        if (possibleCurrentStates.contains(finalState)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "" + this.transitions;
    }

}
