package com.labs.lab2;

import net.sourceforge.plantuml.SourceStringReader;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FiniteAutomaton {
    private List<String> alphabet;
    private List<String> states;
    private String initialState;
    private Set<String> finalStates;
    private Map<Transition, List<String>> transitions;

    public FiniteAutomaton(List<String> alphabet, List<String> states, String initialState, Set<String> finalStates,
                           Map<Transition, List<String>> transitions) {
        this.alphabet = alphabet;
        this.states = states;
        this.initialState = initialState;
        this.finalStates = finalStates;
        this.transitions = transitions;
    }

    public boolean stringBelongToLanguage(final String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return false;
        }
        return stringBelongToLanguage(Arrays.asList(this.initialState), inputString);
    }

    public boolean stringBelongToLanguage(List<String> possibleStates, final String inputString) {
        if (possibleStates == null) {
            return false;
        }
        if (inputString.isEmpty()) {
            return possibleStates.stream().anyMatch(finalStates::contains);
        }
        for (String possibleState : possibleStates) {
            Transition transition = new Transition(possibleState, Character.toString(inputString.charAt(0)));
            if (stringBelongToLanguage(this.transitions.get(transition), inputString.substring(1))) {
                return true;
            }
        }
        return false;
    }

    public Grammar toGrammar() {
        List<String> terminals = new ArrayList<>(this.alphabet);
        List<String> nonTerminals = new ArrayList<>(this.states);
        String start = this.initialState;
        Map<String, List<String>> rules = new HashMap<>();
        for (Transition transition : this.transitions.keySet()) {
            rules.putIfAbsent(transition.getState(), new ArrayList<>());
            rules.get(transition.getState()).addAll(
                    this.transitions.get(transition).stream()
                            .map(s -> transition.getInput() + s)
                            .collect(Collectors.toList()));
            rules.get(transition.getState()).addAll(
                    this.transitions.get(transition).stream()
                            .filter(finalStates::contains)
                            .map(s -> transition.getInput())
                            .collect(Collectors.toList()));
        }
        for (String finalState : finalStates) {
            rules.putIfAbsent(finalState, new ArrayList<>());
            rules.get(finalState).add("");
        }
        return new Grammar(terminals, nonTerminals, rules, start);
    }

    public boolean isDFA() {
        return this.transitions.keySet().stream()
                .allMatch(s -> !s.getInput().equals("") && this.transitions.get(s).size() == 1);
    }

    public FiniteAutomaton toDFA() {
        Set<Set<String>> q = new HashSet<>();
        Set<String> sigma = new HashSet<>(alphabet);
        Map<Transition, List<String>> dfaTransitions = new HashMap<>();
        Set<String> initialStateSet = new HashSet<>(Collections.singleton(initialState));
        q.add(initialStateSet);
        Queue<Set<String>> statesToProcess = new LinkedList<>();
        statesToProcess.add(initialStateSet);
        Set<Set<String>> qF = new HashSet<>();

        while (!statesToProcess.isEmpty()) {
            Set<String> currentState = statesToProcess.poll();
            for (String finalState : finalStates) {
                if (currentState.contains(finalState)) {
                    qF.add(currentState);
                    break;
                }
            }
            for (String symbol : sigma) {
                Set<String> nextState = new HashSet<>();
                for (String state : currentState) {
                    Transition transition = new Transition(state, symbol);
                    if (transitions.containsKey(transition)) {
                        nextState.addAll(transitions.get(transition));
                    }
                }
                if (!nextState.isEmpty()) {
                    String fromState = String.join("_", currentState);
                    String toState = String.join("_", nextState);
                    dfaTransitions.put(new Transition(fromState, symbol), Collections.singletonList(toState));
                    if (!q.contains(nextState)) {
                        q.add(nextState);
                        statesToProcess.add(nextState);
                    }
                }
            }
        }

        List<String> dfaStates = q.stream().map(s -> String.join("_", s)).collect(Collectors.toList());
        Set<String> dfaFinalStates = qF.stream().map(s -> String.join("_", s)).collect(Collectors.toSet());

        return new FiniteAutomaton(new ArrayList<>(sigma), dfaStates, String.join("_", initialStateSet), dfaFinalStates, dfaTransitions);
    }

    @Override
    public String toString() {
        String result = "Q = {" + String.join(", ", this.states) + "}";
        result += "\nΣ = {" + String.join(" ,", this.alphabet) + "}";
        result += "\nq0 = " + this.initialState;
        result += "\nF = {" + String.join(", ", this.finalStates) + "}";
        for (Transition transition : this.transitions.keySet()) {
            result += "\n\t" + transition + " = {" + String.join(", ", this.transitions.get(transition)) + "}";
        }
        return result;
    }

    public void toImage(String filePath) throws IOException {
        StringBuilder plantUml = new StringBuilder("@startuml\n");
        plantUml.append("hide empty description\n");
        plantUml.append("skinparam state {\n  BackgroundColor LightBlue\n  BorderColor Black\n}\n");

        // Define initial state
        plantUml.append("[*] --> ").append(initialState).append("\n");

        // Define final states (without the exit transition)
        for (String finalState : finalStates) {
            plantUml.append(finalState).append(" : Final State\n");
        }

        // Define transitions
        for (Map.Entry<Transition, List<String>> entry : transitions.entrySet()) {
            Transition transition = entry.getKey();
            for (String destination : entry.getValue()) {
                plantUml.append(transition.getState()).append(" --> ")
                        .append(destination).append(" : ").append(transition.getInput()).append("\n");
            }
        }

        plantUml.append("@enduml");

        // Generate image
        SourceStringReader reader = new SourceStringReader(plantUml.toString());
        FileOutputStream output = new FileOutputStream(new File(filePath));
        reader.generateImage(output);
        output.close();
    }


}
