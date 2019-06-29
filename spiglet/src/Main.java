import syntaxtree.*;
import visitor.*;
import linearscan.LinearScan;

import java.io.*;

public class Main {
    public static void main(String args[]) {
        try {
            InputStream in = new FileInputStream(args[0]);
            PrintStream out = System.out;
            if (args.length > 1)
                out = new PrintStream(args[1]);
            Node root = new SpigletParser(in).Goal();
            LinearScan linearScan = new LinearScan(root);
            out.print(linearScan.run());
            in.close();
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java Main P.spg P.kg");
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
