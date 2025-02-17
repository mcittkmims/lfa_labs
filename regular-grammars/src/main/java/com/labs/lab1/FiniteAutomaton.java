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
        if (inputString == null || inputString.isEmpty()) {
            return false;
        }

        return stringBelongToLanguage(Arrays.asList(this.initialState),inputString);
    }

    public boolean stringBelongToLanguage(List<String> possibleStates, final String inputString) {
        if (possibleStates == null) {
            return false;
        }

        if (inputString.isEmpty()) {
            return possibleStates.contains(this.finalState);
        }

        for (String possibleState : possibleStates) {
            Transition transition = new Transition(possibleState, Character.toString(inputString.charAt(0)));
            if (stringBelongToLanguage(this.transitions.get(transition), inputString.substring(1))) {
                return true;
            }
        }


        return false;
        
    }

    @Override
    public String toString() {
        String result = "Q = {" + String.join(", ", this.states) + "}";
        result = result + "\nΣ = {" + String.join(" ,", this.alphabet) + "}";
        result = result + "\nq0 = " + this.initialState;
        result = result + "\nF = " + this.finalState;
        for (Transition transition : this.transitions.keySet()) {
            result = result + "\n\t" + transition + " = {" + String.join(", ", this.transitions.get(transition)) + "}";
        }
        return result;
    }

}
