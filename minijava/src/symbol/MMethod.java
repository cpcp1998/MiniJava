package symbol;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class MMethod extends MIdentifier implements VarContainer{
    public String returnType;
    public MClass parent;
    public HashMap<String, MVar> varList = new HashMap<>();
    public HashMap<String, Integer> varID = new HashMap<>();
    public ArrayList<String> paramList = new ArrayList<>();

    public MMethod(MClassList allClassList, String name, int row, int col, String returnType, MClass parent) {
        super(allClassList, name, "method", row, col);
        this.returnType = returnType;
        this.parent = parent;
    }

    public void insertVar(MVar var) throws DuplicateDefinitionException {
        String name = var.name;
        if (varList.containsKey(name)) {
            throw new DuplicateDefinitionException(var);
        } else {
            varList.put(name, var);
            varID.put(name, varID.size());
        }
    }

    public void insertParam(MVar var) throws DuplicateDefinitionException {
        insertVar(var);
        paramList.add(var.name);
    }

    @Override
    public void checkUndefinedType(List<String> errMessage) {
        if (!getAllClassList().containsKey(returnType)) {
            errMessage.add("Undefined return type \"" + returnType + "\" of method at line " + row + ", column " + col);
        }
        for (MVar var : varList.values()) {
            var.checkUndefinedType(errMessage);
        }
    }

    public boolean isParamListCompatible(MMethod m) {
        if (!returnType.equals(m.returnType)) {
            return false;
        }
        int paramListSize = paramList.size();
        if (paramListSize != m.paramList.size()) {
            return false;
        }
        for (int i = 0; i < paramListSize; ++i) {
            if (!varList.get(paramList.get(i)).type.equals(m.varList.get(m.paramList.get(i)).type)) {
                return false;
            }
        }
        return true;
    }

    public MMethod getMethod(String name) {
        return parent.getMethod(name);
    }

    public MVar getVar(String name) {
        MVar var = varList.get(name);
        if (var == null) {
            var = parent.getVar(name);
        }
        return var;
    }
}
