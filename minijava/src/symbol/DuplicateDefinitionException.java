package symbol;

public class DuplicateDefinitionException extends Exception{
    public DuplicateDefinitionException(MIdentifier identifier) {
        super(initialise(identifier));
        this.identifier = identifier;
    }

    public MIdentifier identifier;

    private static String initialise(MIdentifier identifier) {
        String message = "Duplicated definition \"";
        message += identifier.category;
        message += " ";
        message += identifier.name;
        message += "\" at line ";
        message += identifier.row;
        message += ", column ";
        message += identifier.col;
        message += ".";
        return message;
    }
}
