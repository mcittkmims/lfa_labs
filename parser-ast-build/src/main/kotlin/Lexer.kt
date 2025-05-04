package com.labs.lab6
import java.util.regex.Pattern

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
    private var position = 0
    private var line = 1
    private var column = 1
    private val tokens = mutableListOf<Token>()

    // Define regex patterns for token types
    private val tokenPatterns = mapOf(
        // Whitespace
        TokenType.WHITESPACE to Pattern.compile("^[ \\t\\r]+"),

        // Comments
        TokenType.COMMENT to Pattern.compile("^(//.*|/\\*[\\s\\S]*?\\*/)"),

        // Keywords
        TokenType.VAR to Pattern.compile("^var\\b"),
        TokenType.IF to Pattern.compile("^if\\b"),
        TokenType.ELSE to Pattern.compile("^else\\b"),
        TokenType.FOR to Pattern.compile("^for\\b"),
        TokenType.IN to Pattern.compile("^in\\b"),

        // Functions
        TokenType.FETCH to Pattern.compile("^fetch\\b"),
        TokenType.SELECT to Pattern.compile("^select\\b"),
        TokenType.XPATH to Pattern.compile("^xpath\\b"),
        TokenType.TEXT to Pattern.compile("^text\\b"),
        TokenType.ATTR to Pattern.compile("^attr\\b"),
        TokenType.HTML to Pattern.compile("^html\\b"),
        TokenType.SAVE to Pattern.compile("^save\\b"),
        TokenType.PRINT to Pattern.compile("^print\\b"),

        // Boolean literals
        TokenType.BOOLEAN to Pattern.compile("^(true|false)\\b"),

        // Literals
        TokenType.STRING to Pattern.compile("^\"([^\"\\\\]|\\\\.)*\""),
        TokenType.NUMBER to Pattern.compile("^\\d+(\\.\\d+)?"),
        TokenType.IDENTIFIER to Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*"),

        // Operators
        TokenType.EQUALS to Pattern.compile("^=(?!=)"),
        TokenType.EQUAL_EQUAL to Pattern.compile("^=="),
        TokenType.NOT_EQUAL to Pattern.compile("^!="),
        TokenType.GREATER_EQUAL to Pattern.compile("^>="),
        TokenType.LESS_EQUAL to Pattern.compile("^<="),
        TokenType.GREATER to Pattern.compile("^>(?!=)"),
        TokenType.LESS to Pattern.compile("^<(?!=)"),
        TokenType.PLUS to Pattern.compile("^\\+"),
        TokenType.MINUS to Pattern.compile("^-"),
        TokenType.MULTIPLY to Pattern.compile("^\\*"),
        TokenType.DIVIDE to Pattern.compile("^/(?![/*])"),
        TokenType.AND to Pattern.compile("^&&"),
        TokenType.OR to Pattern.compile("^\\|\\|"),
        TokenType.NOT to Pattern.compile("^!(?!=)"),

        // Symbols
        TokenType.LEFT_PAREN to Pattern.compile("^\\("),
        TokenType.RIGHT_PAREN to Pattern.compile("^\\)"),
        TokenType.LEFT_BRACE to Pattern.compile("^\\{"),
        TokenType.RIGHT_BRACE to Pattern.compile("^\\}"),
        TokenType.LEFT_BRACKET to Pattern.compile("^\\["),
        TokenType.RIGHT_BRACKET to Pattern.compile("^\\]"),
        TokenType.SEMICOLON to Pattern.compile("^;"),
        TokenType.COMMA to Pattern.compile("^,"),
        TokenType.DOT to Pattern.compile("^\\."),
        TokenType.DOLLAR to Pattern.compile("^\\$")
    )

    fun tokenize(): List<Token> {
        while (position < source.length) {
            var matched = false

            // Handle newlines separately to update line and column counts
            if (source[position] == '\n') {
                line++
                column = 1
                position++
                matched = true
                continue
            }

            val remainingSource = source.substring(position)

            // Try each pattern until one matches
            for ((type, pattern) in tokenPatterns) {
                val matcher = pattern.matcher(remainingSource)

                if (matcher.find()) {
                    val lexeme = matcher.group()

                    // Skip whitespace without creating tokens
                    if (type != TokenType.WHITESPACE) {
                        tokens.add(Token(type, lexeme, line, column))
                    }

                    // Update position and column
                    position += lexeme.length

                    // For comments, we need to adjust line count if they contain newlines
                    if (type == TokenType.COMMENT) {
                        val newlines = lexeme.count { it == '\n' }
                        if (newlines > 0) {
                            line += newlines
                            // Set column to position after the last newline
                            val lastNewlinePos = lexeme.lastIndexOf('\n')
                            column = lexeme.length - lastNewlinePos
                        } else {
                            column += lexeme.length
                        }
                    } else {
                        column += lexeme.length
                    }

                    matched = true
                    break
                }
            }

            // If no pattern matched, it's an unknown token
            if (!matched) {
                tokens.add(Token(TokenType.UNKNOWN, source[position].toString(), line, column))
                position++
                column++
            }
        }

        tokens.add(Token(TokenType.EOF, "", line, column))
        return tokens
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