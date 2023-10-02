package progetto2023_47.parser.ast;

import progetto2023_47.visitors.Visitor;

public interface AST {
	<T> T accept(Visitor<T> visitor);
}
