package linearscan;

import syntaxtree.*;
import visitor.*;

public class RegisterUsageVisitor extends GJVoidDepthFirst<RegisterUsage> {
    @Override
    public void visit(HLoadStmt n, RegisterUsage argu) {
        argu.outputVirtual.add(Integer.valueOf(n.f1.f1.f0.toString()));
        n.f2.accept(this, argu);
    }

    @Override
    public void visit(MoveStmt n, RegisterUsage argu) {
        argu.outputVirtual.add(Integer.valueOf(n.f1.f1.f0.toString()));
        n.f2.accept(this, argu);
    }

    @Override
    public void visit(Temp n, RegisterUsage argu) {
        argu.inputVirtual.add(Integer.valueOf(n.f1.f0.toString()));
    }
}
