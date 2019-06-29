package linearscan;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

import java.io.PrintStream;

public class StatementTypeVisitor extends GJNoArguDepthFirst<StatementType> {
    @Override
    public StatementType visit(Stmt n) {
        return n.f0.accept(this);
    }

    @Override
    public StatementType visit(NoOpStmt n) {
        return StatementType.NOOP;
    }

    @Override
    public StatementType visit(ErrorStmt n) {
        return StatementType.ERROR;
    }

    @Override
    public StatementType visit(CJumpStmt n) {
        return StatementType.CJUMP;
    }

    @Override
    public StatementType visit(JumpStmt n) {
        return StatementType.JUMP;
    }

    @Override
    public StatementType visit(HStoreStmt n) {
        return StatementType.HSTORE;
    }

    @Override
    public StatementType visit(HLoadStmt n) {
        return StatementType.HLOAD;
    }

    @Override
    public StatementType visit(MoveStmt n) {
        StatementType exprType = n.f2.accept(this);
        if (exprType != null) return exprType;
        return StatementType.MOVE;
    }

    @Override
    public StatementType visit(PrintStmt n) {
        return StatementType.PRINT;
    }

    @Override
    public StatementType visit(Exp n) { return n.f0.choice.accept(this); }

    @Override
    public StatementType visit(Call n) { return StatementType.CALL; }

    @Override
    public StatementType visit(HAllocate n) { return StatementType.HALLOCATE; }

    @Override
    public StatementType visit(BinOp n) { return StatementType.BINOP; }
}