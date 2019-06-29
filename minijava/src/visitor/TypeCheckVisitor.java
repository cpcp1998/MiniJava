package visitor;

import symbol.*;
import syntaxtree.*;

import java.io.PrintStream;
import java.util.ArrayList;

public class TypeCheckVisitor extends GJDepthFirst<TypeInfo, MType> {
    public ArrayList<String> errMessage = new ArrayList<>();

    public boolean isError() {
        return !errMessage.isEmpty();
    }

    public void printError(PrintStream out) {
        for (String message : errMessage) {
            out.println(message);
        }
    }

    @Override
    public TypeInfo visit(MainClass n, MType argu) {
        MClass cls = ((MClassList)argu).get(n.f1.f0.toString());
        MMethod method = cls.getMethod("main");
        n.f15.accept(this, method);
        return null;
    }

    @Override
    public TypeInfo visit(ClassDeclaration n, MType argu) {
        MClass cls = ((MClassList)argu).get(n.f1.f0.toString());
        n.f4.accept(this, cls);
        return null;
    }

    @Override
    public TypeInfo visit(ClassExtendsDeclaration n, MType argu) {
        MClass cls = ((MClassList)argu).get(n.f1.f0.toString());
        n.f6.accept(this, cls);
        return null;
    }

    @Override
    public TypeInfo visit(MethodDeclaration n, MType argu) {
        MMethod method = ((MClass)argu).getMethod(n.f2.f0.toString());
        n.f8.accept(this, method);
        TypeInfo returnValue = n.f10.accept(this, method);
        if (returnValue != null && !argu.getAllClassList().convertable(returnValue.type, method.returnType)) {
            errMessage.add(returnValue.errMessage(method.returnType));
        }
        return null;
    }

    @Override
    public TypeInfo visit(AssignmentStatement n, MType argu) {
        MVar var = ((MMethod)argu).getVar(n.f0.f0.toString());
        if(var == null) {
            errMessage.add("Undefined variable \"" + n.f0.f0.toString() + "\" at line "
                    + n.f0.f0.beginLine + ", column " + n.f0.f0.beginColumn);
            return null;
        }
        TypeInfo value = n.f2.accept(this, argu);
        if (value == null) {
            return null;
        }
        if (!argu.getAllClassList().convertable(value.type, var.type)) {
            errMessage.add(value.errMessage(var.type));
            return null;
        }
        return null;
    }

    @Override
    public TypeInfo visit(ArrayAssignmentStatement n, MType argu) {
        MVar array = ((MMethod)argu).getVar(n.f0.f0.toString());
        if(array == null) {
            errMessage.add("Undefined variable \"" + n.f0.f0.toString() + "\" at line "
                    + n.f0.f0.beginLine + ", column " + n.f0.f0.beginColumn);
            return null;
        }
        if (!"int []".equals(array.type)){
            errMessage.add("Type error at line " + n.f0.f0.beginLine + ", column " + n.f0.f0.beginColumn
                    + ": expected int [], got " + array.type + ".");
            return null;
        }
        TypeInfo index = n.f2.accept(this, argu);
        if (index == null) {
            return null;
        }
        if (!"int".equals(index.type)) {
            errMessage.add(index.errMessage("int"));
            return null;
        }
        TypeInfo value = n.f5.accept(this, argu);
        if (value == null) {
            return null;
        }
        if (!"int".equals(value.type)) {
            errMessage.add(value.errMessage("int"));
            return null;
        }
        return null;
    }

    @Override
    public TypeInfo visit(IfStatement n, MType argu) {
        TypeInfo condition = n.f2.accept(this, argu);
        if (condition != null && !"boolean".equals(condition.type)) {
            errMessage.add(condition.errMessage("boolean"));
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }

    @Override
    public TypeInfo visit(WhileStatement n, MType argu) {
        TypeInfo condition = n.f2.accept(this, argu);
        if (condition != null && !"boolean".equals(condition.type)) {
            errMessage.add(condition.errMessage("boolean"));
        }
        n.f4.accept(this, argu);
        return null;
    }

    @Override
    public TypeInfo visit(PrintStatement n, MType argu) {
        TypeInfo param = n.f2.accept(this, argu);
        if (param != null && !"int".equals(param.type)) {
            errMessage.add(param.errMessage("int"));
        }
        return null;
    }

    @Override
    public TypeInfo visit(Expression n, MType argu) {
        return n.f0.choice.accept(this, argu);
    }

    @Override
    public TypeInfo visit(AndExpression n, MType argu) {
        TypeInfo leftOp = n.f0.accept(this, argu);
        if (leftOp == null) {
            return null;
        }
        if (!"boolean".equals(leftOp.type)) {
            errMessage.add(leftOp.errMessage("boolean"));
            return null;
        }
        TypeInfo rightOp = n.f2.accept(this, argu);
        if (rightOp == null) {
            return null;
        }
        if (!"boolean".equals(rightOp.type)) {
            errMessage.add(rightOp.errMessage("boolean"));
            return null;
        }
        return new TypeInfo("boolean", leftOp.row, leftOp.col);
    }

    @Override
    public TypeInfo visit(CompareExpression n, MType argu) {
        TypeInfo leftOp = n.f0.accept(this, argu);
        if (leftOp == null) {
            return null;
        }
        if (!"int".equals(leftOp.type)) {
            errMessage.add(leftOp.errMessage("int"));
            return null;
        }
        TypeInfo rightOp = n.f2.accept(this, argu);
        if (rightOp == null) {
            return null;
        }
        if (!"int".equals(rightOp.type)) {
            errMessage.add(rightOp.errMessage("int"));
            return null;
        }
        return new TypeInfo("boolean", leftOp.row, leftOp.col);
    }

    @Override
    public TypeInfo visit(PlusExpression n, MType argu) {
        TypeInfo leftOp = n.f0.accept(this, argu);
        if (leftOp == null) {
            return null;
        }
        if (!"int".equals(leftOp.type)) {
            errMessage.add(leftOp.errMessage("int"));
            return null;
        }
        TypeInfo rightOp = n.f2.accept(this, argu);
        if (rightOp == null) {
            return null;
        }
        if (!"int".equals(rightOp.type)) {
            errMessage.add(rightOp.errMessage("int"));
            return null;
        }
        return new TypeInfo("int", leftOp.row, leftOp.col);
    }

    @Override
    public TypeInfo visit(MinusExpression n, MType argu) {
        TypeInfo leftOp = n.f0.accept(this, argu);
        if (leftOp == null) {
            return null;
        }
        if (!"int".equals(leftOp.type)) {
            errMessage.add(leftOp.errMessage("int"));
            return null;
        }
        TypeInfo rightOp = n.f2.accept(this, argu);
        if (rightOp == null) {
            return null;
        }
        if (!"int".equals(rightOp.type)) {
            errMessage.add(rightOp.errMessage("int"));
            return null;
        }
        return new TypeInfo("int", leftOp.row, leftOp.col);
    }

    @Override
    public TypeInfo visit(TimesExpression n, MType argu) {
        TypeInfo leftOp = n.f0.accept(this, argu);
        if (leftOp == null) {
            return null;
        }
        if (!"int".equals(leftOp.type)) {
            errMessage.add(leftOp.errMessage("int"));
            return null;
        }
        TypeInfo rightOp = n.f2.accept(this, argu);
        if (rightOp == null) {
            return null;
        }
        if (!"int".equals(rightOp.type)) {
            errMessage.add(rightOp.errMessage("int"));
            return null;
        }
        return new TypeInfo("int", leftOp.row, leftOp.col);
    }

    @Override
    public TypeInfo visit(ArrayLookup n, MType argu) {
        TypeInfo array = n.f0.accept(this, argu);
        if (array == null) {
            return null;
        }
        if (!"int []".equals(array.type)) {
            errMessage.add(array.errMessage("int []"));
            return null;
        }
        TypeInfo index = n.f2.accept(this, argu);
        if (index == null) {
            return null;
        }
        if (!"int".equals(index.type)) {
            errMessage.add(index.errMessage("int"));
            return null;
        }
        return new TypeInfo("int", array.row, array.col);
    }

    @Override
    public TypeInfo visit(ArrayLength n, MType argu) {
        TypeInfo array = n.f0.accept(this, argu);
        if (array == null) {
            return null;
        }
        if (!"int []".equals(array.type)) {
            errMessage.add(array.errMessage("int []"));
            return null;
        }
        return new TypeInfo("int", array.row, array.col);
    }

    @Override
    public TypeInfo visit(MessageSend n, MType argu) {
        TypeInfo var = n.f0.accept(this, argu);
        if (var == null) {
            return null;
        }
        MClass cls = argu.getAllClassList().get(var.type);
        if (cls == null) {
            errMessage.add("Type error at line " + var.row + ", column " + var.col + ": not a class.");
            return null;
        }
        MMethod method = cls.getMethod(n.f2.f0.toString());
        if (method == null) {
            errMessage.add("Type error at line " + n.f1.beginLine + ", column" + n.f1.beginColumn
                    + ": class \"" + cls.name + "\" does not have method \"" + n.f2.f0.toString() + "\".");
            return null;
        }
        ArrayList<TypeInfo> argumentList = new ArrayList<>();
        if (n.f4.present()) {
            ExpressionList expressionList = (ExpressionList)n.f4.node;
            argumentList.add(expressionList.f0.accept(this, argu));
            if (expressionList.f1.present()) {
                for (Node node : expressionList.f1.nodes) {
                    argumentList.add(((ExpressionRest)node).f1.accept(this, argu));
                }
            }
        }
        int paramSize = method.paramList.size();
        if (paramSize != argumentList.size()) {
            errMessage.add("Type error at line " + n.f3.beginLine + ", column " + n.f3.beginColumn
                    + ": incorrect argument count.");
            return null;
        }
        for (int i = 0; i < paramSize; ++i) {
            if (argumentList.get(i) == null) {
                return null;
            }
            if (!argu.getAllClassList().convertable(
                    argumentList.get(i).type,
                    method.getVar(method.paramList.get(i)).type)) {
                errMessage.add(argumentList.get(i).errMessage(method.getVar(method.paramList.get(i)).type));
                return null;
            }
        }
        return new TypeInfo(method.returnType, var.row, var.col);
    }

    @Override
    public TypeInfo visit(PrimaryExpression n, MType argu) {
        if (n.f0.which != 3) { // 3 represents Identifier
            return n.f0.choice.accept(this, argu);
        }

        // Identifier
        TypeInfo ret = null;
        Identifier identifier = (Identifier)n.f0.choice;
        String name = identifier.f0.toString();
        MVar var = ((MMethod)argu).getVar(name);
        if(var == null) {
            errMessage.add("Undefined variable \"" + name + "\" at line "
                    + identifier.f0.beginLine + ", column " + identifier.f0.beginColumn);
        } else {
            ret = new TypeInfo(var.type, identifier.f0.beginLine, identifier.f0.beginColumn);
        }
        return ret;
    }

    @Override
    public TypeInfo visit(IntegerLiteral n, MType argu) {
        return new TypeInfo("int", n.f0.beginLine, n.f0.beginColumn);
    }

    @Override
    public TypeInfo visit(TrueLiteral n, MType argu) {
        return new TypeInfo("boolean", n.f0.beginLine, n.f0.beginColumn);
    }

    @Override
    public TypeInfo visit(FalseLiteral n, MType argu) {
        return new TypeInfo("boolean", n.f0.beginLine, n.f0.beginColumn);
    }

    @Override
    public TypeInfo visit(ThisExpression n, MType argu) {
        return new TypeInfo(((MMethod)argu).parent.name, n.f0.beginLine, n.f0.beginColumn);
    }

    @Override
    public TypeInfo visit(ArrayAllocationExpression n, MType argu) {
        TypeInfo index = n.f3.accept(this, argu);
        TypeInfo ret = null;
        if (index != null) {
            if (!"int".equals(index.type)) {
                errMessage.add(index.errMessage("int"));
            } else {
                ret = new TypeInfo("int []", n.f0.beginLine, n.f0.beginColumn);
            }
        }
        return ret;
    }

    @Override
    public TypeInfo visit(AllocationExpression n, MType argu) {
        String type = n.f1.f0.toString();
        TypeInfo ret = null;
        if (!argu.getAllClassList().classList.containsKey(type)){
            errMessage.add("Undefined type \"" + type + "\" at line "
                    + n.f1.f0.beginLine + ", column " + n.f1.f0.beginColumn);
        } else {
            ret = new TypeInfo(type, n.f0.beginLine, n.f0.beginColumn);
        }
        return ret;
    }

    @Override
    public TypeInfo visit(NotExpression n, MType argu) {
        TypeInfo typeInfo = n.f1.accept(this, argu);
        if (typeInfo != null) {
            if (!"boolean".equals(typeInfo.type)) {
                errMessage.add(typeInfo.errMessage("boolean"));
                typeInfo = null;
            } else {
                typeInfo = new TypeInfo("boolean", n.f0.beginLine, n.f0.beginColumn);
            }
        }
        return typeInfo;
    }

    @Override
    public TypeInfo visit(BracketExpression n, MType argu) {
        TypeInfo typeInfo  = n.f1.accept(this, argu);
        if (typeInfo != null) {
            typeInfo = new TypeInfo(typeInfo.type, n.f0.beginLine, n.f0.beginColumn);
        }
        return typeInfo;
    }
}

class TypeInfo {
    public String type;
    public int row, col;

    public TypeInfo (String type, int row, int col){
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public String errMessage(String expectedType) {
        return "Type error at line " + row + ", column " + col + ": expect " + expectedType + ", get " + type + ".";
    }
}