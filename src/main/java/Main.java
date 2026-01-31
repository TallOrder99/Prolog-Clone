import ast.Program;
import compiler.ASTGenerator;
import compiler.ASTGenerator; // CHECK: Ensure this matches your file name (AstBuilder.java)
import wam.Compiler;
import wam.CompilerResult;
import wam.WamMachine;
import generated.LogicMiniLexer;
import generated.LogicMiniParser;
import optimizer.ClauseOptimizer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Mini-Prolog Compiler [Phase 6 Final] ===");
        System.out.println("STEP 1: Load Knowledge Base.");
        System.out.println("   Type your facts/rules. Type 'done.' on a new line to finish.");

        Scanner scanner = new Scanner(System.in);
        StringBuilder kbBuffer = new StringBuilder();

        // --- 1. READ KNOWLEDGE BASE ---
        while (true) {
            System.out.print("KB> ");
            String line = scanner.nextLine().trim();
            if (line.equals("done.")) break;
            if (!line.isEmpty()) {
                kbBuffer.append(line).append("\n");
            }
        }

        String knowledgeBase = kbBuffer.toString();
        System.out.println("Knowledge Base Loaded.\n");

        // --- 2. QUERY LOOP ---
        System.out.println("STEP 2: Query Mode.");
        System.out.println("   Type queries (e.g., 'animal(X).'). Type 'exit.' to quit.");

        WamMachine vm = new WamMachine();

        while (true) {
            System.out.print("?- ");
            String queryLine = scanner.nextLine().trim();

            if (queryLine.equals("exit.")) break;
            if (queryLine.isEmpty()) continue;

            // Normalize input (ensure it starts with '?-')
            if (!queryLine.startsWith("?-")) queryLine = "?- " + queryLine;

            try {
                // Combine the static KB with the dynamic Query
                String fullSource = knowledgeBase + "\n" + queryLine;

                // Run the full pipeline
                runPipeline(fullSource, vm, scanner, queryLine);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                // e.printStackTrace(); // Uncomment for debugging
            }
        }

        System.out.println("Bye!");
    }

    private static void runPipeline(String source, WamMachine vm, Scanner scanner, String queryLine) {
        // A. PARSE
        LogicMiniLexer lexer = new LogicMiniLexer(CharStreams.fromString(source));
        LogicMiniParser parser = new LogicMiniParser(new CommonTokenStream(lexer));

        // CHECK: Verify if your class is named 'AstBuilder' or 'ASTGenerator'
        ASTGenerator astBuilder = new ASTGenerator();
        Program prog = (Program) astBuilder.visit(parser.program());

        // B. OPTIMIZE
        ClauseOptimizer opt = new ClauseOptimizer();
        Program optimized = opt.optimize(prog);

        // C. COMPILE
        Compiler compiler = new Compiler();
        CompilerResult res = compiler.compile(optimized);

        // D. EXECUTE
        vm.loadCode(res.code(), res.labels());
        vm.run();

        // E. PRINT RESULTS & HANDLE BACKTRACKING
        processResults(vm, scanner, queryLine);
    }

    private static void processResults(WamMachine vm, Scanner scanner, String queryLine) {
        // Detect if this is a Ground Query (Yes/No) or a Variable Query (Find X)
        // Heuristic: If the query line has NO uppercase letters, it's Yes/No.
        boolean isGroundQuery = !queryLine.matches(".*[A-Z].*");

        while (true) {
            if (vm.fail) {
                System.out.println("false.");
                return;
            } else {
                // --- SUCCESS CASE ---

                if (isGroundQuery) {
                    // Case 1: Yes/No Question (e.g. sibling(luke, leia))
                    System.out.println("true.");
                } else {
                    // Case 2: Search Question (e.g. sibling(luke, X))
                    String val = null;

                    // Check Register 1 (A1) for the answer
                    if (vm.regs[1] != null && vm.regs[1].tag != null) {
                        int addr = vm.deref(vm.regs[1].pointer);

                        // Safety Check: Ensure address is within heap bounds
                        if (addr >= 0 && addr < vm.heap.length && vm.heap[addr] != null) {
                            // Only print if it's not an unbound variable pointing to itself
                            if (vm.heap[addr].tag != wam.WamTag.REF || vm.heap[addr].pointer != addr) {
                                val = vm.heap[addr].value;
                            }
                        }
                    }

                    if (val != null) {
                        System.out.println("X = " + val);
                    } else {
                        // Fallback: If we couldn't find a value in Reg 1, just say true.
                        System.out.println("true.");
                    }
                }

                // --- BACKTRACKING CHECK ---
                if (vm.hasChoices()) {
                    System.out.print("   Next solution? (y/n) > ");
                    String input = scanner.nextLine().trim();
                    if (input.equalsIgnoreCase("y")) {
                        vm.fail = true; // Trigger failure
                        vm.backtrack(); // Restore state
                        vm.run();       // Resume
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }
}