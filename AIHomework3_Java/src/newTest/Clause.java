package newTest;

import java.util.*;

public class Clause {
	public Set<Literal> literals;
	
	public Clause() {
		this.literals = new HashSet<Literal>();
	}
	
	@Override
    public int hashCode() {
        return this.literals.hashCode();
    }
 
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Clause)) {
        	return false;
        }
        
        Clause clause = (Clause) obj;
        return clause.literals.equals(this.literals);
    }
    
	@Override
	public String toString() { 
	    return this.literals.toString();
	}
}