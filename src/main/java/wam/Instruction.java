package wam;

/**
 * Represents a single WAM instruction, including its opcode and arguments.
 */
public class Instruction {
    public Opcode op;

    // Not all arguments are used by every instruction.

    /** Argument 1: Register number (e.g., A1, X1). */
    public int reg;

    /** Argument 2: A name, typically a functor "f/n" or a constant "c". */
    public String name;

    /** Argument 3: A code label for jumps (e.g., the address of a procedure). */
    public String label;

    public Instruction(Opcode op, int reg, String name, String label) {
        this.op = op;
        this.reg = reg;
        this.name = name;
        this.label = label;
    }

    // --- Static Factory Methods for Convenience ---
    // These make the compiler's job in Phase 4 much cleaner.

    public static Instruction PutStructure(String name, int reg) {
        return new Instruction(Opcode.PUT_STRUCTURE, reg, name, null);
    }

    public static Instruction PutVariable(int reg) {
        return new Instruction(Opcode.PUT_VARIABLE, reg, null, null);
    }

    public static Instruction PutConstant(String name, int reg) {
        return new Instruction(Opcode.PUT_CONSTANT, reg, name, null);
    }

    public static Instruction PutValue(int sourceReg, int destReg) {
        // We can reuse the 'name' field to store the source register as a string for printing.
        return new Instruction(Opcode.PUT_VALUE, destReg, "A"+sourceReg, null);
    }

    public static Instruction GetConstant(String name, int reg) {
        return new Instruction(Opcode.GET_CONSTANT, reg, name, null);
    }

    public static Instruction GetValue(int sourceReg, int destReg) {
        return new Instruction(Opcode.GET_VALUE, destReg, "A"+sourceReg, null);
    }

    public static Instruction UnifyVariable(int reg) {
        return new Instruction(Opcode.UNIFY_VARIABLE, reg, null, null);
    }

    public static Instruction GetValue(int reg) {
        return new Instruction(Opcode.GET_VALUE, reg, null, null);
    }

    public static Instruction Call(String label) {
        return new Instruction(Opcode.CALL, 0, null, label);
    }

    public static Instruction Proceed() {
        return new Instruction(Opcode.PROCEED, 0, null, null);
    }

    public static Instruction Allocate() {
        return new Instruction(Opcode.ALLOCATE, 0, null, null);
    }

    public static Instruction Deallocate() {
        return new Instruction(Opcode.DEALLOCATE, 0, null, null);
    }

    public static Instruction Halt() {
        return new Instruction(Opcode.HALT, 0, null, null);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s", op)); // Left-align opcode in 16 chars

        if (op == Opcode.CALL || op == Opcode.TRY_ME_ELSE || op == Opcode.RETRY_ME_ELSE || op == Opcode.TRUST_ME) {
            sb.append(label);
        } else {
            if (reg > 0) {
                sb.append(String.format("A%-2d", reg)); // Argument register
            }
            if (name != null) {
                sb.append(reg > 0 ? ", " : "");
                sb.append(name);
            }
        }
        return sb.toString().trim();
    }
}