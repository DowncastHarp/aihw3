package aiHomework3;

import java.io.*;
import java.util.*;

class Literal {
	public Boolean negated;
	public String name;
	public ArrayList<String> arguments;
	
	public Literal() {
		this.negated = false;
		this.name = "";
		this.arguments = new ArrayList<String>();
	}
}

class Clause {
	public Set<Literal> literals;
	
	public Clause() {
		this.literals = new HashSet<Literal>();
	}
}

public class homework {
	
	public static void main( String[] args ) throws Exception
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		File inFile = new File( "input.txt" );
		File outFile = new File( "output.txt" );

		int numQueries = 0;
		List<String> queries = new ArrayList<String>();
		int numRawSentences = 0;
		List<String> rawSentences = new ArrayList<String>();
		Set<Clause> knowledgeBase = new HashSet<Clause>();

		// Read the input file
		try {
			br = new BufferedReader( new FileReader( inFile ) );

			// Number of Queries
			String numQueriesStr = br.readLine();
			numQueries = Integer.parseInt( numQueriesStr );

			// Queries
			for( int i = 0; i < numQueries; ++i )
			{
				String query = br.readLine();
				queries.add( query );
			}

			// Number of Raw Sentences
			String numRawSentencesStr = br.readLine();
			numRawSentences = Integer.parseInt( numRawSentencesStr );

			// Sentences
			for( int i = 0; i < numRawSentences; ++i )
			{
				String sentence = br.readLine();
				rawSentences.add( sentence );
			}
		}
		catch ( IOException e )
		{
			System.out.println( "Error with input: " + e.getMessage() );
		}
		finally {
			if (br != null)
			{
				br.close();
			}
		}

		// Parse the raw sentences. Convert to Conjunctive Normal Form. Add final sentence to the KB
		for( String rawSentence : rawSentences )
		{
			knowledgeBase.addAll( ConvertToCNF( rawSentence ) );
		}

		//Test the queries against the knowledge base here

		try {
			bw = new BufferedWriter( new FileWriter( outFile ) );
			for( String rawSentence : rawSentences )
			{
				bw.write( rawSentence );
				bw.newLine();
			}
			
			bw.newLine();
			
			for( Clause kbClause : knowledgeBase )
			{
//				bw.write( knowledgeBase.get( i ) );
//				bw.newLine();
			}
			
			bw.newLine();
			
			for( String query : queries )
			{
				bw.write( query );
				bw.newLine();
			}
		}
		catch ( IOException e )
		{
			System.out.println( "Error with output: " + e.getMessage() );
		}
		finally {
			if (bw != null)
			{
				bw.close();
			}
		}
	}
	
	private static List<Clause> ConvertToCNF( String rawSentence )
	{
		List<Clause> convertedClauses = new ArrayList<Clause>();
		
		// Create a StringBuilder object and remove the whitespace from the rawSentence
		StringBuilder modified = new StringBuilder( rawSentence.replaceAll( "\\s+", "" ) );
		
		// 1. Replace "=>" with ~, |
		while ( modified.indexOf( "=>" ) != -1)
		{
			int impIndex = modified.indexOf( "=>" );
			int start = impIndex;
			int end = impIndex + 2;
			// Get the entire section this is in that's enclosed by parentheses
			int leftParenIndex = 0;
			int rightParenIndex = 0;
			
			while ( leftParenIndex < 1 )
			{
				--start;
				if ( modified.charAt( start ) == ')' )
				{
					--leftParenIndex;
				}
				else if (modified.charAt( start ) == '(' )
				{
					++leftParenIndex;
				}
			}
			
			while ( rightParenIndex < 1 )
			{
				--end;
				if ( modified.charAt( end ) == ')' )
				{
					--rightParenIndex;
				}
				else if (modified.charAt( end ) == '(' )
				{
					++rightParenIndex;
				}
			}
			modified.replace( impIndex, impIndex + 2, ")|");
			modified.insert( start + 1, "(~" );
		}
		
		// 2. Move ~ inwards
		int notIndex = - 1;
		
		while ( modified.indexOf( "~", notIndex + 1) != -1 )
		{
			notIndex = modified.indexOf( "~", notIndex + 1);
			if( modified.charAt( notIndex + 1 ) != '(' )
			{
				continue;
			}
			
			// Get the sentence within the negated parentheses
			int startNegate = notIndex + 1;
			int endNegate = getIndexOfClosingParenthesis( modified.toString(), startNegate );
			
			String clauseToNegate = modified.substring( startNegate + 1, endNegate - 1 );
			// Simplify negation
			if( clauseToNegate.charAt( 0 ) == '~' )
			{
				// Remove the negation
				modified.replace( startNegate, endNegate, clauseToNegate.substring(1) );
			}
			else
			{
				// Distribute negation
				int endOfFirstParenthesis = getIndexOfClosingParenthesis( clauseToNegate, 0 );
				
				// If we're distributing a ~ over &, we need to convert the & to |
				String operator = ( clauseToNegate.charAt(endNegate) == '|' ) ? "&" : "|";
				String leftOperand = clauseToNegate.substring( startNegate, endOfFirstParenthesis );
				String rightOperand = clauseToNegate.substring( endOfFirstParenthesis + 1 );
				modified.append( "((~" ).append( leftOperand ).append( ")" ).append( operator ).append( "(~" ).append( rightOperand ).append( "))" );
			}
			
			notIndex = -1;
			String dummy = modified.toString();
			int asdfasd = 0;
			
		}
		// 3. Distribute | over &
		
		
		return convertedClauses;
	}
	
	private static int getIndexOfClosingParenthesis( String input, int startParenIndex )
	{
		int endParenIndex = startParenIndex;
		int openParenCount = 0;
		int closeParenCount = 0;
		
		while( openParenCount == 0 || openParenCount != closeParenCount )
		{
			if(input.charAt( endParenIndex ) == '(' )
			{
				++openParenCount;
			}
			else if (input.charAt( endParenIndex ) == ')' )
			{
				++closeParenCount;
			}
			++endParenIndex;
		}
		
		return endParenIndex;
	}
}
