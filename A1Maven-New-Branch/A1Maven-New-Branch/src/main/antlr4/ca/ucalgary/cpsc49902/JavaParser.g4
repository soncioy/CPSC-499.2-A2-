parser grammar JavaParser;

options { tokenVocab = JavaLexer; }

compilationUnit
    : (PACKAGE qualifiedIdentifier SEMICOLON)?
      importDeclaration*
      typeDeclaration*
    ;

importDeclaration
    : IMPORT qualifiedIdentifier (PERIOD ASTERISK)? SEMICOLON
    ;

typeDeclaration
    : classDeclaration
    | interfaceDeclaration
    | SEMICOLON
    ;

 ============================================================================
 CLASS DECLARATIONS
 ============================================================================

classDeclaration
    : modifier* CLASS Identifier (EXTENDS qualifiedIdentifier)?
      (IMPLEMENTS qualifiedIdentifierList)? classBody
    ;

classBody
    : OPEN_BRACE classBodyDeclaration* CLOSE_BRACE
    ;

classBodyDeclaration
    : modifier* memberDeclaration
    | STATIC? block
    | SEMICOLON
    ;

memberDeclaration
    : methodDeclaration
    | fieldDeclaration
    | classDeclaration
    | interfaceDeclaration
    ;


FormalParame



variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;


 ============================================================================
 INTERFACE DECLARATIONS
 ============================================================================

interfaceDeclaration
    : modifier* INTERFACE Identifier (EXTENDS qualifiedIdentifierList)?
      interfaceBody
    ;

interfaceBody
    : OPEN_BRACE interfaceBodyDeclaration* CLOSE_BRACE
    ;

interfaceBodyDeclaration
    : modifier* interfaceMemberDeclaration
    | SEMICOLON
    ;

interfaceMemberDeclaration
    : interfaceMethodDeclaration
    | interfaceFieldDeclaration
    | classDeclaration
    | interfaceDeclaration
    ;


 ============================================================================
 FORMAL PARAMETERS
 ============================================================================

formalParameter
    : FINAL? type Identifier bracketsOpt
    ;

 ============================================================================
 MODIFIERS
 ============================================================================

modifier
    : PUBLIC
    | PROTECTED
    | PRIVATE
    | STATIC
    | ABSTRACT
    | FINAL
    | NATIVE
    | SYNCHRONIZED
    | TRANSIENT
    | VOLATILE
    | STRICTFP
    ;

 ============================================================================
 TYPES
 ============================================================================

type
    : qualifiedIdentifier bracketsOpt
    | basicType bracketsOpt
    ;

basicType
    : BYTE
    | SHORT
    | CHAR
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    | BOOLEAN
    ;

qualifiedIdentifier
    : Identifier (PERIOD Identifier)*
    ;

qualifiedIdentifierList
    : qualifiedIdentifier (COMMA qualifiedIdentifier)*
    ;

bracketsOpt
    : (OPEN_BRACKET CLOSE_BRACKET)*
    ;

 ============================================================================
 STATEMENTS
 ============================================================================

block
    : OPEN_BRACE blockStatement* CLOSE_BRACE
    ;

blockStatement
    : localVariableDeclaration SEMICOLON
    | statement
    | classDeclaration
    ;

forInit
    : localVariableDeclaration
    | expressionList
    ;

forUpdate
    : expressionList
    ;


switchBlockStatementGroup
    : switchLabel+ blockStatement*
    ;

switchLabel
    : CASE constantExpression COLON
    | DEFAULT COLON
    ;

synchronizedStatement
    : SYNCHRONIZED OPEN_PARENTHESIS expression CLOSE_PARENTHESIS block
    ;


catchClause
    : CATCH OPEN_PARENTHESIS formalParameter CLOSE_PARENTHESIS block
    ;

 ============================================================================
 EXPRESSIONS
 ============================================================================

expression
    : expression1 (assignmentOperator expression1)?
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

statementExpression
    : expression
    ;

constantExpression
    : expression
    ;

expression1
    : expression2 (expression1Rest)?
    ;

expression1Rest
    : QUESTION expression COLON expression1
    ;

expression2
    : expression3 (expression2Rest)*
    ;

expression2Rest
    : infixOp expression3
    | INSTANCEOF type
    ;

infixOp
    : DOUBLE_PIPE
    | DOUBLE_AMPERSAND
    | PIPE
    | CARET
    | AMPERSAND
    | DOUBLE_EQUALS
    | EXCLAMATION_EQUALS
    | LESS_THAN
    | GREATER_THAN
    | LESS_THAN_OR_EQUALS
    | GREATER_THAN_OR_EQUALS
    | DOUBLE_LESS_THAN
    | DOUBLE_GREATER_THAN
    | TRIPLE_GREATER_THAN
    | PLUS
    | MINUS
    | ASTERISK
    | SLASH
    | PERCENT
    ;

expression3
    : prefixOp expression3
    | OPEN_PARENTHESIS (expression | type) CLOSE_PARENTHESIS expression3
    | primary (selector)* (postfixOp)*
    ;

prefixOp
    : DOUBLE_PLUS
    | DOUBLE_MINUS
    | EXCLAMATION
    | TILDE
    | PLUS
    | MINUS
    ;

postfixOp
    : DOUBLE_PLUS
    | DOUBLE_MINUS
    ;


identifierSuffix
    : OPEN_BRACKET (CLOSE_BRACKET bracketsOpt PERIOD CLASS
                   | expression CLOSE_BRACKET)
    | arguments
    | PERIOD (CLASS
             | THIS
             | SUPER arguments
             | NEW innerCreator)
    ;

selector
    : PERIOD Identifier argumentsOpt
    | PERIOD THIS
    | PERIOD SUPER superSuffix
    | PERIOD NEW innerCreator
    | OPEN_BRACKET expression CLOSE_BRACKET
    ;


argumentsOpt
    : (arguments)?
    ;

arguments
    : OPEN_PARENTHESIS (expression (COMMA expression)*)? CLOSE_PARENTHESIS
    ;

creator
    : qualifiedIdentifier (arrayCreatorRest | classCreatorRest)
    ;

innerCreator
    : Identifier classCreatorRest
    ;

arrayCreatorRest
    : OPEN_BRACKET (CLOSE_BRACKET bracketsOpt arrayInitializer
                   | expression CLOSE_BRACKET (OPEN_BRACKET
                     (expression)? CLOSE_BRACKET)* bracketsOpt)
    ;

classCreatorRest
    : arguments classBody?
    ;

arrayInitializer
    : OPEN_BRACE (variableInitializer (COMMA variableInitializer)*
                 (COMMA)?)? CLOSE_BRACE
    ;

literal
    : IntegerLiteral
    | FloatingPointLiteral
    | CharacterLiteral
    | StringLiteral
    | BooleanLiteral
    | NullLiteral
    ;
