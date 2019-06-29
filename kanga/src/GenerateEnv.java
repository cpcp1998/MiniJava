public class GenerateEnv {
    StringBuilder script;
    private int argsInStack;
    private int stackSize;
    private int reservedStack;
    private boolean isMain;
    boolean inSimpleExp = false;

    GenerateEnv() {
        script = new StringBuilder();
    }

    void enterFunction(String symbol, int args, int stack, int argMax, boolean isMain) {
        this.isMain = isMain;
        this.indent().append(".text\n");
        this.indent().append(".globl").indent().append(symbol).append('\n');
        this.append(symbol).append(":\n");
        if (!isMain)
            this.indent().append("sw $fp, -8($sp)\n");
        this.indent().append("move $fp, $sp\n");
        argsInStack = args > 4 ? args - 4 : 0;
        reservedStack = argMax > 4 ? argMax - 4 : 0;
        stackSize = (isMain ? 1 : 2) + stack - argsInStack + reservedStack;
        this.indent().append("subu $sp, $sp, ").append(4 * stackSize).append('\n');
        this.indent().append("sw $ra, -4($fp)\n");
    }

    void leaveFunction() {
        this.indent().append("lw $ra, -4($fp)\n");
        this.indent().append("addu $sp, $sp, ").append(4 * stackSize).append('\n');
        if (!this.isMain)
            this.indent().append("lw $fp, -8($sp)\n");
        this.indent().append("j $ra\n");
        this.append('\n');
    }

    String stackVariable(int n) {
        if (n < argsInStack) {
            return 4 * n + "($fp)";
        }
        return 4 * (n - argsInStack + reservedStack) + "($sp)";
    }

    GenerateEnv append(boolean s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(char s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(char[] s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(double s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(float s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(int s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(long s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(Object s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(String s) {
        script.append(s);
        return this;
    }

    GenerateEnv append(Operand s) {
        script.append(s.value);
        return this;
    }

    GenerateEnv indent() {
        script.append('\t');
        return this;
    }

    public String toString() {
        return script.toString();
    }
}
