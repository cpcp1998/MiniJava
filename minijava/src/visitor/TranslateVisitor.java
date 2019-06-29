package visitor;

import symbol.*;
import syntaxtree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class TranslateVisitor extends GJDepthFirst<String, TransEnv> {
    public static final int MAX_ARG = 10;

    private MClassList classList;

    public TranslateVisitor(MClassList classList) {
        this.classList = classList;
        for (MClass cls : classList.classList.values()) {
            cls.assignFieldID();
            cls.assignMethodID();
        }
    }

    public static String nameMangle(String cls, String method) {
        return "_" + cls.length() + cls + method.length() + method;
    }

    public void initPiglet(TransEnv env) {
        env.append("init [0]\nBEGIN\n");
        env.indent += 1;
        env.indent().append("MOVE TEMP 0 HALLOCATE ").append(classList.classID.size() * 4).append("\n");
        for (String name : classList.classList.keySet()) {
            MClass cls = classList.classList.get(name);
            int classID = classList.classID.get(name);
            env.indent().append("MOVE TEMP 1 HALLOCATE ").append(cls.methodCount * 4).append("\n");
            env.indent += 1;
            for (String methodName : cls.getAllMethods()) {
                MMethod method = cls.getMethod(methodName);
                int methodID = cls.getMethodID(methodName);
                String mangledName = nameMangle(method.parent.name, method.name);
                env.indent().append("HSTORE TEMP 1 ").append(methodID * 4).append(" ").append(mangledName).append("\n");
            }
            env.indent -= 1;
            env.indent().append("HSTORE TEMP 0 ").append(classID * 4).append(" TEMP 1\n");
        }
        env.indent().append("RETURN TEMP 0\n");
        env.indent -= 1;
        env.append("END\n");
    }

    @Override
    public String visit(Goal n, TransEnv env) {
        env = new TransEnv();
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        initPiglet(env);
        return env.script.toString();
    }

    @Override
    public String visit(MainClass n, TransEnv env) {
        env.cls = classList.get(n.f1.f0.toString());
        env.method = env.cls.getMethod("main");
        env.addMethod(nameMangle(env.cls.name, env.method.name), 1);
        env.append("MAIN\n");
        env.indent = 1;
        env.indent().append("MOVE TEMP 0 CALL init ()\n");
        n.f15.accept(this, env);
        env.indent = 0;
        env.append("END\n");
        env.method = null;
        env.cls = null;
        return null;
    }

    @Override
    public String visit(TypeDeclaration n, TransEnv env) {
        return n.f0.accept(this, env);
    }

    @Override
    public String visit(ClassDeclaration n, TransEnv env) {
        env.cls = classList.get(n.f1.f0.toString());
        n.f4.accept(this, env);
        env.cls = null;
        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n, TransEnv env) {
        env.cls = classList.get(n.f1.f0.toString());
        n.f6.accept(this, env);
        env.cls = null;
        return null;
    }

    /* Calling conversion:
     *   TEMP 0 for global info
     *   TEMP 1 for this
     *   TEMP 2~n+1 for arguments
     *   if n > MAX_ARG, TEMP 2 is a pointer to the list for all the arguments.
     */
    @Override
    public String visit(MethodDeclaration n, TransEnv env) {
        env.method = env.cls.getMethod(n.f2.f0.toString());
        String mangledName = nameMangle(env.cls.name, env.method.name);
        env.addMethod(mangledName, env.method.varID.size() + 2);
        env.append(mangledName);
        if (env.method.paramList.size() > MAX_ARG) {
            env.append(" [3]\n");
            env.append("BEGIN\n");
            env.indent = 1;
            for (int i = env.method.paramList.size() - 1; i >= 0; --i) {
                env.indent().append("HLOAD TEMP ").append(i + 2).append(" TEMP 2 ").append(i * 4).append("\n");
            }
        } else {
            env.append(" [").append(env.method.paramList.size() + 2).append("]\n");
            env.append("BEGIN\n");
            env.indent = 1;
        }
        n.f8.accept(this, env);
        String ret = n.f10.accept(this, env);
        env.indent().append("RETURN ").append(ret).append("\n");
        env.indent = 0;
        env.append("END\n");
        env.method = null;
        return null;
    }

    @Override
    public String visit(Statement n, TransEnv env) {
        n.f0.accept(this, env);
        return null;
    }

    @Override
    public String visit(Block n, TransEnv env) {
        env.indent += 1;
        n.f1.accept(this, env);
        env.indent -= 1;
        return null;
    }

    @Override
    public String visit(AssignmentStatement n, TransEnv env) {
        String varName = n.f0.f0.toString();
        String value = n.f2.accept(this, env);

        env.indent();
        if (env.method.varList.containsKey(varName)) {
            env.append("MOVE TEMP ").append(env.method.varID.get(varName) + 2).append(" ");
        } else {
            env.append("HSTORE TEMP 1 ").append(env.cls.getFieldID(varName) * 4 + 4).append(" ");
        }
        env.append(value).append("\n");
        return null;
    }

    @Override
    public String visit(ArrayAssignmentStatement n, TransEnv env) {
        String varName = n.f0.f0.toString();
        String array;
        if (env.method.varList.containsKey(varName)) {
            array = "TEMP " + (env.method.varID.get(varName) + 2);
        } else {
            array = env.nextTemp();
            env.indent().append("HLOAD ").append(array);
            env.append(" TEMP 1 ").append(env.cls.getFieldID(varName) * 4 + 4).append("\n");
        }

        String index = n.f2.accept(this, env);
        String value = n.f5.accept(this, env);

        String length = env.nextTemp();
        String errorLabel = env.nextLabel();
        String checkLabel = env.nextLabel();

        env.indent().append("CJUMP LT ").append(array).append(" 1 ").append(checkLabel).append("\n");
        env.append(errorLabel).indent().append("ERROR\n");
        env.append(checkLabel).indent().append("HLOAD ").append(length).append(" ").append(array).append(" 0\n");
        env.indent().append("CJUMP LT MINUS 0 1 ").append(index).append(" ").append(errorLabel).append("\n");
        env.indent().append("CJUMP LT ").append(index).append(" ").append(length).append(" ").append(errorLabel).append("\n");
        env.indent().append("HSTORE PLUS ").append(array).append(" TIMES ").append(index).append(" 4 4 ").append(value).append("\n");
        return null;
    }

    @Override
    public String visit(IfStatement n, TransEnv env) {
        String elseLabel = env.nextLabel();
        String finalLabel = env.nextLabel();

        String condition = n.f2.accept(this, env);
        env.append("CJUMP ").append(condition).append(" ").append(elseLabel).append("\n");

        n.f4.accept(this, env);
        env.indent().append("JUMP ").append(finalLabel).append("\n");

        env.append(elseLabel).indent().append("NOOP\n");

        n.f6.accept(this, env);

        env.append(finalLabel).indent().append("NOOP\n");

        return null;
    }

    @Override
    public String visit(WhileStatement n, TransEnv env) {
        String startLabel = env.nextLabel();
        String finalLabel = env.nextLabel();

        env.append(startLabel).indent().append("NOOP\n");

        String condition = n.f2.accept(this, env);
        env.indent().append("CJUMP ").append(condition).append(" ").append(finalLabel).append("\n");

        n.f4.accept(this, env);

        env.indent().append("JUMP ").append(startLabel).append("\n");

        env.append(finalLabel).indent().append("NOOP\n");

        return null;
    }

    @Override
    public String visit(PrintStatement n, TransEnv env) {
        String value = n.f2.accept(this, env);
        env.indent().append("PRINT ").append(value).append("\n");
        return null;
    }

    @Override
    public String visit(Expression n, TransEnv env) {
        return n.f0.accept(this, env);
    }

    @Override
    public String visit(AndExpression n, TransEnv env) {
        String finalLabel = env.nextLabel();

        String ret = n.f0.accept(this, env);
        env.indent().append("CJUMP LT 0 ").append(ret).append(" ").append(finalLabel).append("\n");

        String other = n.f2.accept(this, env);
        env.indent().append("MOVE ").append(ret).append(" ").append(other);

        env.append(finalLabel).indent().append("NOOP\n");

        env.lastType = "boolean";
        return ret;
    }

    @Override
    public String visit(CompareExpression n, TransEnv env) {
        String left = n.f0.accept(this, env);
        String right = n.f2.accept(this, env);

        env.indent().append("MOVE ").append(left).append(" LT ").append(left).append(" ").append(right).append("\n");

        env.lastType = "boolean";
        return left;
    }

    @Override
    public String visit(PlusExpression n, TransEnv env) {
        String left = n.f0.accept(this, env);
        String right = n.f2.accept(this, env);

        env.indent().append("MOVE ").append(left).append(" PLUS ").append(left).append(" ").append(right).append("\n");

        env.lastType = "int";
        return left;
    }

    @Override
    public String visit(MinusExpression n, TransEnv env) {
        String left = n.f0.accept(this, env);
        String right = n.f2.accept(this, env);

        env.indent().append("MOVE ").append(left).append(" MINUS ").append(left).append(" ").append(right).append("\n");

        env.lastType = "int";
        return left;
    }

    @Override
    public String visit(TimesExpression n, TransEnv env) {
        String left = n.f0.accept(this, env);
        String right = n.f2.accept(this, env);

        env.indent().append("MOVE ").append(left).append(" TIMES ").append(left).append(" ").append(right).append("\n");

        env.lastType = "int";
        return left;
    }

    @Override
    public String visit(ArrayLookup n, TransEnv env) {
        String array = n.f0.accept(this, env);

        String index = n.f2.accept(this, env);

        String length = env.nextTemp();
        String errorLabel = env.nextLabel();
        String checkLabel = env.nextLabel();

        env.indent().append("CJUMP LT ").append(array).append(" 1 ").append(checkLabel).append("\n");
        env.append(errorLabel).indent().append("ERROR\n");
        env.append(checkLabel).indent().append("HLOAD ").append(length).append(" ").append(array).append(" 0\n");
        env.indent().append("CJUMP LT MINUS 0 1 ").append(index).append(" ").append(errorLabel).append("\n");
        env.indent().append("CJUMP LT ").append(index).append(" ").append(length).append(" ").append(errorLabel).append("\n");

        String ret = env.nextTemp();
        env.indent().append("HLOAD ").append(ret).append(" PLUS ").append(array).append(" TIMES ").append(index).append(" 4 4 ").append("\n");

        env.lastType = "int";
        return ret;
    }

    @Override
    public String visit(ArrayLength n, TransEnv env) {
        String array = n.f0.accept(this, env);

        String length = env.nextTemp();
        String errorLabel = env.nextLabel();
        String finalLabel = env.nextLabel();

        env.indent().append("CJUMP LT ").append(array).append(" 1 ").append(finalLabel).append("\n");
        env.append(errorLabel).indent().append("ERROR\n");
        env.append(finalLabel).indent().append("HLOAD ").append(length).append(" ").append(array).append(" 0\n");

        env.lastType = "int";
        return length;
    }

    @Override
    public String visit(MessageSend n, TransEnv env) {
        String object = n.f0.accept(this, env);
        MClass cls = classList.classList.get(env.lastType);
        int methodID = cls.getMethodID(n.f2.f0.toString());
        int paramCount = cls.getMethod(n.f2.f0.toString()).paramList.size();
        String returnType = cls.getMethod(n.f2.f0.toString()).returnType;

        env.paramList.push(new ArrayList<>());
        n.f4.accept(this, env);

        String passLabel = env.nextLabel();
        env.indent().append("CJUMP LT ").append(object).append(" 1").append(passLabel).append("\n");
        env.indent().append("ERROR\n");

        String vtable = env.nextTemp();
        String addr = env.nextTemp();
        env.append(passLabel).indent().append("HLOAD ").append(vtable).append(" ").append(object).append(" 0\n");
        env.indent().append("HLOAD ").append(addr).append(" ").append(vtable).append(" ").append(methodID * 4).append("\n");

        if (paramCount > MAX_ARG) {
            String firstParam = env.nextTemp();
            env.indent().append("MOVE ").append(firstParam).append(" HALLOCATE ").append(paramCount * 4).append("\n");
            for (int i = 0; i < paramCount; ++ i) {
                env.indent().append("HSTORE ").append(firstParam).append(" ").append(i * 4);
                env.append(" ").append(env.paramList.peek().get(i)).append("\n");
            }
            env.paramList.peek().clear();
            env.paramList.peek().add(firstParam);
            paramCount = 1;
        }

        String ret = env.nextTemp();

        env.indent().append("MOVE ").append(ret).append(" CALL ").append(addr).append(" (TEMP 0 ").append(object);
        for (String param : env.paramList.peek()) {
            env.append(" ").append(param);
        }
        env.append(")\n");
        env.paramList.pop();

        env.lastType = returnType;
        return ret;
    }

    @Override
    public String visit(ExpressionList n, TransEnv env) {
        env.paramList.peek().add(n.f0.accept(this, env));
        n.f1.accept(this, env);
        return null;
    }

    @Override
    public String visit(ExpressionRest n, TransEnv env) {
        env.paramList.peek().add(n.f1.accept(this, env));
        return null;
    }

    @Override
    public String visit(PrimaryExpression n, TransEnv env) {
        return n.f0.accept(this, env);
    }

    @Override
    public String visit(IntegerLiteral n, TransEnv env) {
        String ret = env.nextTemp();
        env.indent().append("MOVE ").append(ret).append(" ").append(n.f0.toString()).append("\n");

        env.lastType = "int";
        return ret;
    }

    @Override
    public String visit(TrueLiteral n, TransEnv env) {
        String ret = env.nextTemp();
        env.indent().append("MOVE ").append(ret).append(" 1").append("\n");

        env.lastType = "boolean";
        return ret;
    }

    @Override
    public String visit(FalseLiteral n, TransEnv env) {
        String ret = env.nextTemp();
        env.indent().append("MOVE ").append(ret).append(" 0").append("\n");

        env.lastType = "boolean";
        return ret;
    }

    @Override
    public String visit(Identifier n, TransEnv env) {
        String varName = n.f0.toString();
        String ret = env.nextTemp();
        if (env.method.varList.containsKey(varName)) {
            env.indent().append("MOVE ").append(ret).append(" TEMP ").append(env.method.varID.get(varName) + 2).append("\n");
        } else {
            env.indent().append("HLOAD ").append(ret);
            env.append(" TEMP 1 ").append(env.cls.getFieldID(varName) * 4 + 4).append("\n");
        }

        env.lastType = env.method.getVar(varName).type;
        return ret;
    }

    @Override
    public String visit(ThisExpression n, TransEnv env) {
        String ret = env.nextTemp();
        env.indent().append("MOVE ").append(ret).append(" TEMP 1\n");

        env.lastType = env.cls.name;
        return ret;
    }

    @Override
    public String visit(ArrayAllocationExpression n, TransEnv env) {
        String length = n.f3.accept(this, env);
        String ret = env.nextTemp();

        String passLabel = env.nextLabel();
        env.indent().append("CJUMP LT ").append(length).append(" 0 ").append(passLabel).append("\n");
        env.indent().append("ERROR\n");
        env.append(passLabel);

        env.indent().append("MOVE ").append(ret).append(" HALLOCATE TIMES 4 PLUS 1 ").append(length).append("\n");
        env.indent().append("HSTORE ").append(ret).append(" 0 ").append(length).append("\n");

        String index = env.nextTemp();
        String startLabel = env.nextLabel();
        String finishLabel = env.nextLabel();
        env.indent().append("MOVE ").append(index).append(" 0\n");
        env.append(startLabel).indent().append("CJUMP LT ").append(index).append(" ").append(length);
        env.append(" ").append(finishLabel).append("\n");
        env.indent().append("HSTORE PLUS ").append(ret).append(" TIMES 4").append(index).append(" 4 0\n");
        env.indent().append("MOVE ").append(index).append(" PLUS ").append(index).append(" 1\n");
        env.indent().append("JUMP ").append(startLabel).append("\n");
        env.append(finishLabel).indent().append("NOOP\n");

        env.lastType = "int []";
        return ret;
    }

    @Override
    public String visit(AllocationExpression n, TransEnv env) {
        String ret = env.nextTemp();
        int classID = classList.classID.get(n.f1.f0.toString());
        int size = classList.get(n.f1.f0.toString()).fieldCount;

        env.indent().append("MOVE ").append(ret).append(" HALLOCATE ").append(size * 4 + 4).append("\n");
        String temp = env.nextTemp();
        env.indent().append("HLOAD ").append(temp).append(" TEMP 0 ").append(classID * 4).append("\n");
        env.indent().append("HSTORE ").append(ret).append(" 0 ").append(temp).append("\n");
        for (int i = 0; i < size; ++i) {
            env.indent().append("HSTORE ").append(ret).append(" ").append(i * 4 + 4).append(" 0\n");
        }

        env.lastType = n.f1.f0.toString();
        return ret;
    }

    @Override
    public String visit(NotExpression n, TransEnv env) {
        String op = n.f1.accept(this, env);
        env.indent().append("MOVE ").append(op).append(" MINUS 1 ").append(op).append("\n");
        env.lastType = "boolean";
        return op;
    }

    @Override
    public String visit(BracketExpression n, TransEnv env) {
        return n.f1.accept(this, env);
    }
}

class TransEnv {
    private HashMap<String, Integer> nextTemp;
    private int nextLabel = 0;
    public MMethod method = null;
    public MClass cls = null;
    public int indent = 0;
    public StringBuilder script;
    public String lastType;
    public Stack<List<String>> paramList;

    TransEnv() {
        nextTemp = new HashMap<>();
        script = new StringBuilder();
        paramList = new Stack<>();
    }

    public void addMethod(String name, int first) {
        nextTemp.put(name, first);
    }

    String nextTemp() {
        String name = TranslateVisitor.nameMangle(cls.name, method.name);
        int next = nextTemp.get(name);
        nextTemp.put(name, next + 1);
        return "TEMP " + next;
    }

    String nextLabel() {
        return "L" + (nextLabel++);
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