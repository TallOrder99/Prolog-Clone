package ast;
import java.util.ArrayList;
import java.util.List;

public class Clause {
    private Structure head;
    private List<Term> body; // Empty if it's a Fact

    public Clause(Structure head, List<Term> body) {
        this.head = head;
        this.body = body;
    }

    public Structure getHead() { return head; }
    public List<Term> getBody() { return body; }
    public boolean isFact() { return body.isEmpty(); }

    @Override
    public String toString() {
        String h = head.toString();
        if (isFact()) return h + ".";
        // Convert body terms to string
        return h + " :- " + body.toString() + ".";
    }
}