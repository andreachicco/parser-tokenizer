package progetto2023_47.visitors.typechecking;

import static progetto2023_47.visitors.typechecking.AtomicType.*;

import progetto2023_47.environments.EnvironmentException;
import progetto2023_47.environments.GenEnvironment;
import progetto2023_47.parser.ast.Block;
import progetto2023_47.parser.ast.Exp;
import progetto2023_47.parser.ast.Stmt;
import progetto2023_47.parser.ast.StmtSeq;
import progetto2023_47.parser.ast.Variable;
import progetto2023_47.visitors.Visitor;

public class Typecheck implements Visitor<Type> {

	private final GenEnvironment<Type> env = new GenEnvironment<>();

    // useful to typecheck binary operations where operands must have the same type 
	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// useful to typecheck if a type is INT or VECT
	private Type checkIntOrVect(Type t) {
		if(t == INT || t == VECT)
			return t;
		throw new TypecheckerException(t.toString(), INT.toString() + " or " + VECT.toString());
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitMyLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undeclared variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for statements; no value returned by the visitor

	@Override
	public Type visitAssignStmt(Variable var, Exp exp) {
	    var.accept(this).checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitPrintStmt(Exp exp) {
		exp.accept(this);
		return null;
	}

	@Override
	public Type visitVarStmt(Variable var, Exp exp) {
	    env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
	    BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if(elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Type visitForeachStmt(Variable iterator, Exp exp, Block block){
		VECT.checkEqual(exp.accept(this));
		env.enterScope();
		env.dec(iterator, INT);
		block.accept(this);
		env.exitScope();
		return null;
	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterScope();
	    stmtSeq.accept(this);
		env.exitScope();
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitEmptyStmtSeq() {
	    return null;
	}

	@Override
	public Type visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
	    first.accept(this);
	    rest.accept(this);
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public Type visitAdd(Exp left, Exp right) {
		Type t1 = checkIntOrVect(left.accept(this));
		t1.checkEqual(right.accept(this));
		return t1;
	}

	@Override
	public AtomicType visitIntLiteral(int value) {
		return INT;
	}

	@Override
	public Type visitMul(Exp left, Exp right) {
	    Type t1 = checkIntOrVect(left.accept(this));
		Type t2 = checkIntOrVect(right.accept(this));
		return t1 != t2 ? VECT : INT;
	}

	@Override
	public AtomicType visitSign(Exp exp) {
	    INT.checkEqual(exp.accept(this));
		return INT;
	}

	@Override
	public Type visitVariable(Variable var) {
	    return env.lookup(var);
	}

	@Override
	public AtomicType visitNot(Exp exp) {
		BOOL.checkEqual(exp.accept(this));
		return BOOL;
	}

	@Override
	public AtomicType visitAnd(Exp left, Exp right) {
	    checkBinOp(left, right, BOOL);
		return BOOL;
	}

	@Override
	public AtomicType visitBoolLiteral(boolean value) {
	    return BOOL;
	}

	@Override
	public AtomicType visitEq(Exp left, Exp right) {
	    left.accept(this).checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public PairType visitPairLit(Exp left, Exp right) {
	    return new PairType(left.accept(this), right.accept(this));
	}

	@Override
	public Type visitFst(Exp exp) {
	    return exp.accept(this).getFstPairType();
	}

	@Override
	public Type visitSnd(Exp exp) {
	    return exp.accept(this).getSndPairType();
	}

	@Override
	public AtomicType visitVectLiteral(Exp exp1, Exp exp2) {
	    INT.checkEqual(exp1.accept(this));
		INT.checkEqual(exp2.accept(this));
		return VECT;
	}
}
