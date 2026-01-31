package wam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack; // Added explicit import

/**
 * Represents the entire state of the Warren Abstract Machine.
 * This includes all memory areas, registers, and state flags.
 */
public class WamMachine {

    // --- MEMORY AREAS ---
    public static final int MEM_SIZE = 10000;

    /** CODE: The list of instructions (the program). */
    public List<Instruction> code = new ArrayList<>();

    /** HEAP: Stores compound terms (structures). Grows upwards. */
    public WamCell[] heap = new WamCell[MEM_SIZE];

    /** STACK: Stores control information (environments and choice points). */
    public WamCell[] stack = new WamCell[MEM_SIZE];

    /** REGISTERS: Argument registers (A1, A2, ...) for passing parameters. */
    public WamCell[] regs = new WamCell[256]; // Let's support up to 255 registers.

    /** TRAIL: An "undo" list. Stores addresses of variables that need to be unbound on backtracking. */
    public int[] trail = new int[MEM_SIZE];


    // --- CPU REGISTERS (POINTERS and FLAGS) ---

    /** P: Program Counter. Index of the next instruction in 'code'. */
    public int P = 0;

    /** CP: Continuation Pointer. Return address for the current goal. */
    public int CP = 0;

    /** E: Environment Pointer. Points to the last environment on the stack. */
    public int E = -1;

    /** B: Backtrack Pointer. Points to the last choice point on the stack. */
    public int B = -1;

    /** TR: Trail Pointer. Top of the trail stack. */
    public int TR = 0;

    /** H: Heap Pointer. Top of the heap. */
    public int H = 0;

    /** S: Structure Pointer. Used for unifying structure arguments. */
    public int S = 0;

    /** modeRead: Flag indicating if unify instructions should match (read) or build (write). */
    public boolean modeRead = false;

    /** fail: Flag indicating that the current unification has failed, triggering backtracking. */
    public boolean fail = false;


    // --- PROGRAM METADATA ---

    /** Label Map: Resolves string labels (e.g., "parent/2") to integer indices in 'code'. */
    public Map<String, Integer> labels = new HashMap<>();

// --- CHOICE POINT MANAGEMENT ---

    /**
     * An inner class to represent a choice point saved on the choice stack.
     * It's a snapshot of the machine's registers at the moment of a choice.
     */
    private static class ChoicePoint {
        // Registers to restore on backtrack
        final int savedP;   // Program Counter (points to the next alternative clause)
        final int savedCP;  // Continuation Pointer
        final int savedE;   // Environment Pointer
        final int savedB;   // Previous Choice Point Pointer (forms a linked list)
        final int savedTR;  // Trail Pointer
        final int savedH;   // Heap Pointer
        final WamCell[] savedRegs; // A deep copy of the argument registers

        public ChoicePoint(int p, int cp, int e, int b, int tr, int h, WamCell[] regs) {
            this.savedP = p;
            this.savedCP = cp;
            this.savedE = e;
            this.savedB = b;
            this.savedTR = tr;
            this.savedH = h;

            // CRITICAL: We need a deep copy of the registers. A shallow copy (this.savedRegs = regs)
            // would mean that changes to the machine's registers would also change our saved copy.
            this.savedRegs = new WamCell[regs.length];
            for (int i = 0; i < regs.length; i++) {
                if (regs[i] != null) {
                    this.savedRegs[i] = new WamCell(regs[i].tag, regs[i].value, regs[i].pointer);
                }
            }
        }
    }

    /** The stack of choice points. This drives the backtracking mechanism. */
    private final Stack<ChoicePoint> choiceStack = new Stack<>();
    // --- MACHINE CONTROL ---

    public WamMachine() {
        // The constructor is simple; the fields are initialized with default values.
    }

    /**
     * Loads a compiled program into the machine's code area and resets the state.
     * @param newCode The list of instructions to load.
     * @param labelMap The map of labels to code addresses.
     */
    public void loadCode(List<Instruction> newCode, Map<String, Integer> labelMap) {
        this.code = newCode;
        this.labels = labelMap;
        reset();
    }

    /**
     * Resets all CPU registers and flags to their initial state before a run.
     */
    public void reset() {
        P = 0;
        CP = 0;
        E = -1;
        B = -1;
        TR = 0;
        H = 0;
        S = 0;
        fail = false;
        modeRead = false;
    }

    // --- RUNTIME HELPER METHODS ---

    /**
     * Retrieves a memory cell from the heap by its address.
     * In a full WAM, this would also check the stack.
     * @param address The integer index into the heap array.
     * @return The WamCell at that address.
     */
    private WamCell getHeapCell(int address) {
        if (address >= H || address < 0) {
            System.err.println("Error: Attempted to access invalid heap address " + address);
            fail = true;
            return null;
        }
        return heap[address];
    }

    /**
     * DEREFERENCE: Follows a chain of REF pointers to find the ultimate value.
     * @param address The starting memory address (usually from a register or another cell).
     * @return The address of the final, dereferenced cell.
     */
    public int deref(int address) {
        WamCell cell = getHeapCell(address);
        if (cell == null) return address; // Return original address on error

        // A REF cell pointing to itself is an unbound variable.
        // A REF cell pointing elsewhere is part of a reference chain.
        if (cell.tag == WamTag.REF && cell.pointer != address) {
            return deref(cell.pointer); // Recursively follow the chain.
        }

        // The cell is either a value (CON/STR) or an unbound variable.
        // In either case, we have found the end of the chain.
        return address;
    }

    /**
     * BIND: Binds a variable (a REF cell) to another value.
     * This is a side-effecting operation that modifies the heap.
     * It also records the binding on the trail for potential backtracking.
     * @param refAddr The address of the REF cell to bind.
     * @param valAddr The address of the value cell it should point to.
     */
    public void bind(int refAddr, int valAddr) {
        WamCell refCell = getHeapCell(refAddr);
        if (refCell == null) return;

        // Record the address of the variable we are about to modify.
        trail[TR] = refAddr;
        TR++; // Increment the trail pointer.

        // Perform the binding by updating the pointer.
        refCell.pointer = valAddr;
    }

    /**
     * UNWIND TRAIL: Resets variables that were bound after a certain point in time.
     * This is the core mechanism for undoing work during backtracking.
     * @param savedTR The trail pointer from a previous choice point.
     */
    public void unwindTrail(int savedTR) {
        // Loop backwards from the current trail top to the saved state.
        while (TR > savedTR) {
            TR--; // Decrement pointer first
            int addrToUnbind = trail[TR];
            WamCell variableCell = getHeapCell(addrToUnbind);

            // An unbound variable in WAM is a REF cell that points to itself.
            if (variableCell != null) {
                variableCell.pointer = addrToUnbind;
            }
        }
    }

    /**
     * UNIFY: The core unification algorithm of the WAM.
     */
    public void unify(int addr1, int addr2) {
        int d_addr1 = deref(addr1);
        int d_addr2 = deref(addr2);

        if (fail) return;

        WamCell c1 = getHeapCell(d_addr1);
        WamCell c2 = getHeapCell(d_addr2);

        if (d_addr1 == d_addr2) {
            return;
        }

        if (c1.tag == WamTag.REF) {
            bind(d_addr1, d_addr2);
            return;
        }
        if (c2.tag == WamTag.REF) {
            bind(d_addr2, d_addr1);
            return;
        }

        if (c1.tag == WamTag.CON && c2.tag == WamTag.CON) {
            if (c1.value.equals(c2.value)) {
                return;
            } else {
                fail = true;
                return;
            }
        }

        if (c1.tag == WamTag.STR && c2.tag == WamTag.STR) {
            if (c1.value.equals(c2.value)) {
                return;
            } else {
                fail = true;
                return;
            }
        }

        fail = true;
    }

    /**
     * BACKTRACK: The core backtracking mechanism.
     */
    public void backtrack() {
        if (choiceStack.isEmpty()) {
            this.fail = true;
            return;
        }

        ChoicePoint cp = choiceStack.peek();

        this.P = cp.savedP;
        this.CP = cp.savedCP;
        this.E = cp.savedE;
        this.B = cp.savedB;
        this.H = cp.savedH;

        System.arraycopy(cp.savedRegs, 0, this.regs, 0, cp.savedRegs.length);

        unwindTrail(cp.savedTR);
        this.fail = false;
    }

    /**
     * THE EXECUTION LOOP.
     */
    public void run() {
        boolean running = true;

        while (running && !fail) {
            if (P >= code.size()) break;

            Instruction instr = code.get(P);
            P++;

            // Debug print:
            // System.out.println("Executing " + (P-1) + ": " + instr + " | CP=" + CP);

            switch (instr.op) {
                // --- CONTROL FLOW ---
                case HALT:
                    running = false;
                    break;

                case CALL:
                    CP = P;
                    if (labels.containsKey(instr.label)) {
                        P = labels.get(instr.label);
                    } else {
                        System.err.println("Error: Unknown label " + instr.label);
                        fail = true;
                    }
                    break;

                case PROCEED:
                    if (CP == 0) running = false;
                    else P = CP;
                    break;

                case ALLOCATE:
                    // Create a new stack frame (environment).
                    E++;
                    // FIX: Save the current Continuation Pointer (CP) into the stack frame.
                    // We store it in a WamCell, using the pointer field to hold the integer CP.
                    stack[E] = new WamCell(WamTag.CON, "ENV_RET_ADDR", CP);
                    break;

                case DEALLOCATE:
                    // FIX: Restore the CP from the stack frame before destroying it.
                    if (E >= 0 && stack[E] != null) {
                        CP = stack[E].pointer;
                    } else {
                        // Should not happen in valid code
                        fail = true;
                    }
                    // Destroy the stack frame.
                    E--;
                    // Return to the caller (using the restored CP).
                    P = CP;
                    break;


                // --- DATA MOVEMENT (PUT) ---

                case PUT_CONSTANT:
                    regs[instr.reg] = WamCell.Cons(instr.name);
                    break;

                case PUT_VARIABLE:
                    heap[H] = WamCell.Ref(H);
                    regs[instr.reg] = WamCell.Ref(H);
                    H++;
                    break;

                case PUT_VALUE:
                    int sourceReg = Integer.parseInt(instr.name.substring(1));
                    regs[instr.reg] = regs[sourceReg];
                    break;


                // --- LOGIC MATCHING (GET) ---

                case GET_CONSTANT:
                    // Fixed: Use safe unification helper
                    unifyRegisterWithConstant(instr.reg, instr.name);
                    break;

                case GET_VARIABLE:
                    if (regs[instr.reg] == null) {
                        fail = true;
                    }
                    break;

                case GET_VALUE:
                    int srcReg = Integer.parseInt(instr.name.substring(1));
                    unify(regs[instr.reg].pointer, regs[srcReg].pointer);
                    break;


                // --- BACKTRACKING ---

                case TRY_ME_ELSE:
                    int elseLabel = labels.get(instr.label);
                    choiceStack.push(new ChoicePoint(elseLabel, CP, E, B, TR, H, regs));
                    break;

                case RETRY_ME_ELSE:
                    int nextLabel = labels.get(instr.label);
                    ChoicePoint currentCP = choiceStack.pop();
                    choiceStack.push(new ChoicePoint(nextLabel, currentCP.savedCP, currentCP.savedE, currentCP.savedB, currentCP.savedTR, currentCP.savedH, currentCP.savedRegs));
                    break;

                case TRUST_ME:
                    if (!choiceStack.isEmpty()) {
                        choiceStack.pop();
                    }
                    break;
            }

            if (fail) {
                backtrack();
            }
        }
    }

    private void unifyRegisterWithConstant(int regIndex, String constName) {
        WamCell cell = regs[regIndex];

        // Follow the reference chain
        while (cell.tag == WamTag.REF && cell.pointer != -1) {
            if (cell.pointer >= 0 && cell.pointer < H) {
                WamCell target = heap[cell.pointer];
                if (target == cell) break; // It points to itself (unbound)
                cell = target;
            } else {
                break;
            }
        }

        if (cell.tag == WamTag.REF) {
            // Bind unbound variable
            heap[H] = WamCell.Cons(constName);
            bind(cell.pointer, H);
            H++;
        } else if (cell.tag == WamTag.CON) {
            if (!cell.value.equals(constName)) fail = true;
        } else {
            fail = true;
        }
    }

    public boolean hasChoices() {
        return !choiceStack.isEmpty();
    }
}