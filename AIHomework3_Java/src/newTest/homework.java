package newTest;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class homework {
	
	private static final String inputFile = "input.txt";
	private static final String outputFile = "output.txt";
	private static final long timeout = 20000;
	
	public static void main(String args[]) {
		List<String> q = new ArrayList<String>();
		List<String> kb_raw = new ArrayList<String>();
		readFile(q, kb_raw);
		
		Set<String> kb = preProcessingKB(kb_raw);
		
		List<Literal> query_format = new ArrayList<Literal>();
		for (String s: q) {
			String modified = removeSpace(s);
			if (modified.charAt(0) == '~') {
				modified = "(" + modified + ")";
			}
			query_format.add(transformLiteralInFormat(modified));
		}
		Set<Clause> kb_format = transformKBInFormat(kb);
		
		List<Boolean> result = resolution(query_format, kb_format);
		System.out.println(result);
		writeFile(result);
	}
	
	/*================================= Main steps =================================*/
	//1.
	private static void readFile(List<String> queries, List<String> knowledge) {
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(inputFile));
			
			int numberOfQueries = Integer.parseInt(br.readLine());
			for (int i = 0; i < numberOfQueries; i ++) {
				queries.add(br.readLine());
			}
			
			int numberOfKnowledgeBase = Integer.parseInt(br.readLine());
			for (int i = 0; i < numberOfKnowledgeBase; i ++) {
				knowledge.add(br.readLine());
			}
		 
			br.close();
		} catch(IOException e){
			System.out.println("input error");
		}
	}
	
	//2.
	private static Set<String> preProcessingKB(List<String> knowledge) {
		Set<String> kb = new HashSet<String>();
		
		for (int i = 0; i < knowledge.size(); i ++) {
			String rawSentence = knowledge.get(i);
			toCNF(rawSentence, kb);
		}
		
		return kb;
	}
	
	//3.
	private static List<Boolean> resolution(List<Literal> query, Set<Clause> kb) {
		List<Boolean> result = new ArrayList<Boolean>();
		
		for (Literal l: query) {
			Clause clause = new Clause();
			l.negate = !l.negate;
			clause.literals.add(l);
			
			long startTime = System.currentTimeMillis();
			result.add(resolutionHelper(kb, clause, startTime));
		}
		
		return result;
	}
	
	//4.
	private static void writeFile(List<Boolean> outputResult) {
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			
			if (outputResult.size() > 0) {
				bw.write(outputResult.get(0).toString());
				for (int i = 1; i < outputResult.size(); i ++) {
					bw.newLine();
					bw.write(outputResult.get(i).toString());
				}
			}
			
			bw.close();
		} catch(IOException e){
			System.out.println("output error");
		}
	}
	
	
	/*================================= Helper function =================================*/
	
	/*================================= Resolution helper function =================================*/
	private static boolean resolutionHelper(Set<Clause> kb, Clause query, long startTime) {
		Set<Clause> copyKB = new HashSet<Clause>(kb);
		copyKB.add(query);
		while (true) {
			List<Clause> currentClauses = new ArrayList<Clause>(copyKB);
			Set<Clause> resolvedResults = new HashSet<Clause>();
			System.out.println(currentClauses.size());
			System.out.println(currentClauses);
			for (int i = 0; i < currentClauses.size() - 1; i ++) {
				Clause clause1 = currentClauses.get(i);
				for (int j = i + 1; j < currentClauses.size(); j ++) {
					long endTime = System.currentTimeMillis();
					if (endTime - startTime > timeout) {
						return false;
					}
					Clause clause2 = currentClauses.get(j);
					Set<Clause> results = resolveTowClauses(clause1, clause2);
					
					for (Clause resolved: results) {
						if (resolved == null) {
							continue;
						} else if (resolved.literals.size() == 0) {
							return true;
						} else {
							resolvedResults.add(resolved);
						}
					}
				}
			}
			if (copyKB.containsAll(resolvedResults)) {
				return false;
			} else {
				copyKB.addAll(resolvedResults);
			}
		}
	}
	
	private static Set<Clause> resolveTowClauses(Clause clause1, Clause clause2) {
		Set<Clause> clauses = new HashSet<Clause>();
		
		for (Literal literal1: clause1.literals) {
			Set<Literal> literal2Set = new HashSet<Literal>();
			for (Literal l: clause2.literals) {
				if (literal1.name.equals(l.name) && literal1.negate != l.negate) {
					literal2Set.add(l);
				}
			}
			
			for (Literal literal2: literal2Set) {
				Literal copyLiteral2 = literal2;
				Clause copyClause2 = clause2;
				
				for (String arg: literal2.arguments) {
					if (literal1.arguments.contains(arg)) {
						copyLiteral2 = standardizeArguments(literal2);
						copyClause2 = new Clause();
						for (Literal literal: clause2.literals) {
							copyClause2.literals.add(standardizeArguments(literal));
						}
						break;
					}
				}
				
				Map<String, String> substitution = unification(literal1, copyLiteral2);
				
				if (substitution != null) {
					Clause newQuery = new Clause();
					Set<Literal> results = new HashSet<Literal>();
					
					for(Literal l : clause1.literals){
						if(!l.equals(literal1)){
							Literal substLiteral = substitute(l, substitution);
							results.add(substLiteral);
						}
					}
					
					for(Literal l : copyClause2.literals){
						if(!l.equals(copyLiteral2)){
							Literal substLiteral = substitute(l, substitution);
							results.add(substLiteral);
						}
					}
					newQuery.literals.addAll(results);
					
					clauses.add(newQuery);
				}
			}
		}
		
		return clauses;
	}
	
	private static Literal standardizeArguments(Literal target) {
		Literal result = new Literal();
		for (String s: target.arguments) {
			String arg = s;
			if (Character.isLowerCase(arg.charAt(0))) {
				arg += "1";
			}
			result.arguments.add(arg);
		}
		result.negate = target.negate;
		result.name = target.name;
		return result;
	}
	
	private static Map<String, String> unification(Literal query, Literal target) {
		if (query.arguments.size() != target.arguments.size()) {
			return null;
		} else {
			Map<String, String> substitution = new TreeMap<String, String>();
			for (int i = 0; i < query.arguments.size(); i ++) {
				String argument1 = query.arguments.get(i);
				String argument2 = target.arguments.get(i);
				substitution = unify(argument1, argument2, substitution);
				if (substitution == null) {
					return null;
				}
			}
			
			return substitution;
		}
	}
	
	private static Map<String, String> unify(String argument1, String argument2, Map<String, String> sub) {
		if (sub == null) {
			return null;
		} else if (!argument1.equals(argument2)) {
			if (Character.isUpperCase(argument1.charAt(0)) &&
					Character.isUpperCase(argument2.charAt(0))) {
				return null;
			} else if (Character.isLowerCase(argument1.charAt(0))) {
				return unifyVar(argument1, argument2, sub);
			} else if (Character.isLowerCase(argument2.charAt(0))) {
				return unifyVar(argument2, argument1, sub);
			}
		}
		return sub;
	}
	
	private static Map<String, String> unifyVar(String var, String s, Map<String, String> sub){
		if(sub.containsKey(var)){
			return unify(sub.get(var), s, sub);
		} else if(sub.containsKey(s)){
			return unify(var, sub.get(s), sub);
		} else{
			sub.put(var, s);
			return sub;
		}
	}
	
	private static Literal substitute(Literal target, Map<String, String> substitution) {
		Literal result = new Literal();
		for (String s: target.arguments) {
			if (substitution.containsKey(s)) {
				result.arguments.add(substitution.get(s));
			} else {
				result.arguments.add(s);
			}
		}
		result.negate = target.negate;
		result.name = target.name;
		
		return result;
	}
	
	/*================================= Transform function =================================*/
	private static Set<Clause> transformKBInFormat(Set<String> kb) {
		Set<Clause> kbInFormat = new HashSet<Clause>();
		
		for (String c: kb) {
			Clause clause = new Clause();
			clause.literals.addAll(transformClauseInFormat(c));	
			kbInFormat.add(clause);
		}
		return kbInFormat;
	}
	
	private static List<Literal> transformClauseInFormat(String input) {
		Pattern p = Pattern.compile("~*[A-Z]\\w*\\([A-Za-z,]+\\)");
		Matcher m = p.matcher(input);
		Set<String> literalSet = new HashSet<String>();
		while(m.find()){
			literalSet.add(m.group());
		}
		
		List<Literal> result = new ArrayList<Literal>();
		for(String s : literalSet) {
			String modified = s;
			if (modified.charAt(0) == '~') {
				modified = "(" + modified + ")";
			}
			result.add(transformLiteralInFormat(modified));
		}
		
		return result;
	}
	
	private static Literal transformLiteralInFormat(String input) {
		boolean notFlag = false;
		if (input.charAt(0) == '(') {
			notFlag = true;
		}
		String processed = input;
		if (notFlag) {
			processed = input.substring(2, input.length() - 1);
		}
		
		int nameEndIndex = 0;
		while (processed.charAt(nameEndIndex) != '(') {
			nameEndIndex ++;
		}
		String name = processed.substring(0, nameEndIndex);
		String arguments = processed.substring(nameEndIndex + 1, processed.length() - 1);
		String[] argument = arguments.split(",");
		
		Literal literal = new Literal();
		for (int i = 0; i < argument.length; i ++) {
			literal.arguments.add(argument[i]);
		}
		literal.negate = notFlag;
		literal.name = name;
		
		return literal;
	}
	
	/*================================= Transfer input to CNF form =================================*/
	private static void toCNF(String rawSentence, Set<String> kb) {
		String sentence = removeSpace(rawSentence);
		
		sentence = removeImplication(sentence);
		sentence = moveNotInward(sentence);
		List<String> clauses = applyDistribute(sentence);
		kb.addAll(clauses);
	}
	
	private static String removeImplication(String rawSentence) {
		while (rawSentence.indexOf("=>") != -1) {
			int index = rawSentence.indexOf("=>");
			int start = index;
			int end = index + 1;
			int left = 0;
			int right = 0;
			
			while (left < 1) {
				start --;
				if (rawSentence.charAt(start) == ')') {
					left --;
				} else if (rawSentence.charAt(start) == '(') {
					left ++;
				}
			}
			
			while (right < 1) {
				end ++;
				if (rawSentence.charAt(end) == '(') {
					right --;
				} else if (rawSentence.charAt(end) == ')') {
					right ++;
				}
			}
			
			rawSentence = rawSentence.substring(0, start + 1) +
					"(~" + rawSentence.substring(start + 1, index) + ")" + 
					"|" + 
					rawSentence.substring(index + 2, end) +
					rawSentence.substring(end, rawSentence.length());
		}
		
		return rawSentence;
	}
	
	private static String moveNotInward(String rawSentence) {
		int index = -1;
		
		while (findSymbol(rawSentence, '~', index + 1) != -1) {
			index = findSymbol(rawSentence, '~', index + 1);
			if (rawSentence.charAt(index + 1) != '(') {
				continue;
			}
			
			int[] parenthesesIndex = getFirstOuterParentheses(rawSentence, index + 1, index + 1);
			int start = parenthesesIndex[0];
			int end = parenthesesIndex[1];
				
			String modified = rawSentence.substring(start, end);
			modified = moveNotHelper(modified);
			rawSentence = rawSentence.substring(0, start - 2) + modified + rawSentence.substring(end + 1);
			index = start - 2;
		}
		
		return rawSentence;
	}
	
	private static String moveNotHelper(String input) {
		String inner = input.substring(1, input.length() - 1);
		
		if (inner.charAt(0) == '~') {
			return inner.substring(1);
		} else {
			int[] parenthesesIndex = getFirstOuterParentheses(inner, 0, 0);
			int start = parenthesesIndex[0];
			int end = parenthesesIndex[1];
			
			String connection = "|";
			if (inner.charAt(end) == '|') {
				connection = "&";
			}
			String first = inner.substring(start, end);
			String second = inner.substring(end + 1);
			return "((~" + first + ")" + connection + "(~" + second + "))";
		}
	}
	
	private static List<String> applyDistribute(String rawSentence) {
		if (rawSentence.charAt(0) != '(' || (rawSentence.charAt(0) == '(' && rawSentence.charAt(1) == '~')) {
			List<String> result = new ArrayList<String>();
			result.add(rawSentence);
			return result;
		}
		String modified = rawSentence.substring(1, rawSentence.length() - 1);
		int[] parenthesesIndex = getFirstOuterParentheses(modified, 0, 0);
		int start = parenthesesIndex[0];
		int end = parenthesesIndex[1];
			
		String first = modified.substring(start, end);
		String second = modified.substring(end + 1);
		List<String> firstResult = applyDistribute(first);
		List<String> secondResult = applyDistribute(second);
		List<String> result = new ArrayList<String>();
		
		char connection = modified.charAt(end);
		if (connection == '|') {
			for (int i = 0; i < firstResult.size(); i ++) {
				for (int j = 0; j < secondResult.size(); j ++) {
					String newOne = "(" + firstResult.get(i) + "|" + secondResult.get(j) + ")";
					result.add(newOne);
				}
			}
		} else {
			result.addAll(firstResult);
			result.addAll(secondResult);
		}
		return result;
	}

	/*================================= General public handle string function =================================*/	
	private static String removeSpace(String rawSentence) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < rawSentence.length(); i ++) {
			if (rawSentence.charAt(i) != ' ') {
				sb.append(rawSentence.charAt(i));
			}
		}
		
		return sb.toString();
	}
	
	private static int findSymbol(String input, char symbol, int startIndex) {
		for (int i = startIndex; i < input.length(); i ++) {
			if (input.charAt(i) == symbol) {
				return i;
			}
		}
		return -1;
	}
	
	private static int[] getFirstOuterParentheses(String input, int start, int end) {	
		int leftCount = 0, rightCount = 0;
		while(leftCount == 0 || leftCount != rightCount){
			if(input.charAt(end) == '('){
				leftCount++;
			}
			else if(input.charAt(end) == ')'){
				rightCount++;
			}
			end++;
		}
		
		return new int[]{start, end};
	}
}