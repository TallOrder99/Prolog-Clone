package wam;

/**
 * Defines the type of data stored in a WamCell.
 * This is the core of the WAM's tagged architecture.
 */
public enum WamTag {
    /** A reference (pointer) to another cell. Represents a WAM variable. */
    REF,

    /** A structure marker. The cell's value is the functor/arity, and its pointer points to the first argument on the heap. */
    STR,

    /** A constant value (an atom). The cell's value holds the atom's name. */
    CON

    // In a more complete WAM, you might add:
    // LIS for lists, INT for integers, etc.
}