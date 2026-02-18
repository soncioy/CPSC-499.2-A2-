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

        while (parent != null && !parent.toString().contains("PrimaryExpression")) {
            parent = (SimpleNode) parent.jjtGetParent();
        }

        if (parent != null) {
            // Safety: Skip 'new' expressions
            Node grandParent = parent.jjtGetParent();
            if (grandParent instanceof ASTAllocationExpression || grandParent instanceof ASTCreator) {
                return super.visit(node, data);
            }

            // NULL CHECK FIX: This prevents SYNTAX_ERROR on ValidAssertIdentifier
            Token firstTok = parent.jjtGetFirstToken();
            if (firstTok != null && "new".equals(firstTok.image)) {
                return super.visit(node, data);
            }

            Token first = parent.jjtGetFirstToken();
            Token last = node.jjtGetLastToken();
            String expression = getFullText(first, last);

            if (expression.contains("(")) {
                storage.add(new InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));
                // Do NOT return null; continue to find nested calls
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();
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