package com.labs.lab2;

import java.util.*;
import java.util.regex.Pattern;

public class Grammar {

    private List<String> terminals;
    private List<String> nonTerminals;
    private Map<String, List<String>> rules;
    private String start;
    private Random random = new Random();

    public Grammar(List<String> terminals, List<String> nonTerminals,
                   Map<String, List<String>> rules, String start) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        this.rules = rules;
        this.start = start;
    }

    public String generateString() {
        String result = start;
        while (hasNonTerminals(result)) {
            for (String nonTerminal : nonTerminals) {
                if (result.contains(nonTerminal)) {
                    result = result.replaceFirst(nonTerminal,
                            rules.get(nonTerminal).get(random.nextInt(rules.get(nonTerminal).size())));
                    continue;
                }
            }
        }
        return result;
    }

    public FiniteAutomaton toFiniteAutomaton() {
        List<String> alphabet = new ArrayList<>(this.terminals);
        List<String> states = new ArrayList<>(this.nonTerminals);
        states.add("X");
        String initialState = this.start;
        String finalState = "X";
        Map<Transition, List<String>> transitions = new HashMap<>();

        for (String nonTerminal : rules.keySet()) {
            for (String result : rules.get(nonTerminal)) {
                // String input = (Character.isLowerCase(result.charAt(0))) ? Character.toString(result.charAt(0)) : "";
                // Transition transition = new Transition(nonTerminal, input);
                Transition transition = new Transition(nonTerminal, Character.toString(result.charAt(0)));
                transitions.putIfAbsent(transition, new ArrayList<>());
                transitions.get(transition).add(Character.toString((result.length() > 1) ? result.charAt(1) : 'X'));
            }
        }
        
        return new FiniteAutomaton(alphabet, states, initialState, Set.of(finalState), transitions);

        
    }

    private boolean hasNonTerminals(String string) {

        return nonTerminals.stream().anyMatch(string::contains);
    }

    public String getCategory(){
        if(this.rules.keySet().stream()
                .allMatch(t -> this.nonTerminals.stream()
                        .filter(s -> t.contains(s) && t.replaceFirst(s, "").isEmpty()).count() == 1)){
            String endsWithRegex = "^(?:" + String.join("|", this.terminals) + ")*(?:" + String.join("|", this.nonTerminals) + ")$";
            String startsWithRegex = "^(" + String.join("|", this.nonTerminals) + ")(?:" + String.join("|", this.terminals) + ")*$";
            String terminalsOnlyRegex = "^(?:" + String.join("|", this.terminals) + ")+$";
            if (this.rules.values().stream()
                    .allMatch(l -> l.stream().allMatch(s -> (Pattern.compile(endsWithRegex).matcher(s).matches() || s.isEmpty() || Pattern.compile(terminalsOnlyRegex).matcher(s).matches()) )) ||
                this.rules.values().stream()
                    .allMatch(l -> l.stream().allMatch(s -> (Pattern.compile(startsWithRegex).matcher(s).matches() || s.isEmpty() || Pattern.compile(terminalsOnlyRegex).matcher(s).matches()) ))){
                return "Type 3";
            }
            return "Type 2";

        }

        if(this.rules.values().stream().anyMatch(l -> l.stream().anyMatch(String::isEmpty))){
            return "Type 0";
        }

        return "Type 1";

    }

    @Override
    public String toString() {

        String result = "G = (Vn, Vt, S, P)";
        result = result + "\nVn = {" + String.join(", ", nonTerminals) + "}";
        result = result + "\nVt = {" + String.join(", ", terminals) + "}";
        result = result + "\nP = {";
        for (String rule : this.rules.keySet()) {
            result = result + "\n\t" + rule + " --> " + String.join(" | ", this.rules.get(rule));
        }
        result = result + "\n    }";
        return result;

    }


}
