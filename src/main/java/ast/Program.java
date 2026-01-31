package ast;
import java.util.List;

public class Program {
    private List<Clause> clauses;
    private List<Term> query; // Queries are list of goals

    public Program(List<Clause> clauses, List<Term> query) {
        this.clauses = clauses;
        this.query = query;
    }

    public List<Clause> getClauses() { return clauses; }
    public List<Term> getQuery() { return query; }
}