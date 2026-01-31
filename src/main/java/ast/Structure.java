package ast;
import java.util.List;
import java.util.stream.Collectors;

public class Structure extends Term {
    private String functor;
    private List<Term> args;

    public Structure(String functor, List<Term> args) {
        this.functor = functor;
        this.args = args;
    }

    public String getFunctor() { return functor; }
    public List<Term> getArgs() { return args; }
    public int getArity() { return args.size(); }

    @Override
    public String toString() {
        if (args.isEmpty()) return functor;
        String argsStr = args.stream().map(Term::toString).collect(Collectors.joining(", "));
        return functor + "(" + argsStr + ")";
    }
}