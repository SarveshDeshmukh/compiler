package cop5556fa17;

import java.util.ArrayList;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
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
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		cw.visitField(ACC_STATIC,"x","I",null,0).visitEnd();
		cw.visitField(ACC_STATIC,"y","I",null,0).visitEnd();
		cw.visitField(ACC_STATIC, "X", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "Y", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "r", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "a", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "R", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "A", "I", null, new Integer(0)).visitEnd();
		cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256)).visitEnd();
		cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256)).visitEnd();
		cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215)).visitEnd();

		
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		
		Expression e = declaration_Variable.e;
		String name = declaration_Variable.name;	
		String fieldName;
		String fieldType = "";
		Object initValue = null;
		FieldVisitor fv;
		fieldName = name;
		Type type = declaration_Variable.decType;
		if(type == Type.INTEGER){
			fieldType = "I";
			initValue = new Integer(0);
		}else if(type == Type.BOOLEAN){
			fieldType = "Z";
			initValue = new Boolean(false);
		}
		//initValue = "";
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, initValue);
		fv.visitEnd();

		if(e != null){
			e.visit(this, null);			
			mv.visitFieldInsn(PUTSTATIC,className,fieldName,fieldType);
		} 	
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Kind op = expression_Binary.op;
		
		if(e0 != null){
			e0.visit(this, null);
		}
		//CodeGenUtils.genPrint(DEVEL, mv,"*******************");
		//CodeGenUtils.genPrintTOS(DEVEL,mv,expression_Binary.type);
		if(e1 != null){
			e1.visit(this, null);
		}
		//CodeGenUtils.genPrintTOS(DEVEL,mv,expression_Binary.type);
		Label L1 = new Label();
		if(op == Kind.OP_OR){
			if(e0.type == Type.BOOLEAN || e1.type == Type.INTEGER){
				mv.visitInsn(IOR);
			}
		}else if(op == Kind.OP_AND){
			if(e0.type == Type.BOOLEAN || e1.type == Type.INTEGER){
				mv.visitInsn(IAND);
			}	
		}else if(op == Kind.OP_EQ){
			if(e0.type == Type.INTEGER || e1.type == Type.BOOLEAN){
				mv.visitJumpInsn(IF_ICMPEQ, L1);
				mv.visitLdcInsn(false);
			}else{
				mv.visitJumpInsn(IF_ACMPEQ, L1);
				mv.visitLdcInsn(false);
			}
		}else if(op == Kind.OP_NEQ){
			if(e0.type == Type.INTEGER || e1.type == Type.BOOLEAN){
				mv.visitJumpInsn(IF_ICMPNE, L1);
				mv.visitLdcInsn(false);
			}else{
				mv.visitJumpInsn(IF_ACMPNE, L1);
				mv.visitLdcInsn(false);
			}
		}else if(op == Kind.OP_LT){
			mv.visitJumpInsn(IF_ICMPLT, L1);
			mv.visitLdcInsn(false);
		}else if(op == Kind.OP_GT){
			mv.visitJumpInsn(IF_ICMPGT, L1);
			mv.visitLdcInsn(false);
		}else if(op == Kind.OP_LE){
			mv.visitJumpInsn(IF_ICMPLE, L1);
			mv.visitLdcInsn(false);
		}else if(op == Kind.OP_GE){
			mv.visitJumpInsn(IF_ICMPGE, L1);
			mv.visitLdcInsn(false);
		}else if(op == Kind.OP_PLUS){
			if(e0.type == Type.INTEGER &&  e1.type == Type.INTEGER){
				mv.visitInsn(IADD);
			} 	
		}else if(op == Kind.OP_MINUS){
			if(e0.type == Type.INTEGER &&  e1.type == Type.INTEGER){
				mv.visitInsn(ISUB);
			}
		}else if(op == Kind.OP_TIMES){
			if(e0.type == Type.INTEGER &&  e1.type == Type.INTEGER){
				mv.visitInsn(IMUL);
			}
		}else if(op == Kind.OP_DIV){
			if(e0.type == Type.INTEGER &&  e1.type == Type.INTEGER){
				mv.visitInsn(IDIV);
			}
		}else if(op == Kind.OP_MOD){
			if(e0.type == Type.INTEGER &&  e1.type == Type.INTEGER){
				mv.visitInsn(IREM);
			}
		}
		Label finished = new Label();
		mv.visitJumpInsn(GOTO, finished);
		mv.visitLabel(L1);
		mv.visitLdcInsn(true);
		mv.visitLabel(finished);
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		Expression e = expression_Unary.e;
		Kind op = expression_Unary.op;
		Type type = expression_Unary.type;
		e.visit(this, arg);
		
		if(op == Kind.OP_PLUS) {
			
		} else if(op == Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		} else if(op == Kind.OP_EXCL) {
			if(type == Type.INTEGER) {
				int val = INTEGER.MAX_VALUE;
				mv.visitLdcInsn(new Integer(val));
				mv.visitInsn(IXOR);
			} else if(type == Type.BOOLEAN) {
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
			}	
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, type);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		Expression e0 = index.e0;
		Expression e1 = index.e1;
		
		e0.visit(this, null);
		e1.visit(this, null);
		
		if(!index.isCartesian){
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		Index index = expression_PixelSelector.index;
		String name = expression_PixelSelector.name;
		mv.visitFieldInsn(GETSTATIC,className, name,ImageSupport.ImageDesc);
		index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig,false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		Expression conditional = expression_Conditional.condition;
		Expression expressionFalse = expression_Conditional.falseExpression;
		Expression expressionTrue = expression_Conditional.trueExpression;
		Label trueLabel = new Label();
		
		if(conditional != null){
			conditional.visit(this, null);
		}
		Label falseLabel = new Label();
		mv.visitJumpInsn(IFEQ, falseLabel);
		
		Label finished = new Label();
		expressionTrue.visit(this, null);
		mv.visitJumpInsn(GOTO, finished);
		mv.visitLabel(falseLabel);
		expressionFalse.visit(this, null);
		mv.visitLabel(finished);
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.type);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		
		String name = declaration_Image.name;
		Source source = declaration_Image.source;
		Expression xSize = declaration_Image.xSize;
		Expression ySize = declaration_Image.ySize;
		
	
		cw.visitField(ACC_STATIC, name, ImageSupport.ImageDesc, null, null).visitEnd();
		
		if( source!=null )
		{
			source.visit(this, null);
			
			if(xSize==null && ySize==null)
			{
				mv.visitInsn(ACONST_NULL);
				
				mv.visitInsn(ACONST_NULL);
				
			}
			else
			{
				xSize.visit(this, null);
				
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				
				ySize.visit(this, null);
				
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
			
			mv.visitFieldInsn(PUTSTATIC, className, name, ImageSupport.ImageDesc);
			
		}
		else
		{
			if(declaration_Image.xSize==null && declaration_Image.ySize==null)
			{
				mv.visitLdcInsn(256);
				mv.visitLdcInsn(256);	
			}
			else
			{
				xSize.visit(this, null);
				ySize.visit(this, null);
			} 
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage",ImageSupport.makeImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		}	
		return null;

	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		String fileOrUrl = source_StringLiteral.fileOrUrl;
		mv.visitLdcInsn(fileOrUrl);
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO
		Expression paramNum = source_CommandLineParam.paramNum;
		mv.visitVarInsn(ALOAD, 0);
		paramNum.visit(this, null);
		mv.visitInsn(AALOAD);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		String name = source_Ident.name;
		mv.visitFieldInsn(GETSTATIC,className,name,ImageSupport.StringDesc);
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		String name = declaration_SourceSink.name;
		Source source = declaration_SourceSink.source;
		
		cw.visitField(ACC_STATIC, name, ImageSupport.StringDesc, null, null).visitEnd();
		if(source!=null){
			source.visit(this, null);
		}	
		mv.visitFieldInsn(PUTSTATIC, className, name, ImageSupport.StringDesc);
		
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(expression_IntLit.value);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		Expression eArg = expression_FunctionAppWithExprArg.arg;
		Kind function = expression_FunctionAppWithExprArg.function;
		if(eArg != null){
			eArg.visit(this, null);
		}
		if(function == Kind.KW_abs){
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		if(function == Kind.KW_log){
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}		
	return null;
		
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		Index arg1 = expression_FunctionAppWithIndexArg.arg;
		Expression e0 = arg1.e0;
		Expression e1 = arg1.e1;
		
		Kind function = expression_FunctionAppWithIndexArg.function;
		if(e0 != null){
			e0.visit(this, null);
		}
		if(e1 != null){
			e1.visit(this, null);
		}
		if(function == Kind.KW_polar_a)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		if(function == Kind.KW_polar_r)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		if(function == Kind.KW_cart_x)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		if(function == Kind.KW_cart_y)	
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		Kind kind = expression_PredefinedName.kind;
		
		if( kind == Kind.KW_DEF_X ) {
			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
		}
		else if(kind == Kind.KW_DEF_Y){
			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
		}
		else if(kind == Kind.KW_y){
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		}
		else if(kind == Kind.KW_Z){
			mv.visitLdcInsn(0xFFFFFF);
		}
		else if(kind == Kind.KW_x){
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		}
						
		else if (kind == Kind.KW_Y) {
			//mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			//mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
		}
		
		
		else if (kind == Kind.KW_r) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		
		else if(kind == Kind.KW_X) {
			//mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			//mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
		}
		
		else if (kind == Kind.KW_a) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		else if(kind == Kind.KW_A) {
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			//mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		
		else if (kind == Kind.KW_R) {
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			/*mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);*/
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		return null;

	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Type type = statement_Out.getDec().decType;
		String name = statement_Out.name;
		Sink sink = statement_Out.sink;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		if(type == Type.INTEGER){
			mv.visitFieldInsn(GETSTATIC, className, name, "I");
			// Need to print here since the value of boolean is not available putstatic is done
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}
		else if(type == Type.BOOLEAN){			
			mv.visitFieldInsn(GETSTATIC, className, name, "Z");
			// Need to print here since the value of boolean is not available putstatic is done
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		else if (type == Type.IMAGE)
		{
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			sink.visit(this, arg);
		}	
		//throw new UnsupportedOperationException();	
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		
		String name = statement_In.name;
		Source source = statement_In.source;
		
		String fieldType = "";
		if(source != null){
			source.visit(this,null);
		}
		Type type = statement_In.getDec().decType;
		if(type == Type.INTEGER){
			fieldType = "I";
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC,className,name,fieldType);
		}else if (type == Type.BOOLEAN){
			fieldType = "Z";
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC,className,name,fieldType);
		}else if (type == Type.IMAGE)
		{
			Declaration_Image dec = (Declaration_Image) statement_In.getDec();
			Expression xSize = dec.xSize;
			Expression ySize = dec.ySize;
			String name1 = dec.name;
			if(xSize == null && ySize == null){
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
			}
			else {
				xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;",false);
				}
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
				mv.visitFieldInsn(PUTSTATIC, className, name1, ImageSupport.ImageDesc);
		}
		
		
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		
		//TODO  (see comment)
		Expression e = statement_Assign.e;
		LHS lhs = statement_Assign.lhs;
		String name = lhs.name;
		Label label1 = new Label();
		
		if(lhs.type == Type.IMAGE)
		{	
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			
			
			
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			mv.visitLabel(label1);
			mv.visitFieldInsn(PUTSTATIC, className,"y", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			
			
			Label label4 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, label4);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			
			Label label2 = new Label();
			mv.visitLabel(label2);
			mv.visitFieldInsn(PUTSTATIC, className,"x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			
			
			Label label3 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, label3);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className,"x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, label2);
			
			mv.visitLabel(label3);
			mv.visitFieldInsn(GETSTATIC, className,"y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, label1);
			
			
			mv.visitLabel(label4);
			
		}else{
			if(e != null){
				e.visit(this, null);
			}
			if(lhs != null){
				lhs.visit(this, null);
			}
			if(lhs.type == e.type){
				//statement_Assign.setCartesian(lhs.isCartesian);
				
			}else{
				throw new SemanticException(statement_Assign.firstToken,"Type mismatch: LHS.type != e.type");
			}
		}
					
		return null;
		//throw new UnsupportedOperationException();
	}
		
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		Type type = lhs.type;
		Index index = lhs.index;
		String name = lhs.name;
		
		if(type == Type.INTEGER){
			mv.visitFieldInsn(PUTSTATIC, className, name, "I");
		}
		else if(type == Type.BOOLEAN){
			mv.visitFieldInsn(PUTSTATIC, className, name, "Z");
		}
		else if(type == Type.IMAGE){
			mv.visitFieldInsn(GETSTATIC, className, name, ImageSupport.ImageDesc);
			
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		//throw new UnsupportedOperationException();
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		String name = sink_Ident.name;
		mv.visitFieldInsn(GETSTATIC,className,name,ImageSupport.StringDesc );
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		mv.visitLdcInsn(new Boolean(expression_BooleanLit.value));
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO

		Type type = expression_Ident.type;
		if(type == Type.INTEGER){
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		}else if(type == Type.BOOLEAN){
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		}

		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.type);
		return null;
	}

}