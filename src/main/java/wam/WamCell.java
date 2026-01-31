package wam;

/**
 * Represents a single "word" or "cell" in the WAM's memory.
 * Each cell has a tag indicating its type and a value/pointer.
 */
public class WamCell {
    public WamTag tag;

    // The 'value' field is used for CONstants (e.g., "tom")
    // and for STRucture functors (e.g., "parent/2").
    public String value;

    // The 'pointer' field is used for REFerences and STRuctures.
    // It holds the integer index (the "address") of another cell.
    public int pointer;

    public WamCell(WamTag tag, String value, int pointer) {
        this.tag = tag;
        this.value = value;
        this.pointer = pointer;
    }

    // --- Factory Methods for convenience ---

    /** Factory method to create a CONstant cell. */
    public static WamCell Cons(String name) {
        // A constant has no pointer, so we can set it to 0.
        return new WamCell(WamTag.CON, name, 0);
    }

    /** Factory method to create a STRucture cell. */
    public static WamCell Str(int heapAddress) {
        // The functor name is not known at this stage, it will be set by
        // a PUT_STRUCTURE instruction. The pointer points to the first argument.
        return new WamCell(WamTag.STR, null, heapAddress);
    }

    /** Factory method to create a REFerence cell (a variable). */
    public static WamCell Ref(int address) {
        // A reference has no literal value, only a pointer to another cell.
        return new WamCell(WamTag.REF, null, address);
    }

    @Override
    public String toString() {
        switch (tag) {
            case CON:
                return "CON(" + value + ")";
            case STR:
                // Note: a real debugger would also show the functor, but for now this is fine.
                return "STR(-> " + pointer + ")";
            case REF:
                return "REF(-> " + pointer + ")";
            default:
                return "UNKNOWN";
        }
    }
}