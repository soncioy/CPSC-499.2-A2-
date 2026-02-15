package ca.ucalgary.cpsc49902;

import java.io.File;

public class InvocationFile {
    public final String expression;
    public final String filename;
    public final int line;
    public final int column;

    public InvocationFile(String expression, String filePath, int line, int column) {
        this.expression = expression;
        this.line = line;
        this.column = column;

        if (filePath != null) {
            this.filename = new File(filePath).getName();
        } else {
            this.filename = "unknown";
        }
    }

    @Override
    public String toString() {
        return String.format("%s: file %s, line %d, column %d",
                expression, filename, line, column);
    }
}