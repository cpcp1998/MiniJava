package visitor;

import sun.util.locale.provider.FallbackLocaleProviderAdapter;
import syntaxtree.*;

import java.io.PrintStream;
import java.util.*;

public class TranslateVisitor extends GJDepthFirst<String, TransEnv>{
    @Override
    public String visit(Goal n, TransEnv env) {
        env = new TransEnv();
        env.enterFunction(0);
        env.append("MAIN\n");
        env.indent += 1;
        n.f1.accept(this, env);
        env.indent -= 1;
        env.append("END\n");

        n.f3.accept(this, env);
        return env.script.toString();
    }

    @Override
    public String visit(Procedure n, TransEnv env) {
        int paramCount = Integer.valueOf(n.f2.f0.toString());
        env.enterFunction(paramCount);
        env.append(n.f0.f0.toString()).append(" [").append(paramCount).append("]\n");
        env.append("BEGIN\n");
        env.indent += 1;
        String ret = n.f4.accept(this, env);
        env.indent().append("RETURN ").append(ret).append("\n");
        env.indent -= 1;
        env.append("END\n");
        return null;
    }

    @Override
    public String visit(StmtList n, TransEnv env) {
        env.printLabel = true;
        n.f0.accept(this, env);
        env.printLabel = false;
        return null;
    }

    @Override
    public String visit(Stmt n, TransEnv env) {
        env.printLabel = false;
        n.f0.accept(this, env);
        env.printLabel = true;
        return null;
    }

    @Override
    public String visit(NoOpStmt n, TransEnv env) {
        env.indent().append("NOOP\n");
        return null;
    }

    @Override
    public String visit(ErrorStmt n, TransEnv env) {
        env.indent().append("ERROR\n");
        return null;
    }

    @Override
    public String visit(CJumpStmt n, TransEnv env) {
        String condition = n.f1.accept(this, env);
        env.indent().append("CJUMP ").append(condition).append(" ").append(n.f2.f0.toString()).append("\n");
        return null;
    }

    @Override
    public String visit(JumpStmt n, TransEnv env) {
        env.indent().append("JUMP ").append(n.f1.f0.toString()).append("\n");
        return null;
    }

    @Override
    public String visit(HStoreStmt n, TransEnv env) {
        String address = n.f1.accept(this, env);
        String value = n.f3.accept(this, env);
        env.indent().append("HSTORE ").append(address).append(" ").append(n.f2.f0.toString());
        env.append(" ").append(value).append("\n");
        return null;
    }

    @Override
    public String visit(HLoadStmt n, TransEnv env) {
        String dst = n.f1.accept(this, env);
        String address = n.f2.accept(this, env);
        env.indent().append("HLOAD ").append(dst).append(" ");
        env.append(address).append(" ").append(n.f3.f0.toString()).append("\n");
        return null;
    }

    @Override
    public String visit(MoveStmt n, TransEnv env) {
        String dst = n.f1.accept(this, env);
        String value = n.f2.accept(this, env);
        env.indent().append("MOVE ").append(dst).append(" ").append(value).append("\n");
        return null;
    }

    @Override
    public String visit(PrintStmt n, TransEnv env) {
        String value = n.f1.accept(this, env);
        env.indent().append("PRINT ").append(value).append("\n");
        return null;
    }

    @Override
    public String visit(Exp n, TransEnv env) {
        return n.f0.accept(this, env);
    }

    @Override
    public String visit(StmtExp n, TransEnv env) {
        n.f1.accept(this, env);
        return n.f3.accept(this, env);
    }

    @Override
    public String visit(Call n, TransEnv env) {
        String function = n.f1.accept(this, env);
        List<String> params = new ArrayList<>();
        if (n.f3.present()) {
            for (Node node : n.f3.nodes) {
                params.add(node.accept(this, env));
            }
        }
        String ret = env.nextTemp();

        env.indent().append("MOVE ").append(ret).append(" CALL ").append(function).append(" (");
        for (int i = 0; i < params.size(); ++i) {
            env.append(" ").append(params.get(i));
        }
        env.append(" )\n");
        return ret;
    }

    @Override
    public String visit(HAllocate n, TransEnv env) {
        String size = n.f1.accept(this, env);
        String ret = env.nextTemp();

        env.indent().append("MOVE ").append(ret).append(" HALLOCATE ").append(size).append("\n");
        return ret;
    }

    @Override
    public String visit(BinOp n, TransEnv env) {
        String left = n.f1.accept(this, env);
        String right = n.f2.accept(this, env);
        String op = n.f0.accept(this, env);
        String ret = env.nextTemp();

        env.indent().append("MOVE ").append(ret).append(" ").append(op).append(" ");
        env.append(left).append(" ").append(right).append("\n");
        return ret;
    }

    @Override
    public String visit(Operator n, TransEnv env) {
        return n.f0.choice.toString();
    }

    @Override
    public String visit(Temp n, TransEnv env) {
        return env.mapTemp("TEMP " + n.f1.f0.toString());
    }

    @Override
    public String visit(IntegerLiteral n, TransEnv env) {
        String temp = env.nextTemp();
        env.indent().append("MOVE ").append(temp).append(" ").append(n.f0.toString()).append("\n");
        return temp;
    }

    /* 1. For statement: printLabel is true;
     * 2. For procedure and jump: print directly without involving this function
     * 3. For expression: printLabel is false;
     */
    @Override
    public String visit(Label n, TransEnv env) {
        if (env.printLabel) {
            env.append(n.f0.toString());
            return null;
        } else {
            String temp = env.nextTemp();
            env.indent().append("MOVE ").append(temp).append(" ").append(n.f0.toString()).append("\n");
            return temp;
        }
    }
}

class TransEnv {
    int indent = 0;
    StringBuilder script;
    private HashMap<Integer, Integer> varRemap;
    private int nextTemp;
    boolean printLabel = false;

    TransEnv() {
        script = new StringBuilder();
    }

    void enterFunction(int paramCount) {
        varRemap = new HashMap<>();
        for (int i = 0; i < paramCount; ++i) {
            varRemap.put(i, i);
        }
        nextTemp = paramCount;
    }

    String nextTemp() {
        return "TEMP " + (nextTemp++);
    }

    String mapTemp(String origin) {
        int id = Integer.valueOf(origin.substring(5));
        if (!varRemap.containsKey(id)) {
            varRemap.put(id, nextTemp++);
        }
        return "TEMP " + varRemap.get(id);
    }

    TransEnv append(boolean s) {
        script.append(s);
        return this;
    }

    TransEnv append(char s) {
        script.append(s);
        return this;
    }

    TransEnv append(char[] s) {
        script.append(s);
        return this;
    }

    TransEnv append(double s) {
        script.append(s);
        return this;
    }

    TransEnv append(float s) {
        script.append(s);
        return this;
    }

    TransEnv append(int s) {
        script.append(s);
        return this;
    }

    TransEnv append(long s) {
        script.append(s);
        return this;
    }

    TransEnv append(Object s) {
        script.append(s);
        return this;
    }

    TransEnv append(String s) {
        script.append(s);
        return this;
    }

    TransEnv indent() {
        for (int i = 0; i < indent; ++i)
            script.append('\t');
        return this;
    }
}
