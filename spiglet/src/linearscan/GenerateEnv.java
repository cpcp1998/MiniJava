package linearscan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenerateEnv {
    private StringBuilder script;
    public Map<Integer, String> allocation;
    public Map<String, String> globalLabels;
    public LinkedList<String> availableTemp;

    public GenerateEnv(Map<Integer, String> allocation, Map<String, String> globalLabels) {
        script = new StringBuilder();
        this.allocation = allocation;
        this.globalLabels = globalLabels;
        this.availableTemp = new LinkedList<>();
        this.resetTemp();
    }

    public void resetTemp() {
        availableTemp.clear();
        availableTemp.add("t8");
        availableTemp.add("t9");
    }

    public String nextTemp() {
        if(availableTemp.isEmpty()) return null;
        String ret = availableTemp.get(0);
        availableTemp.remove(0);
        return ret;
    }

    public String toString() {
        return script.toString();
    }

    public GenerateEnv append(boolean s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(char s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(char[] s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(double s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(float s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(int s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(long s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(Object s) {
        script.append(s);
        return this;
    }

    public GenerateEnv append(String s) {
        script.append(s);
        return this;
    }

    public GenerateEnv indent() {
        script.append('\t');
        return this;
    }
}
