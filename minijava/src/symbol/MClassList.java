package symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MClassList implements MType{
    public HashMap<String, MClass> classList = new HashMap<>();
    public HashMap<String, Integer> classID = new HashMap<>();

    public void insertClass(MClass cls) throws DuplicateDefinitionException {
        String name = cls.name;
        if (classList.containsKey(name)) {
            throw new DuplicateDefinitionException(cls);
        } else {
            classList.put(name, cls);
            classID.put(name, classID.size());
        }
    }

    @Override
    public MClassList getAllClassList() {
        return this;
    }

    @Override
    public void checkUndefinedType(List<String> errMessage){
        for (MClass cls : classList.values()){
            cls.checkUndefinedType(errMessage);
        }
    }

    public void checkCyclicInheritance(List<String> errMessage){
        HashSet<String> visited = new HashSet<>();
        HashSet<String> visiting = new HashSet<>();
        for (String className : classList.keySet()) {
            while (className != null && !visited.contains(className) && !visiting.contains(className)) {
                visiting.add(className);
                className = classList.get(className).baseClass;
            }
            if (className == null || visited.contains(className)) {
                visited.addAll(visiting);
                visiting.clear();
            } else {
                errMessage.add("Cyclic Inheritance involving class \"" + className + "\"");
                visited.addAll(visiting);
                visiting.clear();
            }
        }
    }

    public void checkOverride(List<String> errMessage) {
        for (MClass cls : classList.values()) {
            cls.checkOverride(errMessage);
        }
    }

    public boolean containsKey(String key) {
        if (key.equals("void")) {
            return true;
        }
        if (key.equals("int")) {
            return true;
        }
        if (key.equals("boolean")) {
            return true;
        }
        if (key.equals("int []")) {
            return true;
        }
        if (key.equals("String []")) {
            return true;
        }
        return classList.containsKey(key);
    }

    public MClass get(String key) {
        return classList.get(key);
    }

    public boolean convertable(String source, String target) {
        while(source != null && !source.equals(target) && classList.containsKey(source)) {
            MClass cls = classList.get(source);
            source = cls.baseClass;
        }
        return target.equals(source);
    }
}
