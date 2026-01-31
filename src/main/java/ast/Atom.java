package ast;
public class Atom extends Term {
    private String name;
    public Atom(String name) { this.name = name; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}