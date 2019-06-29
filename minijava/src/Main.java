import syntaxtree.*;
import visitor.*;
import symbol.*;

import java.io.*;

public class Main {
    public static void main(String args[]) {
        try {
            InputStream in = new FileInputStream(args[0]);
            PrintStream out = System.out;
            if (args.length > 1)
                out = new PrintStream(args[1]);
            Node root = new MiniJavaParser(in).Goal();
            in.close();
            MClassList allClassList = new MClassList();
            BuildSymbolTableVisitor buildSymbolTableVisitor = new BuildSymbolTableVisitor();
            root.accept(buildSymbolTableVisitor, allClassList);
            if (buildSymbolTableVisitor.isError()){
                System.out.println("Type error");
                buildSymbolTableVisitor.printError(System.out);
                return;
            }
            TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor();
            root.accept(typeCheckVisitor, allClassList);
            if (typeCheckVisitor.isError()){
                System.out.println("Type error");
                typeCheckVisitor.printError(System.out);
                return;
            }
            System.out.println("Program type checked successfully");
            TranslateVisitor translateVisitor = new TranslateVisitor(allClassList);
            out.print(root.accept(translateVisitor, null));
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java Main P.java P.pg");
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found.");
        }
        catch(SecurityException e) {
            System.out.println("Cannot write.");
        }
        catch(IOException e){
            System.out.println("IO Exception.");
        }
        catch(ParseException e) {
            System.out.println(e.getMessage());
        }
    }
}
