# Intro to formal languages. Regular grammars. Finite Automata.

### Course: Formal Languages & Finite Automata
### Author: Adrian Vremere 

----

## Theory

### Formal Languages and Automata

Formal languages are a fundamental concept in computer science, particularly in compiler design and language processing. A **formal language** consists of a set of strings formed from an alphabet, following specific rules.

### Alphabet and Strings
- An **alphabet (Σ)** is a finite, nonempty set of symbols (e.g., Σ = {0,1} for binary).
- A **string (word)** is a finite sequence of symbols from Σ (e.g., 1011).
- The **empty string (ε)** contains no symbols.
- The **length of a string** is denoted by |w| (e.g., |101| = 3).

### Languages
A **language (L)** is a set of strings over an alphabet. It can be finite or infinite:
- Example: L = {w | w contains an equal number of 0’s and 1’s}.
- Operations on languages include **union, intersection, concatenation, and closure**.

### Grammars and Chomsky Hierarchy
A **grammar (G)** is defined as **G = (VN, VT, P, S)**, where:
- **VN**: Non-terminal symbols.
- **VT**: Terminal symbols.
- **P**: Production rules.
- **S**: Start symbol.

The **Chomsky hierarchy** classifies grammars into four types:
1. **Type 0**: Recursively enumerable grammars (most general).
2. **Type 1**: Context-sensitive grammars.
3. **Type 2**: Context-free grammars (CFGs).
4. **Type 3**: Regular grammars (least powerful).

### Finite Automata
A **finite automaton** is an abstract computational model used to recognize languages. It consists of states and transitions based on input symbols.

#### Deterministic Finite Automaton (DFA)
A DFA is defined as **(Q, Σ, δ, q0, F)** where:
- **Q**: Finite set of states.
- **Σ**: Input alphabet.
- **δ**: Transition function (δ: Q × Σ → Q).
- **q0**: Start state.
- **F**: Set of accepting (final) states.

#### Nondeterministic Finite Automaton (NFA)
An NFA allows multiple possible transitions from a state, including **ε-transitions** (moves without consuming input). NFAs can be converted to equivalent DFAs.

#### Regular Expressions and Regular Languages
Regular expressions define **regular languages**, which are languages that can be recognized by finite automata. These expressions use operators like:
- **Concatenation** (e.g., `ab` means "a" followed by "b").
- **Union** (e.g., `a|b` means "a" or "b").
- **Kleene star** (e.g., `a*` means "zero or more repetitions of a").

Finite automata and regular expressions form the foundation for lexical analysis in compilers, pattern matching in text processing, and protocol verification.


##  Objectives:

1. Discover what a language is and what it needs to have in order to be considered a formal one;

2. Provide the initial setup for the evolving project that you will work on during this semester. You can deal with each laboratory work as a separate task or project to demonstrate your understanding of the given themes, but you also can deal with labs as stages of making your own big solution, your own project. Do the following:

    a. Create GitHub repository to deal with storing and updating your project;

    b. Choose a programming language. Pick one that will be easiest for dealing with your tasks, you need to learn how to solve the problem itself, not everything around the problem (like setting up the project, launching it correctly and etc.);

    c. Store reports separately in a way to make verification of your work simpler (duh)

3. According to your variant number, get the grammar definition and do the following:

    a. Implement a type/class for your grammar;

    b. Add one function that would generate 5 valid strings from the language expressed by your given grammar;

    c. Implement some functionality that would convert and object of type Grammar to one of type Finite Automaton;

    d. For the Finite Automaton, please add a method that checks if an input string can be obtained via the state transition from it;


## Implementation description

```
public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<String> terminals = Arrays.asList("a", "b");
        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("bA"));
        rules.put("A", Arrays.asList("b", "aB", "bA"));
        rules.put("C", Arrays.asList("cA"));
        rules.put("B", Arrays.asList("bC", "aB"));

        Grammar grammar = new Grammar(terminals, nonTerminals, rules, "S");
        System.out.println("\nGrammar: ");
        System.out.println(grammar);

        FiniteAutomaton automaton = grammar.toFiniteAutomaton();
        System.out.println("\nFinite Automaton: ");
        System.out.println(automaton);
        
        System.out.println("\n5 generated strings: ");
        for (int i = 0; i < 5; i++) {
            String generatedString = grammar.generateString();
            System.out.println("\n\tGenerated word: " + generatedString);
            System.out.println("\tBelongs to language: " + automaton.stringBelongToLanguage(generatedString));
        }

        System.out.println("\nDo you want to check a string? (y/n)");
        String input = scanner.nextLine();
        if (input.equals("y")){
            while (true) {
                System.out.print("Enter the word(empty line to exit): ");
                input = scanner.nextLine();
                if (input.isBlank()) {
                    break;
                }
                System.out.println("Belongs to language: " + automaton.stringBelongToLanguage(input));
            }
        }
        
    }
}
```

- I define a **context-free grammar** with terminals, non-terminals, and production rules, then convert it into a **finite automaton**.  
- The program **generates random strings**, checks if they belong to the language, and allows users to manually test their own strings.  
- User interaction is handled via **Scanner**, enabling continuous string validation until the user chooses to exit.  


## Conclusions / Screenshots / Results


## References