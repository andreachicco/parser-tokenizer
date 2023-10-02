package progetto2023_47.parser;

import progetto2023_47.parser.ast.Prog;

public interface Parser extends AutoCloseable {

	Prog parseProg() throws ParserException;

}