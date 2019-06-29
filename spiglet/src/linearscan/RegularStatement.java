package linearscan;

import syntaxtree.*;
import visitor.*;

public class RegularStatement implements Statement {
    private Stmt stmt;
    private StatementType type;
    private int ID;
    private RegisterUsage registerUsage;

    public RegularStatement(Stmt stmt) {
        this.stmt = stmt;
        StatementTypeVisitor statementTypeVisitor = new StatementTypeVisitor();
        type = stmt.accept(statementTypeVisitor);
        registerUsage = new RegisterUsage();
        RegisterUsageVisitor registerUsageVisitor = new RegisterUsageVisitor();
        stmt.accept(registerUsageVisitor, registerUsage);
    }

    @Override
    public StatementType type() {
        return type;
    }

    @Override
    public boolean isBranch() {
        return (type == StatementType.CJUMP || type == StatementType.JUMP);
    }

    @Override
    public String branchTarget() {
        if (type == StatementType.JUMP) {
            return ((JumpStmt)stmt.f0.choice).f1.f0.toString();
        }
        if (type == StatementType.CJUMP) {
            return ((CJumpStmt)stmt.f0.choice).f2.f0.toString();
        }
        return null;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public void setID(int ID) {
        this.ID = ID;
    }

    @Override
    public RegisterUsage registerUsage() {
        return registerUsage;
    }

    @Override
    public int paramCount() {
        if (type != StatementType.CALL) return 0;
        return ((Call)((MoveStmt)stmt.f0.choice).f2.f0.choice).f3.size();
    }

    @Override
    public void generate(GenerateEnv env) {
        GenerateVisitor visitor = new GenerateVisitor();
        stmt.accept(visitor, env);
    }
}
