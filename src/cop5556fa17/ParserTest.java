package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}

	@Test
	public void testStmt1() throws LexicalException, SyntaxException {
		String input = "stmt iOut -> SCREEN ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "stmt"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Statement_Out dec = (Statement_Out) ast.decsAndStatements
				.get(0);  
		assertEquals("iOut", dec.name);
		assertEquals(Sink_SCREEN.class,dec.sink.getClass());
	}
	

	@Test
	public void testStmt3() throws LexicalException, SyntaxException {
		String input = "stmt lhsId [[x,y]] = a+b ;";
		//String input = "stmt d = a+b ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
//		Statement_Assign ob = new Statement_Assign(;)
		assertEquals(ast.name, "stmt"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Statement_Assign dec = (Statement_Assign) ast.decsAndStatements
				.get(0);  
		assertEquals("lhsId", dec.lhs.name);
		assertEquals(Expression_PredefinedName.class, ((Expression_Binary)(dec.e)).e0.getClass());
		//assertEquals(Expression_PredefinedName.class, ((Expression_Binary)(dec.e)).e0.getClass());
		assertEquals(Expression_PredefinedName.class, dec.lhs.index.e0.getClass());
		//assertEquals(Sink_SCREEN.class,dec.sink.getClass());
		//assertNull(dec.e);
	}
	
	@Test
	public void testStmt7() throws LexicalException, SyntaxException {
		String input = "dcl int k = sin(b+a);";
		//String input = "stmt d = a+b ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
	}
	
	@Test
	public void testStmt6() throws LexicalException, SyntaxException {
		String input = "dcl file homework = @ b;";
		//String input = "stmt d = a+b ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
	}
	
	@Test
	public void testStmt5() throws LexicalException, SyntaxException {
		String input = "dcl int k ! + pixId ? A : DEF_X;";
		//String input = "stmt d = a+b ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		//Program ast = parser.parse();
		//show(ast);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}

	}
	
	@Test
	public void testStmt2() throws LexicalException, SyntaxException {
		String input = "stmt iOut ->  ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}	
	
	
	@Test
	public void exp1() throws SyntaxException, LexicalException {
	String input = "Z-old";
	Expression e = (new Parser(new Scanner(input).scan())).expression();
	show(e);
	assertEquals(Expression_Binary.class, e.getClass());
	Expression_Binary ebin = (Expression_Binary)e;
	assertEquals(Expression_PredefinedName.class, ebin.e0.getClass());
	assertEquals(KW_Z, ((Expression_PredefinedName)(ebin.e0)).kind);
	assertEquals(Expression_Ident.class, ebin.e1.getClass());
	assertEquals("old", ((Expression_Ident)(ebin.e1)).name);
	assertEquals(OP_MINUS, ebin.op);
	}
}
