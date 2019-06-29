public class Operand {
    public enum OperandType {
        REGISTER, IMMEDIATE, LABEL, STACK,
        ADD, ADDI, MUL, SUB, SLT, SLTI
    }
    public OperandType type;
    public String value;

    public Operand(OperandType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
