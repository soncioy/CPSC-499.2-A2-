package ca.ucalgary.cpsc49902;

import ca.ucalgary.cpsc49902.javacc.Java12Parser;
import ca.ucalgary.cpsc49902.javacc.Node;

import java.io.FileInputStream;
import java.io.InputStreamReader; // Import this

public class TreeGenerator {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.out.println("Wrong number of arguments for Tree generation.");
            System.exit(1);
        }

        try(FileInputStream inputStream = new FileInputStream(args[0])){
            // FIX IS HERE: Use InputStreamReader
            Java12Parser parser = new Java12Parser(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
            Node root = parser.CompilationUnit();
            TreeGeneratorHelper.dump(root, "");
        }
    }
}