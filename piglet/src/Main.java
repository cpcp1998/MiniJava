import syntaxtree.*;
import visitor.*;

import java.io.*;

public class Main {
    public static void main(String args[]) {
        try {
            InputStream in = new FileInputStream(args[0]);
            PrintStream out = System.out;
            if (args.length > 1)
                out = new PrintStream(args[1]);
            Node root = new PigletParser(in).Goal();
            in.close();
            TranslateVisitor translateVisitor = new TranslateVisitor();
            out.print(root.accept(translateVisitor, null));
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java Main P.pg P.spg");
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
