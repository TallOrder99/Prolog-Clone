grammar LogicMini;

// --- PARSER RULES (The Structure) ---

// The root node: A program is a list of clauses followed by a query
program : clause* query EOF ;

// A clause is either a Fact (head.) or a Rule (head :- body.)
clause  : structure DOT                                # FactRule
        | structure COLON_DASH term_list DOT           # RuleRule
        ;

// A query starts with ?-
query   : Q_MARK_DASH term_list DOT ;

// A list of terms separated by commas (used in args or rule bodies)
term_list : term (COMMA term)* ;

// A term can be a Structure (cat(tom)), an Atom (cat), or a Variable (X)
term    : ATOM LPAREN term_list RPAREN   # StructureTerm
        | ATOM                           # AtomTerm
        | VARIABLE                       # VariableTerm
        ;

// To support "cat" as a structure with 0 args, we map it internally later.
structure : ATOM LPAREN term_list RPAREN
          | ATOM
          ;

// --- LEXER RULES (The Raw Text) ---

COLON_DASH : ':-' ;
Q_MARK_DASH : '?-' ;
DOT        : '.' ;
COMMA      : ',' ;
LPAREN     : '(' ;
RPAREN     : ')' ;

// Variables must start with Uppercase
VARIABLE   : [A-Z][a-zA-Z0-9_]* ;

// Atoms/Functors must start with lowercase
ATOM       : [a-z][a-zA-Z0-9_]* ;

// Skip whitespace
WS         : [ \t\r\n]+ -> skip ;