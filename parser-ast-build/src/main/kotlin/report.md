# Topic: Parser & Building an Abstract Syntax Tree

### Course: Formal Languages & Finite Automata
### Author: Adrian Vremere

----

## Theory

### Parser

A parser is a component in a compiler or interpreter that takes a stream of tokens as input and constructs a syntactic structure based on a formal grammar. It ensures that the sequence of tokens adheres to the rules of the language and organizes them into a structure that represents their hierarchical relationships.

There are two main categories of parsers:

- **Top-down parsers**: Start from the start symbol and try to rewrite it to the input string (e.g., Recursive Descent).
- **Bottom-up parsers**: Start from the input symbols and try to reduce them to the start symbol (e.g., LR parsers).

### Abstract Syntax Tree (AST)

An Abstract Syntax Tree (AST) is a tree-like data structure that represents the abstract syntactic structure of source code. Each node corresponds to a construct in the language, such as expressions, statements, or declarations, but omits irrelevant syntactic details like punctuation.

The AST reflects the logical structure of the code and is used for semantic analysis, optimization, and code generation.

### Building an Abstract Syntax Tree

The AST is typically constructed during the parsing phase or as a direct result of it:

1. **Lexical Analysis**: Source code is converted into tokens by a lexer.
2. **Parsing**: Tokens are analyzed according to grammar rules.
3. **AST Construction**: As grammar rules are matched, nodes are created to form the AST.

For example, the expression `a + b * c` produces the following AST:
```angular2html
   +
  / \
 a   *
    / \
   b   c
```

This structure captures operator precedence and associativity more clearly than raw tokens.

### Advantages of AST

- Represents code in a structured, hierarchical manner.
- Easier to traverse and analyze than raw code or token streams.
- Enables semantic checks and optimizations.
- Serves as a foundation for tools like linters, transpilers, and compilers.



## Objectives:
1. Get familiar with parsing, what it is and how it can be programmed [1].
2. Get familiar with the concept of AST [2].
3. In addition to what has been done in the 3rd lab work do the following:
    1. In case you didn't have a type that denotes the possible types of tokens you need to:
        1. Have a type __*TokenType*__ (like an enum) that can be used in the lexical analysis to categorize the tokens.
        2. Please use regular expressions to identify the type of the token.
    2. Implement the necessary data structures for an AST that could be used for the text you have processed in the 3rd lab work.
    3. Implement a simple parser program that could extract the syntactic information from the input text.

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

### Expression and Statement Structure

The code defines two primary structures: expressions (`Expr`) and statements (`Stmt`). These are implemented as sealed classes in Kotlin, with specific data classes for different types of expressions and statements.

```kotlin
sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Variable(val name: Token) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()
    data class Get(val obj: Expr, val name: Token) : Expr() // For object property access
}

sealed class Stmt {
    data class Expression(val expression: Expr) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
    data class For(val variable: Token, val iterable: Expr, val body: Stmt) : Stmt()
    data class Block(val statements: List<Stmt>) : Stmt()
    data class Var(val name: Token, val initializer: Expr?) : Stmt()

    // Web scraping-specific statements
    data class Fetch(val url: Expr) : Stmt()
    data class Select(val selector: Expr, val target: Expr? = null) : Stmt()
    data class Save(val data: Expr, val filename: Expr) : Stmt()
    data class Print(val expression: Expr) : Stmt()
}
```

Expressions represent values or operations that produce values. The specialized expressions include `Binary` for operations like addition and comparison, `Grouping` for parenthesized expressions, `Literal` for constants, `Variable` for named references, `Unary` for operations like negation, `Call` for function calls, and `Get` for property access.

Statements represent actions to be performed. The language includes common constructs like `Expression`, `If`, `For`, `Block`, and `Var`, as well as domain-specific statements like `Fetch`, `Select`, `Save`, and `Print` that are tailored for web scraping tasks.

### Parser Class

The `Parser` class is the core component that transforms a list of tokens into an abstract syntax tree using recursive descent parsing:

```kotlin
class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            skipComments() // Skip any comments before parsing declarations
            if (!isAtEnd()) {
                declaration()?.let { statements.add(it) }
            }
        }
        return statements
    }
}
```

The `parse` method serves as the entry point, collecting statements that form the program. It skips comments and processes each declaration until it reaches the end of the token stream.

### Declaration and Statement Parsing

The parser distinguishes between declarations and other types of statements:

```kotlin
private fun declaration(): Stmt? {
    skipComments() // Skip comments before each declaration
    return try {
        when {
            match(TokenType.VAR) -> varDeclaration()
            else -> statement()
        }
    } catch (error: ParseError) {
        synchronize()
        null
    }
}

private fun varDeclaration(): Stmt {
    val name = consume(TokenType.IDENTIFIER, "Expect variable name.")

    var initializer: Expr? = null
    if (match(TokenType.EQUALS)) {
        initializer = expression()
    }

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
    return Stmt.Var(name, initializer)
}
```

The `declaration` method checks if the next token is a variable declaration keyword. If an error occurs, it calls `synchronize` to recover and continue parsing. The `varDeclaration` method handles variable declarations by consuming an identifier token, an optional initializer expression, and a semicolon.

For other statements, the `statement` method branches based on the token type:

```kotlin
private fun statement(): Stmt {
    skipComments() // Skip comments before parsing statements
    return when {
        match(TokenType.IF) -> ifStatement()
        match(TokenType.FOR) -> forStatement()
        match(TokenType.LEFT_BRACE) -> Stmt.Block(block())
        match(TokenType.FETCH) -> fetchStatement()
        match(TokenType.SAVE) -> saveStatement()
        match(TokenType.PRINT) -> printStatement()
        else -> expressionStatement()
    }
}
```

### Control Flow Statements

The parser implements control flow statements like `if` and `for`:

```kotlin
private fun ifStatement(): Stmt {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
    val condition = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

    val thenBranch = statement()
    var elseBranch: Stmt? = null
    if (match(TokenType.ELSE)) {
        elseBranch = statement()
    }

    return Stmt.If(condition, thenBranch, elseBranch)
}

private fun forStatement(): Stmt {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

    val variable = consume(TokenType.IDENTIFIER, "Expect variable name in for loop.")
    consume(TokenType.IN, "Expect 'in' after variable name in for loop.")
    val iterable = expression()

    consume(TokenType.RIGHT_PAREN, "Expect ')' after for loop header.")

    val body = statement()

    return Stmt.For(variable, iterable, body)
}
```

The `ifStatement` method parses an if condition and its branches, while the `forStatement` method parses a for-in loop, which is specifically designed for iterating over collections like the results of a web page selection.

### Web Scraping-Specific Statements

The language includes specialized statements for web scraping:

```kotlin
private fun fetchStatement(): Stmt {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'fetch'.")
    val url = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after URL.")
    consume(TokenType.SEMICOLON, "Expect ';' after fetch statement.")

    return Stmt.Fetch(url)
}

private fun saveStatement(): Stmt {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'save'.")
    val data = expression()
    consume(TokenType.COMMA, "Expect ',' after data expression.")
    val filename = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after filename.")
    consume(TokenType.SEMICOLON, "Expect ';' after save statement.")

    return Stmt.Save(data, filename)
}
```

The `fetchStatement` method parses a URL expression to load web content, while the `saveStatement` method parses expressions for data to save and a filename to save it to.

### Expression Parsing

Expression parsing follows a grammar hierarchy that respects operator precedence:

```kotlin
private fun expression(): Expr {
    return assignment()
}

private fun assignment(): Expr {
    val expr = or()

    if (match(TokenType.EQUALS)) {
        val value = assignment()

        if (expr is Expr.Variable) {
            return Expr.Binary(
                expr,
                previous(),
                value
            )
        }

        error(previous(), "Invalid assignment target.")
    }

    return expr
}
```

The parsing chain continues through methods like `or`, `and`, `equality`, etc., ensuring operators are handled with the correct precedence. For example:

```kotlin
private fun term(): Expr {
    var expr = factor()

    while (match(TokenType.PLUS, TokenType.MINUS)) {
        val operator = previous()
        val right = factor()
        expr = Expr.Binary(expr, operator, right)
    }

    return expr
}

private fun factor(): Expr {
    var expr = unary()

    while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
        val operator = previous()
        val right = unary()
        expr = Expr.Binary(expr, operator, right)
    }

    return expr
}
```

In this structure, `factor` handles multiplication and division, which have higher precedence than addition and subtraction in `term`.

### Property Access and Method Calls

The parsing of method calls and property access is crucial for web scraping operations:

```kotlin
private fun call(): Expr {
    var expr = primary()

    while (true) {
        skipComments() // Skip comments before method calls or property access
        if (match(TokenType.LEFT_PAREN)) {
            expr = finishCall(expr)
        } else if (match(TokenType.DOT)) {
            // After a dot, we might have tokens like SELECT which are both keywords and methods
            if (check(TokenType.SELECT) || check(TokenType.TEXT) || check(TokenType.ATTR) ||
                check(TokenType.HTML) || check(TokenType.IDENTIFIER)) {
                // Treat these tokens as identifiers in this context
                val name = advance()
                expr = Expr.Get(expr, name)
            } else {
                val name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.")
                expr = Expr.Get(expr, name)
            }
        } else {
            break
        }
    }

    return expr
}
```

This method handles expressions like `element.select(".class")` or `product.text()`, which are common in DOM traversal. It processes any dots or parentheses that follow an expression, building a tree of property accesses and method calls.

### Primary Expressions

At the lowest level, the `primary` method handles the most basic expressions:

```kotlin
private fun primary(): Expr {
    if (match(TokenType.BOOLEAN)) {
        return Expr.Literal(previous().lexeme == "true")
    }

    if (match(TokenType.NUMBER)) {
        return Expr.Literal(previous().lexeme.toDoubleOrNull())
    }

    if (match(TokenType.STRING)) {
        // Remove the quotes
        val str = previous().lexeme
        return Expr.Literal(str.substring(1, str.length - 1))
    }

    if (match(TokenType.IDENTIFIER)) {
        return Expr.Variable(previous())
    }

    if (match(TokenType.SELECT)) {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'select'.")
        val selector = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after selector.")
        return Expr.Call(
            Expr.Variable(Token(TokenType.SELECT, "select", previous().line, previous().column)),
            previous(),
            listOf(selector)
        )
    }

    if (match(TokenType.LEFT_PAREN)) {
        val expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
        return Expr.Grouping(expr)
    }

    throw error(peek(), "Expect expression.")
}
```

This method handles literals (booleans, numbers, strings), variables, select expressions, and parenthesized expressions.

### Error Handling

For error handling, the parser uses a synchronization mechanism to recover from syntax errors:

```kotlin
private fun synchronize() {
    advance()

    while (!isAtEnd()) {
        if (previous().type == TokenType.SEMICOLON) {
            skipComments() // Skip any comments after a semicolon
            return
        }

        // Skip comments during error recovery
        if (check(TokenType.COMMENT)) {
            advance()
            continue
        }

        when (peek().type) {
            TokenType.VAR, TokenType.FOR, TokenType.IF, TokenType.FETCH,
            TokenType.SAVE, TokenType.PRINT -> return
            else -> advance()
        }
    }
}
```

This method skips tokens until it finds a likely statement boundary, allowing the parser to continue processing the rest of the program even after encountering syntax errors.

### AST Printer

The `AstPrinter` class visualizes the abstract syntax tree:

```kotlin
class AstPrinter {
    private var indentLevel = 0
    private val indent = "  " // Two spaces for each level of indentation

    fun print(statements: List<Stmt>): String {
        return statements.joinToString("\n") { printStmt(it, 0) }
    }

    // Recursive methods to print statements and expressions with proper indentation...
}
```

The printer recursively traverses the tree, indenting each level to show the hierarchical structure. This is especially useful for debugging and understanding how the parser interprets the code.

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

```
fetch("https://example.com");
```
The parser recognizes this as a `fetch` statement, consuming the URL string as a literal expression. It creates a `Stmt.Fetch` node containing the URL literal.

```
var products = select(".product-item");
```
The parser identifies a variable declaration, consuming the identifier "products" and parsing the initializer. It processes `select(".product-item")` as a function call with a string argument, creating a `Stmt.Var` node with name "products" and a `Call` expression.

```
for (product in products) { ... }
```
For this loop statement, the parser consumes the variable "product", the keyword "in", and the iterable expression "products". It then processes the body as a block of statements, resulting in a `Stmt.For` node with the variable, iterable, and body.

Inside the loop, expressions like `product.select(".name").text()` are parsed as nested property access and method calls:
- First, "product" becomes a `Variable` expression
- The ".select" becomes a property access via `Get`
- The parentheses make it a method call via `Call`
- The ".text()" adds another layer of property access and call

For the conditional:
```
if (price < 100) { ... }
```
The parser creates a `Binary` expression comparing the variable "price" with the literal "100", then processes the body statements, resulting in an `Stmt.If` node.

The `print` and `save` statements are processed as function calls with arguments. For expressions like `name + "," + price + "," + url`, the parser builds nested `Binary` expressions for the string concatenations.

#### Output

```
AST Structure:
└─ Fetch
   └─ URL:
    └─ Literal: "https://example.com"
└─ Variable Declaration: products
   └─ Initializer:
    └─ Call: select
       └─ Arg 1:
        └─ Literal: ".product-item"
└─ For
   ├─ Variable: product
   ├─ Iterable:
    └─ Variable: products
   └─ Body:
    └─ Block
       .
       .
       .
       └─ Statement 4:
        └─ If
           ├─ Condition:
            └─ Binary: <
               ├─ Left:
                └─ Variable: price
               └─ Right:
                └─ Literal: 100.0
           ├─ Then:
            └─ Block
               .
               .
               .
               └─ Statement 2:
                └─ Save
                   ├─ Data:
                    └─ Binary: +
                       ├─ Left:
                        └─ Binary: +
                           ├─ Left:
                            └─ Binary: +
                               ├─ Left:
                                └─ Binary: +
                                   ├─ Left:
                                    └─ Variable: name
                                   └─ Right:
                                    └─ Literal: ","
                               └─ Right:
                                └─ Variable: price
                           └─ Right:
                            └─ Literal: ","
                       └─ Right:
                        └─ Variable: url
                   └─ Filename:
                    └─ Literal: "affordable_products.csv"
```
### Conclusions

This lab on **Parser & Building an Abstract Syntax Tree** focused on breaking down source code into a structured AST. By parsing a script that fetches and processes product data, we built a clear hierarchical representation of its logic. The AST helped visualize variable declarations, function calls, loops, and conditionals, giving insight into how parsers understand and organize code. This lab highlighted the importance of ASTs in language processing and laid the groundwork for future compiler or interpreter development.

## References:
****

<a id="ref3"></a>[1] Parsing - Wikipedia -
https://en.wikipedia.org/wiki/Parsing

<a id="ref4"></a>[2] Abstract syntax tree - Wikipedia -
https://en.wikipedia.org/wiki/Abstract_syntax_tree
























