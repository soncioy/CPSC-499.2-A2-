package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.AnalysisToo.InvocationRecord;
import ca.ucalgary.cpsc49902.javacc.*;
import java.util.List;

public class MethodVisitor extends Java12ParserDefaultVisitor {
    private final String fileName;
    private final List<InvocationRecord> storage;

    public MethodVisitor(String fileName, List<InvocationRecord> storage) {
        this.fileName = fileName;
        this.storage = storage;
    }

    private String clean(String s) {
        if (s == null) return "";
        // Remove comments
        s = s.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "");
        // Remove trailing semicolon
        if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
        // Remove class bodies { ... }
        int braceIndex = s.indexOf("{");
        if (braceIndex != -1) s = s.substring(0, braceIndex);
        // Remove whitespace
        return s.replaceAll("\\s", "");
    }

    private String getFullText(Token first, Token last) {
        if (first == null || last == null) return "";
        StringBuilder sb = new StringBuilder();
        Token t = first;
        while (t != null) {
            sb.append(t.image);
            if (t == last) break;
            t = t.next;
        }
        return sb.toString();
    }

    @Override
    public Object visit(ASTArguments node, Object data) {
        SimpleNode parent = (SimpleNode) node.jjtGetParent();

        // Climb up to find the PrimaryExpression root (e.g., "System.out.println")
        while (parent != null && !parent.toString().contains("PrimaryExpression")) {
            parent = (SimpleNode) parent.jjtGetParent();
        }

        if (parent != null) {
            // Safety: Skip arguments inside 'new' calls (handled by AllocationExpression)
            Node grandParent = parent.jjtGetParent();
            if (grandParent instanceof ASTAllocationExpression || grandParent instanceof ASTCreator) {
                return super.visit(node, data);
            }
            // Skip if the expression starts with 'new'
            if (parent.jjtGetFirstToken().image.equals("new")) {
                return super.visit(node, data);
            }

            Token first = parent.jjtGetFirstToken();
            Token last = node.jjtGetLastToken();

            String expression = getFullText(first, last);
            if (expression.contains("(")) {
                storage.add(new InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));
                return null; // Stop recursion
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken(); // Arguments ')' token

        String expression = getFullText(first, last);
        storage.add(new InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));
        return null;
    }

    @Override
    public Object visit(ASTExplicitConstructorInvocation node, Object data) {
        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();
        storage.add(new InvocationRecord(clean(getFullText(first, last)), fileName, first.beginLine, first.beginColumn));
        return null;
    }
}