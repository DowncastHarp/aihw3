package newTest;

import java.util.ArrayList;
import java.util.List;

public class Literal {
	public Boolean negate;
	public String name;
	public List<String> arguments;
	
	public Literal() {
		this.name = "";
		this.negate = false;
		this.arguments = new ArrayList<String>();
	}
	
	@Override
    public int hashCode() {
        return this.negate.hashCode() + this.name.hashCode() + this.arguments.hashCode();
    }
 
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Literal)) {
        	return false;
        }
        
        Literal literal = (Literal) obj;
        return literal.name.equals(this.name) &&
        		literal.negate == this.negate &&
        		literal.arguments.equals(this.arguments);
    }
    
    @Override
	public String toString() {
    	String symbol = "";
    	if (this.negate) {
    		symbol = "~";
    	}
	    return symbol + this.name + this.arguments;
	}
}