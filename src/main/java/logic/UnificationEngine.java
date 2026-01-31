package logic;

import ast.Atom;
import ast.Structure;
import ast.Term;
import ast.Variable;
import java.util.Map;

public class UnificationEngine {

    /**
     * Follows a chain of variable bindings to find the ultimate value of a term.
     * This is a critical helper for the main unification algorithm.
     * <p>
     * Example: Given bindings {X=Y, Y=tom}, deref(X) will return tom.
     * If a variable is unbound, it dereferences to itself.
     * If a term is not a variable, it dereferences to itself.
     *
     * @param t         The term to dereference.
     * @param bindings  The current map of variable bindings.
     * @return The fully dereferenced term.
     */
    private Term deref(Term t, Map<String, Term> bindings) {
        // Case 1: The term is not a variable (e.g., an Atom or Structure).
        // It cannot be looked up in the bindings, so it is its own final value.
        if (!(t instanceof Variable)) {
            return t;
        }

        // The term is a variable, so we might need to look it up.
        String varName = ((Variable) t).getName();

        // Case 2: The variable has a binding in the map.
        if (bindings.containsKey(varName)) {
            // IMPORTANT: The binding itself might be another variable in a chain.
            // So, we recursively call deref on the value we just looked up.
            return deref(bindings.get(varName), bindings);
        } else {
            // Case 3: The variable is not in the map. It's an unbound ("free") variable.
            // It dereferences to itself.
            return t;
        }
    }

    /**
     * Attempts to unify two terms given a set of existing bindings. If unification
     * is possible, it returns true and modifies the bindings map with any new
     * variable assignments. If it's impossible, it returns false and the bindings
     * map is left in a potentially partially-modified but consistent state.
     *
     * @param t1        The first term.
     * @param t2        The second term.
     * @param bindings  The map of current variable bindings (will be modified).
     * @return          {@code true} if the terms unify, {@code false} otherwise.
     */
    public boolean unify(Term t1, Term t2, Map<String, Term> bindings) {
        // Step 0: Always dereference first to work with the "real" values.
        Term term1 = deref(t1, bindings);
        Term term2 = deref(t2, bindings);

        // Case 1: The terms are identical.
        // This handles cases like unify(john, john) or unify(X, X) where X is unbound.
        if (term1.equals(term2)) {
            return true;
        }

        // Case 2: One of the terms is an unbound variable. Bind it.
        // This is the core "assignment" step of unification.
        if (term1 instanceof Variable) {
            bindings.put(((Variable) term1).getName(), term2);
            return true;
        }
        if (term2 instanceof Variable) {
            bindings.put(((Variable) term2).getName(), term1);
            return true;
        }

        // Case 3: Both terms are Atoms. Unification succeeds if their names are the same.
        if (term1 instanceof Atom atom1 && term2 instanceof Atom atom2) {
            return atom1.getName().equals(atom2.getName());
        }

        // Case 4: Both terms are Structures. This is the recursive step.
        if (term1 instanceof Structure s1 && term2 instanceof Structure s2) {
            // 4a: Functors must match.
            if (!s1.getFunctor().equals(s2.getFunctor())) {
                return false;
            }
            // 4b: Arity (number of arguments) must match.
            if (s1.getArity() != s2.getArity()) {
                return false;
            }

            // 4c: All corresponding arguments must unify recursively.
            for (int i = 0; i < s1.getArity(); i++) {
                boolean argsUnify = unify(s1.getArgs().get(i), s2.getArgs().get(i), bindings);
                // If any pair of arguments fails to unify, the entire structure unification fails.
                if (!argsUnify) {
                    return false;
                }
            }
            // If all arguments unified successfully, the structures unify.
            return true;
        }

        // Case 5: Mismatch. If we've reached this point, the terms are of
        // incompatible types (e.g., an Atom and a Structure). They cannot unify.
        return false;
    }
}