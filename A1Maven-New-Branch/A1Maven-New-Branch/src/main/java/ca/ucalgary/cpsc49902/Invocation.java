package ca.ucalgary.cpsc49902;

public class Invocation {
    public final String expression;
    public final String filename;
    public final int line;
    public final int column;

    public Invocation(String expression, String filename, int line, int column) {
        this.expression = expression;
        this.filename = filename;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        // Format: <expression>: file <id>, line <#>, column <#>
        return String.format("%s: file %s, line %d, column %d",
                expression, filename, line, column);
    }
}