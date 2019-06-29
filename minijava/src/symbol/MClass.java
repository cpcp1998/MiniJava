package symbol;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class MClass extends MIdentifier implements VarContainer {
    public HashMap<String, MMethod> methodList = new HashMap<>();
    public HashMap<String, MVar> fieldList = new HashMap<>();
    public HashMap<String, Integer> methodID = null;
    public HashMap<String, Integer> fieldID = null;
    public int methodCount = 0;
    public int fieldCount = 0;
    public String baseClass;

    public MClass(MClassList allClassList, String name, int row, int col, String baseClass) {
        super(allClassList, name, "class", row, col);
        this.baseClass = baseClass;
    }

    public void insertMethod(MMethod method) throws DuplicateDefinitionException {
        String name = method.name;
        if (methodList.containsKey(name)) {
            throw new DuplicateDefinitionException(method);
        } else {
            methodList.put(name, method);
        }
    }

    public MMethod getMethod(String name) {
        MMethod method = methodList.get(name);
        if (method == null && baseClass != null) {
            method = getAllClassList().get(baseClass).getMethod(name);
        }
        return method;
    }

    public MVar getVar(String name) {
        MVar field = fieldList.get(name);
        if (field == null && baseClass != null) {
            field = getAllClassList().get(baseClass).getVar(name);
        }
        return field;
    }

    @Override
    public void insertVar(MVar var) throws DuplicateDefinitionException {
        String name = var.name;
        if (fieldList.containsKey(name)) {
            throw new DuplicateDefinitionException(var);
        } else {
            fieldList.put(name, var);
        }
    }

    @Override
    public void checkUndefinedType(List<String> errMessage) {
        if (baseClass != null && !getAllClassList().classList.containsKey(baseClass)) {
            errMessage.add("Undefined base type \"" + baseClass + "\" of class at line " + row + ", column " + col);
        }
        for (MMethod method : methodList.values()) {
            method.checkUndefinedType(errMessage);
        }
        for (MVar var : fieldList.values()) {
            var.checkUndefinedType(errMessage);
        }
    }

    public void checkOverride(List<String> errMessage) {
        for (MMethod method : methodList.values()) {
            MMethod overrideMethod = null;
            if (baseClass != null) {
                overrideMethod = getAllClassList().get(baseClass).getMethod(method.name);
            }
            if (overrideMethod != null) {
                if (!method.isParamListCompatible(overrideMethod)) {
                    errMessage.add("Incompatible override of method \"" + method.name
                            + "\" at line " + method.row + ", column " + method.col);
                }
            }
        }
    }

    public void assignFieldID() {
        if (fieldID != null) return;
        fieldID = new HashMap<>();
        if (baseClass != null) {
            getAllClassList().get(baseClass).assignFieldID();
            fieldCount = getAllClassList().get(baseClass).fieldCount;
        }
        for (String var : fieldList.keySet()) {
            fieldID.put(var, fieldCount);
            ++fieldCount;
        }
    }

    public Integer getFieldID(String name) {
        Integer id = fieldID.get(name);
        if (id == null && baseClass != null) {
            id = getAllClassList().get(baseClass).getFieldID(name);
        }
        return id;
    }

    public void assignMethodID() {
        if (methodID != null) return;
        methodID = new HashMap<>();
        if (baseClass != null) {
            getAllClassList().get(baseClass).assignMethodID();
            methodCount = getAllClassList().get(baseClass).methodCount;
        }
        for (String method : methodList.keySet()) {
            if (baseClass == null || getAllClassList().get(baseClass).getMethodID(method) == null) {
                methodID.put(method, methodCount);
                ++methodCount;
            }
        }
    }

    public Integer getMethodID(String name) {
        Integer id = methodID.get(name);
        if (id == null && baseClass != null) {
            id = getAllClassList().get(baseClass).getMethodID(name);
        }
        return id;
    }

    public Set<String> getAllMethods() {
        HashSet<String> methods = new HashSet<>(methodList.keySet());
        if (baseClass != null) {
            methods.addAll(getAllClassList().get(baseClass).getAllMethods());
        }
        return methods;
    }
}
