package com.labs.lab6

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Variable(val name: Token) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()
    data class Get(val obj: Expr, val name: Token) : Expr() // For object property access (e.g., product.select)
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

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            skipComments() // Skip comments within blocks
            if (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                declaration()?.let { statements.add(it) }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

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

    private fun printStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'print'.")
        val value = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
        consume(TokenType.SEMICOLON, "Expect ';' after print statement.")

        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

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

    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL
            )) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

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

    private fun unary(): Expr {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            skipComments() // Skip comments before method calls or property access
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(TokenType.DOT)) {
                // After a dot, we might have tokens like SELECT which are both keywords and methods
                // Check if the next token is a method name that could be used after a dot
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

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, paren, arguments)
    }

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

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    // Skip over any comment tokens
    private fun skipComments() {
        while (check(TokenType.COMMENT) || check(TokenType.WHITESPACE)) {
            advance()
        }
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        System.err.println("[line ${token.line}, column ${token.column}] Error at '${token.lexeme}': $message")
        return ParseError()
    }

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

    private class ParseError : RuntimeException()
}


class AstPrinter {
    private var indentLevel = 0
    private val indent = "  " // Two spaces for each level of indentation

    fun print(statements: List<Stmt>): String {
        return statements.joinToString("\n") { printStmt(it, 0) }
    }

    private fun printStmt(stmt: Stmt, level: Int): String {
        val currentIndent = indent.repeat(level)

        return when (stmt) {
            is Stmt.Expression -> "$currentIndent└─ Expression\n${printExpr(stmt.expression, level + 1)}"

            is Stmt.If -> {
                val result = StringBuilder()
                result.append("$currentIndent└─ If\n")
                result.append("$currentIndent   ├─ Condition:\n${printExpr(stmt.condition, level + 2)}\n")
                result.append("$currentIndent   ├─ Then:\n${printStmt(stmt.thenBranch, level + 2)}\n")

                if (stmt.elseBranch != null) {
                    result.append("$currentIndent   └─ Else:\n${printStmt(stmt.elseBranch, level + 2)}")
                }

                result.toString()
            }

            is Stmt.For -> {
                val result = StringBuilder()
                result.append("$currentIndent└─ For\n")
                result.append("$currentIndent   ├─ Variable: ${stmt.variable.lexeme}\n")
                result.append("$currentIndent   ├─ Iterable:\n${printExpr(stmt.iterable, level + 2)}\n")
                result.append("$currentIndent   └─ Body:\n${printStmt(stmt.body, level + 2)}")
                result.toString()
            }

            is Stmt.Block -> {
                val result = StringBuilder("$currentIndent└─ Block\n")
                stmt.statements.forEachIndexed { index, statement ->
                    val prefix = if (index == stmt.statements.size - 1) "└─" else "├─"
                    result.append("$currentIndent   $prefix Statement ${index + 1}:\n")
                    result.append(printStmt(statement, level + 2))
                    if (index < stmt.statements.size - 1) result.append("\n")
                }
                result.toString()
            }

            is Stmt.Var -> {
                val result = StringBuilder("$currentIndent└─ Variable Declaration: ${stmt.name.lexeme}\n")
                if (stmt.initializer != null) {
                    result.append("$currentIndent   └─ Initializer:\n${printExpr(stmt.initializer, level + 2)}")
                } else {
                    result.append("$currentIndent   └─ Initializer: null")
                }
                result.toString()
            }

            is Stmt.Fetch -> {
                "$currentIndent└─ Fetch\n$currentIndent   └─ URL:\n${printExpr(stmt.url, level + 2)}"
            }

            is Stmt.Select -> {
                val result = StringBuilder("$currentIndent└─ Select\n")
                result.append("$currentIndent   └─ Selector:\n${printExpr(stmt.selector, level + 2)}")
                if (stmt.target != null) {
                    result.append("\n$currentIndent   └─ Target:\n${printExpr(stmt.target, level + 2)}")
                }
                result.toString()
            }

            is Stmt.Save -> {
                val result = StringBuilder("$currentIndent└─ Save\n")
                result.append("$currentIndent   ├─ Data:\n${printExpr(stmt.data, level + 2)}\n")
                result.append("$currentIndent   └─ Filename:\n${printExpr(stmt.filename, level + 2)}")
                result.toString()
            }

            is Stmt.Print -> {
                "$currentIndent└─ Print\n$currentIndent   └─ Expression:\n${printExpr(stmt.expression, level + 2)}"
            }
        }
    }

    private fun printExpr(expr: Expr, level: Int): String {
        val currentIndent = indent.repeat(level)

        return when (expr) {
            is Expr.Binary -> {
                val result = StringBuilder("$currentIndent└─ Binary: ${expr.operator.lexeme}\n")
                result.append("$currentIndent   ├─ Left:\n${printExpr(expr.left, level + 2)}\n")
                result.append("$currentIndent   └─ Right:\n${printExpr(expr.right, level + 2)}")
                result.toString()
            }

            is Expr.Grouping -> {
                "$currentIndent└─ Grouping\n${printExpr(expr.expression, level + 1)}"
            }

            is Expr.Literal -> {
                val value = when (expr.value) {
                    is String -> "\"${expr.value}\""
                    null -> "null"
                    else -> expr.value.toString()
                }
                "$currentIndent└─ Literal: $value"
            }

            is Expr.Variable -> {
                "$currentIndent└─ Variable: ${expr.name.lexeme}"
            }

            is Expr.Unary -> {
                val result = StringBuilder("$currentIndent└─ Unary: ${expr.operator.lexeme}\n")
                result.append("$currentIndent   └─ Right:\n${printExpr(expr.right, level + 2)}")
                result.toString()
            }

            is Expr.Call -> {
                val result = StringBuilder()

                when (expr.callee) {
                    is Expr.Variable -> {
                        result.append("$currentIndent└─ Call: ${(expr.callee as Expr.Variable).name.lexeme}\n")
                    }
                    else -> {
                        result.append("$currentIndent└─ Call\n")
                        result.append("$currentIndent   ├─ Callee:\n${printExpr(expr.callee, level + 2)}\n")
                    }
                }

                expr.arguments.forEachIndexed { index, arg ->
                    val prefix = if (index == expr.arguments.size - 1) "└─" else "├─"
                    result.append("$currentIndent   $prefix Arg ${index + 1}:\n${printExpr(arg, level + 2)}")
                    if (index < expr.arguments.size - 1) result.append("\n")
                }

                result.toString()
            }

            is Expr.Get -> {
                val result = StringBuilder("$currentIndent└─ Property Access: ${expr.name.lexeme}\n")
                result.append("$currentIndent   └─ Object:\n${printExpr(expr.obj, level + 2)}")
                result.toString()
            }
        }
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
    val tokens = lexer.tokenize()

    // Filter out comment and whitespace tokens before parsing
    val filteredTokens = tokens.filter { it.type != TokenType.COMMENT && it.type != TokenType.WHITESPACE }

    println("Tokens after filtering comments and whitespace:")
    filteredTokens.forEach { println("${it.type}: '${it.lexeme}'") }
    println()

    val parser = Parser(filteredTokens)
    try {
        val statements = parser.parse()
        val printer = AstPrinter()
        println("AST Structure:")
        println(printer.print(statements))
    } catch (e: Exception) {
        println("Error parsing code: ${e.message}")
        e.printStackTrace()
    }
}