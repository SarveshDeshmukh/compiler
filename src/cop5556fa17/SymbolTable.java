package cop5556fa17;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cop5556fa17.AST.Declaration;


public class SymbolTable {
	public HashMap<String, Declaration> symbolTable;
	public SymbolTable()
	{
	symbolTable = new HashMap<String,Declaration>();
	}
public Declaration lookup(String ident){
	Declaration d;
		if(symbolTable.containsKey(ident)) 
			return symbolTable.get(ident);
		else
		return null;
	}

	public boolean insert(String ident, Declaration dec)
	{
		if(!symbolTable.containsKey(ident)) {
			symbolTable.put(ident, dec);
			return true;
		}
		else
			return false;
	}

}

