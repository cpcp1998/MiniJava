package linearscan;

import syntaxtree.*;

import java.util.List;

public class LinearScan {
    private Goal root;

    public LinearScan(Node root) {
        this.root = (Goal)root;
    }

    public String run() {
        BuildFunctionVisitor buildFunctionVisitor = new BuildFunctionVisitor();
        List<Function> functions = root.accept(buildFunctionVisitor, null);
        int label = 0;
        for (Function function : functions) {
            function.buildBlocks();
            function.sortBlocks();
            function.numberStatements();
            function.computeGlobalLiveSets();
            function.buildIntervals();
            function.allocate();
            label = function.generateLabel(label);
        }

        StringBuilder ret = new StringBuilder();
        for (Function function : functions) {
            ret.append(function.generate());
        }
        return ret.toString();
    }
}
