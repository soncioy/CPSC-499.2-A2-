package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.javacc.Node;
import ca.ucalgary.cpsc49902.javacc.SimpleNode;
import ca.ucalgary.cpsc49902.javacc.Token;

public class TreeGeneratorHelper {
    public static void dump(Node n, String indentation){
        SimpleNode simpleNode = (SimpleNode) n;

        Token firstToken = simpleNode.jjtGetFirstToken();
        Token lastToken = simpleNode.jjtGetLastToken();

        String spanning = " ";

        if(firstToken != null && lastToken != null){
            spanning = " [" + firstToken.beginLine + ":" + firstToken.beginColumn + " .. " + lastToken.endLine + ":" + lastToken.endColumn + "]";
        }

        System.out.println(indentation + simpleNode.toString() + spanning);

        for (int i = 0; i < simpleNode.jjtGetNumChildren(); i++) {
            dump(simpleNode.jjtGetChild(i), indentation + " ");
        }
    }
}
