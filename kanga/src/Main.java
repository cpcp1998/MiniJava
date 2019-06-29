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
            Node root = new KangaParser(in).Goal();
            GenerateEnv env = new GenerateEnv();
            GenerateVisitor visitor = new GenerateVisitor();
            root.accept(visitor, env);
            out.print(env.toString());
            in.close();
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java Main P.kg P.s");
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