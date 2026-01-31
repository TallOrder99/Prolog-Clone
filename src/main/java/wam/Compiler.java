package wam;

import ast.*;

import java.util.*;

public class Compiler {

    // --- The Final Output of the Compilation ---

    /** The generated list of WAM instructions. */
    private final List<Instruction> code = new ArrayList<>();

    /** A map from a label string (e.g., "parent/2") to its address (index) in the code list. */
    private final Map<String, Integer> labels = new HashMap<>();


    // --- The Internal State during Compilation ---

    /**
     * The compiler's "scratchpad" for the current clause.
     * Maps a variable name like "X" to the register number it has been assigned.
     * This is crucial for generating correct get_value/put_value instructions.
     */
    private final Map<String, Integer> variableMap = new HashMap<>();

    /** Tracks the next available register for a new variable within a clause. */
    private int regCounter = 0;


    /**
     * The main entry point for the compiler.
     * @param program The AST of the program to be compiled.
     * @return A CompilerResult containing the generated code and labels.
     */
    public CompilerResult compile(Program program) {
        // Reset state for a fresh compilation.
        code.clear();
        labels.clear();

        // Step 1: Compile the query. This becomes the entry point of our WAM program.
        if (program.getQuery() != null && !program.getQuery().isEmpty()) {
            compileQuery(program.getQuery());
            // We need a way to stop the machine after the query is done.
            // We will add a HALT opcode for this.
        }

        // Step 2: Group all clauses by their predicate signature (e.g., "parent/2").
        Map<String, List<Clause>> groupedClauses = groupClauses(program.getClauses());

        // Step 3: Compile each group of clauses into a single predicate procedure.
        for (Map.Entry<String, List<Clause>> entry : groupedClauses.entrySet()) {
            compilePredicate(entry.getKey(), entry.getValue());
        }

        return new CompilerResult(code, labels);
    }

    /** Helper method to add an instruction to our code list. */
    private void emit(Instruction instruction) {
        code.add(instruction);
    }

    /**
     * Groups a flat list of clauses into a map where keys are predicate signatures
     * (e.g., "parent/2") and values are the lists of clauses for that predicate.
     * Using LinkedHashMap preserves the original order of predicates.
     */
    private Map<String, List<Clause>> groupClauses(List<Clause> clauses) {
        Map<String, List<Clause>> map = new LinkedHashMap<>();
        for (Clause c : clauses) {
            String key = c.getHead().getFunctor() + "/" + c.getHead().getArity();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }
        return map;
    }
    /**
     * Compiles the main query of the program.
     * This generates the initial sequence of instructions that the WAM will execute.
     * @param goals The list of terms in the query (e.g., [grandparent(john, A)]).
     */
    private void compileQuery(List<Term> goals) {
        // A query is like the 'main' function. We reset the variable-to-register map.
        variableMap.clear();
        regCounter = 1; // WAM registers A1, A2, ... start from 1.

        // For now, we'll assume a simple query with one goal.
        // A full implementation would loop through all goals.
        Term goal = goals.get(0);

        if (goal instanceof Structure) {
            Structure s = (Structure) goal;

            // --- Step 1: Put arguments into registers ---
            // For a goal like p(t1, t2, ..., tn), we must place t1 into A1, t2 into A2, etc.
            for (int i = 0; i < s.getArity(); i++) {
                Term arg = s.getArgs().get(i);
                int reg = i + 1; // A1, A2, ...

                // Delegate the complex work of compiling a single term to a helper.
                // The 'true' flag indicates we are in "PUT mode" (building a term).
                compileTerm(arg, reg, true);
            }

            // --- Step 2: Call the predicate ---
            // After setting up the arguments in the registers, call the corresponding procedure.
            String predicateLabel = s.getFunctor() + "/" + s.getArity();
            emit(Instruction.Call(predicateLabel));

            // --- Step 3: Halt after the query is done ---
            emit(Instruction.Halt());

        } else {
            // Handle simple atom queries like `?- p.` if necessary.
            // For this project, we can assume queries are always structures.
        }
    }

    /**
     * Compiles a single Term AST node into one or more WAM instructions.
     * This is a central helper method used by both query and clause compilation.
     *
     * @param t The Term to compile.
     * @param reg The target/source register number (e.g., 1 for A1).
     * @param isPutMode True for PUT instructions (building), false for GET instructions (matching).
     */
    private void compileTerm(Term t, int reg, boolean isPutMode) {
        if (t instanceof Atom atom) {
            // Case 1: Term is a constant atom like 'john'.
            Opcode op = isPutMode ? Opcode.PUT_CONSTANT : Opcode.GET_CONSTANT;
            emit(new Instruction(op, reg, atom.getName(), null));

        } else if (t instanceof Variable variable) {
            // Case 2: Term is a variable like 'X'. This is the most complex case.
            String varName = variable.getName();

            if (variableMap.containsKey(varName)) {
                // --- Subcase 2a: This variable has been seen before. ---
                // We need to reference its existing location.
                int sourceReg = variableMap.get(varName);
                Opcode op = isPutMode ? Opcode.PUT_VALUE : Opcode.GET_VALUE;
                // We use the 'name' field in the instruction to show the source register, for clarity.
                emit(new Instruction(op, reg, "A" + sourceReg, null));
            } else {
                // --- Subcase 2b: This is the first time we see this variable. ---
                // We need to create it and record its location.
                variableMap.put(varName, reg); // Record that 'X' now lives in register 'reg'.
                Opcode op = isPutMode ? Opcode.PUT_VARIABLE : Opcode.GET_VARIABLE;
                emit(new Instruction(op, reg, varName, null));
            }

        } else if (t instanceof Structure structure) {
            // Case 3: Term is a structure like 'p(Y)'.
            // This is a simplified compilation. A full WAM would handle nested
            // structures using the S register and a unify_* instruction sequence.
            String functor = structure.getFunctor() + "/" + structure.getArity();
            Opcode op = isPutMode ? Opcode.PUT_STRUCTURE : Opcode.GET_STRUCTURE;
            emit(new Instruction(op, reg, functor, null));

            // For this project, we assume a full compiler would then emit a series of
            // unify_* instructions for the arguments. We will handle this more
            // explicitly and simply in the compileClause step.
        }
    }

    /**
     * Compiles a group of clauses that belong to the same predicate.
     * This method is responsible for generating the "choice" or "indexing"
     * instructions (try/retry/trust) that link the clauses together.
     *
     * @param predicateSignature The signature of the predicate, e.g., "parent/2".
     * @param clauses            The list of clauses for this predicate.
     */
    private void compilePredicate(String predicateSignature, List<Clause> clauses) {
        // --- Step 1: Set the label for this predicate ---
        // The address of the first instruction for this predicate is the current size of the code list.
        labels.put(predicateSignature, code.size());

        if (clauses.size() == 1) {
            // --- Case 1: Only one clause for this predicate ---
            // No choice points are needed. Just compile the clause directly.
            compileClause(clauses.get(0));

        } else {
            // --- Case 2: Multiple clauses, requiring backtracking logic ---
            for (int i = 0; i < clauses.size(); i++) {
                Clause clause = clauses.get(i);
                // Generate a unique label for the *next* clause.
                String nextClauseLabel = predicateSignature + "_clause" + (i + 1);

                // --- Step 2: Generate the correct indexing instruction ---
                if (i == 0) {
                    // First clause: `try_me_else`. This creates the choice point.
                    // It says "Try me; if I fail, jump to `nextClauseLabel`".
                    emit(new Instruction(Opcode.TRY_ME_ELSE, 0, null, nextClauseLabel));
                } else if (i < clauses.size() - 1) {
                    // Intermediate clauses: `retry_me_else`. This updates the choice point.
                    // It says "Backtrack here, try me; if I fail, jump to `nextClauseLabel`".
                    emit(new Instruction(Opcode.RETRY_ME_ELSE, 0, null, nextClauseLabel));
                } else {
                    // Last clause: `trust_me`. This commits to the last choice.
                    // It says "Backtrack here, try me; if I fail, the whole predicate fails.
                    // If I succeed, throw away the choice point."
                    emit(new Instruction(Opcode.TRUST_ME, 0, null, null));
                }

                // --- Step 3: Compile the clause itself ---
                compileClause(clause);

                // --- Step 4: Set the label for the next choice ---
                // The `_else` part of the previous instruction needs to know where to jump.
                // We set the label we generated earlier to point to the current code address.
                if (i < clauses.size() - 1) {
                    labels.put(nextClauseLabel, code.size());
                }
            }
        }
    }

    /**
     * Compiles a single clause, which consists of a head and an optional body.
     * This involves generating 'get' instructions for the head and 'put'/'call'
     * instructions for the body goals.
     *
     * @param c The Clause AST node to compile.
     */
    private void compileClause(Clause c) {
        variableMap.clear();
        regCounter = 1;

        Structure head = c.getHead();
        List<Term> body = c.getBody();

        // --- Step 1: Manage the Stack Frame ---
        if (!body.isEmpty()) {
            emit(Instruction.Allocate());

            // *** THE FIX: SAVE ARGUMENTS TO SAFE REGISTERS ***
            // Rules overwrite A1, A2... when calling sub-goals.
            // We must copy incoming arguments (A1..An) to safe registers (R10..Rn)
            // so they survive the body calls.
            int safeRegStart = 10;
            for (int i = 0; i < head.getArity(); i++) {
                Term arg = head.getArgs().get(i);
                int incomingReg = i + 1; // A1, A2...
                int safeReg = safeRegStart + i; // R10, R11...

                if (arg instanceof Variable) {
                    String name = ((Variable) arg).getName();
                    if (!variableMap.containsKey(name)) {
                        // Map the variable 'X' to Safe Register 10
                        variableMap.put(name, safeReg);

                        // Emit instruction: Copy A1 -> R10
                        // We use PutValue for register-to-register copy.
                        emit(Instruction.PutValue(incomingReg, safeReg));
                    }
                }
            }
        }

        // --- Step 2: Compile the Head ---
        // For Rules: Since we manually handled variables above, this loop primarily
        // handles Constants and Unification checks in the head.
        for (int i = 0; i < head.getArity(); i++) {
            Term arg = head.getArgs().get(i);
            int reg = i + 1;

            // If it's a variable, we already handled it in "The Fix" block above.
            // We only need to compile if it's a Constant (to generate GET_CONSTANT).
            if (!(arg instanceof Variable)) {
                compileTerm(arg, reg, false);
            }
        }

        // --- Step 3: Compile the Body ---
        if (body.isEmpty()) {
            emit(Instruction.Proceed());
        } else {
            for (int i = 0; i < body.size(); i++) {
                Term goal = body.get(i);
                if (goal instanceof Structure s) {
                    // 3a: Put goal arguments
                    for (int j = 0; j < s.getArity(); j++) {
                        Term arg = s.getArgs().get(j);
                        int reg = j + 1;
                        compileTerm(arg, reg, true);
                    }
                    // 3b: Call
                    String predicateLabel = s.getFunctor() + "/" + s.getArity();
                    emit(Instruction.Call(predicateLabel));
                }
            }
            emit(Instruction.Deallocate());
        }
    }
}