package progetto2023_47.parser.ast;

import static java.util.Objects.requireNonNull;

import progetto2023_47.visitors.Visitor;

public class ForeachStmt implements Stmt {
	private final Variable iterator;
	private final Exp exp;
	private final Block block; 

	public ForeachStmt(Variable iterator, Exp Exp, Block Block) {
		this.iterator = requireNonNull(iterator);
		this.exp = requireNonNull(Exp);
		this.block = requireNonNull(Block);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + iterator + "," + exp + "," + block + ")";
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitForeachStmt(iterator, exp, block);
	}
}

