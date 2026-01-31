package wam;

/**
 * Defines the set of all possible instructions for our Warren Abstract Machine.
 * These are the fundamental operations the machine can perform.
 */
public enum Opcode {
    // --- Argument Register Instructions ---

    // 'Put' instructions are for building terms (e.g., in a query or rule body).
    // They place data INTO argument registers.
    PUT_STRUCTURE,  // put_structure f/n, Ai
    PUT_VARIABLE,   // put_variable Yn, Ai
    PUT_VALUE,      // For moving register values.
    PUT_CONSTANT,   // For putting a literal constant.

    // 'Get' instructions are for matching terms (e.g., in the head of a clause).
    // They match data FROM argument registers against the clause's head.
    GET_STRUCTURE,  // get_structure f/n, Ai
    GET_VARIABLE,   // get_variable Yn, Ai
    GET_VALUE,      // For matching register values.
    GET_CONSTANT,   // For matching a literal constant.

    // --- Heap Unification Instructions ---

    // 'Unify' instructions are for arguments inside structures.
    // They operate in either "read mode" (matching) or "write mode" (building).
    UNIFY_VARIABLE, // unify_variable Yn
    UNIFY_VALUE,    // unify_value Yn

    // --- Control Flow Instructions ---
    CALL,           // call p/n
    PROCEED,        // proceed (return from a successful query)
    ALLOCATE,       // allocate (create a new stack frame for a rule)
    DEALLOCATE,     // deallocate (destroy the stack frame and return)
    HALT,           // Stop the machine execution

    // --- Indexing and Backtracking Instructions ---
    TRY_ME_ELSE,    // try_me_else L  (Create a choice point, try current clause)
    RETRY_ME_ELSE,  // retry_me_else L (Backtrack to choice point, try next clause)
    TRUST_ME        // trust_me (Commit to the last clause, remove choice point)
}