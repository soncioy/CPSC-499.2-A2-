package ca.ucalgary.cpsc49902;

import java.util.List;
import ca.ucalgary.cpsc49902.javacc.*;

public class MethodVisitor implements Java12ParserVisitor {
    private final String fileName;
    private final List<InvocationFile> storage;

    public MethodVisitor(String fileName, List<InvocationFile> storage) {
        this.fileName = fileName;
        this.storage = storage;
    }

    private String clean(String s) {
        return s.replaceAll("\\s", "");
    }

    private String getLeadingSpecials(Token t) {
        if (t.specialToken == null) return "";

        // 1. The specialToken field points to the *last* special token.
        //    We must walk backwards to find the *first* one.
        Token tmp = t.specialToken;
        while (tmp.specialToken != null) {
            tmp = tmp.specialToken;
        }

        // 2. Now walk forwards using .next until we hit the visible token
        StringBuilder sb = new StringBuilder();
        while (tmp != null && tmp != t) {
            sb.append(tmp.image);
            tmp = tmp.next;
        }
        return sb.toString();
    }

    private String getFullText(Token first, Token last) {
        if (first == null || last == null) return "";
        StringBuilder sb = new StringBuilder();
        Token t = first;

        // Iterate through the visible tokens
        while (t != null) {
            // 1. Before appending the visible token, append its special tokens (comments)
            sb.append(getLeadingSpecials(t));

            // 2. Append the visible token
            sb.append(t.image);

            // 3. Stop if we've reached the last token
            if (t == last) break;

            t = t.next;
        }
        return sb.toString();
    }

    @Override
    public Object visit(ASTArguments node, Object data) {
        SimpleNode parent = (SimpleNode) node.jjtGetParent();
        Token first = parent.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();

        String expression = getFullText(first, last);

        storage.add(new InvocationFile(clean(expression), fileName, first.beginLine, first.beginColumn));
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        Token first = node.jjtGetFirstToken();
        Token last = node.jjtGetLastToken();
        String expression = getFullText(first, last);
        storage.add(new InvocationFile(clean(expression), fileName, first.beginLine, first.beginColumn));
        return node.childrenAccept(this, data);
    }

    // --- MANDATORY INTERFACE METHODS (BOILERPLATE) ---
    // All these are required for the class to be valid and "resolvable" by the compiler.
    @Override public Object visit(SimpleNode node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTCompilationUnit node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTIdentifier node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTQualifiedIdentifier node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTLiteral node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTType node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTBasicType node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTBracketsOpt node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTTypeList node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTQualifiedIdentifierList node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTModifiersOpt node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTModifier node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPackageDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTImportDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTTypeDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTClassDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTClassBody node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTClassBodyDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTInterfaceDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTInterfaceBody node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTInterfaceBodyDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTClassOrInterfaceDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTMemberDecl node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTMethodDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTConstructorDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTExplicitConstructorInvocation node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTFieldDeclaration node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTVariableDeclarators node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTVariableDeclarator node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTVariableInitializer node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTArrayInitializer node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTParametersList node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTFormalParameter node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTBlock node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTBlockStatement node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTLocalVariableDeclarationStatement node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTStatement node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTParExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTForInit node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTForUpdate node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTCatchClause node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTSwitchBlockStatementGroup node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTAssignmentOperator node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTConditionalExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTConditionalOrExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTConditionalAndExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTInclusiveOrExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTExclusiveOrExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTAndExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTEqualityExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTInstanceOfExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTRelationalExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTShiftExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTAdditiveExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTMultiplicativeExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTUnaryExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPreIncrementExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPreDecrementExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTCastExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPostfixExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPrimaryExpression node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPrimaryPrefix node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTPrimarySuffix node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTResultType node, Object data) { return node.childrenAccept(this, data); }
    @Override public Object visit(ASTArrayDimsAndInits node, Object data) { return node.childrenAccept(this, data); }
}