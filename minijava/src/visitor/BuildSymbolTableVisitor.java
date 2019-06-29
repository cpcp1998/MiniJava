package visitor;

import symbol.*;
import syntaxtree.*;

import java.io.PrintStream;
import java.util.ArrayList;

public class BuildSymbolTableVisitor extends GJVoidDepthFirst<MType> {
    public ArrayList<String> errMessage = new ArrayList<>();

    public boolean isError() {
        return !errMessage.isEmpty();
    }

    public void printError(PrintStream out) {
        for (String message : errMessage) {
            out.println(message);
        }
    }

    @Override
    public void visit(Goal goal, MType argu) {
        goal.f0.accept(this, argu);
        goal.f1.accept(this, argu);
        argu.checkUndefinedType(errMessage);
        if (errMessage.isEmpty()) {
            ((MClassList)argu).checkCyclicInheritance(errMessage);
        }
        if (errMessage.isEmpty()) {
            ((MClassList)argu).checkOverride(errMessage);
        }
    }


    @Override
    public void visit(MainClass n, MType argu) {
        MClass cls = new MClass(argu.getAllClassList(),
                n.f1.f0.toString(),
                n.f1.f0.beginLine,
                n.f1.f0.beginColumn,
                null);

        try {
            ((MClassList) argu).insertClass(cls);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }

        MMethod method = new MMethod(argu.getAllClassList(),
                "main",
                n.f6.beginLine,
                n.f6.beginColumn,
                "void",
                cls);

        try {
            cls.insertMethod(method);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }

        MVar param = new MVar(argu.getAllClassList(),
                n.f11.f0.toString(),
                n.f11.f0.beginLine,
                n.f11.f0.endColumn,
                "String []");
        try {
            (method).insertParam(param);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }

        n.f14.accept(this, method);
    }

    @Override
    public void visit(ClassDeclaration n, MType argu){
        MClass cls = new MClass(argu.getAllClassList(),
                n.f1.f0.toString(),
                n.f1.f0.beginLine,
                n.f1.f0.beginColumn,
                null);
        try {
            ((MClassList) argu).insertClass(cls);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }
        n.f3.accept(this, cls);
        n.f4.accept(this, cls);
    }


    @Override
    public void visit(ClassExtendsDeclaration n, MType argu) {
        MClass cls = new MClass(argu.getAllClassList(),
                n.f1.f0.toString(),
                n.f1.f0.beginLine,
                n.f1.f0.beginColumn,
                n.f3.f0.toString());
        try {
            ((MClassList) argu).insertClass(cls);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }
        n.f5.accept(this, cls);
        n.f6.accept(this, cls);
    }

    @Override
    public void visit(VarDeclaration n, MType argu) {
        MVar var = new MVar(argu.getAllClassList(),
                n.f1.f0.toString(),
                n.f1.f0.beginLine,
                n.f1.f0.beginColumn,
                typeName(n.f0));
        try {
            ((VarContainer)argu).insertVar(var);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }
    }

    @Override
    public void visit(MethodDeclaration n, MType argu) {
        MMethod method = new MMethod(argu.getAllClassList(),
                n.f2.f0.toString(),
                n.f2.f0.beginLine,
                n.f2.f0.beginColumn,
                typeName(n.f1),
                (MClass)argu);
        try {
            ((MClass)argu).insertMethod(method);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }
        n.f4.accept(this, method);
        n.f7.accept(this, method);
    }

    @Override
    public void visit(FormalParameter n, MType argu) {
        MVar var = new MVar(argu.getAllClassList(),
                n.f1.f0.toString(),
                n.f1.f0.beginLine,
                n.f1.f0.beginColumn,
                typeName(n.f0));
        try {
            ((MMethod)argu).insertParam(var);
        }
        catch(DuplicateDefinitionException e) {
            errMessage.add(e.getMessage());
        }
    }

    private String typeName(Type n) {
        switch(n.f0.which) {
            case 0:
                return "int []";
            case 1:
                return "boolean";
            case 2:
                return "int";
            case 3:
                return ((Identifier)n.f0.choice).f0.toString();
                default:
                    throw new RuntimeException("Unknown type");
        }
    }
}
