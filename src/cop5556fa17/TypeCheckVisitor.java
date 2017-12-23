package cop5556fa17;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


import cop5556fa17.TypeUtils.Type;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
	public Map<String, Declaration> symbolTable = new HashMap<String, Declaration>();	
	
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}
		}		
			
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression e = declaration_Variable.e;
		String name = declaration_Variable.name;
		
		if(e != null){
			e.visit(this, null);
		}
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_Variable);
			declaration_Variable.decType = TypeUtils.getType(declaration_Variable.type);
		}
		else{
			throw new SemanticException(declaration_Variable.firstToken, "SymbolTable already contains " +name);
		}
		
		if(e != null){
			if(declaration_Variable.decType == e.type){
				//
			}else{
				throw new SemanticException(declaration_Variable.firstToken,"Type mismatch: "+declaration_Variable.decType+ " and " + e.type);
			}
		}
		
		return declaration_Variable.decType;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		
		// TODO Auto-generated method stub
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		if(e0 != null){
			e0.visit(this,null);
		}
		if(e1 != null){
			e1.visit(this,null);
		}
		Kind op = expression_Binary.op;
		if(op == Kind.OP_EQ || op == Kind.OP_NEQ){
			expression_Binary.type = Type.BOOLEAN;
		}else if((op == Kind.OP_GE || op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_LE) && e0.type == Type.INTEGER ){
			expression_Binary.type = Type.BOOLEAN;
		}else if((op == Kind.OP_AND || op == Kind.OP_OR) && (e0.type == Type.INTEGER || e0.type == Type.BOOLEAN)){
			expression_Binary.type = e0.type;
		}else if((op == Kind.OP_DIV || op == Kind.OP_MINUS || op == Kind.OP_MOD || op == Kind.OP_PLUS || op == Kind.OP_POWER || op == Kind.OP_TIMES) && (e0.type == Type.INTEGER)){
			expression_Binary.type = Type.INTEGER;
		}else{
			expression_Binary.type = null;
		}
		
		if(e0.type == e1.type && expression_Binary.type != null){
			
		}else{
			throw new SemanticException(expression_Binary.firstToken, "Constraint not satisfied: e0.type == e1.type && expression_Binary.type != null");
		}

		
		return expression_Binary.type;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = expression_Unary.e;
		if(e != null){
			e.visit(this, null);
		}
		Kind op = expression_Unary.op;
		if(op == Kind.OP_EXCL && (e.type == Type.BOOLEAN || e.type == Type.INTEGER)){
			expression_Unary.type = e.type;
		}
		else if((op == Kind.OP_PLUS || op == Kind.OP_MINUS) && (e.type == Type.INTEGER)){
			expression_Unary.type = Type.INTEGER;
		}else{
			expression_Unary.type = null;
		}
		if(expression_Unary.type == null){
			throw new SemanticException(expression_Unary.firstToken, "Type of Unary Expression is null");
		}
		
		return expression_Unary.type;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = index.e0;
		Expression e1 = index.e1;
		
		if(e0 != null){
			e0.visit(this, null);
		}
		if(e1 != null){
			e1.visit(this, null);
		}
		if(e0.type == Type.INTEGER && e1.type ==Type.INTEGER){
			//
			if(e0.getClass() == Expression_PredefinedName.class && e1.getClass() == Expression_PredefinedName.class){
				Expression_PredefinedName ex0 = (Expression_PredefinedName)e0;
				Expression_PredefinedName ex1 = (Expression_PredefinedName)e1;
				index.setCartesian(!(ex0.kind == Kind.KW_r && ex1.kind == Kind.KW_a));
			}
			index.setCartesian(!(e0.firstToken.kind == Kind.KW_r && e1.firstToken.kind == Kind.KW_a));
			
		}else{
			throw new SemanticException(index.firstToken, "Required e0.type = INTEGER. Found e0: " + e0.type+", e1: "+e1.type);
		}
		
		//throw new UnsupportedOperationException();
		return index.type;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Index index = expression_PixelSelector.index;
		String name = expression_PixelSelector.name;
		if(index != null){
			index.visit(this, null);
		}
		if(symbolTable.containsKey(name)){
			Type pixType = symbolTable.get(name).decType;
			if(pixType == Type.IMAGE){
				expression_PixelSelector.type = Type.INTEGER;
			}else if(index == null){
				expression_PixelSelector.type = pixType;
			}else{
				expression_PixelSelector.type = null;
			}
			
			if(expression_PixelSelector.type == null){
				throw new SemanticException(expression_PixelSelector.firstToken, "Type of PixelSelector is null");
			}
			
		}else{
			throw new SemanticException(expression_PixelSelector.firstToken,"SymbolTable does not contain "+name);
		}
		return expression_PixelSelector.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression condition = expression_Conditional.condition;
		Expression trueExpression = expression_Conditional.trueExpression;
		Expression falseExpression = expression_Conditional.falseExpression;
		if(condition != null){
			condition.visit(this, null);
		}
		if(trueExpression != null){
			trueExpression.visit(this, null);
		}
		if(falseExpression != null){
			falseExpression.visit(this, null);
		}
		if(condition.type == Type.BOOLEAN && (trueExpression.type == falseExpression.type)){
			
		}else{
			throw new SemanticException(expression_Conditional.firstToken, "Constraint not satisfied: condition.type == Type.BOOLEAN && trueExpression.type == falseExpression.type");
		}
		expression_Conditional.type = trueExpression.type;
		
		return expression_Conditional.type;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		String name = declaration_Image.name;
		Expression xSize = declaration_Image.xSize;
		Expression ySize = declaration_Image.ySize;
		Source source = declaration_Image.source;
		if(source != null){
			source.visit(this, null);
		}
		if(xSize != null){
			xSize.visit(this, null);
		}
		if(ySize != null){
			ySize.visit(this,null);
		}
		
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_Image);
			declaration_Image.decType = Type.IMAGE;
		}else{
			throw new SemanticException(declaration_Image.firstToken,"SymbolTable already contains"+name);
		}
		if(xSize!=null){
			if(ySize!=null && xSize.type.equals(Type.INTEGER) && ySize.type.equals(Type.INTEGER)){
				//
			}else{
				throw new SemanticException(declaration_Image.firstToken,"Constraint If xSize != null then ySize!= null not satisfied.");
			}	
		}
		return declaration_Image.decType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//boolean flag = true;
		int f = 1;
		try
		{
			URL urlToCheck = new URL(source_StringLiteral.fileOrUrl);
			urlToCheck.toURI();
		}
		catch(Exception e)
		{
			//flag =false;
			f = 0;
		}
		if(f==1)
		{
			source_StringLiteral.type = Type.URL;
		}
		else
		{
			source_StringLiteral.type = Type.FILE;
		}
		return source_StringLiteral.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression paramNum  = source_CommandLineParam.paramNum;
		if(paramNum != null){
			paramNum.visit(this, null);
		}
		if(paramNum.type != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, "Type mismatch. Required : INTEGER. Found :"+paramNum.type);
		}else{
			//
		}
		source_CommandLineParam.type = null;
/*		if(paramNum != null){
			paramNum.visit(this, null);
			source_CommandLineParam.type = paramNum.type;
		}
		
		if(source_CommandLineParam.type != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, "Type mismatch. Required : INTEGER. Found :"+source_CommandLineParam.type);
		}
*/		return source_CommandLineParam.type;	
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = source_Ident.name;
		if(!symbolTable.containsKey(name)){
			throw new SemanticException(source_Ident.firstToken, name + " not defined in "+source_Ident.toString() );
		}else{
			source_Ident.type = symbolTable.get(name).decType;
			if(source_Ident.type == Type.FILE || source_Ident.type ==Type.URL){
				
			}else{
				throw new SemanticException(source_Ident.firstToken, "Type mismatch. Required: FILE or URL. Found" +source_Ident.type);
			}
		}
		return source_Ident.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Source source = declaration_SourceSink.source;
		String name = declaration_SourceSink.name;
		
		if(source != null){
			source.visit(this, null);
		}
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_SourceSink);
			if(declaration_SourceSink.type ==  Kind.KW_url){
				declaration_SourceSink.decType = Type.URL;
			}else if(declaration_SourceSink.type == Kind.KW_file){
				declaration_SourceSink.decType = Type.FILE;
			}else{
				throw new SemanticException(declaration_SourceSink.firstToken, "SourceSink type mismatch. Required FILE or URL");
			}
		}else{
			throw new SemanticException(declaration_SourceSink.firstToken, "Symboltable already contains "+ name);
		}
		
		//WHAT IF SOURCE IS NULL
		if(declaration_SourceSink.decType == source.type || source.type == null){
			
		}else{
			throw new SemanticException(declaration_SourceSink.firstToken, "Type mismatch declaration_SourceSink.decType != source.type");
		}
		
		return declaration_SourceSink.decType;
			
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.type = Type.INTEGER;
		return expression_IntLit.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression argEx = expression_FunctionAppWithExprArg.arg;
		if(argEx != null){
			argEx.visit(this, null);
			if(argEx.type != Type.INTEGER){
				throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Type mismatch. Found" + argEx.type + ". Required : INTEGER" );
			}
			else{
				expression_FunctionAppWithExprArg.type = Type.INTEGER;
			}
		}
		
		return argEx.type;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Index argEx = expression_FunctionAppWithIndexArg.arg;
		if(argEx!= null){
			argEx.visit(this, null);
		}
		expression_FunctionAppWithIndexArg.type = Type.INTEGER;
		return expression_FunctionAppWithIndexArg.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.type = Type.INTEGER;
		return expression_PredefinedName.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		String name = statement_Out.name;
		Sink sink = statement_Out.sink;
		if(sink != null){
			sink.visit(this, null);
		}
		
		if(symbolTable.containsKey(name)){
			statement_Out.setDec(symbolTable.get(name));
		}else{
			throw new SemanticException(statement_Out.firstToken, "SymbolTable does not contain "+name);
		}
		Declaration dec = symbolTable.get(name);
		if(((dec.decType == Type.INTEGER || dec.decType == Type.BOOLEAN) && (sink.type == Type.SCREEN))
			|| ((dec.decType == Type.IMAGE) && (sink.type == Type.FILE || sink.type == Type.SCREEN))){
			//
		}else{
			throw new SemanticException(statement_Out.firstToken, "REQUIRE: ((name.Type == INTEGER || name.Type == BOOLEAN) && Sink.Type == SCREEN)|| (name.Type == IMAGE && (Sink.Type ==FILE || Sink.Type == SCREEN))");
		}
		
		return statement_Out.getDec();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Source source = statement_In.source;
		String name = statement_In.name;
		if(source != null){
			source.visit(this, null);
		}
		if(symbolTable.containsKey(name)){
			statement_In.setDec(symbolTable.get(name));
		}else{
			throw new SemanticException(statement_In.firstToken, name+ " not found in SybolTable");
		}
		/*//SOURCE CAN BE NULL
		if((symbolTable.containsKey(name))&&(symbolTable.get(name).decType == source.type )){
			//
		}else{
			throw new SemanticException(statement_In.firstToken, "constraint (name.Declaration != null) & (name.type == Source.type");
		}
		*/
		return statement_In.getDec();
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = statement_Assign.e;
		LHS lhs = statement_Assign.lhs;
		
		if(e != null){
			e.visit(this, null);
		}
		if(lhs != null){
			lhs.visit(this, null);
		}
		if(lhs.type == e.type){
			
		}
		else if(lhs.type == Type.IMAGE){
			if(e.type == Type.INTEGER){
				//
			}else{
				throw new SemanticException(statement_Assign.firstToken,"Type mismatch: Required LHS type Image, e type Integer. Found LHS type "+lhs.type.toString()+ "Found e "+e.type.toString() );
			}
			
		}else{
			throw new SemanticException(statement_Assign.firstToken,"Type mismatch between e and lhs");
		}
		
		statement_Assign.setCartesian(lhs.isCartesian);
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Index index = lhs.index;
		String name = lhs.name;
		
		if(index != null){
			index.visit(this, null);
		}
		if(!symbolTable.containsKey(name)){
			throw new SemanticException(lhs.firstToken, "symbolTable does not contain "+ name);
		}
		
		lhs.setDec(symbolTable.get(name));
		lhs.type = lhs.getDec().decType;
		if(index != null){
			lhs.isCartesian = index.isCartesian();
		}else{
			lhs.isCartesian = false;
		}		
		return lhs.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.type = Type.SCREEN;
		return sink_SCREEN.type;
		//throw new UnsupportedOperationException();
	}
	
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = sink_Ident.name;
		if(!symbolTable.containsKey(name)){
			throw new SemanticException(sink_Ident.firstToken, "SymbolTable does not contain "+ name);
		}else{
			sink_Ident.type = symbolTable.get(name).decType;
		}
		
		
		if(sink_Ident.type != Type.FILE){
			throw new SemanticException(sink_Ident.firstToken, "Type mismatch. Sink_Ident type : required: INTEGER. found: "+sink_Ident.type);
		}else{
			//
		}
		return sink_Ident.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_BooleanLit.type = Type.BOOLEAN;
		return expression_BooleanLit.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = expression_Ident.name;
		if(symbolTable.containsKey(name)){
			expression_Ident.type = symbolTable.get(name).decType;
		}else{
			throw new SemanticException(expression_Ident.firstToken, "Symbol table does not contain "+name);
		}
		return expression_Ident.type;
		//throw new UnsupportedOperationException();
	}

}
