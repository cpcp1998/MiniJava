package linearscan;

import syntaxtree.IntegerLiteral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Block {
    public List<Statement> stmts;
    public List<Block> successors;
    public List<Block> predecessors;
    public Set<Integer> liveGen;
    public Set<Integer> liveKill;
    public Set<Integer> liveOut;
    public Set<Integer> liveIn;

    public Block() {
        stmts = new ArrayList<>();
        successors = new ArrayList<>();
        predecessors = new ArrayList<>();
    }

    public void computeLocalLiveSets() {
        liveGen = new HashSet<>();
        liveKill = new HashSet<>();
        for (Statement stmt : stmts) {
            RegisterUsage registerUsage = stmt.registerUsage();
            for (Integer register : registerUsage.inputVirtual) {
                if (!liveKill.contains(register)) liveGen.add(register);
            }
            liveKill.addAll(registerUsage.tempVirtual);
            liveKill.addAll(registerUsage.outputVirtual);
        }
    }
}
