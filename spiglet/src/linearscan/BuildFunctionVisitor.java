package linearscan;

import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.List;

public class BuildFunctionVisitor extends GJDepthFirst<List<Function>, List<Function>> {
    @Override
    public List<Function> visit(Goal n, List<Function> argu) {
        List<Function> functionList = new ArrayList<>();
        Function mainFunction = new Function("MAIN", 0);
        functionList.add(mainFunction);
        n.f1.accept(this, functionList);
        n.f3.accept(this, functionList);
        return functionList;
    }

    @Override
    public List<Function> visit(Procedure n, List<Function> argu) {
        String name = n.f0.f0.toString();
        int paramCount = Integer.valueOf(n.f2.f0.toString());
        Function function = new Function(name, paramCount);
        argu.add(function);
        n.f4.accept(this, argu);
        return null;
    }

    @Override
    public List<Function> visit(StmtExp n, List<Function> argu) {
        n.f1.accept(this, argu);
        argu.get(argu.size() - 1).stmts.add(new ReturnStatement(n.f3));
        return null;
    }

    @Override
    public List<Function> visit(Stmt n, List<Function> argu) {
        argu.get(argu.size() - 1).stmts.add(new RegularStatement(n));
        return null;
    }

    @Override
    public List<Function> visit(Label n, List<Function> argu) {
        Function function = argu.get(argu.size() - 1);
        int nextStmt = function.stmts.size();
        function.labels.put(n.f0.toString(), nextStmt);
        return null;
    }
}
