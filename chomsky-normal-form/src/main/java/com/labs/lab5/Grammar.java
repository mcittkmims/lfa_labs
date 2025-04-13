package com.labs.lab5;
import java.util.*;


public class Grammar {
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<String, Set<String>> productionRules;
    private String startSymbol;


    public Grammar(Set<String> nonTerminals, Set<String> terminals, Map<String, Set<String>> productionRules, String startSymbol) {
        this.nonTerminals = new HashSet<>(nonTerminals);
        this.terminals = new HashSet<>(terminals);
        this.productionRules = new HashMap<>();

        // Deep copy of the production rules
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            this.productionRules.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Verify start symbol is a non-terminal
        if (!nonTerminals.contains(startSymbol)) {
            throw new IllegalArgumentException("Start symbol must be a non-terminal");
        }
        this.startSymbol = startSymbol;

        validateGrammar();
    }



    private void validateGrammar() {
        // Check if all keys in productionRules are in nonTerminals
        for (String nonTerminal : productionRules.keySet()) {
            if (!nonTerminals.contains(nonTerminal)) {
                throw new IllegalArgumentException("Production rule contains undefined non-terminal: " + nonTerminal);
            }
        }

        // Check if all symbols in productions are either terminals or non-terminals
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            for (String production : entry.getValue()) {
                if (production.equals("ε")) continue; // Empty production (epsilon) is allowed

                String[] symbols = production.split(" ");
                for (String symbol : symbols) {
                    if (!terminals.contains(symbol) && !nonTerminals.contains(symbol) && !symbol.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Production contains undefined symbol: " + symbol +
                                        " in rule: " + entry.getKey() + " -> " + production
                        );
                    }
                }
            }
        }
    }


    public Grammar removeNullProductions() {
        // Step 1: Find all nullable non-terminals
        Set<String> nullableNonTerminals = findNullableNonTerminals();

        // Step 2 & 3: Add new productions by omitting nullable non-terminals
        Map<String, Set<String>> newProductionRules = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            String lhs = entry.getKey();
            Set<String> newProductions = new HashSet<>();

            for (String production : entry.getValue()) {
                if (production.equals("ε")) continue; // Skip the empty production for now

                // Generate all possible new productions by omitting nullable non-terminals
                List<String> symbols = Arrays.asList(production.split(" "));
                List<String> generatedProductions = generateAllPossibleProductions(symbols, nullableNonTerminals);

                newProductions.addAll(generatedProductions);
            }

            // If the original rule had an empty production and we didn't add any empty production
            // from the generation process, we need to keep the original empty production
            if (entry.getValue().contains("ε") && !nullableNonTerminals.contains(lhs)) {
                newProductions.add("ε");
            }

            newProductionRules.put(lhs, newProductions);
        }

        // Step 4: Remove all direct null productions unless the start symbol can derive empty
        for (Map.Entry<String, Set<String>> entry : newProductionRules.entrySet()) {
            entry.getValue().removeIf(production -> production.equals("ε"));
        }

        return new Grammar(nonTerminals, terminals, newProductionRules, startSymbol);
    }


    private Set<String> findNullableNonTerminals() {
        Set<String> nullableNonTerminals = new HashSet<>();
        boolean changed;

        // First, find all non-terminals that directly derive empty string
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            if (entry.getValue().contains("ε")) {
                nullableNonTerminals.add(entry.getKey());
            }
        }

        // Then, iteratively find more nullable non-terminals
        do {
            changed = false;

            for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
                if (nullableNonTerminals.contains(entry.getKey())) continue;

                for (String production : entry.getValue()) {
                    if (production.equals("ε")) continue;

                    String[] symbols = production.split(" ");
                    boolean allNullable = true;

                    for (String symbol : symbols) {
                        if (!nullableNonTerminals.contains(symbol)) {
                            allNullable = false;
                            break;
                        }
                    }

                    if (allNullable) {
                        nullableNonTerminals.add(entry.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);

        return nullableNonTerminals;
    }


    private List<String> generateAllPossibleProductions(List<String> symbols, Set<String> nullableNonTerminals) {
        List<String> result = new ArrayList<>();
        generateProductionsHelper(symbols, nullableNonTerminals, 0, new ArrayList<>(), result);
        return result;
    }


    private void generateProductionsHelper(List<String> symbols, Set<String> nullableNonTerminals,
                                           int index, List<String> current, List<String> result) {
        if (index == symbols.size()) {
            // Avoid adding empty production here, handled separately
            if (!current.isEmpty()) {
                result.add(String.join(" ", current));
            } else {
                // Add "ε" for empty production instead of empty string
                result.add("ε");
            }
            return;
        }

        String symbol = symbols.get(index);

        // Include the current symbol
        current.add(symbol);
        generateProductionsHelper(symbols, nullableNonTerminals, index + 1, current, result);
        current.remove(current.size() - 1);

        // Exclude the current symbol if it's nullable
        if (nullableNonTerminals.contains(symbol)) {
            generateProductionsHelper(symbols, nullableNonTerminals, index + 1, current, result);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Start Symbol: ").append(startSymbol).append("\n");
        sb.append("Non-terminals: ").append(nonTerminals).append("\n");
        sb.append("Terminals: ").append(terminals).append("\n");
        sb.append("Production Rules:\n");

        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            sb.append(entry.getKey()).append(" -> ");
            boolean first = true;
            for (String production : entry.getValue()) {
                if (!first) {
                    sb.append(" | ");
                }
                sb.append(production);
                first = false;
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Set<String> getNonTerminals() {
        return new HashSet<>(nonTerminals);
    }


    public Set<String> getTerminals() {
        return new HashSet<>(terminals);
    }

    public Map<String, Set<String>> getProductionRules() {
        Map<String, Set<String>> copy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Grammar removeInaccessibleSymbols() {
        // Step 1 & 2: Find all accessible symbols
        Set<String> accessibleSymbols = findAccessibleSymbols();

        // Step 3: Remove inaccessible symbols and their rules
        Set<String> newNonTerminals = new HashSet<>();
        Set<String> newTerminals = new HashSet<>();
        Map<String, Set<String>> newProductionRules = new HashMap<>();

        // Keep only accessible non-terminals
        for (String nonTerminal : nonTerminals) {
            if (accessibleSymbols.contains(nonTerminal)) {
                newNonTerminals.add(nonTerminal);
            }
        }

        // Keep only accessible terminals
        for (String terminal : terminals) {
            if (accessibleSymbols.contains(terminal)) {
                newTerminals.add(terminal);
            }
        }

        // Keep only production rules for accessible non-terminals
        for (String nonTerminal : newNonTerminals) {
            Set<String> productions = productionRules.getOrDefault(nonTerminal, new HashSet<>());
            Set<String> newProductions = new HashSet<>();

            // Keep only productions with accessible symbols
            for (String production : productions) {
                if (production.equals("ε")) {
                    newProductions.add(production);
                    continue;
                }

                String[] symbols = production.split(" ");
                boolean allSymbolsAccessible = true;

                for (String symbol : symbols) {
                    if (!accessibleSymbols.contains(symbol)) {
                        allSymbolsAccessible = false;
                        break;
                    }
                }

                if (allSymbolsAccessible) {
                    newProductions.add(production);
                }
            }

            if (!newProductions.isEmpty()) {
                newProductionRules.put(nonTerminal, newProductions);
            }
        }

        return new Grammar(newNonTerminals, newTerminals, newProductionRules, startSymbol);
    }

    private Set<String> findAccessibleSymbols() {
        Set<String> accessibleSymbols = new HashSet<>();
        Set<String> accessibleNonTerminals = new HashSet<>();

        // Start with the start symbol
        accessibleSymbols.add(startSymbol);
        accessibleNonTerminals.add(startSymbol);

        boolean changed;
        do {
            changed = false;
            Set<String> newAccessibleSymbols = new HashSet<>();

            // For each accessible non-terminal
            for (String nonTerminal : accessibleNonTerminals) {
                Set<String> productions = productionRules.getOrDefault(nonTerminal, new HashSet<>());

                // For each production rule
                for (String production : productions) {
                    if (production.equals("ε")) continue;

                    // For each symbol in the production
                    String[] symbols = production.split(" ");
                    for (String symbol : symbols) {
                        if (!accessibleSymbols.contains(symbol)) {
                            newAccessibleSymbols.add(symbol);
                            changed = true;

                            // If the symbol is a non-terminal, add it to accessible non-terminals
                            if (nonTerminals.contains(symbol)) {
                                accessibleNonTerminals.add(symbol);
                            }
                        }
                    }
                }
            }

            // Add all newly found accessible symbols
            accessibleSymbols.addAll(newAccessibleSymbols);

        } while (changed);

        return accessibleSymbols;
    }

    public Grammar toChomskyNormalForm() {
        // Create copies to modify
        Set<String> newNonTerminals = new HashSet<>(nonTerminals);
        Set<String> newTerminals = new HashSet<>(terminals);
        Map<String, Set<String>> newProductionRules = new HashMap<>();

        // Deep copy of the production rules
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            newProductionRules.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Step 1: Replace terminals in mixed productions
        // For each production of form A -> α where α contains both terminals and non-terminals
        int terminalNonTerminalCounter = 1;
        Map<String, String> terminalToNonTerminal = new HashMap<>();

        // First pass: create new non-terminals for terminals
        for (String terminal : terminals) {
            String newNonTerminal = "X" + terminalNonTerminalCounter++;
            terminalToNonTerminal.put(terminal, newNonTerminal);

            // Add a new production rule X -> terminal
            Set<String> newProduction = new HashSet<>();
            newProduction.add(terminal);
            newProductionRules.put(newNonTerminal, newProduction);
            newNonTerminals.add(newNonTerminal);
        }

        // Second pass: replace terminals in mixed productions
        Map<String, Set<String>> updatedRules = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : newProductionRules.entrySet()) {
            String lhs = entry.getKey();
            Set<String> productions = entry.getValue();
            Set<String> modifiedProductions = new HashSet<>();

            for (String production : productions) {
                // Skip if this is a production X -> terminal (added earlier)
                if (!production.contains(" ") && terminals.contains(production)) {
                    modifiedProductions.add(production);
                    continue;
                }

                String[] symbols = production.split(" ");
                StringBuilder modifiedProduction = new StringBuilder();
                boolean modified = false;

                // Check if we need to replace any terminal in a mixed production
                for (int i = 0; i < symbols.length; i++) {
                    String symbol = symbols[i];

                    // If this is a terminal in a production with multiple symbols
                    if (terminals.contains(symbol) && symbols.length > 1) {
                        modifiedProduction.append(terminalToNonTerminal.get(symbol));
                        modified = true;
                    } else {
                        modifiedProduction.append(symbol);
                    }

                    if (i < symbols.length - 1) {
                        modifiedProduction.append(" ");
                    }
                }

                if (modified) {
                    modifiedProductions.add(modifiedProduction.toString());
                } else {
                    modifiedProductions.add(production);
                }
            }

            if (!modifiedProductions.isEmpty()) {
                updatedRules.put(lhs, modifiedProductions);
            }
        }

        newProductionRules = updatedRules;

        // Step 2: Break longer productions into binary productions
        // Use an iterative approach instead of recursion to avoid stack overflow

        // Map to store production patterns to their corresponding non-terminal
        Map<String, String> productionPatternMap = new HashMap<>();
        int longProductionCounter = 1;
        boolean madeChanges;

        do {
            madeChanges = false;
            updatedRules = new HashMap<>();

            // Process each non-terminal and its productions
            for (Map.Entry<String, Set<String>> entry : newProductionRules.entrySet()) {
                String lhs = entry.getKey();
                Set<String> productions = entry.getValue();
                Set<String> newProductions = new HashSet<>();

                for (String production : productions) {
                    String[] symbols = production.split(" ");

                    // If the production has 2 or fewer symbols, keep it as is
                    if (symbols.length <= 2) {
                        newProductions.add(production);
                        continue;
                    }

                    madeChanges = true;

                    // Handle productions with more than 2 symbols
                    // We'll convert A -> B1 B2 B3...Bn to A -> B1 Y and Y -> B2 B3...Bn
                    String firstSymbol = symbols[0];

                    // Create the remainder string (B2 B3...Bn)
                    StringBuilder remainderBuilder = new StringBuilder();
                    for (int i = 1; i < symbols.length; i++) {
                        remainderBuilder.append(symbols[i]);
                        if (i < symbols.length - 1) {
                            remainderBuilder.append(" ");
                        }
                    }
                    String remainder = remainderBuilder.toString();

                    // Check if we already have a non-terminal for this pattern
                    String newNonTerminal;
                    if (productionPatternMap.containsKey(remainder)) {
                        newNonTerminal = productionPatternMap.get(remainder);
                    } else {
                        newNonTerminal = "Y" + longProductionCounter++;
                        productionPatternMap.put(remainder, newNonTerminal);
                        newNonTerminals.add(newNonTerminal);

                        // Create a production rule for the new non-terminal
                        if (!updatedRules.containsKey(newNonTerminal)) {
                            updatedRules.put(newNonTerminal, new HashSet<>());
                        }
                        updatedRules.get(newNonTerminal).add(remainder);
                    }

                    // Add the new binary production: A -> B1 Y
                    newProductions.add(firstSymbol + " " + newNonTerminal);
                }

                updatedRules.put(lhs, newProductions);
            }

            // Update the production rules for the next iteration
            newProductionRules = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : updatedRules.entrySet()) {
                newProductionRules.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }

        } while (madeChanges);

        return new Grammar(newNonTerminals, newTerminals, newProductionRules, startSymbol);
    }

    public Grammar removeUnitProductions() {
        // Step 1: Compute unit production closures for each non-terminal
        Map<String, Set<String>> unitClosure = new HashMap<>();

        // Initialize each non-terminal's closure with itself
        for (String nonTerminal : nonTerminals) {
            Set<String> closure = new HashSet<>();
            closure.add(nonTerminal);
            unitClosure.put(nonTerminal, closure);
        }

        // Compute the transitive closure of unit productions
        boolean changed;
        do {
            changed = false;
            for (String nonTerminal : nonTerminals) {
                Set<String> closure = unitClosure.get(nonTerminal);
                Set<String> newMembers = new HashSet<>();

                for (String member : closure) {
                    Set<String> productions = productionRules.getOrDefault(member, new HashSet<>());
                    for (String production : productions) {
                        // Check if the production is a unit production (single non-terminal)
                        if (!production.contains(" ") && nonTerminals.contains(production)) {
                            newMembers.add(production);
                        }
                    }
                }

                // Add all new members to the closure
                for (String newMember : newMembers) {
                    if (!closure.contains(newMember)) {
                        closure.add(newMember);
                        changed = true;
                    }
                }
            }
        } while (changed);

        // Step 2 & 3: Create new production rules by replacing unit productions
        Map<String, Set<String>> newProductionRules = new HashMap<>();

        for (String nonTerminal : nonTerminals) {
            Set<String> newProductions = new HashSet<>();
            Set<String> unitProductions = unitClosure.get(nonTerminal);

            for (String unit : unitProductions) {
                Set<String> productions = productionRules.getOrDefault(unit, new HashSet<>());
                for (String production : productions) {
                    // Only add non-unit productions
                    if (production.contains(" ") || !nonTerminals.contains(production) || production.equals("ε")) {
                        newProductions.add(production);
                    }
                }
            }

            newProductionRules.put(nonTerminal, newProductions);
        }

        return new Grammar(nonTerminals, terminals, newProductionRules, startSymbol);
    }

    public Grammar removeNonProductiveSymbols() {
        // Step 1 & 2: Find all productive non-terminals
        Set<String> productiveNonTerminals = findProductiveNonTerminals();

        // Step 3 & 4: Remove non-productive non-terminals and their rules
        Set<String> newNonTerminals = new HashSet<>(productiveNonTerminals);
        Map<String, Set<String>> newProductionRules = new HashMap<>();

        // Copy production rules for productive non-terminals
        for (String nonTerminal : productiveNonTerminals) {
            Set<String> productions = productionRules.getOrDefault(nonTerminal, new HashSet<>());
            Set<String> newProductions = new HashSet<>();

            // Step 5: Keep only productions with productive symbols
            for (String production : productions) {
                if (production.equals("ε")) {
                    newProductions.add(production);
                    continue;
                }

                String[] symbols = production.split(" ");
                boolean allSymbolsProductive = true;

                for (String symbol : symbols) {
                    if (nonTerminals.contains(symbol) && !productiveNonTerminals.contains(symbol)) {
                        allSymbolsProductive = false;
                        break;
                    }
                }

                if (allSymbolsProductive) {
                    newProductions.add(production);
                }
            }

            if (!newProductions.isEmpty()) {
                newProductionRules.put(nonTerminal, newProductions);
            }
        }

        return new Grammar(newNonTerminals, terminals, newProductionRules, startSymbol);
    }

    private Set<String> findProductiveNonTerminals() {
        Set<String> productiveNonTerminals = new HashSet<>();
        boolean changed;

        // First, find all non-terminals that directly derive terminal strings
        for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
            String nonTerminal = entry.getKey();
            for (String production : entry.getValue()) {
                if (production.equals("ε")) {
                    // Empty string is considered a terminal string
                    productiveNonTerminals.add(nonTerminal);
                    break;
                }

                String[] symbols = production.split(" ");
                boolean allTerminals = true;

                for (String symbol : symbols) {
                    if (nonTerminals.contains(symbol)) {
                        allTerminals = false;
                        break;
                    }
                }

                if (allTerminals) {
                    productiveNonTerminals.add(nonTerminal);
                    break;
                }
            }
        }

        // Then, iteratively find more productive non-terminals
        do {
            changed = false;

            for (Map.Entry<String, Set<String>> entry : productionRules.entrySet()) {
                String nonTerminal = entry.getKey();
                if (productiveNonTerminals.contains(nonTerminal)) continue;

                for (String production : entry.getValue()) {
                    if (production.equals("ε")) {
                        productiveNonTerminals.add(nonTerminal);
                        changed = true;
                        break;
                    }

                    String[] symbols = production.split(" ");
                    boolean allProductive = true;

                    for (String symbol : symbols) {
                        if (nonTerminals.contains(symbol) && !productiveNonTerminals.contains(symbol)) {
                            allProductive = false;
                            break;
                        }
                    }

                    if (allProductive) {
                        productiveNonTerminals.add(nonTerminal);
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);

        return productiveNonTerminals;
    }
}