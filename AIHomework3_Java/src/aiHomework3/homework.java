package aiHomework3;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

	private static final String inFile = "input.txt";
	private static final String outFile = "output.txt";
	private static int timeout = 10000;
	
	public static void main( String[] args ) throws Exception
	{
		BufferedReader br = null;
		BufferedWriter bw = null;

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

		List<Clause> converted = new ArrayList<Clause>();
		// Parse the raw sentences. Convert to Conjunctive Normal Form. Add final sentence to the KB
		for( String rawSentence : rawSentences )
		{
			converted.addAll( ConvertToCNF( rawSentence ) );
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
				for( Literal lit : kbClause.literals )
				{
					bw.write( "Negate: " + lit.negated + ", Name: " + lit.name + ", Args: " );
					for( String arg : lit.arguments )
					{
						bw.write( arg + ",");
					}
					bw.newLine();
				}
				bw.newLine();
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
	
	private static Boolean resolveQuery( Literal query, Set<Clause> kb )
	{
		// copy the kb to make modifications to it freely.
		Set<Clause> kbClone = new HashSet<Clause>(kb);
		Clause clause = new Clause();
		
		query.negated = !query.negated;
		clause.literals.add( query );
		
		// add the new clause to the kbClone
		kbClone.add( clause );
		
		
		return true;
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
				modified.replace( notIndex - 1, endNegate + 1, clauseToNegate.substring(1) );
			}
			else
			{
				// Distribute negation
				int endOfFirstParenthesis = getIndexOfClosingParenthesis( clauseToNegate, 0 );
				
				// If we're distributing a ~ over &, we need to convert the & to |
				String operator = ( clauseToNegate.charAt(endOfFirstParenthesis) == '|' ) ? "&" : "|";
				String leftOperand = clauseToNegate.substring( 0, endOfFirstParenthesis );
				String rightOperand = clauseToNegate.substring( endOfFirstParenthesis + 1 );
				modified.replace( notIndex, endNegate, "(~" + leftOperand + ")" + operator + "(~" + rightOperand + ")" );
			}
		}
		
		// 3. Distribute | over &
		List<String> rawClauses = distributeOrOverAnd( modified.toString() );
		
		// Convert the string clauses into Literal and Clause objects
		for ( String rawClause : rawClauses )
		{
			convertedClauses.add( convertStringToClause( rawClause ) );
		}
		
		return convertedClauses;
	}
	
	private static int getIndexOfClosingParenthesis( String input, int startParenIndex )
	{
		int currentIndex = startParenIndex;
		int openParenCount = 0;
		int closeParenCount = 0;
		
		while( openParenCount == 0 || openParenCount != closeParenCount )
		{
			if(input.charAt( currentIndex ) == '(' )
			{
				++openParenCount;
			}
			else if (input.charAt( currentIndex ) == ')' )
			{
				++closeParenCount;
			}
			++currentIndex;
		}
		
		return currentIndex;
	}
	
	private static List<String> distributeOrOverAnd(String input)
	{
		// return if there is only a single clause in the input
		// the guarantees about parentheses allow us to determine this easily
		List<String> result = new ArrayList<String>();
		if( input.charAt(0) != '(' || (input.charAt(0) == '(' && input.charAt(1) == '~'))
		{
			result.add(input.toString());
			return result;
		}
		
		int endFirstOperand = getIndexOfClosingParenthesis( input.toString(), 1);
		
		String firstOperand = input.substring( 1, endFirstOperand );
		String secondOperand = input.substring( endFirstOperand + 1 );
		// Continue breaking apart the clauses within the operands until there's only one left
		List<String> firstOperandClauses = distributeOrOverAnd( firstOperand );
		List<String> secondOperandClauses = distributeOrOverAnd( secondOperand );
		
		char operator = input.charAt( endFirstOperand );
		// If the first operand was |'d with the second, distribute it to the second operand
		if( operator == '|' )
		{
			for( String first : firstOperandClauses )
			{
				for ( String second : secondOperandClauses )
				{
					StringBuilder distributed = new StringBuilder( "(" );
					distributed.append( first ).append( "|" ).append( second ).append( ")" );
					result.add( distributed.toString() );
				}
			}
		}
		else
		{
			result.addAll( firstOperandClauses );
			result.addAll( secondOperandClauses );
		}
		
		return result;
	}
	
	private static Clause convertStringToClause( String input )
	{
		//Literal:
		//	-Can start with or without negation
		//	-First letter is capitalized
		//	-Followed by and open paren and any number of arguments ( up to 100 )
		//	-Arguments:
		//		-Constants start with an uppercase letter
		//		-Variables are all lowercase
		
		Pattern literalPattern = Pattern.compile( "~*[A-Z]\\w*\\([A-Za-z,]+\\)" );
		Matcher literalMatcher = literalPattern.matcher( input );
		
		List<String> rawLiterals =  new ArrayList<String>();
		while( literalMatcher.find() )
		{
			rawLiterals.add( literalMatcher.group() );
		}
		
		List<Literal> clauseLiterals = new ArrayList<Literal>();
		for( String rawLiteral : rawLiterals )
		{
			clauseLiterals.add( convertStringToLiteral( rawLiteral ) );
		}
		Clause clause = new Clause();
		clause.literals.addAll( clauseLiterals );
		
		return clause;
	}
	
	private static Literal convertStringToLiteral( String input )
	{
		Literal literal = new Literal();
		if( input.charAt(0) == '~' )
		{
			literal.negated = true;
			input = input.substring( 1 );
		}
		
		int parenIndex = 0;
		while (input.charAt(parenIndex) != '(' )
		{
			++parenIndex;
		}

		// remove the close paren, and split the literal on the open paren
		input = input.substring( 0, input.length() - 1 );
		String[] literalSplit = input.split( "\\(", 2 );
		
		literal.name = literalSplit[0];
		literal.arguments.addAll( Arrays.asList( literalSplit[1].split( "," ) ) );
		
		return literal;
	}
}
