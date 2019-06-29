package linearscan;

import javafx.util.Pair;
import syntaxtree.*;
import visitor.GJDepthFirst;
import visitor.GJVoidDepthFirst;

public class GenerateVisitor extends GJDepthFirst<String, GenerateEnv> {
    @Override
    public String visit(NoOpStmt n, GenerateEnv env) {
        env.indent().append("NOOP\n");
        return null;
    }

    @Override
    public String visit(ErrorStmt n, GenerateEnv env) {
        env.indent().append("ERROR\n");
        return null;
    }

    @Override
    public String visit(CJumpStmt n, GenerateEnv env) {
        String cond = n.f1.accept(this, env);
        env.indent().append("CJUMP ").append(cond).append(' ');
        env.append(n.f2.accept(this, env)).append('\n');
        env.resetTemp();
        return null;
    }

    @Override
    public String visit(JumpStmt n, GenerateEnv env) {
        env.indent().append("JUMP ").append(n.f1.accept(this, env)).append('\n');
        return null;
    }

    @Override
    public String visit(HStoreStmt n, GenerateEnv env) {
        String src = n.f3.accept(this, env);
        String pos = n.f1.accept(this, env);
        env.indent().append("HSTORE ").append(pos).append(' ').append(n.f2.accept(this, env));
        env.append(' ').append(src).append('\n');
        env.resetTemp();
        return null;
    }

    @Override
    public String visit(HLoadStmt n, GenerateEnv env) {
        Pair<String, String> tgt = output(n.f1, env);
        env.resetTemp();
        String src = n.f2.accept(this, env);
        env.indent().append("HLOAD ").append(tgt.getKey());
        env.append(' ').append(src).append(' ').append(n.f3.accept(this, env)).append('\n');
        if (!tgt.getValue().equals("")) env.indent().append(tgt.getValue()).append('\n');
        env.resetTemp();
        return null;
    }

    @Override
    public String visit(MoveStmt n, GenerateEnv env) {
        Pair<String, String> tgt = output(n.f1, env);
        env.resetTemp();
        String src = n.f2.accept(this, env);
        if (!src.equals(tgt.getKey()))
            env.indent().append("MOVE ").append(tgt.getKey()).append(' ').append(src).append('\n');
        if (!tgt.getValue().equals("")) env.indent().append(tgt.getValue()).append('\n');
        env.resetTemp();
        return null;
    }

    @Override
    public String visit(PrintStmt n, GenerateEnv env) {
        String argu = n.f1.accept(this, env);
        env.indent().append("PRINT ").append(argu).append('\n');
        env.resetTemp();
        return null;
    }

    @Override
    public String visit(Exp n, GenerateEnv env) {
        return n.f0.choice.accept(this, env);
    }

    @Override
    public String visit(Call n, GenerateEnv env) {
        int pos = 0;
        for (Node node : n.f3.nodes) {
            if (pos < 4) {
                env.availableTemp.addFirst("a" + pos);
            }
            String temp = node.accept(this, env);
            if (pos < 4 && !temp.equals("a" + pos)) {
                env.indent().append("MOVE a").append(pos).append(" ").append(temp).append('\n');
            } else if(pos >= 4) {
                env.indent().append("PASSARG ").append(pos - 3).append(" ").append(temp).append('\n');
            }
            env.resetTemp();
            pos++;
        }
        String function = n.f1.accept(this, env);
        env.indent().append("CALL ").append(function).append('\n');
        env.resetTemp();
        return "v0";
    }

    @Override
    public String visit(HAllocate n, GenerateEnv env) {
        return "HALLOCATE " + n.f1.accept(this, env);
    }

    @Override
    public String visit(BinOp n, GenerateEnv env) {
        return n.f0.accept(this, env) + " " +
        n.f1.accept(this, env) + " " +
        n.f2.accept(this, env);
    }

    @Override
    public String visit(Operator n, GenerateEnv env) {
        return n.f0.choice.toString();
    }

    @Override
    public String visit(SimpleExp n, GenerateEnv env) {
        return n.f0.choice.accept(this, env);
    }

    @Override
    public String visit(Temp n, GenerateEnv env) {
        int number = Integer.valueOf(n.f1.f0.toString());
        String register = env.allocation.get(number);
        if (register.charAt(0) == '/') {
            String stackSlot = register.substring(1);
            String temp = env.nextTemp();
            env.indent().append("ALOAD ").append(temp).append(" SPILLEDARG ");
            env.append(stackSlot).append("\n");
            return temp;
        } else {
            return register;
        }
    }

    @Override
    public String visit(IntegerLiteral n, GenerateEnv env) {
        return n.f0.toString();
    }

    @Override
    public String visit(Label n, GenerateEnv env) {
        if (env.globalLabels.containsKey(n.f0.toString()))
            return env.globalLabels.get(n.f0.toString());
        else
            return n.f0.toString();
    }

    private Pair<String, String> output(Temp n, GenerateEnv env) {
        StringBuilder ret = new StringBuilder();
        int number = Integer.valueOf(n.f1.f0.toString());
        String register = env.allocation.get(number);
        if (register == null) return new Pair<>("t8", "");
        if (register.charAt(0) == '/') {
            String stackSlot = register.substring(1);
            String temp = env.nextTemp();
            ret.append("ASTORE SPILLEDARG ").append(stackSlot).append(" ").append(temp);
            register = temp;
        }
        return new Pair<>(register, ret.toString()) ;
    }
}
