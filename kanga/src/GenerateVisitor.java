import sun.security.provider.ConfigFile;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class GenerateVisitor extends GJDepthFirst<Operand, GenerateEnv> {
    @Override
    public Operand visit(Goal n, GenerateEnv argu) {
        argu.enterFunction("main",
                Integer.valueOf(n.f2.f0.toString()),
                Integer.valueOf(n.f5.f0.toString()),
                Integer.valueOf(n.f8.f0.toString()),
                true);
        n.f10.accept(this, argu);
        argu.leaveFunction();

        n.f12.accept(this, argu);
        argu.append("         .text\n" +
                "         .globl _halloc\n" +
                "_halloc:\n" +
                "         li $v0, 9\n" +
                "         syscall\n" +
                "         j $ra\n" +
                "\n" +
                "         .text\n" +
                "         .globl _print\n" +
                "_print:\n" +
                "         li $v0, 1\n" +
                "         syscall\n" +
                "         la $a0, newl\n" +
                "         li $v0, 4\n" +
                "         syscall\n" +
                "         j $ra\n" +
                "\n" +
                "         .data\n" +
                "         .align   0\n" +
                "newl:    .asciiz \"\\n\" \n" +
                "         .data\n" +
                "         .align   0\n" +
                "str_er:  .asciiz \" ERROR: abnormal termination\\n\" \n");
        return null;
    }

    @Override
    public Operand visit(Procedure n, GenerateEnv argu) {
        argu.enterFunction(n.f0.f0.toString(),
                Integer.valueOf(n.f2.f0.toString()),
                Integer.valueOf(n.f5.f0.toString()),
                Integer.valueOf(n.f8.f0.toString()),
                false);
        n.f10.accept(this, argu);
        argu.leaveFunction();
        return null;
    }

    @Override
    public Operand visit(NoOpStmt n, GenerateEnv argu) {
        argu.indent().append("nop\n");
        return null;
    }

    @Override
    public Operand visit(ErrorStmt n, GenerateEnv argu) {
        argu.indent().append("la $a0, str_er\n");
        argu.indent().append("li $v0, 4\n");
        argu.indent().append("syscall\n");
        return null;
    }

    @Override
    public Operand visit(CJumpStmt n, GenerateEnv argu) {
        argu.indent().append("beqz ").append(n.f1.accept(this, argu));
        argu.append(" ").append(n.f2.f0.toString()).append('\n');
        return null;
    }

    @Override
    public Operand visit(JumpStmt n, GenerateEnv argu) {
        argu.indent().append("b ").append(n.f1.f0.toString()).append('\n');
        return null;
    }

    @Override
    public Operand visit(HStoreStmt n, GenerateEnv argu) {
        argu.indent().append("sw ").append(n.f3.accept(this, argu)).append(", ");
        argu.append(n.f2.f0.toString()).append("(").append(n.f1.accept(this, argu)).append(")\n");
        return null;
    }

    @Override
    public Operand visit(HLoadStmt n, GenerateEnv argu) {
        argu.indent().append("lw ").append(n.f1.accept(this, argu)).append(" ");
        argu.append(n.f3.f0.toString()).append("(").append(n.f2.accept(this, argu)).append(")\n");
        return null;
    }

    @Override
    public Operand visit(MoveStmt n, GenerateEnv argu) {
        Operand expr = n.f2.accept(this, argu);
        Operand reg = n.f1.accept(this, argu);
        String operator = null;
        switch(expr.type) {
            case REGISTER:
                operator = "move";
                break;
            case IMMEDIATE:
                operator = "li";
                break;
            case LABEL:
                operator = "la";
                break;
            case ADD:
                operator = "add";
                break;
            case ADDI:
                operator = "addi";
                break;
            case SUB:
                operator = "sub";
                break;
            case MUL:
                operator = "mul";
                break;
            case SLT:
                operator = "slt";
                break;
            case SLTI:
                operator = "slti";
                break;
        }
        argu.indent().append(operator).append(" ").append(reg).append(" ").append(expr).append("\n");
        return null;
    }

    @Override
    public Operand visit(PrintStmt n, GenerateEnv argu) {
        Operand expr = n.f1.accept(this, argu);
        switch(expr.type) {
            case REGISTER:
                argu.indent().append("move $a0 ").append(expr).append("\n");
                break;
            case IMMEDIATE:
                argu.indent().append("li $a0 ").append(expr).append("\n");
                break;
            case LABEL:
                argu.indent().append("la $a0 ").append(expr).append("\n");
                break;
        }
        argu.indent().append("jal _print\n");
        return null;
    }

    @Override
    public Operand visit(ALoadStmt n, GenerateEnv argu) {
        argu.indent().append("lw ").append(n.f1.accept(this, argu)).append(" ");
        argu.append(n.f2.accept(this, argu)).append("\n");
        return null;
    }

    @Override
    public Operand visit(AStoreStmt n, GenerateEnv argu) {
        argu.indent().append("sw ").append(n.f2.accept(this, argu)).append(", ");
        argu.append(n.f1.accept(this, argu)).append("\n");
        return null;
    }

    @Override
    public Operand visit(PassArgStmt n, GenerateEnv argu) {
        argu.indent().append("sw ").append(n.f2.accept(this, argu)).append(", ");
        argu.append(Integer.valueOf(n.f1.f0.toString()) * 4 - 4).append("($sp)\n");
        return null;
    }

    @Override
    public Operand visit(CallStmt n, GenerateEnv argu) {
        Operand expr = n.f1.accept(this, argu);
        switch (expr.type) {
            case REGISTER:
                argu.indent().append("jalr ").append(expr).append('\n');
                break;
            case IMMEDIATE:
                argu.indent().append("jal ").append(expr).append('\n');
                break;
            case LABEL:
                argu.indent().append("jal ").append(expr).append('\n');
                break;
        }
        return null;
    }

    @Override
    public Operand visit(Exp n, GenerateEnv argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public Operand visit(HAllocate n, GenerateEnv argu) {
        Operand expr = n.f1.accept(this, argu);
        switch(expr.type) {
            case REGISTER:
                argu.indent().append("move $a0, ").append(expr).append('\n');
                break;
            case IMMEDIATE:
                argu.indent().append("li $a0 ").append(expr).append('\n');
                break;
            case LABEL:
                argu.indent().append("la $a0 ").append(expr).append('\n');
                break;
        }
        argu.indent().append("jal _halloc\n");
        return new Operand(Operand.OperandType.REGISTER, "$v0");
    }

    @Override
    public Operand visit(BinOp n, GenerateEnv argu) {
        Operand left = n.f1.accept(this, argu);
        Operand right = n.f2.accept(this, argu);
        Operand operator = n.f0.accept(this, argu);
        if (right.type == Operand.OperandType.IMMEDIATE) {
            switch (operator.type) {
                case ADD:
                    operator.type = Operand.OperandType.ADDI;
                    break;
                case SLT:
                    operator.type = Operand.OperandType.SLTI;
                    break;
                default:
                    argu.indent().append("li $v0 ").append(right).append('\n');
                    right = new Operand(Operand.OperandType.REGISTER, "$v0");
            }
        }
        if (right.type == Operand.OperandType.LABEL) {
            argu.indent().append("la $v0 ").append(right).append('\n');
            right = new Operand(Operand.OperandType.REGISTER, "$v0");
        }
        return new Operand(operator.type, left + ", " + right);
    }

    @Override
    public Operand visit(Operator n, GenerateEnv argu) {
        switch (n.f0.choice.toString()) {
            case "LT":
                return new Operand(Operand.OperandType.SLT, null);
            case "PLUS":
                return new Operand(Operand.OperandType.ADD, null);
            case "MINUS":
                return new Operand(Operand.OperandType.SUB, null);
            case "TIMES":
                return new Operand(Operand.OperandType.MUL, null);
            default:
                return null;
        }
    }

    @Override
    public Operand visit(SpilledArg n, GenerateEnv argu) {
        int index = Integer.valueOf(n.f1.f0.toString());
        return new Operand(Operand.OperandType.STACK, argu.stackVariable(index));
    }

    @Override
    public Operand visit(SimpleExp n, GenerateEnv argu) {
        argu.inSimpleExp = true;
        Operand value = n.f0.accept(this, argu);
        argu.inSimpleExp = false;
        return value;
    }

    @Override
    public Operand visit(Reg n, GenerateEnv argu) {
        return new Operand(Operand.OperandType.REGISTER, "$" + n.f0.choice.toString());
    }

    @Override
    public Operand visit(IntegerLiteral n, GenerateEnv argu) {
        return new Operand(Operand.OperandType.IMMEDIATE, n.f0.toString());
    }

    @Override
    public Operand visit(Label n, GenerateEnv argu) {
        if (!argu.inSimpleExp) {
            argu.append(n.f0.toString()).append(':');
        }
        return new Operand(Operand.OperandType.LABEL, n.f0.toString());
    }
}

