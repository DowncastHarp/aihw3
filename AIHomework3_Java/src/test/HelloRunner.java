package test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class HelloRunner 
{
	public static void main( String[] args) throws Exception 
	{
	
		ANTLRInputStream input = new ANTLRInputStream( System.in );
		
		HelloLexer helloLexer = new HelloLexer(input);
		
		CommonTokenStream tokens = new CommonTokenStream(helloLexer);
		
		HelloParser parser = new HelloParser(tokens);
		ParseTree tree = parser.r(); // begin parsing at rule 'r'
		System.out.println(tree.toStringTree(parser)); // print LISP-style tree
	}
}
