lexer grammar JavaLexer;

@header {
    //package ca.ucalgary.cpsc49902;
}

// SECTION 1: KEYWORDS - 50 Reserved Keywords (alphabetical order matching JavaCC)
ABSTRACT     : 'abstract';
BOOLEAN      : 'boolean';
BREAK        : 'break';
BYTE         : 'byte';
CASE         : 'case';
CATCH        : 'catch';
CHAR         : 'char';
CLASS        : 'class';
CONST        : 'const';
CONTINUE     : 'continue';
DEFAULT      : 'default';
DO           : 'do';
DOUBLE       : 'double';
ELSE         : 'else';
EXTENDS      : 'extends';
FINAL        : 'final';
FINALLY      : 'finally';
FLOAT        : 'float';
FOR          : 'for';
GOTO         : 'goto';
IF           : 'if';
IMPLEMENTS   : 'implements';
IMPORT       : 'import';
INSTANCEOF   : 'instanceof';
INT          : 'int';
INTERFACE    : 'interface';
LONG         : 'long';
NATIVE       : 'native';
NEW          : 'new';
PACKAGE      : 'package';
PRIVATE      : 'private';
PROTECTED    : 'protected';
PUBLIC       : 'public';
RETURN       : 'return';
SHORT        : 'short';
STATIC       : 'static';
STRICTFP     : 'strictfp';
SUPER        : 'super';
SWITCH       : 'switch';
SYNCHRONIZED : 'synchronized';
THIS         : 'this';
THROW        : 'throw';
THROWS       : 'throws';
TRANSIENT    : 'transient';
TRY          : 'try';
VOID         : 'void';
VOLATILE     : 'volatile';
WHILE        : 'while';

// SECTION 2: LITERALS
TRUE  : 'true';
FALSE : 'false';
NULL  : 'null';

INTEGER_LITERAL
    : '0' [xX] HexDigit+ [lL]?          // Hexadecimal
    | '0' [0-7]+ [lL]?                  // Octal
    | '0' [lL]?                         // Zero
    | [1-9] Digit* [lL]?                // Decimal
    ;

FLOATING_POINT_LITERAL
    : Digit+ '.' Digit* Exponent? [fFdD]?
    | '.' Digit+ Exponent? [fFdD]?
    | Digit+ Exponent [fFdD]?
    | Digit+ [fFdD]
    ;

CHARACTER_LITERAL
    : '\'' (UnicodeEscape | EscapeSequence | ~['\\\r\n]) '\''
    ;

STRING_LITERAL
    : '"' (UnicodeEscape | EscapeSequence | ~["\\\r\n])* '"'
    ;

// SECTION 3: SEPARATORS
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
SEMI   : ';';
COMMA  : ',';
DOT    : '.';

// SECTION 4: OPERATORS (Prioritized by length and symbol uniqueness)
URSHIFT_ASSIGN : '>>>=';
URSHIFT        : '>>>';
RSHIFT_ASSIGN  : '>>=';
LSHIFT_ASSIGN  : '<<=';
LSHIFT         : '<<';
RSHIFT         : '>>';
EQUAL          : '==';
LE             : '<=';
GE             : '>=';
NOTEQUAL       : '!=';
AND            : '&&';
OR             : '||';
INC            : '++';
DEC            : '--';
ADD_ASSIGN     : '+=';
SUB_ASSIGN     : '-=';
MUL_ASSIGN     : '*=';
DIV_ASSIGN     : '/=';
AND_ASSIGN     : '&=';
OR_ASSIGN      : '|=';
XOR_ASSIGN     : '^=';
MOD_ASSIGN     : '%=';
ASSIGN         : '=';
GT             : '>';
LT             : '<';
BANG           : '!';
TILDE          : '~';
QUESTION       : '?';
COLON          : ':';
ADD            : '+';
SUB            : '-';
MUL            : '*';
DIV            : '/';
BITAND         : '&';
BITOR          : '|';
CARET          : '^';
MOD            : '%';

// SECTION 5: IDENTIFIER
IDENTIFIER : [a-zA-Z_$] [a-zA-Z0-9_$]*;

// SECTION 6: WHITESPACE AND COMMENTS
WS : [ \t\r\n\f\u001A]+ -> channel(HIDDEN);

SINGLE_LINE_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN);

FORMAL_COMMENT : '/**' .*? '*/' -> channel(HIDDEN);

MULTI_LINE_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);

// SECTION 7: FRAGMENTS
fragment Digit      : [0-9];
fragment HexDigit   : [0-9a-fA-F];
fragment Exponent   : [eE] [+-]? Digit+;
fragment UnicodeEscape : '\\' 'u' HexDigit HexDigit HexDigit HexDigit;
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' [0-3] [0-7] [0-7]
    | '\\' [0-7] [0-7]
    | '\\' [0-7]
    ;