package progetto2023_47.visitors.execution;

import java.io.PrintWriter;

import progetto2023_47.environments.EnvironmentException;
import progetto2023_47.environments.GenEnvironment;
import progetto2023_47.parser.ast.Block;
import progetto2023_47.parser.ast.Exp;
import progetto2023_47.parser.ast.Stmt;
import progetto2023_47.parser.ast.StmtSeq;
import progetto2023_47.parser.ast.Variable;
import progetto2023_47.visitors.Visitor;

import static java.util.Objects.requireNonNull;

public class Execute implements Visitor<Value> {

	private final GenEnvironment<Value> env = new GenEnvironment<>();
	private final PrintWriter printWriter; // output stream used to print values

	public Execute() {
		printWriter = new PrintWriter(System.out, true);
	}

	public Execute(PrintWriter printWriter) {
		this.printWriter = requireNonNull(printWriter);
	}

	// useful to check if two vectors have the same length
	private void checkVectSameDim(VectValue v1, VectValue v2){
		if (v1.getDim() != v2.getDim())
			throw new InterpreterException("Vectors must have the same dimension");
	}

	private boolean isIntValue(Value v){ return v instanceof IntValue;}

	private boolean isVectValue(Value v){ return v instanceof VectValue;}

	// dynamic semantics for programs; no value returned by the visitor

	@Override
	public Value visitMyLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
			// possible runtime errors
			// EnvironmentException: undefined variable
		} catch (EnvironmentException e) {
			throw new InterpreterException(e);
		}
		return null;
	}

	// dynamic semantics for statements; no value returned by the visitor

	@Override
	public Value visitAssignStmt(Variable var, Exp exp) {
	    env.update(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitPrintStmt(Exp exp) {
		printWriter.println(exp.accept(this));
		return null;
	}

	@Override
	public Value visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
	    if(exp.accept(this).toBool())
			thenBlock.accept(this);
		else if(elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Value visitForeachStmt(Variable iterator, Exp exp, Block block){
		VectValue vector = exp.accept(this).toVect();
		env.enterScope();
		env.dec(iterator, new IntValue(0));
		for (int el : vector.getVector()) {
			env.update(iterator, new IntValue(el));
			block.accept(this);
		}
		env.exitScope();
		return null;
	}

	@Override
	public Value visitBlock(StmtSeq stmtSeq) {
		env.enterScope();
		stmtSeq.accept(this);
		env.exitScope();
		return null;
	}

	// dynamic semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Value visitEmptyStmtSeq() {
	    return null;
	}

	@Override
	public Value visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
	    first.accept(this);
		rest.accept(this);
		return null;
	}

	// dynamic semantics of expressions; a value is returned by the visitor

	@Override
	public Value visitAdd(Exp left, Exp right) {
		Value v1 = left.accept(this);
		Value v2 = right.accept(this);
		if(isIntValue(v1) && isIntValue(v2))
			return new IntValue(v1.toInt() + v2.toInt());
		else if(isVectValue(v1) && isVectValue(v2)){
			VectValue vec1 = v1.toVect();
			VectValue vec2 = v2.toVect();
			checkVectSameDim(vec1, vec2);
			int length = vec1.getDim();
			int[] result = new int[length];
			for(int i = 0; i < length; i++){
				result[i] = vec1.getIndex(i) + vec2.getIndex(i);
			}
			return new VectValue(result);
		}
		else
			throw new InterpreterException("Expecting INT or VECT");
	}

	@Override
	public IntValue visitIntLiteral(int value) {
	    return new IntValue(value);
	}

    @Override
	public Value visitMul(Exp left, Exp right) {
	    Value v1 = left.accept(this);
		Value v2 = right.accept(this);
		if(isIntValue(v1) && isIntValue(v2))
			return new IntValue(v1.toInt() * v2.toInt());
		else if(isVectValue(v1) && isVectValue(v2)){
			VectValue vec1 = v1.toVect();
			VectValue vec2 = v2.toVect();
			checkVectSameDim(vec1, vec2);
			int scalar_prod = 0;
			for(int i = 0; i < vec1.getDim(); i++){
				scalar_prod += (vec1.getIndex(i) * vec2.getIndex(i));
			}
			return new IntValue(scalar_prod);
		}
		else if(isIntValue(v1) && isVectValue(v2))
			return new VectValue(v2.toVect().scaleFactor(v1.toInt()));
		else if(isVectValue(v1) && isIntValue(v2))
			return new VectValue(v1.toVect().scaleFactor(v2.toInt()));
		else
			throw new InterpreterException("Expecting INT or VECT");
	}
    
	@Override
	public IntValue visitSign(Exp exp) {
	    return new IntValue(-exp.accept(this).toInt());
	}

	@Override
	public Value visitVariable(Variable var) {
	    return env.lookup(var);
	}

	@Override
	public BoolValue visitNot(Exp exp) {
		return new BoolValue(!exp.accept(this).toBool());
	}

	@Override
	public BoolValue visitAnd(Exp left, Exp right) {
	    return new BoolValue(left.accept(this).toBool() && right.accept(this).toBool());
	}

	@Override
	public BoolValue visitBoolLiteral(boolean value) {
	    return new BoolValue(value);
	}

	@Override
	public BoolValue visitEq(Exp left, Exp right) {
	    return new BoolValue(left.accept(this).equals(right.accept(this)));
	}

	@Override
	public PairValue visitPairLit(Exp left, Exp right) {
	    return new PairValue(left.accept(this), right.accept(this));
	}

	@Override
	public Value visitFst(Exp exp) {
	    return exp.accept(this).toPair().getFstVal();
	}

	@Override
	public Value visitSnd(Exp exp) {
	    return exp.accept(this).toPair().getSndVal();
	}

	@Override
	public VectValue visitVectLiteral(Exp exp1, Exp exp2) {
	    int ind = exp1.accept(this).toInt();
		int dim = exp2.accept(this).toInt(); 
		return new VectValue(ind, dim);
	}

}

