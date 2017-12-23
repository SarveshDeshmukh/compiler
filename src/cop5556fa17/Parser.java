package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
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
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public void consume(){
		t = scanner.nextToken();
	}
	
	public void match(Kind k)throws SyntaxException{
		if(t.kind == k ){
			consume();
		}else{
			throw new SyntaxException(t,"Syntax Error : Found "+t.kind + " at line :"+t.line + " at position :" + t.pos_in_line +". Expected : "+ k);
		}
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	 Program program() throws SyntaxException {
		//TODO  implement this
		 
		Program p = null;
		Token firstToken = t;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
	
		if(t.kind == Kind.IDENTIFIER){
			match(Kind.IDENTIFIER);
		}
		else{
			throw new SyntaxException(t, "Syntax error");
		}
		while( isDeclaration() || isStatement() ){
			
			if(isDeclaration()){
				Declaration d = declaration();	
				decsAndStatements.add(d);
			}
			else if(isStatement()){
				Statement s = statement();
				decsAndStatements.add(s);
			}	
			match(Kind.SEMI);	
		}
			
		if(t.kind != Kind.EOF){
			throw new SyntaxException(t, "Syntax error: Expected a declaration or a statement");
		}
		
		p = new Program(firstToken, firstToken, decsAndStatements);
		return p;		
		//throw new UnsupportedOperationException();
	}
	
	Declaration declaration() throws SyntaxException{
		
		Declaration d = null;
//		Token firstToken = t;
		if(t.kind == Kind.KW_int || t.kind == Kind.KW_boolean){
			d = variableDeclaration();
		}
		else if(t.kind == Kind.KW_image){
			d = imageDeclaration();
		}
		else if(t.kind == Kind.KW_url || t.kind == Kind.KW_file){
			d = sourceSinkDeclaration();
		}
		
		return d;
	}
	
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException{
		
		Token firstToken = t;
		Declaration_SourceSink dsrc = null;
		
		Token type = sourceSyncType();
		Token name = t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Source s = source();
		
		dsrc = new Declaration_SourceSink(firstToken, type, name, s);
		return dsrc;
	}
	
	Token sourceSyncType() throws SyntaxException{
		Token type = null;
		if(t.kind == KW_url){
			type = t;
			match(KW_url);
			return type;
			
		}else if(t.kind == KW_file){
			type = t;
			match(KW_file);
			return type;
		}
		else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected KW_url or KW_file but found: "+t.kind);
		}
	}
	
	Source source() throws SyntaxException{
		
		Token firstToken = t;
		if(t.kind == Kind.STRING_LITERAL){
			
			String fileUrl = t.getText();
			match(Kind.STRING_LITERAL);
			return new Source_StringLiteral(firstToken, fileUrl);
		}
		else if(t.kind == Kind.OP_AT){
			match(Kind.OP_AT);
			Expression paramNum = expression();
			return new Source_CommandLineParam(firstToken, paramNum);
		}else if(t.kind == Kind.IDENTIFIER){
			Token name = t;
			match(Kind.IDENTIFIER);
			return new Source_Ident(firstToken, name);
			
		}else{
			throw new SyntaxException(t,"Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected OP_AT or STRING_LITERAL OR IDENTIFIER but found: "+t.kind);
		}
	}

	Declaration_Variable variableDeclaration() throws SyntaxException{
		
		Declaration_Variable dvar = null;
		Token firstToken = t;
		Token type = varType();
		Token name = t;
		Expression e = null;
		match(Kind.IDENTIFIER);
		if(t.kind == Kind.OP_ASSIGN){
			match(Kind.OP_ASSIGN);
			e = expression();
		}else{
			//
		}
		dvar = new Declaration_Variable(firstToken, type, name, e);
		return dvar;
	}
	
	Declaration_Image imageDeclaration() throws SyntaxException{
		Declaration_Image dimg = null;
		Token firstToken = t;
		Expression xSize = null;
		Expression ySize = null;
		Source source = null;
			
		match(Kind.KW_image);
		
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			xSize = expression();
			match(Kind.COMMA);
			ySize = expression();
			match(Kind.RSQUARE);
		}else{
			//
		}
		Token name = t;
		match(Kind.IDENTIFIER);
		if(t.kind == OP_LARROW){
			match(OP_LARROW);
			source = source();
		}else{
			//
		}
		dimg = new Declaration_Image(firstToken, xSize, ySize, name, source);
		return dimg;
	}
	
	Token varType() throws SyntaxException{
		
		Token type = null;
		if(t.kind == KW_int){
			type = t;
			match(KW_int);
			return type;
			
		}else if(t.kind == KW_boolean){
			type = t;
			match(KW_boolean);
			return type;
		}
		else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected KW_int or KW_boolean but found: "+t.kind);
		}
	}

	Statement statement() throws SyntaxException {

		Statement s = null;

		Token next = scanner.peek();

		if (next.kind == Kind.OP_RARROW) {
			s = imageOutStatement();
		}
		else if (next.kind == Kind.OP_LARROW) {
			s = imageInStatement();
		}
		else if (next.kind == Kind.OP_ASSIGN || next.kind == Kind.LSQUARE) {
			s = assignmentStatement();
		}
		else {
			throw new SyntaxException(t, "Illegal token in a statement");
		}

		return s;

	}
	
	Statement_Assign assignmentStatement() throws SyntaxException{
		Statement_Assign sassn = null;
		Token firstToken = t;
		LHS lhs = null;
		Expression e = null;
		lhs = lhs();
		match(Kind.OP_ASSIGN);
		e = expression();
		sassn = new Statement_Assign(firstToken,lhs,e);
		return sassn;
	}
	
	LHS lhs()throws SyntaxException{
		LHS lhs = null;
		Token firstToken = t;
		Index index = null;
		
		Token name = t;
		match(Kind.IDENTIFIER);
		
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			index = lhsSelector();
			match(Kind.RSQUARE);
			lhs = new LHS(firstToken, name, index);
			return lhs;
		}else{
			//
			lhs = new LHS(firstToken, name, null);
			return lhs;
		}
	}
	
	Index lhsSelector() throws SyntaxException{
//		Token firstToken = t;
		Index index = null;
//		Expression e0 = null;
//		Expression e1 = null;
		match(Kind.LSQUARE);
		if(t.kind == Kind.KW_x){
			index = xySelector();
		}else if(t.kind == Kind.KW_r){
			index = raSelector();
			
		}else{
			throw new SyntaxException(t,"Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected KW_r or KW_x but found: "+t.kind);
		}
		match(Kind.RSQUARE);
		return index;
	}
	
	Index selector() throws SyntaxException{
		Index index = null;
		Token firstToken = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = expression();
		match(Kind.COMMA);
		e1 = expression();
		index = new Index(firstToken, e0,e1);
		return index;
	}
	Index xySelector() throws SyntaxException{
		Index index = null;
		Token firstName = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = new Expression_PredefinedName(t, Kind.KW_x);
		match(Kind.KW_x);
		match(Kind.COMMA);
		e1 = new Expression_PredefinedName(t, Kind.KW_y);
		match(Kind.KW_y);
		index = new Index(firstName, e0, e1);
		return index;
	}
	
	Index raSelector() throws SyntaxException{
		Index index = null;
		Token firstName = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = new Expression_PredefinedName(t, Kind.KW_r);
		match(Kind.KW_r);
		match(Kind.COMMA);
		e1 = new Expression_PredefinedName(t, Kind.KW_a);
		match(Kind.KW_a);
		index = new Index(firstName, e0, e1);
		return index;
	}
	
	Statement_Out imageOutStatement() throws SyntaxException{
		Token firstToken = t;
		Statement_Out sout = null;
		Token name = t;
		match(IDENTIFIER);
		match(Kind.OP_RARROW);
		Sink sink = sink();
		sout = new Statement_Out(firstToken, name, sink);
		return sout;
	}
	
	Sink sink() throws SyntaxException{
		Token firstToken = t;
		Token name = null;
		if(t.kind == Kind.IDENTIFIER){
			name = t;
			match(Kind.IDENTIFIER);
			return new Sink_Ident(firstToken, name);
			
		}else if(t.kind == Kind.KW_SCREEN){
			
			match(Kind.KW_SCREEN);
			return new Sink_SCREEN(firstToken);
		}else{
			throw new SyntaxException(t,"Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected IDENTIFIER or KW_SCREEN but found: "+t.kind);
		}
	}
	
	Statement_In imageInStatement() throws SyntaxException{
		Statement_In sin = null;
		Token firstToken = t;
		Token name = t;
		match(IDENTIFIER);
		match(Kind.OP_LARROW);
		Source source = source();
		sin = new Statement_In(firstToken, name, source);
		return sin;
	}
	
	boolean isDeclaration(){
		switch(t.kind){
		case KW_boolean : case KW_int : case KW_image : case KW_url : case KW_file :
			return true;
		default: return false;
		
	}	
		}
		
		
	boolean isStatement(){
		if(t.kind == Kind.IDENTIFIER){
			return true;
		}else{
			return false;
		}
	}
	


	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Token firstToken = t;
		Expression exCond = null;
		Expression condition = null;
		Expression trueExpression = null;
		Expression falseExpression = null;
		
		if(isOrExpression()){
			condition = orexpression();
			if(t.kind == Kind.OP_Q){
				match(Kind.OP_Q);
				trueExpression = expression();
				match(Kind.OP_COLON);
				falseExpression = expression();
				exCond = new Expression_Conditional(firstToken, condition, trueExpression,falseExpression);
				return exCond;
			}
			return condition;
			
		}else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Not an expression");
		}
		
		//throw new UnsupportedOperationException();
	}

	Expression orexpression()throws SyntaxException{
		//Expression_Binary exbinary = null;
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		e0 = andExpression();
		while(t.kind == OP_OR){
			op = t;
			match(Kind.OP_OR);
			e1 = andExpression();
			e0 = new Expression_Binary(firstToken,e0,op,e1);
		}
		return e0;
	}
	
	Expression andExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		
		e0 = eqExpression();
		while(t.kind == Kind.OP_AND){
			op = t;
			match(Kind.OP_AND);
			e1 = eqExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression eqExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		
		e0 = relExpression();
		while(t.kind == Kind.OP_EQ || t.kind == Kind.OP_NEQ){
			if(t.kind == Kind.OP_EQ){
				op = t;
				match(Kind.OP_EQ);
			}else{
				op = t;
				match(Kind.OP_NEQ);
			}
			
			e1 = relExpression();
			e0 = new Expression_Binary(firstToken,e0,op,e1);
		}
		return e0;
	}
	
	Expression relExpression()throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		
		e0 = addExpression();
		while(t.kind == Kind.OP_LT || t.kind == OP_GT || t.kind == OP_LE || t.kind == OP_GE){
			if(t.kind == Kind.OP_LT){
				op = t;
				match(Kind.OP_LT);
			}
			else if(t.kind == OP_GT){
				op = t;
				match(Kind.OP_GT);
			}
			else if(t.kind == OP_LE){
				op = t;
				match(Kind.OP_LE);
			}
			else{
				op = t;
				match(Kind.OP_GE);
			}
			e1 = addExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
	
		e0 = multiExpression();
		while(t.kind == OP_PLUS || t.kind == OP_MINUS){
			if(t.kind == Kind.OP_PLUS){
				op = t;
				match(Kind.OP_PLUS);
			}
			else{
				op = t;
				match(Kind.OP_MINUS);
			}
			e1 = multiExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression multiExpression() throws SyntaxException{
		Token firstToken = t;
		Expression e0 = null;
		Token op = null;
		Expression e1 = null;
		
		e0 = unaryExpression();
		
		while(t.kind == OP_TIMES || t.kind == OP_DIV || t.kind == Kind.OP_MOD){
			if(t.kind == Kind.OP_TIMES){
				op=t;
				match(Kind.OP_TIMES);
			}
			else if(t.kind == OP_DIV){
				op=t;
				match(Kind.OP_DIV);
			}
			else{
				op=t;
				match(Kind.OP_MOD);
			}
			e1 = unaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression unaryExpression() throws SyntaxException{
		Expression_Unary exUnary = null;
		Token firstToken = t;
		Token op = null;
		Expression e = null;
		
		if(t.kind == Kind.OP_PLUS){
			op = t;
			match(Kind.OP_PLUS);
			e = unaryExpression();
			exUnary = new Expression_Unary(firstToken,op,e);
			return exUnary;
		}else if(t.kind == OP_MINUS){
			op = t;
			match(Kind.OP_MINUS);
			e = unaryExpression();
			exUnary = new Expression_Unary(firstToken,op,e);
			return exUnary;
		}else if(isUnaryExpressionNotPlusMinus()){
			e = unaryExpressionNotPlusMinus();
			return e;
		}
		else{
			throw new SyntaxException(t,"Syntax Error. On line "+t.line + " at position : "+ t.pos_in_line +" Epxected OP_PLUS or OP_MINUS or UnaryExpressionNoyPlusMinus");
		}
		/*exUnary = new Expression_Unary(firstToken, op, e);
		return exUnary;*/
	}
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException{
		
//		Expression_Unary exUnary = null;
		Token firstToken = t;
		Token op = null;
		Expression e = null;
		
		if(t.kind == OP_EXCL){
			op = t;
			match(Kind.OP_EXCL);
			e = unaryExpression();
			return new Expression_Unary(firstToken, op, e);
		}else if(isPrimary()){
			e = primary();
		}else if(isIdentOrPixelSelectorExpression()){
			e = identOrPixelSelectorExpression();
		}else if(t.kind == KW_x){
			e = new Expression_PredefinedName(firstToken,KW_x);
			match(KW_x);
		}
		else if(t.kind == KW_y){
			e = new Expression_PredefinedName(firstToken,KW_y);
			match(KW_y);
		}
		else if(t.kind == KW_r){
			e = new Expression_PredefinedName(firstToken,KW_r);
			match(KW_r);
		}
		else if(t.kind == KW_a){
			e = new Expression_PredefinedName(firstToken,KW_a);
			match(KW_a);
		}
		else if(t.kind == KW_X){
			e = new Expression_PredefinedName(firstToken,KW_X);
			match(KW_X);
		}else if(t.kind == KW_Y){
			e = new Expression_PredefinedName(firstToken,KW_Y);
			match(KW_Y);
		}
		else if(t.kind == KW_Z){
			e = new Expression_PredefinedName(firstToken,KW_Z);
			match(KW_Z);
		}
		else if(t.kind == KW_A){
			e = new Expression_PredefinedName(firstToken,KW_A);
			match(KW_A);
		}
		else if(t.kind == KW_R){
			e = new Expression_PredefinedName(firstToken,KW_R);
			match(KW_R);
		}
		else if(t.kind == KW_DEF_X){
			e = new Expression_PredefinedName(firstToken,KW_DEF_X);
			match(KW_DEF_X);
		}
		else if(t.kind == Kind.KW_DEF_Y){
			e = new Expression_PredefinedName(firstToken,KW_DEF_Y);
			match(KW_DEF_Y);
		}else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line );
		}
//		exUnary = new Expression_Unary(firstToken, op, e);
//		return exUnary;
		return e;
	}
	
	Expression identOrPixelSelectorExpression() throws SyntaxException{
		Expression e = null;
		Token firstToken = t;
		Token name = t;
		Index index = null;
		if(t.kind == IDENTIFIER){
			e = new Expression_Ident(firstToken,t);
			match(Kind.IDENTIFIER);
			
			if(t.kind == Kind.LSQUARE){
				match(Kind.LSQUARE);
				index = selector();
				match(Kind.RSQUARE);
				e = new Expression_PixelSelector(firstToken, name, index);
			}	
		}
		else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected IDENTIFIER but found: "+t.kind);
		}
		return e;
	}
	
	Expression primary ()throws SyntaxException{
		Token firstToken = t;
		Expression e = null;
		if(t.kind == INTEGER_LITERAL){
			int value = t.intVal();
			match(INTEGER_LITERAL);
			e = new Expression_IntLit(firstToken, value);
			
		}else if(t.kind == Kind.LPAREN){
			match(Kind.LPAREN);
			e = expression();
			match(Kind.RPAREN);
		}else if(isFunctionApp()){
			e = functionApplication();
		}
		else if(t.kind == Kind.BOOLEAN_LITERAL){
			String value = t.getText();
			boolean bool_val = false;
			if(value.equals("true")){
				bool_val = true;
			}
			match(Kind.BOOLEAN_LITERAL);
			e = new Expression_BooleanLit(firstToken, bool_val); 
		}else{
			throw new SyntaxException(t, "Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected INTEGER_LITERAL or LPAREN or BOOLEAN_LITERAL but found: "+t.kind);
		}
		
		return e;
	}
	
	Expression functionApplication()throws SyntaxException{
		Expression e = null;
		Token firstToken = t;
		Kind function;
		function = functionName();
		if(t.kind == LPAREN){
			match(Kind.LPAREN);
			Expression arg = expression();
			match(RPAREN);
			e = new Expression_FunctionAppWithExprArg(firstToken, function, arg);
		}else if(t.kind == LSQUARE){
			match(Kind.LSQUARE);
			Index arg = selector();
			match(Kind.RSQUARE);
			e = new Expression_FunctionAppWithIndexArg(firstToken, function, arg);
		}
		else{
			throw new SyntaxException(t,"Syntax error on line" +t.line + " at position :" + t.pos_in_line +". Expected LPAREN or LSQUARE but found: "+t.kind);
		}
		
		return e;
	}
	
	Kind functionName() throws SyntaxException{
		switch(t.kind){
		case KW_sin : match(Kind.KW_sin);
		return Kind.KW_sin; 
		case KW_cos :match(Kind.KW_cos);
		return Kind.KW_cos;  
		case KW_atan :match(Kind.KW_atan);
		return Kind.KW_atan;
		case KW_abs :match(Kind.KW_abs);
		return Kind.KW_abs;
		case KW_cart_x:match(Kind.KW_cart_x);
		return Kind.KW_cart_x;  
		case KW_cart_y:match(Kind.KW_cart_y);
		return Kind.KW_cart_y; 
		case KW_polar_a:match(Kind.KW_polar_a);
		return Kind.KW_polar_a;
		case KW_polar_r:match(Kind.KW_polar_r);
		return Kind.KW_polar_r;
		default:
			throw new SyntaxException(t, "Syntaxt error on Line :" +t.line+ " at position :" + t.pos_in_line + ". Found "+ t.kind+ ". Expected one of KW_sin, "
					+ "KW_cos, KW_atom, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r");		
		}
	}
	boolean isOrExpression(){
		return ((t.kind == OP_PLUS) ||(t.kind == OP_MINUS) || isUnaryExpressionNotPlusMinus());
	}
	
	boolean isUnaryExpressionNotPlusMinus(){
		return ((t.kind == Kind.OP_EXCL) || isPrimary() || isIdentOrPixelSelectorExpression() || (t.kind == Kind.KW_x)||
				(t.kind == KW_y) || (t.kind == KW_r) ||(t.kind == KW_a) ||(t.kind == KW_X)||(t.kind == KW_Y)||
				(t.kind == KW_Z) || (t.kind == KW_A)||(t.kind == KW_R)||(t.kind == Kind.KW_DEF_X) ||(t.kind == KW_DEF_Y));
	}
	
	boolean isPrimary(){
		return ((t.kind == INTEGER_LITERAL) || (t.kind == Kind.LPAREN) || isFunctionApp() || (t.kind ==Kind.BOOLEAN_LITERAL));
	}
	
	boolean isIdentOrPixelSelectorExpression(){
		return (t.kind == Kind.IDENTIFIER);
	}
	boolean isFunctionApp(){
		switch(t.kind){
		case KW_sin:case KW_cos: case KW_atan : case KW_abs: case KW_cart_x: case KW_cart_y: case KW_polar_a : case KW_polar_r: 
			return true;
		default : return false;
		}
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
