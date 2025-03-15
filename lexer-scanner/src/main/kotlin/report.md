# Topic: Lexer & Scanner

### Course: Formal Languages & Finite Automata
### Author: Adrian Vremere

----

## Theory
A **lexer** (short for lexical analyzer) is a fundamental component of a compiler or interpreter that processes an input sequence of characters and converts it into a sequence of **tokens**. This process is known as **lexical analysis**.

### Lexical Analysis
Lexical analysis is the first phase of compilation or interpretation. It involves:
1. **Reading the input source code** character by character.
2. **Grouping characters into meaningful lexemes** (words, numbers, symbols).
3. **Converting lexemes into tokens**, which are categorized representations used by the parser.

### Tokens and Lexemes
- **Lexeme**: A sequence of characters that forms a unit in the source code (e.g., `if`, `while`, `42`, `+`).
- **Token**: A structured representation of a lexeme, usually in the form `<TOKEN_TYPE, VALUE>`.

Example:

```Lexeme: if → Token: <KEYWORD, "if"> Lexeme: 42 → Token: <NUMBER, 42> Lexeme: + → Token: <OPERATOR, "+">```


### Lexer Components
A lexer typically consists of:
1. **Input Buffer**: Stores the source code text.
2. **Scanner**: Reads characters and identifies lexemes.
3. **Tokenizer**: Assigns tokens to lexemes based on predefined rules.
4. **Symbol Table**: Stores identifiers and keywords.

### Role of Regular Expressions
Lexers often use **regular expressions (regex)** to define token patterns.  
For example:
- `\d+` → Matches integers (e.g., `42`, `100`).
- `[a-zA-Z_][a-zA-Z0-9_]*` → Matches identifiers (e.g., `myVariable`, `_count`).

### Example of a Simple Lexer
A simple lexer for arithmetic expressions might tokenize:

```3 + 5 * 2```

into:

```<NUMBER, 3> <OPERATOR, +> <NUMBER, 5> <OPERATOR, *> <NUMBER, 2>```

A lexer simplifies code processing by converting raw text into structured tokens. This token stream is then fed into the **parser**, which checks for syntax correctness and builds the program’s structure.

## Objectives:
1. Understand what lexical analysis [1] is.
2. Get familiar with the inner workings of a lexer/scanner/tokenizer.
3. Implement a sample lexer and show how it works.

## Syntax Implementation

I decided to create a lexer for a domain-specific language for web scraping with support for selectors, variables, conditions, and loops.

### Basic Structure
Programs consist of statements that end with semicolons:
```
statement1;
statement2;
statement3;
```

### Comments
```
// Single line comment

/* 
   Multi-line 
   comment
*/
```

### Variables
#### Declaration
```
var name = value;
```

#### Reference
```
$name
```

#### Data Types
- String: `"text"`
- Number: `42` or `3.14`
- Boolean: `true` or `false`
- List: Collection returned by selectors or created manually

### Selectors
#### CSS Selectors
```
select(".class-name");
select("#element-id");
select("div.container > p");
```

#### XPath Selectors
```
xpath("//div[@class='item']");
xpath("//a[contains(@href, 'example.com')]");
```

### Data Extraction
Chain these methods after a selector:

#### Get Text Content
```
select(".title").text();
```

#### Get Attribute Value
```
select("a").attr("href");
```

#### Get HTML Content
```
select(".container").html();
```

### Control Flow
#### If Statements
```
if (condition) {
  // statements
}

if (condition) {
  // statements if true
} else {
  // statements if false
}
```

#### For Loops
```
for (item in collection) {
  // statements
}
```

### Built-in Functions

#### Fetch a Web Page
```
fetch("https://example.com");
```

#### Save Data to File
```
save(data, "filename.txt");
```

#### Print to Console
```
print("message");
print($variable);
```

### Operators

#### Arithmetic
- Addition: `+`
- Subtraction: `-`
- Multiplication: `*`
- Division: `/`

#### Comparison
- Equal to: `==`
- Not equal to: `!=`
- Greater than: `>`
- Less than: `<`
- Greater than or equal: `>=`
- Less than or equal: `<=`

#### Logical
- AND: `&&`
- OR: `||`
- NOT: `!`

## Code Implementation

```kotlin
enum class TokenType {
    // Keywords
    VAR, IF, ELSE, FOR, IN,
    
    // Functions
    FETCH, SELECT, XPATH, TEXT, ATTR, HTML, SAVE, PRINT,
    
    // Literals
    IDENTIFIER, STRING, NUMBER, BOOLEAN,
    
    // Operators and Symbols (abbreviated)
    // ...
}
```

This enumeration defines all possible token types in our DSL. It organizes tokens into logical categories including keywords (like `var` and `if`), built-in functions (like `fetch` and `select`), literals, operators, and symbols. This comprehensive classification enables the lexer to properly identify and categorize each piece of the source code during analysis.

```kotlin
data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int
)
```

The Token class represents a single meaningful unit of code. Each token stores not just its classification (type) and actual text (lexeme), but also precise location information (line and column) for error reporting. This positional data proves invaluable during later compilation phases when providing helpful feedback to users about syntax errors.

```kotlin
class Lexer(private val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private var column = 1
    private val tokens = mutableListOf<Token>()
    
    private val keywords = mapOf(
        "var" to TokenType.VAR,
        "if" to TokenType.IF,
        // Additional mappings...
    )
```

The Lexer class maintains critical state information while processing source code. It tracks the current position with `start` and `current` indices, allowing it to identify token boundaries. Line and column counters ensure accurate position reporting. The keywords map provides quick lookup for reserved words in the language, distinguishing them from regular identifiers.

```kotlin
fun tokenize(): List<Token> {
    while (!isAtEnd()) {
        start = current
        scanToken()
    }
    
    tokens.add(Token(TokenType.EOF, "", line, column))
    return tokens
}
```

This function orchestrates the entire tokenization process. It iterates through the source code, marking the start of each new token and invoking the scanner to identify it. Once complete, it appends an EOF (end-of-file) token to signal the end of input and returns the collected token stream. This clean design separates the high-level control flow from the detailed token recognition logic.

```kotlin
private fun scanToken() {
    val c = advance()
    
    when (c) {
        '(' -> addToken(TokenType.LEFT_PAREN)
        ')' -> addToken(TokenType.RIGHT_PAREN)
        // Additional single-character cases...
        
        '/' -> {
            // Comment handling logic (abbreviated)
        }
        
        '"' -> string()
        
        else -> {
            if (isDigit(c)) number()
            else if (isAlpha(c)) identifier()
            else addToken(TokenType.UNKNOWN)
        }
    }
}
```

The scanner examines each character to determine what type of token it represents. Simple tokens like parentheses are handled directly, while complex patterns like comments require additional logic. For specialized token types like strings, numbers, and identifiers, the scanner delegates to dedicated methods. This structured approach makes the code both readable and maintainable, with clear handling for each possible character case.

```kotlin
if (match('/')) {
    // Single line comment
    while (peek() != '\n' && !isAtEnd()) {
        advance()
    }
    addToken(TokenType.COMMENT)
} else if (match('*')) {
    // Multi-line comment logic (abbreviated)
} else {
    addToken(TokenType.DIVIDE)
}
```

This fragment demonstrates how the lexer distinguishes between division operators and comments. After encountering a forward slash, it looks ahead to determine the token type: another slash indicates a single-line comment, an asterisk signals a multi-line comment, and anything else is treated as division. For comments, the lexer consumes characters until reaching the appropriate terminator (newline or `*/`).

```kotlin
private fun string() {
    while (peek() != '"' && !isAtEnd()) {
        if (peek() == '\n') {
            line++
            column = 0
        }
        advance()
    }
    
    // Closing quote and token creation (abbreviated)
}
```

The string handler processes text between quotation marks. It carefully consumes characters until finding a closing quote, accounting for newlines within the string. This method also checks for unterminated strings (reaching the end of file without a closing quote), providing an opportunity for error reporting. This attention to detail ensures correct handling of even complex string literals.

```kotlin
private fun number() {
    // Integer part
    while (isDigit(peek())) advance()
    
    // Decimal part (abbreviated)
    
    addToken(TokenType.NUMBER)
}

private fun identifier() {
    while (isAlphaNumeric(peek())) advance()
    
    val text = source.substring(start, current)
    val type = keywords[text] ?: TokenType.IDENTIFIER
    
    addToken(type)
}
```

These handlers process multi-character tokens with specific patterns. The number handler handles both integer and decimal numbers, while the identifier handler collects sequences of letters, digits, and underscores. The identifier method also checks if the resulting text matches a keyword, intelligently determining the appropriate token type. This demonstrates how the lexer handles context-dependent token classification.

```kotlin
private fun peek(): Char {
    return if (isAtEnd()) '\u0000' else source[current]
}

private fun isAlpha(c: Char): Boolean {
    return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
}

private fun addToken(type: TokenType) {
    val lexeme = source.substring(start, current)
    tokens.add(Token(type, lexeme, line, column - lexeme.length))
}
```

The lexer relies on various helper methods to streamline its operation. Lookahead functions like `peek()` enable character inspection without consumption. Character classification methods like `isAlpha()` provide semantic checking. Token creation helpers ensure consistent handling of lexemes and position information. These well-factored utilities make the main lexing code cleaner and more focused on logical structure rather than implementation details.

The complete implementation creates a robust lexical analyzer that transforms raw source code into a structured token stream. This stream then becomes the input for the parser, which will construct a meaningful representation of the program's structure and logic for further processing.

## Conclusions / Screenshots / Results

### Results

#### Input Code

```
// Fetch a webpage
fetch("https://example.com");

// Select all product items
var products = select(".product-item");

// Extract data from each product
for (product in products) {
  var name = product.select(".name").text();
  var price = product.select(".price").text();
  var url = product.select("a").attr("href");
  
  if (price < 100) {
    print("Found affordable product: " + name);
    // Save to our results
    save(name + "," + price + "," + url, "affordable_products.csv");
  }
}
```
The lexer processes the input code by identifying various language components and generating corresponding tokens. It correctly identifies comments as `COMMENT` tokens, such as `// Fetch a webpage` and `// Select all product items`. Keywords like `fetch`, `var`, and `for` are categorized under their respective types (`FETCH`, `VAR`, `FOR`).

The lexer also distinguishes between operators and delimiters, such as the assignment operator `=` (EQUALS), the parentheses `()` (LEFT_PAREN, RIGHT_PAREN), and the semicolon `;` (SEMICOLON). Additionally, it identifies strings (e.g., `"https://example.com"`) as `STRING` tokens and variables like `products`, `name`, `price`, and `url` as `IDENTIFIER` tokens.

The lexer accurately processes method calls, such as `select(".product-item")`, identifying each part as tokens like `SELECT`, `DOT`, and `STRING`. The `if` statement and function calls like `print()` and `save()` are also tokenized appropriately, with `IF`, `PRINT`, and `SAVE` tokens generated.

The lexer successfully identifies all components, including operators like `+`, comparison operators like `<`, and the EOF token at the end of the input, indicating the completion of the lexing process. Overall, the lexer performs the expected lexical analysis, providing a tokenized representation of the input code.

#### Output

```
Token(type=COMMENT, lexeme=// Fetch a webpage, line=1, column=1)
Token(type=FETCH, lexeme=fetch, line=2, column=0)
Token(type=LEFT_PAREN, lexeme=(, line=2, column=5)
Token(type=STRING, lexeme="https://example.com", line=2, column=6)
Token(type=RIGHT_PAREN, lexeme=), line=2, column=27)
.
.
.
Token(type=EOF, lexeme=, line=18, column=1)
```
### Conclusions

During this lab on **Lexer & Scanner**, I gained a deeper understanding of **lexical analysis, tokenization, and token types**. I successfully set up a **Kotlin** project to implement the required tasks and used **regular expressions** to process input code efficiently. As part of my assignment, I developed a `Lexer` class, which includes methods for recognizing and categorizing different tokens such as keywords, operators, symbols, and literals. I also created a `Token` data class that holds the token type, lexeme, and location information. Throughout this process, I enhanced my knowledge of **lexical rules**, the importance of **token classification**, and how to handle **comments, whitespace**, and **errors**. To further refine my implementation, I ensured the lexer could process different token types like numbers, strings, and identifiers, and it successfully identified **single-line and multi-line comments**. This exercise helped me solidify my understanding of **scanning techniques** and provided practical insights into building a lexer for a programming language.

## References:
[1] [A sample of a lexer implementation](https://llvm.org/docs/tutorial/MyFirstLanguageFrontend/LangImpl01.html)

[2] [Lexical analysis](https://en.wikipedia.org/wiki/Lexical_analysis)

<a id="ref3"></a>[3] Presentation on "Formal Languages and Compiler Design" - conf. univ., dr. Irina Cojuhari -
https://else.fcim.utm.md/pluginfile.php/110457/mod_resource/content/0/Theme_1.pdf
 