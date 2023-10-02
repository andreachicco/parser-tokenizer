package progetto2023_47.parser.ast;

import progetto2023_47.visitors.Visitor;

public class VectLiteral extends BinaryOp {
	public VectLiteral(Exp exp1, Exp exp2) {
		super(exp1, exp2);
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visitVectLiteral(left,right);
	}
}
