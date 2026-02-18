package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.AnalysisTool.InvocationRecord;
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
        s = s.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "");
        if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
        int braceIndex = s.indexOf("{");
        if (braceIndex != -1) s = s.substring(0, braceIndex);
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

        // Strategy: Climb up to find the root PrimaryExpression
        SimpleNode primaryExpr = parent;
        while (primaryExpr != null && !(primaryExpr instanceof ASTPrimaryExpression)) {
            primaryExpr = (SimpleNode) primaryExpr.jjtGetParent();
        }

        if (primaryExpr != null) {
            Token firstTok = primaryExpr.jjtGetFirstToken();

            // NULL CHECK: Crucial to stop crashing on complex identifiers
            if (firstTok == null) return super.visit(node, data);

            // Skip standalone 'new' calls handled by AllocationExpression
            if ("new".equals(firstTok.image) && primaryExpr.jjtGetNumChildren() <= 2) {
                return super.visit(node, data);
            }

            // Grab the full text from the start of the expression to the end of these arguments
            String expression = getFullText(firstTok, node.jjtGetLastToken());

            if (expression.contains("(")) {
                storage.add(new InvocationRecord(clean(expression), fileName, firstTok.beginLine, firstTok.beginColumn));
                return super.visit(node, data); // Recurse for nested calls
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        // Prevent double counting if this allocation is part of a qualified call
        // e.g., 'outer.new Inner()'
        if (node.jjtGetParent() instanceof ASTPrimarySuffix) {
            return super.visit(node, data);
        }

        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();
        if (first != null && last != null) {
            String expression = getFullText(first, last);
            storage.add(new InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));
        }
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