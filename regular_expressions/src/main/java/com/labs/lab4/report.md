# Topic: Regular Expressions

### Course: Formal Languages & Finite Automata
### Author: Adrian Vremere

----

## Theory

Regular expressions (regex) are powerful patterns used to match character combinations in strings. They provide a concise and flexible means for identifying strings of text, such as particular characters, words, or patterns of characters.

### Basic Concepts of Regular Expressions

Regular expressions are built on formal language theory, specifically on the theory of regular languages. A regular language is a set of strings that can be defined by a regular expression or recognized by a finite state automaton.

The fundamental building blocks of regular expressions include:

1. **Literals**: Basic characters that match themselves, like 'a' matches the character 'a'.

2. **Character Classes**: Sets of characters enclosed in square brackets, where any one character from the set can match. For example, [abc] matches either 'a', 'b', or 'c'.

3. **Quantifiers**: Symbols that specify how many instances of the previous element should be matched:
    - `*` (asterisk): Match 0 or more occurrences
    - `+` (plus): Match 1 or more occurrences
    - `?` (question mark): Match 0 or 1 occurrence
    - `{n}`: Match exactly n occurrences
    - `{n,}`: Match at least n occurrences
    - `{n,m}`: Match between n and m occurrences

4. **Alternation**: The pipe symbol `|` acts as a boolean OR, allowing a match with expressions on either side of the pipe.

5. **Grouping**: Parentheses `()` are used to group parts of the expression together, allowing quantifiers to be applied to entire groups.

### Regular Expression Engines

Regular expression engines are implementations that process regular expressions and find matching patterns in text. They typically work in one of two ways:

1. **DFA (Deterministic Finite Automaton)**: These engines build a state machine from the regular expression and run the input through it. DFA engines are generally faster but can be memory-intensive for complex patterns.

2. **NFA (Non-deterministic Finite Automaton)**: These engines try different paths through the regular expression simultaneously (or through backtracking). NFA engines may be slower but are more flexible with features.

Most modern regex implementations use a combination of these approaches, often starting with a NFA model that is converted to a DFA for performance.

### The Formal Definition of Regular Languages

In formal language theory, regular languages are defined recursively:

1. The empty language ∅ is a regular language.
2. For each symbol a in the alphabet, the singleton language {a} is a regular language.
3. If A and B are regular languages, then A∪B (union), A⋅B (concatenation), and A* (Kleene star) are regular languages.
4. No other languages are regular.

This formal definition corresponds directly to the operations available in regular expressions: alternation (union), concatenation, and repetition (Kleene star).

### Regular Expressions in Computing

In computing, regular expressions have been extended beyond their formal definition to include additional features like backreferences, lookaheads, and lookbehinds. These extensions make them more powerful but also more complex.

Regular expressions are used in many contexts:
- Text editors and word processors for search and replace operations
- Data validation in web forms
- Parsing and transforming text in programming
- Pattern matching in data processing
- Lexical analysis in compilers

### Regular Expression Implementations

Different programming languages and tools implement regular expressions with varying syntax and capabilities:

1. **POSIX Basic Regular Expressions (BRE)**: The most basic form, with minimal metacharacters.
2. **POSIX Extended Regular Expressions (ERE)**: Adds more metacharacters like `+` and `?`.
3. **Perl Compatible Regular Expressions (PCRE)**: The most common modern implementation, adding features like lookaheads/lookbehinds.
4. **JavaScript Regular Expressions**: Based on PCRE but with some differences.

Our implementation in this project uses a simplified subset of regular expressions, focusing on the core concepts of literals, alternation, grouping, and repetition.


## Objectives:

1. Write and cover what regular expressions are, what they are used for;

2. Below you will find 3 complex regular expressions per each variant. Take a variant depending on your number in the list of students and do the following:

   a. Write a code that will generate valid combinations of symbols conform given regular expressions (examples will be shown). Be careful that idea is to interpret the given regular expressions dinamycally, not to hardcode the way it will generate valid strings. You give a set of regexes as input and get valid word as an output

   b. In case you have an example, where symbol may be written undefined number of times, take a limit of 5 times (to evade generation of extremely long combinations);

   c. **Bonus point**: write a function that will show sequence of processing regular expression (like, what you do first, second and so on)


## Implementation description

The implementation follows the Single Responsibility Principle, with each component having a clearly defined role. Below, we focus on the key methods that drive the functionality of the application.

### The Parsing Process

The parsing of regular expressions is handled by the `parse()` method in the `RegExParser` class:

```java
public List<Token> parse(String regex) {
    List<Token> tokens = new ArrayList<>();
    for (int i = 0; i < regex.length(); i++) {
        char c = regex.charAt(i);
        
        if (c == '(') {
            int closingParenIndex = findClosingParen(regex, i);
            String group = regex.substring(i + 1, closingParenIndex);
            String[] options = group.split("\\|");
            
            // Check for quantifiers after the closing parenthesis
            if (closingParenIndex + 1 < regex.length()) {
                char nextChar = regex.charAt(closingParenIndex + 1);
                if (nextChar == '*') {
                    tokens.add(new Token(TokenType.GROUP_ZERO_OR_MORE, options));
                    i = closingParenIndex + 1;
                } else if (nextChar == '+') {
                    tokens.add(new Token(TokenType.GROUP_ONE_OR_MORE, options));
                    i = closingParenIndex + 1;
                } else if (nextChar == '^' && closingParenIndex + 2 < regex.length() && 
                           Character.isDigit(regex.charAt(closingParenIndex + 2))) {
                    int count = Character.getNumericValue(regex.charAt(closingParenIndex + 2));
                    tokens.add(new Token(TokenType.GROUP_POWER, options, count));
                    i = closingParenIndex + 2;
                } else {
                    tokens.add(new Token(TokenType.ALTERNATION, options));
                    i = closingParenIndex;
                }
            } else {
                tokens.add(new Token(TokenType.ALTERNATION, options));
                i = closingParenIndex;
            }
        } else if (i < regex.length() - 1) {
            // Process character with potential quantifier
            // ... additional parsing logic ...
        } else {
            tokens.add(new Token(TokenType.LITERAL, new String[]{String.valueOf(c)}));
        }
    }
    
    return tokens;
}
```

This method works by iterating through each character in the regular expression. When it encounters an opening parenthesis, it finds the matching closing parenthesis using a helper method, extracts the content between them, and splits it by the pipe character to get the alternatives. It then checks if there's a quantifier after the group and creates the appropriate token.

For single characters, it checks if the next character is a quantifier and creates the appropriate token based on that. If there's no quantifier, it creates a LITERAL token.

The `findClosingParen` helper method is essential for correctly parsing nested parentheses:

```java
private int findClosingParen(String regex, int openingParenIndex) {
    int count = 1;
    for (int i = openingParenIndex + 1; i < regex.length(); i++) {
        if (regex.charAt(i) == '(') {
            count++;
        } else if (regex.charAt(i) == ')') {
            count--;
            if (count == 0) {
                return i;
            }
        }
    }
    throw new IllegalArgumentException("Mismatched parentheses in regex: " + regex);
}
```

This method keeps track of the nesting level by incrementing a counter for each opening parenthesis and decrementing it for each closing parenthesis. It returns the index of the matching closing parenthesis when the counter reaches zero.

### Generating Combinations

The core functionality for generating combinations is in the `generateCombinations` method of the `CombinationGenerator` class:

```java
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
                possibilities.add(repeat(token.getValues()[0], token.getCount()));
                break;
                
            case GROUP_ZERO_OR_MORE:
                // Handle group repetition with zero or more occurrences
                // ...
                break;
                
            case GROUP_ONE_OR_MORE:
                // Handle group repetition with one or more occurrences
                // ...
                break;
                
            case GROUP_POWER:
                // Handle group repetition with exact count
                // ...
                break;
        }
        
        allPossibilities.add(possibilities);
    }
    
    return combineAllPossibilities(allPossibilities);
}
```

This method processes each token and generates all possible strings that match it. For LITERAL tokens, it simply adds the character to the list of possibilities. For ALTERNATION tokens, it adds each alternative. For quantifier tokens, it generates all possible repetition counts up to the maximum limit.

For group repetition tokens (GROUP_ZERO_OR_MORE, GROUP_ONE_OR_MORE, GROUP_POWER), it uses the `generateRepeatedOptions` method to generate all possible combinations:

```java
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
```

This method uses recursion to generate all possible combinations of the options repeated the specified number of times. It builds up the combinations one position at a time, trying all possible options for each position.

After generating all possibilities for each token, the `combineAllPossibilities` method combines them to form the final combinations:

```java
private List<String> combineAllPossibilities(List<List<String>> allPossibilities) {
    List<String> result = new ArrayList<>();
    combineHelper(allPossibilities, 0, "", result);
    return result;
}

private void combineHelper(List<List<String>> allPossibilities, int index, 
                          String current, List<String> result) {
    if (index == allPossibilities.size()) {
        result.add(current);
        return;
    }
    
    for (String possibility : allPossibilities.get(index)) {
        combineHelper(allPossibilities, index + 1, current + possibility, result);
    }
}
```

This method also uses recursion, building up combinations one token at a time. For each token, it tries all possible strings that match that token. When it has processed all tokens, it adds the resulting combination to the result list.

### Working with Group Repetitions

The handling of group repetitions is one of the more complex aspects of the implementation. For a group repetition like `(a|b)^2`, the implementation needs to generate all possible combinations of the group repeated the required number of times: "aa", "ab", "ba", "bb".

The process begins in the `generateCombinations` method of the `CombinationGenerator` class, where group repetition tokens are handled specially:

```java
case GROUP_POWER:
    baseOptions = new ArrayList<>();
    for (String option : token.getValues()) {
        baseOptions.add(option);
    }
    
    // Add exactly count repetitions
    List<String> exactRep = generateRepeatedOptions(baseOptions, token.getCount());
    possibilities.addAll(exactRep);
    break;
```

For a GROUP_POWER token, it extracts the options and the count, then calls the `generateRepeatedOptions` method to generate all possible combinations of those options repeated the specified number of times.

The `generateRepeatedOptions` method works by delegating to a recursive helper method:

```java
private List<String> generateRepeatedOptions(List<String> options, int count) {
    List<String> result = new ArrayList<>();
    generateRepeatedOptionsHelper(options, count, "", result);
    return result;
}
```

The recursive helper method builds up combinations one position at a time:

```java
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
```

This method tries all possible options for the current position, then recursively processes the next position. When it has filled all positions, it adds the resulting string to the result list.

For example, when processing `(a|b)^2`, the method starts with an empty string. For the first position, it tries "a" and "b", resulting in two partial combinations: "a" and "b". For each of these, it then processes the second position, trying "a" and "b" again. This results in four final combinations: "aa", "ab", "ba", and "bb".

### Putting It All Together

The `generateCombinations` method in the `RegExCombinationGenerator` class ties everything together:

```java
public List<String> generateCombinations(String regex) {
    List<Token> tokens = parser.parse(regex);
    
    // For debugging
    System.out.println("Parsed tokens:");
    for (Token token : tokens) {
        System.out.println(token);
    }
    
    return generator.generateCombinations(tokens);
}
```

This method takes a regular expression string, parses it into tokens using the parser, optionally prints the tokens for debugging, and then generates all valid combinations using the generator.

The method serves as a facade to the complex subsystem of parsing and combination generation, providing a simple interface for clients to use.

### Helper Utility Methods

Several helper methods play important roles in the implementation. The `repeat` method in the `CombinationGenerator` class is a simple utility that repeats a string a specified number of times:

```java
private String repeat(String str, int times) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < times; i++) {
        sb.append(str);
    }
    return sb.toString();
}
```

This method is used for generating repetitions of single characters or strings. For example, `repeat("a", 3)` returns "aaa".

In the `Main` class, the `printCombinations` method helps with displaying the results:

```java
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
```
This method limits the output to the first 10 combinations and shows the total count, which is useful for cases where the number of combinations is very large.
## Conclusions / Screenshots / Results

### Results

To demonstrate the effectiveness of our implementation, we analyzed three example regular expressions. Below, we present the parsed tokens and resulting combinations for each.

#### Example 1: (S|T)(u|v)w*y+24

The parsed tokens for this expression are:
```
Token{type=ALTERNATION, values=[S, T], count=1}
Token{type=ALTERNATION, values=[u, v], count=1}
Token{type=ZERO_OR_MORE, values=[w], count=1}
Token{type=ONE_OR_MORE, values=[y], count=1}
Token{type=LITERAL, values=[2], count=1}
Token{type=LITERAL, values=[4], count=1}
```

This parsing correctly identifies six tokens:
1. An alternation token for (S|T), with options "S" and "T"
2. An alternation token for (u|v), with options "u" and "v"
3. A zero-or-more token for w*
4. A one-or-more token for y+
5. A literal token for 2
6. A literal token for 4

The generator produced 120 combinations, including:
```
Suy24
Suyy24
Suyyy24
Suyyyy24
Suyyyyy24
Suwy24
Suwyy24
Suwyyy24
Suwyyyy24
Suwyyyyy24
```

The large number of combinations (120) is due to the combinatorial explosion from the multiple options and repetitions:
- 2 options for the first token (S or T)
- 2 options for the second token (u or v)
- 6 options for the third token (empty, w, ww, www, wwww, or wwwww)
- 5 options for the fourth token (y, yy, yyy, yyyy, or yyyyy)
- 1 option for the fifth token (2)
- 1 option for the sixth token (4)

This gives us 2 × 2 × 6 × 5 × 1 × 1 = 120 total combinations.

#### Example 2: L(M|N)O^3P*Q(2|3)

The parsed tokens for this expression are:
```
Token{type=LITERAL, values=[L], count=1}
Token{type=ALTERNATION, values=[M, N], count=1}
Token{type=POWER, values=[O], count=3}
Token{type=ZERO_OR_MORE, values=[P], count=1}
Token{type=LITERAL, values=[Q], count=1}
Token{type=ALTERNATION, values=[2, 3], count=1}
```

This parsing correctly identifies six tokens:
1. A literal token for L
2. An alternation token for (M|N), with options "M" and "N"
3. A power token for O^3, with a count of 3
4. A zero-or-more token for P*
5. A literal token for Q
6. An alternation token for (2|3), with options "2" and "3"

The generator produced 24 combinations, including:
```
LMOOOQ2
LMOOOQ3
LMOOOPQ2
LMOOOPQ3
LMOOOPPQ2
LMOOOPPQ3
LMOOOPPPQ2
LMOOOPPPQ3
LMOOOPPPPQ2
LMOOOPPPPQ3
```

The number of combinations (24) can be calculated as:
- 1 option for the first token (L)
- 2 options for the second token (M or N)
- 1 option for the third token (OOO)
- 6 options for the fourth token (empty, P, PP, PPP, PPPP, or PPPPP)
- 1 option for the fifth token (Q)
- 2 options for the sixth token (2 or 3)

This gives us 1 × 2 × 1 × 6 × 1 × 2 = 24 total combinations.

#### Example 3: R*s(T|U|V)W(x|y|z)^2

The parsed tokens for this expression are:
```
Token{type=ZERO_OR_MORE, values=[R], count=1}
Token{type=LITERAL, values=[s], count=1}
Token{type=ALTERNATION, values=[T, U, V], count=1}
Token{type=LITERAL, values=[W], count=1}
Token{type=GROUP_POWER, values=[x, y, z], count=2}
```

This parsing correctly identifies five tokens:
1. A zero-or-more token for R*
2. A literal token for s
3. An alternation token for (T|U|V), with options "T", "U", and "V"
4. A literal token for W
5. A group-power token for (x|y|z)^2, with options "x", "y", and "z" and a count of 2

The generator produced 162 combinations, including:
```
sTWxx
sTWxy
sTWxz
sTWyx
sTWyy
sTWyz
sTWzx
sTWzy
sTWzz
sUWxx
```

This example demonstrates the difference between POWER and GROUP_POWER tokens. The GROUP_POWER token for (x|y|z)^2 doesn't simply repeat a single character; it generates all combinations of the group options repeated twice. For example, "xx", "xy", "xz", "yx", etc.

The number of combinations (162) can be calculated as:
- 6 options for the first token (empty, R, RR, RRR, RRRR, or RRRRR)
- 1 option for the second token (s)
- 3 options for the third token (T, U, or V)
- 1 option for the fourth token (W)
- 9 options for the fifth token (xx, xy, xz, yx, yy, yz, zx, zy, or zz)

This gives us 6 × 1 × 3 × 1 × 9 = 162 total combinations.

These results demonstrate the effectiveness of our implementation in handling various regular expression patterns, from simple literals to complex group repetitions, and confirm the correct functioning of the parsing and combination generation processes.

### Conclusion

This implementation of a Regular Expression Combination Generator demonstrates effective use of the Single Responsibility Principle, with each method having a clear and focused role. The parsing process breaks down the regular expression into tokens that the combination generator can process. The combination generator then uses recursive techniques to efficiently generate all valid combinations.

The key methods—`parse`, `generateCombinations`, `generateRepeatedOptions`, and `combineAllPossibilities`—work together to handle complex regular expressions with various types of repetition. The implementation is clean, maintainable, and efficient, providing a robust solution to the problem of generating valid combinations from regular expressions.

By focusing on the important methods and their interactions, rather than just describing the classes in general, we gain a deeper understanding of how the system works and how the different components collaborate to achieve the desired outcome.


## References
****

<a id="ref3"></a>[1] Presentation on "Formal Languages and Compiler Design" - conf. univ., dr. Irina Cojuhari -
https://else.fcim.utm.md/pluginfile.php/110457/mod_resource/content/0/Theme_1.pdf

<a id="ref4"></a>[2] Presentation on "Regular Language. Finite Automata" - TUM -
https://drive.google.com/file/d/1rBGyzDN5eWMXTNeUxLxmKsf7tyhHt9Jk/view

[3] LLVM - "Kaleidoscope: Kaleidoscope Introduction and the Lexer" - [https://llvm.org/docs/tutorial/MyFirstLanguageFrontend/LangImpl01.html](https://llvm.org/docs/tutorial/MyFirstLanguageFrontend/LangImpl01.html)

[4] Wikipedia - "Lexical Analysis" - [https://en.wikipedia.org/wiki/Lexical_analysis](https://en.wikipedia.org/wiki/Lexical_analysis)

[5] regex101 - https://regex101.com/