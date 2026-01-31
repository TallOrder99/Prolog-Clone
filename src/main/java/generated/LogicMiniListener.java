package generated;// Generated from C:/Users/25192/Desktop/LogicCompiler/src/main/antlr4/LogicMini.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LogicMiniParser}.
 */
public interface LogicMiniListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LogicMiniParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(LogicMiniParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicMiniParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(LogicMiniParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FactRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 */
	void enterFactRule(LogicMiniParser.FactRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FactRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 */
	void exitFactRule(LogicMiniParser.FactRuleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RuleRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 */
	void enterRuleRule(LogicMiniParser.RuleRuleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RuleRule}
	 * labeled alternative in {@link LogicMiniParser#clause}.
	 * @param ctx the parse tree
	 */
	void exitRuleRule(LogicMiniParser.RuleRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicMiniParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(LogicMiniParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicMiniParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(LogicMiniParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicMiniParser#term_list}.
	 * @param ctx the parse tree
	 */
	void enterTerm_list(LogicMiniParser.Term_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicMiniParser#term_list}.
	 * @param ctx the parse tree
	 */
	void exitTerm_list(LogicMiniParser.Term_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StructureTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void enterStructureTerm(LogicMiniParser.StructureTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StructureTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void exitStructureTerm(LogicMiniParser.StructureTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AtomTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void enterAtomTerm(LogicMiniParser.AtomTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AtomTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void exitAtomTerm(LogicMiniParser.AtomTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VariableTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void enterVariableTerm(LogicMiniParser.VariableTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VariableTerm}
	 * labeled alternative in {@link LogicMiniParser#term}.
	 * @param ctx the parse tree
	 */
	void exitVariableTerm(LogicMiniParser.VariableTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link LogicMiniParser#structure}.
	 * @param ctx the parse tree
	 */
	void enterStructure(LogicMiniParser.StructureContext ctx);
	/**
	 * Exit a parse tree produced by {@link LogicMiniParser#structure}.
	 * @param ctx the parse tree
	 */
	void exitStructure(LogicMiniParser.StructureContext ctx);
}