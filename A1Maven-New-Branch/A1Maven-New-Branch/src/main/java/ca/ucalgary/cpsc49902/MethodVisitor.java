package ca.ucalgary.cpsc49902;

//import ca.ucalgary.cpsc49902.AnalysisTool.InvocationRecord;
import ca.ucalgary.cpsc49902.javacc.*;
import java.util.List;

public class MethodVisitor extends Java12ParserDefaultVisitor {
    private final String fileName;
    private final List<AnalysisTool.InvocationRecord> storage;

    public MethodVisitor(String fileName, List<AnalysisTool.InvocationRecord> storage) {
        this.fileName = fileName;
        this.storage = storage;
    }

    private String clean(String s) {
        return s.replaceAll("\\s", "");
    }

    /**
     * Reconstructs the full source text (including comments) between two tokens.
     */
    private String getFullText(Token first, Token last) {
        if (first == null || last == null) return "";
        StringBuilder sb = new StringBuilder();
        Token t = first;

        while (t != null) {
            // 1. Append special tokens (comments/whitespace) appearing before this token
            if (t.specialToken != null) {
                Token tmp = t.specialToken;
                // Walk backwards to the first special token
                while (tmp.specialToken != null) tmp = tmp.specialToken;
                // Walk forwards appending them
                while (tmp != null) {
                    sb.append(tmp.image);
                    tmp = tmp.next;
                }
            }

            // 2. Append the visible token itself
            sb.append(t.image);

            if (t == last) break;
            t = t.next;
        }
        return sb.toString();
    }

    // =========================================================================
    // VISITOR LOGIC
    // =========================================================================

    @Override
    public Object visit(ASTArguments node, Object data) {
        // Parent node (e.g., MethodInvocation or AllocationExpression) usually holds the full context
        SimpleNode parent = (SimpleNode) node.jjtGetParent();

        // If parent is null (rare), fall back to the node itself
        SimpleNode targetNode = (parent != null) ? parent : node;

        Token first = targetNode.jjtGetFirstToken();
        Token last = node.jjtGetLastToken(); // Arguments end at the closing parenthesis

        String expression = getFullText(first, last);
        storage.add(new AnalysisTool.InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();

        String expression = getFullText(first, last);
        storage.add(new AnalysisTool.InvocationRecord(clean(expression), fileName, first.beginLine, first.beginColumn));

        return super.visit(node, data);
    }
}