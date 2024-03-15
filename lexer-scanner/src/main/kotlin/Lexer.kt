package com.labs.lab3

enum class TokenType {
    // Keywords
    VAR, IF, ELSE, FOR, IN,

    // Functions
    FETCH, SELECT, XPATH, TEXT, ATTR, HTML, SAVE, PRINT,

    // Literals
    IDENTIFIER, STRING, NUMBER, BOOLEAN,

    // Operators
    EQUALS, PLUS, MINUS, MULTIPLY, DIVIDE,
    GREATER, LESS, GREATER_EQUAL, LESS_EQUAL,
    EQUAL_EQUAL, NOT_EQUAL, AND, OR, NOT,

    // Symbols
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    SEMICOLON, COMMA, DOT, DOLLAR,

    // Other
    COMMENT, WHITESPACE, EOF, UNKNOWN
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int
)

class Lexer(private val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private var column = 1
    private val tokens = mutableListOf<Token>()

    private val keywords = mapOf(
        "var" to TokenType.VAR,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "for" to TokenType.FOR,
        "in" to TokenType.IN,
        "fetch" to TokenType.FETCH,
        "select" to TokenType.SELECT,
        "xpath" to TokenType.XPATH,
        "text" to TokenType.TEXT,
        "attr" to TokenType.ATTR,
        "html" to TokenType.HTML,
        "save" to TokenType.SAVE,
        "print" to TokenType.PRINT,
        "true" to TokenType.BOOLEAN,
        "false" to TokenType.BOOLEAN
    )

    fun tokenize(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", line, column))
        return tokens
    }

    private fun scanToken() {
        val c = advance()

        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            '[' -> addToken(TokenType.LEFT_BRACKET)
            ']' -> addToken(TokenType.RIGHT_BRACKET)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '$' -> addToken(TokenType.DOLLAR)
            ';' -> addToken(TokenType.SEMICOLON)
            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '*' -> addToken(TokenType.MULTIPLY)
            '/' -> {
                if (match('/')) {
                    // Single line comment
                    while (peek() != '\n' && !isAtEnd()) {
                        advance()
                    }
                    addToken(TokenType.COMMENT)
                } else if (match('*')) {
                    // Multi-line comment
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') {
                            line++
                            column = 0
                        }
                        advance()
                    }

                    if (isAtEnd()) {
                        // Unterminated comment
                        // Could handle error here
                    } else {
                        // Consume the closing */
                        advance()
                        advance()
                    }
                    addToken(TokenType.COMMENT)
                } else {
                    addToken(TokenType.DIVIDE)
                }
            }
            '=' -> {
                if (match('=')) {
                    addToken(TokenType.EQUAL_EQUAL)
                } else {
                    addToken(TokenType.EQUALS)
                }
            }
            '!' -> {
                if (match('=')) {
                    addToken(TokenType.NOT_EQUAL)
                } else {
                    addToken(TokenType.NOT)
                }
            }
            '<' -> {
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL)
                } else {
                    addToken(TokenType.LESS)
                }
            }
            '>' -> {
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL)
                } else {
                    addToken(TokenType.GREATER)
                }
            }
            '&' -> {
                if (match('&')) {
                    addToken(TokenType.AND)
                } else {
                    // Unexpected character, could handle error
                    addToken(TokenType.UNKNOWN)
                }
            }
            '|' -> {
                if (match('|')) {
                    addToken(TokenType.OR)
                } else {
                    // Unexpected character, could handle error
                    addToken(TokenType.UNKNOWN)
                }
            }
            '"' -> string()
            '\n' -> {
                line++
                column = 0
                // Skip newlines without creating tokens
            }
            ' ', '\r', '\t' -> {
                // Skip whitespace without creating tokens
            }
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    // Unexpected character, could handle error
                    addToken(TokenType.UNKNOWN)
                }
            }
        }
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
                column = 0
            }
            advance()
        }

        if (isAtEnd()) {
            // Unterminated string
            // Could handle error here
            return
        }

        // Consume the closing "
        advance()

        // Get the string value (without the quotes)
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING)
    }

    private fun number() {
        while (isDigit(peek())) {
            advance()
        }

        // Look for a decimal part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the .
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        addToken(TokenType.NUMBER)
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER

        addToken(type)
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) {
            return false
        }

        current++
        column++
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun peekNext(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun advance(): Char {
        current++
        column++
        return source[current - 1]
    }

    private fun addToken(type: TokenType) {
        val lexeme = source.substring(start, current)
        tokens.add(Token(type, lexeme, line, column - lexeme.length))
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    override fun toString(): String {
        return tokens.joinToString(separator = "\n")
    }
}

fun main() {
    val code = """
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
    """.trimIndent()

    val lexer = Lexer(code)
    lexer.tokenize()
    print(lexer)
}
