package generated;// Generated from C:/Users/25192/Desktop/LogicCompiler/src/main/antlr4/LogicMini.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LogicMiniParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LogicMiniVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LogicMiniParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(LogicMiniParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FactRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactRule(LogicMiniParser.FactRuleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code RuleRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleRule(LogicMiniParser.RuleRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicMiniParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(LogicMiniParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicMiniParser#term_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_list(LogicMiniParser.Term_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StructureTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructureTerm(LogicMiniParser.StructureTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AtomTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomTerm(LogicMiniParser.AtomTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VariableTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableTerm(LogicMiniParser.VariableTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link LogicMiniParser#structure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructure(LogicMiniParser.StructureContext ctx);
}