package linearscan;

import syntaxtree.*;

public class ReturnStatement implements Statement {
    private SimpleExp retValue;
    private int ID;
    private RegisterUsage registerUsage;

    public ReturnStatement(SimpleExp retValue) {
        this.retValue = retValue;
        registerUsage = new RegisterUsage();
        RegisterUsageVisitor registerUsageVisitor = new RegisterUsageVisitor();
        retValue.accept(registerUsageVisitor, registerUsage);
    }

    @Override
    public StatementType type() {
        return StatementType.RETURN;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public String branchTarget() {
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
        return 0;
    }

    @Override
    public void generate(GenerateEnv env) {
        GenerateVisitor visitor = new GenerateVisitor();
        String ret = retValue.accept(visitor, env);
        env.indent().append("MOVE v0 ").append(ret).append('\n');
    }
}
