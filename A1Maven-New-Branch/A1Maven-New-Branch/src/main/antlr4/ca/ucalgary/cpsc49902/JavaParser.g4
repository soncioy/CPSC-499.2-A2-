/**
*ANTLR Parser Grammar for Java1.2.
This grammar defines the syntactic structure of the language from the JLS 1.2 requirements Chapter 18
*/
parser grammar JavaParser;


options { tokenVocab=JavaLexer; }

// This is the root of the grammar, These rules define the entry point of the parser
compilationUnit
   : packageDeclaration? importDeclaration* typeDeclaration* EOF
   ;


packageDeclaration
   : PACKAGE qualifiedIdentifier SEMICOLON
   ;


importDeclaration
   : IMPORT qualifiedIdentifier (PERIOD ASTERISK)? SEMICOLON
   ;

/**The following section handles the main blueprint of the code, it defines how classes and interfaces
*are declared including their hierarchy
*/
typeDeclaration
   : modifiers classOrInterfaceDeclaration
   | SEMICOLON
   ;


classOrInterfaceDeclaration
   : classDeclaration
   | interfaceDeclaration
   ;

classDeclaration
   : CLASS Identifier
     (EXTENDS qualifiedIdentifier)?
     (IMPLEMENTS qualifiedIdentifier (COMMA qualifiedIdentifier)*)?
     classBody
   ;


interfaceDeclaration
   : INTERFACE Identifier
     (EXTENDS qualifiedIdentifier (COMMA qualifiedIdentifier)*)?
     interfaceBody
   ;


classBody
   : OPEN_BRACE classBodyDeclaration* CLOSE_BRACE
   ;

/* Anonymous Class Logic
This is where a unique Java 1.2 feature occurs where a class is defined on the fly during an allocation.
this rule is there to ensure that the class's body is linked to the new expression.
*/
anonymousClassBody
   : OPEN_BRACE anonymousClassBodyDeclaration* CLOSE_BRACE
   ;


anonymousClassBodyDeclaration
   : SEMICOLON
   | STATIC? block
   | modifiers anonymousMemberDecl
   ;


anonymousMemberDecl
   : methodOrFieldDecl
   | VOID Identifier voidMethodDeclaratorRest
   | classOrInterfaceDeclaration
   ;


interfaceBody
   : OPEN_BRACE interfaceBodyDeclaration* CLOSE_BRACE
   ;


classBodyDeclaration
   : SEMICOLON
   | STATIC? block
   | modifiers memberDecl
   ;

/*
These rules manage what can exist inside a class like fields, methods, and constructors.
*/
memberDecl
   : methodOrFieldDecl
   | VOID Identifier voidMethodDeclaratorRest
   | Identifier constructorDeclaratorRest
   | classOrInterfaceDeclaration
   ;


methodOrFieldDecl
   : type Identifier methodOrFieldRest
   ;


methodOrFieldRest
   : methodDeclaratorRest
   | variableDeclaratorRest (COMMA variableDeclarator)* SEMICOLON
   ;


interfaceBodyDeclaration
   : SEMICOLON
   | interfaceModifiers interfaceMemberDecl
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
   | qualifiedIdentifier PERIOD SUPER OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS SEMICOLON
   ;


qualifiedIdentifierList
   : qualifiedIdentifier (COMMA qualifiedIdentifier)*
   ;

/*Formal Parameters and Arguments
These rules are there to define how data is passed into methods and constructors.
It ensures that parameters in the declaration have types.
*/
formalParameters
   : OPEN_PARENTHESIS (formalParameter (COMMA formalParameter)*)? CLOSE_PARENTHESIS
   ;


formalParameter
   : type variableDeclaratorId
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


accessModifier
   : PUBLIC
   | PROTECTED
   | PRIVATE
   ;

/* Modifiers and Attributes
These rules define the legal keywords that cna precede a declaration, which enforces Java 1.2 modifiers only.
*/
modifiers
   : accessModifier?
     (STATIC | ABSTRACT | FINAL | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP)*
   ;


interfaceModifiers
   : (PUBLIC | ABSTRACT)*
   ;


type
   : primitiveType (OPEN_BRACKET CLOSE_BRACKET)*
   | qualifiedIdentifier (OPEN_BRACKET CLOSE_BRACKET)*
   ;

/*Leaf Nodes: Primitives and literals
These rules connect the parser with the lexer tokens for basic types and hard coded values.
*/
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
   | modifiers classDeclaration
   | Identifier COLON statement
   | statement
   ;


localVariableDeclarationStatement
   : FINAL? type variableDeclarators SEMICOLON
   ;

/* Statements and control flow
this is the main logic part of the parser. It defines how loops, branches (if,else, and dangling else),
and blocks are structured.
*/

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
   : FINAL? type variableDeclarators
   | statementExpressionList
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
   | primary postfixOp?
   ;

/* Expression Hierarchy
This part of the parser ensures that the math and logic happen in the correct order. for example * before +
*/

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

/* Unary and Postfix Operations
These rules handle operations on a single operand like incremenet, decrement, and logical negations.
Aswell as cast to manage operations for type conversion.
*/
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
   : primary postfixOp?
   ;


postfixOp
   : DOUBLE_PLUS
   | DOUBLE_MINUS
   ;


castExpression
   : OPEN_PARENTHESIS primitiveType bracketsOpt CLOSE_PARENTHESIS unaryExpression
   | OPEN_PARENTHESIS qualifiedIdentifier (OPEN_BRACKET CLOSE_BRACKET)* CLOSE_PARENTHESIS unaryExpressionNotPlusMinus
   ;

/*Primary Expressions and Suffix
this part handles the leaves of the tree. It handles the actual values, object creation and chained access
*/
primary
   : primaryPrefix primarySuffix*
   ;


primaryPrefix
   : literal
   | THIS
   | SUPER
   | OPEN_PARENTHESIS expression CLOSE_PARENTHESIS
   | primitiveType bracketsOpt PERIOD CLASS
   | VOID PERIOD CLASS
   | constructorInvocation
   | arrayCreationExpression
   | qualifiedIdentifier bracketsOpt PERIOD CLASS
   | Identifier
   ;


constructorInvocation
   : NEW qualifiedIdentifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS anonymousClassBody?
   ;


primarySuffix
   : methodCall
   | fieldAccess
   | superMethodCall
   | superFieldAccess
   | qualifiedNew
   | arrayAccess
   | unqualifiedCall
   | classLiteralSuffix
   | thisSuffix
   ;


methodCall
   : PERIOD Identifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS
   ;


fieldAccess
   : PERIOD Identifier
   ;


superMethodCall
   : PERIOD SUPER PERIOD Identifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS
   ;


superFieldAccess
   : PERIOD SUPER PERIOD Identifier
   ;


qualifiedNew
   : PERIOD NEW Identifier OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS anonymousClassBody?
   ;


arrayAccess
   : OPEN_BRACKET expression CLOSE_BRACKET
   ;


unqualifiedCall
   : OPEN_PARENTHESIS argumentList? CLOSE_PARENTHESIS
   ;


classLiteralSuffix
   : PERIOD CLASS
   | OPEN_BRACKET CLOSE_BRACKET bracketsOpt PERIOD CLASS
   ;


thisSuffix
   : PERIOD THIS
   ;


argumentList
   : expression (COMMA expression)*
   ;

/* Array Creation and Initialization
These are specialized rules for Java arrays. It distinguishes between creating an array with a specific size
and creating one with an initializer.
*/
arrayCreationExpression
   : NEW primitiveType arrayCreatorRest
   | NEW qualifiedIdentifier arrayCreatorRest
   ;


arrayCreatorRest
   : OPEN_BRACKET CLOSE_BRACKET bracketsOpt arrayInitializer
   | OPEN_BRACKET expression CLOSE_BRACKET (OPEN_BRACKET expression CLOSE_BRACKET)* bracketsOpt
   ;

/* Qaulified Names and Scope Resolution
This rule is used throughout the parser grammar to handle the dot notation required for paths, class name and other stuff.
*/
qualifiedIdentifier
   : Identifier (PERIOD Identifier)*
   ;


literal
   : IntegerLiteral
   | FloatingPointLiteral
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

