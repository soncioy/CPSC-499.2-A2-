parser grammar JavaParser;

options { tokenVocab=JavaLexer; }

compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration* EOF
    ;

packageDeclaration
    : PACKAGE qualifiedIdentifier SEMICOLON
    ;

importDeclaration
    : IMPORT qualifiedIdentifier (PERIOD ASTERISK)? SEMICOLON
    ;

typeDeclaration
    : modifiers classOrInterfaceDeclaration
    | SEMICOLON
    ;

classOrInterfaceDeclaration
    : classDeclaration
    | interfaceDeclaration
    ;

classDeclaration
    : CLASS Identifier (EXTENDS type)? (IMPLEMENTS typeList)? classBody
    ;

interfaceDeclaration
    : INTERFACE Identifier (EXTENDS typeList)? interfaceBody
    ;

typeList
    : type (COMMA type)*
    ;

classBody
    : OPEN_BRACE classBodyDeclaration* CLOSE_BRACE
    ;

interfaceBody
    : OPEN_BRACE interfaceBodyDeclaration* CLOSE_BRACE
    ;

classBodyDeclaration
    : SEMICOLON
    | STATIC? block
    | modifiers memberDecl
    ;

memberDecl
    : methodOrFieldDecl
    | VOID Identifier methodDeclaratorRest
    | Identifier constructorDeclaratorRest
    | classOrInterfaceDeclaration
    ;

methodOrFieldDecl
    : type Identifier methodOrFieldRest
    ;

methodOrFieldRest
    : methodDeclaratorRest                                          // Method
    | variableDeclaratorRest (COMMA variableDeclarator)* SEMICOLON  // Fields
    ;

interfaceBodyDeclaration
    : SEMICOLON
    | modifiers interfaceMemberDecl
    ;

interfaceMemberDecl
    : interfaceMethodOrFieldDecl
    | VOID Identifier voidInterfaceMethodDeclaratorRest
    | classOrInterfaceDeclaration
    ;

interfaceMethodOrFieldDecl
    : type Identifier interfaceMethodOrFieldRest
    ;

interfaceMethodOrFieldRest
    : constantDeclaratorsRest SEMICOLON
    | interfaceMethodDeclaratorRest
    ;

methodDeclaratorRest
    : formalParameters bracketsOpt (THROWS qualifiedIdentifierList)? (methodBody | SEMICOLON)
    ;

voidMethodDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? (methodBody | SEMICOLON)
    ;

interfaceMethodDeclaratorRest
    : formalParameters bracketsOpt (THROWS qualifiedIdentifierList)? SEMICOLON
    ;

voidInterfaceMethodDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? SEMICOLON
    ;

constructorDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? constructorBody
    ;

constructorBody
    : OPEN_BRACE explicitConstructorInvocation? blockStatement* CLOSE_BRACE
    ;

explicitConstructorInvocation
    : THIS OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS SEMICOLON
    | SUPER OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS SEMICOLON
    | primary PERIOD SUPER OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS SEMICOLON
    ;

qualifiedIdentifierList
    : qualifiedIdentifier (COMMA qualifiedIdentifier)*
    ;

formalParameters
    : OPEN_PARENTHESIS (formalParameter (COMMA formalParameter)*)? CLOSE_PARENTHESIS
    ;

formalParameter
    : FINAL? type variableDeclaratorId
    ;

methodBody
    : block
    ;

variableDeclaratorId
    : Identifier bracketsOpt
    ;

variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;

variableDeclarator
    : Identifier (OPEN_BRACKET CLOSE_BRACKET)* (EQUALS variableInitializer)?
    ;

variableDeclaratorRest
    : bracketsOpt (EQUALS variableInitializer)?
    ;

constantDeclaratorsRest
    : constantDeclaratorRest (COMMA constantDeclarator)*
    ;

constantDeclarator
    : Identifier constantDeclaratorRest
    ;

constantDeclaratorRest
    : bracketsOpt EQUALS variableInitializer
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

arrayInitializer
    : OPEN_BRACE (variableInitializer (COMMA variableInitializer)*)? COMMA? CLOSE_BRACE
    ;

modifiers
    : (PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP)*
    ;

type
    : primitiveType (OPEN_BRACKET CLOSE_BRACKET)*
    | qualifiedIdentifier (OPEN_BRACKET CLOSE_BRACKET)*
    ;

primitiveType
    : BOOLEAN
    | CHAR
    | BYTE
    | SHORT
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    ;

block
    : OPEN_BRACE blockStatement* CLOSE_BRACE
    ;

blockStatement
    : localVariableDeclarationStatement
    | classOrInterfaceDeclaration
    | Identifier COLON statement
    | statement
    ;

localVariableDeclarationStatement
    : FINAL? type variableDeclarators SEMICOLON
    ;

statement
    : block
    | IF OPEN_PARENTHESIS expression CLOSE_PARENTHESIS statement (ELSE statement)?
    | FOR OPEN_PARENTHESIS forInit? SEMICOLON expression? SEMICOLON forUpdate? CLOSE_PARENTHESIS statement
    | WHILE OPEN_PARENTHESIS expression CLOSE_PARENTHESIS statement
    | DO statement WHILE OPEN_PARENTHESIS expression CLOSE_PARENTHESIS SEMICOLON
    | TRY block (catchClause+ finallyClause? | finallyClause)
    | SWITCH OPEN_PARENTHESIS expression CLOSE_PARENTHESIS OPEN_BRACE switchBlockStatementGroup* CLOSE_BRACE
    | SYNCHRONIZED OPEN_PARENTHESIS expression CLOSE_PARENTHESIS block
    | RETURN expression? SEMICOLON
    | THROW expression SEMICOLON
    | BREAK Identifier? SEMICOLON
    | CONTINUE Identifier? SEMICOLON
    | SEMICOLON
    | expressionStatement
    | Identifier COLON statement
    ;

catchClause
    : CATCH OPEN_PARENTHESIS formalParameter CLOSE_PARENTHESIS block
    ;

finallyClause
    : FINALLY block
    ;

switchBlockStatementGroup
    : switchLabel+ blockStatement*
    ;

switchLabel
    : CASE constantExpression COLON
    | DEFAULT COLON
    ;

forInit
    : statementExpressionList
    | FINAL? type variableDeclarators
    ;

forUpdate
    : statementExpressionList
    ;

statementExpressionList
    : statementExpression (COMMA statementExpression)*
    ;

expressionStatement
    : statementExpression SEMICOLON
    ;

statementExpression
    : preIncrementExpression
    | preDecrementExpression
    | primary assignmentOperator assignmentExpression
    | primary postfixOp*
    | classInstanceCreationExpression
    ;

expression
    : assignmentExpression
    ;

assignmentExpression
    : conditionalExpression (assignmentOperator assignmentExpression)?
    ;

assignmentOperator
    : EQUALS
    | PLUS_EQUALS
    | MINUS_EQUALS
    | ASTERISK_EQUALS
    | SLASH_EQUALS
    | AMPERSAND_EQUALS
    | PIPE_EQUALS
    | CARET_EQUALS
    | PERCENT_EQUALS
    | DOUBLE_LESS_THAN_EQUALS
    | DOUBLE_GREATER_THAN_EQUALS
    | TRIPLE_GREATER_THAN_EQUALS
    ;

assignment
    : leftHandSide assignmentOperator assignmentExpression
    ;

leftHandSide
    : qualifiedIdentifier
    | primary
    ;

conditionalExpression
    : conditionalOrExpression (QUESTION expression COLON conditionalExpression)?
    ;

conditionalOrExpression
    : conditionalAndExpression (DOUBLE_PIPE conditionalAndExpression)*
    ;

conditionalAndExpression
    : inclusiveOrExpression (DOUBLE_AMPERSAND inclusiveOrExpression)*
    ;

inclusiveOrExpression
    : exclusiveOrExpression (PIPE exclusiveOrExpression)*
    ;

exclusiveOrExpression
    : andExpression (CARET andExpression)*
    ;

andExpression
    : equalityExpression (AMPERSAND equalityExpression)*
    ;

equalityExpression
    : relationalExpression ((DOUBLE_EQUALS | EXCLAMATION_EQUALS) relationalExpression)*
    ;

relationalExpression
    : shiftExpression ((relationalOperator shiftExpression) | INSTANCEOF type)*
    ;

relationalOperator
    : LESS_THAN_OR_EQUALS
    | GREATER_THAN_OR_EQUALS
    | LESS_THAN
    | GREATER_THAN
    ;

shiftExpression
    : additiveExpression (shiftOperator additiveExpression)*
    ;

shiftOperator
    : DOUBLE_LESS_THAN
    | DOUBLE_GREATER_THAN
    | TRIPLE_GREATER_THAN
    ;

additiveExpression
    : multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression ((ASTERISK | SLASH | PERCENT) unaryExpression)*
    ;

unaryExpression
    : PLUS unaryExpression
    | MINUS unaryExpression
    | preIncrementExpression
    | preDecrementExpression
    | unaryExpressionNotPlusMinus
    ;

preIncrementExpression
    : DOUBLE_PLUS unaryExpression
    ;

preDecrementExpression
    : DOUBLE_MINUS unaryExpression
    ;

unaryExpressionNotPlusMinus
    : TILDE unaryExpression
    | EXCLAMATION unaryExpression
    | castExpression
    | postfixExpression
    ;

postfixExpression
    : primary postfixOp*
    ;

postfixOp
    : DOUBLE_PLUS
    | DOUBLE_MINUS
    ;

castExpression
    : OPEN_PARENTHESIS primitiveType bracketsOpt CLOSE_PARENTHESIS unaryExpression
    | OPEN_PARENTHESIS qualifiedIdentifier (OPEN_BRACKET CLOSE_BRACKET)* CLOSE_PARENTHESIS unaryExpressionNotPlusMinus
    ;

primary
    : primaryPrefix primarySuffix*
    ;

primaryPrefix
    : literal
    | PERIOD IntegerLiteral Identifier?                     // adding this for .5, .5f
    | THIS
    | SUPER
    | OPEN_PARENTHESIS expression CLOSE_PARENTHESIS
    | Identifier
    | qualifiedIdentifier PERIOD THIS
    | NEW qualifiedIdentifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS classBody?
    | arrayCreationExpression
    | primitiveType bracketsOpt PERIOD CLASS
    | VOID PERIOD CLASS
    ;

primarySuffix
    : PERIOD Identifier (OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS)?
    | PERIOD CLASS
    | OPEN_BRACKET CLOSE_BRACKET bracketsOpt PERIOD CLASS
    | PERIOD THIS
    | PERIOD SUPER PERIOD Identifier (OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS)?
    | PERIOD NEW Identifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS classBody?
    | OPEN_BRACKET expression CLOSE_BRACKET
    | OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS
    ;

classInstanceCreationExpression
    : NEW qualifiedIdentifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS classBody?
    ;

classCreatorRest
    : OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS classBody?
    ;

argumentList
    : expression (COMMA expression)*
    ;

arrayCreationExpression
    : NEW primitiveType arrayCreatorRest
    | NEW qualifiedIdentifier arrayCreatorRest
    ;

arrayCreatorRest
    : OPEN_BRACKET CLOSE_BRACKET bracketsOpt arrayInitializer
    | (OPEN_BRACKET expression CLOSE_BRACKET)+ bracketsOpt
    ;

qualifiedIdentifier
    : Identifier (PERIOD Identifier)*
    ;

// updated to deal with different types i.e. hexadecimal, octal
literal
    : IntegerLiteral Identifier?
    | FloatingPointLiteral Identifier?
    | CharacterLiteral
    | StringLiteral
    | BooleanLiteral
    | NullLiteral
    ;

bracketsOpt
    : (OPEN_BRACKET CLOSE_BRACKET)*
    ;

constantExpression
    : expression
    ;