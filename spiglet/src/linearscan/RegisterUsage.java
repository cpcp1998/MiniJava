package linearscan;

import java.util.HashSet;
import java.util.Set;

public class RegisterUsage {
    public Set<Integer> inputVirtual;
    public Set<Integer> outputVirtual;
    public Set<Integer> tempVirtual;

    public RegisterUsage() {
        inputVirtual = new HashSet<>();
        outputVirtual = new HashSet<>();
        tempVirtual = new HashSet<>();
    }
}
