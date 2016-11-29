/**
 * Define a grammar called AI
 */
grammar AI;

@header {
  package aiHomework3;
}

r  : 'not' ID ;				// match keyword not followed by an identifier

ID : [a-z]+ ;				// match lower-case identifiers

WS : [ \t\r\n]+ -> skip ;	// skip spaces, tabs, newlines

