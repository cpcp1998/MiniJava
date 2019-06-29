package linearscan;

public interface Statement {
    boolean isBranch();
    String branchTarget();
    StatementType type();
    int getID();
    void setID(int ID);
    RegisterUsage registerUsage();
    int paramCount();
    void generate(GenerateEnv env);
}
