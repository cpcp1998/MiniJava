package symbol;

public interface VarContainer {
    void insertVar(MVar var) throws DuplicateDefinitionException;
}
