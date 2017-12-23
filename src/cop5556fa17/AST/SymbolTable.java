package cop5556fa17.AST;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	Map<String,ASTNode> symbolTable;

	public SymbolTable() {
		symbolTable = new HashMap<>();
	}
	
	public boolean lookupType(String name) {
		return symbolTable.containsKey(name);
	}
	
	public void insert(String name, ASTNode node) {
		symbolTable.put(name, node);
	}
	
	public ASTNode get(String name) {
		return symbolTable.get(name);
	}
	
}