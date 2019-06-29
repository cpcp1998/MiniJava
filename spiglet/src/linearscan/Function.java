package linearscan;

import java.util.*;

public class Function {
    public String name;
    public int paramCount;
    public List<Statement> stmts;
    public Map<String, Integer> labels;
    public List<Block> blocks;
    public Map<Integer, Interval> virtualInterval;
    public Map<Integer, String> allocation;
    public Map<String, String> globalLabels;
    public int stackSlots;
    public static final int CALLER_SAVE_COUNT = 10 - 2;
    public static final int CALLEE_SAVE_COUNT = 8;
    public static final List<String> CALLER_SAVE = new ArrayList<String>() {{
        for (int i = 0; i < CALLER_SAVE_COUNT; ++i) add("t" + i);
    }};
    public static final List<String> CALLEE_SAVE = new ArrayList<String>() {{
        for (int i = 0; i < CALLEE_SAVE_COUNT; ++i) add("s" + i);
    }};

    public Function(String name, int paramCount){
        stmts = new ArrayList<>();
        labels = new HashMap<>();
        this.name = name;
        this.paramCount = paramCount;
    }

    public void buildBlocks() {
        int size = stmts.size();
        Set<Integer> blockBegin = new HashSet<>();

        for (int i = 0; i < size; ++i) {
            Statement stmt = stmts.get(i);
            if (stmt.isBranch()) {
                blockBegin.add(i+1);
                blockBegin.add(labels.get(stmt.branchTarget()));
            }
        }

        blocks = new ArrayList<>();
        Map<Integer, Block> stmtToBlock = new HashMap<>();
        Block nextBlock = null;
        for (int i = 0; i < size; ++i) {
            if (nextBlock == null) {
                nextBlock = new Block();
                blocks.add(nextBlock);
                stmtToBlock.put(i, nextBlock);
            }
            nextBlock.stmts.add(stmts.get(i));
            if (blockBegin.contains(i+1)) {
                nextBlock = null;
            }
        }

        for (int i = blocks.size() - 2; i >= 0; --i) {
            Block block = blocks.get(i);
            Statement lastStmt = block.stmts.get(block.stmts.size() - 1);
            switch(lastStmt.type()) {
                case CJUMP:
                    block.successors.add(blocks.get(i+1));
                    blocks.get(i+1).predecessors.add(block);
                case JUMP:
                    block.successors.add(stmtToBlock.get(labels.get(lastStmt.branchTarget())));
                    stmtToBlock.get(labels.get(lastStmt.branchTarget())).predecessors.add(block);
                    break;
                default:
                    block.successors.add(blocks.get(i+1));
                    blocks.get(i+1).predecessors.add(block);
            }
        }
    }

    public void sortBlocks() {
        // TODO: reorder blocks (including fix labels)
    }

    public void numberStatements() {
        int ID = 0;
        for (Block block : blocks) {
            for (Statement stmt : block.stmts) {
                stmt.setID(ID);
                ID += 1;
            }
        }
    }

    public void computeGlobalLiveSets() {
        for (Block block : blocks)
            block.computeLocalLiveSets();

        for (Block block : blocks) {
            block.liveOut = new HashSet<>();
            block.liveIn = new HashSet<>();
        }
        boolean changed = true;
        while(changed) {
            changed = false;
            for (int i = blocks.size() - 1; i >= 0; --i) {
                Block block = blocks.get(i);
                Set<Integer> newLiveOut = new HashSet<>();
                Set<Integer> newLiveIn = new HashSet<>();
                for (Block suc : block.successors) {
                    newLiveOut.addAll(suc.liveIn);
                }
                newLiveIn.addAll(block.liveGen);
                for (int register : newLiveOut) {
                    if (!block.liveKill.contains(register))
                        newLiveIn.add(register);
                }
                if (!newLiveIn.equals(block.liveIn)) {
                    changed = true;
                    block.liveIn = newLiveIn;
                }
                if (!newLiveOut.equals(block.liveOut)) {
                    changed = true;
                    block.liveOut = newLiveOut;
                }
            }
        }
    }

    public void buildIntervals() {
        virtualInterval = new HashMap<>();
        for (int i = blocks.size() - 1; i >= 0; --i) {
            Block block = blocks.get(i);
            int blockFrom = block.stmts.get(0).getID();
            int blockTo = block.stmts.get(block.stmts.size()-1).getID() + 1;
            if (blockFrom == 0) blockFrom = -1;

            for (int register : block.liveOut) {
                if (!virtualInterval.containsKey(register)) {
                    virtualInterval.put(register, new Interval());
                }
                virtualInterval.get(register).addRange(blockFrom, blockTo);
            }

            for (int j = block.stmts.size() - 1; j >= 0; --j) {
                Statement statement = block.stmts.get(j);
                RegisterUsage registerUsage = statement.registerUsage();
                for (int register : registerUsage.outputVirtual) {
                    if (virtualInterval.containsKey(register)) {
                        virtualInterval.get(register).left = statement.getID();
                    }
                }
                for (int register : registerUsage.tempVirtual) {
                    if (!virtualInterval.containsKey(register)) virtualInterval.put(register, new Interval());
                    virtualInterval.get(register).addRange(statement.getID(), statement.getID()+1);
                }
                for (int register : registerUsage.inputVirtual) {
                    if (!virtualInterval.containsKey(register)) virtualInterval.put(register, new Interval());
                    virtualInterval.get(register).addRange(blockFrom, statement.getID());
                }
            }
        }
    }

    public void allocate() {
        class Unhandled implements Comparable<Unhandled> {
            public int register;
            private Interval interval;

            public Unhandled(int register, Interval interval) {
                this.register = register;
                this.interval = interval;
            }

            public int compareTo(Unhandled o) {
                if (this.interval.left == o.interval.left)
                    return this.register - o.register;
                return this.interval.left - o.interval.left;
            }
        }

        class Active implements Comparable<Active> {
            public int register;
            private Interval interval;

            public Active(int register, Interval interval) {
                this.register = register;
                this.interval = interval;
            }

            public Active(Unhandled unhandled) {
                this.register = unhandled.register;
                this.interval = unhandled.interval;
            }

            public int compareTo(Active o) {
                if (this.interval.right == o.interval.right)
                    return this.register - o.register;
                return this.interval.right - o.interval.right;
            }
        }



        allocation = new HashMap<>();
        SortedSet<Unhandled> unhandled = new TreeSet<>();
        TreeSet<Active> active = new TreeSet<>();
        for (int register : virtualInterval.keySet()) {
            unhandled.add(new Unhandled(register, virtualInterval.get(register)));
        }
        Set<Integer> callsites = new HashSet<>();
        for (Statement stmt : stmts) {
            if (stmt.type() == StatementType.CALL) callsites.add(stmt.getID());
        }
        Set<String> occupied = new HashSet<>();
        stackSlots = paramCount - 4;
        if (stackSlots < 0) stackSlots = 0;
        while (!unhandled.isEmpty()) {
            Unhandled current = unhandled.first();
            unhandled.remove(current);
            int position = current.interval.left;
            for (Iterator<Active> it = active.iterator(); it.hasNext();) {
                Active i = it.next();
                if (i.interval.right <= position) {
                    it.remove();
                    occupied.remove(allocation.get(i.register));
                }
            }
            List<String> available = new LinkedList<>();
            if (current.interval.contains(callsites)) {
                available.addAll(CALLEE_SAVE);
                available.removeAll(occupied);
            } else {
                available.addAll(CALLER_SAVE);
                available.removeAll(occupied);
                if (available.isEmpty()) {
                    available.addAll(CALLEE_SAVE);
                    available.removeAll(occupied);
                }
            }
            if (!available.isEmpty()) {
                String target = available.get(0);
                occupied.add(target);
                allocation.put(current.register, target);
                active.add(new Active(current));
            } else {
                Active spill = null;
                if (current.interval.contains(callsites)) {
                    for (Iterator<Active> it = active.descendingIterator(); it.hasNext(); ) {
                        spill = it.next();
                        if (CALLEE_SAVE.contains(allocation.get(spill.register))) break;
                    }
                } else {
                    spill = active.last();
                }
                if (spill.interval.right > current.interval.right) {
                    allocation.put(current.register, allocation.get(spill.register));
                    if (spill.register < paramCount && spill.register >= 4) {
                        allocation.put(spill.register, "/" + (spill.register - 4));
                    } else {
                        allocation.put(spill.register, "/" + stackSlots++);
                    }
                    active.remove(spill);
                    active.add(new Active(current));
                } else {
                    if (current.register < paramCount && current.register >= 4) {
                        allocation.put(current.register, "/" + (current.register - 4));
                    } else {
                        allocation.put(current.register, "/" + stackSlots++);
                    }
                }
            }
        }
    }

    public int generateLabel(int start) {
        globalLabels = new HashMap<>();
        for (String label : labels.keySet()) {
            globalLabels.put(label, "L" + start++);
        }
        return start;
    }

    public String generate() {
        GenerateEnv env = new GenerateEnv(allocation, globalLabels);
        List<String> savedRegisters = new ArrayList<>();
        for (String register : allocation.values()) {
            if (CALLEE_SAVE.contains(register) && !savedRegisters.contains(register))
                savedRegisters.add(register);
        }
        env.append(name).append(" [").append(paramCount).append("][");
        env.append(stackSlots + savedRegisters.size()).append("]");
        int maxParams = 0;
        for (Statement stmt : stmts) {
            int paramCount = stmt.paramCount();
            if (paramCount > maxParams)
                maxParams = paramCount;
        }
        env.append('[').append(maxParams).append("]\n");

        for (int i = 0; i < savedRegisters.size(); ++i) {
            env.indent().append("ASTORE SPILLEDARG ").append(stackSlots + i);
            env.append(" ").append(savedRegisters.get(i)).append('\n');
        }

        for (int i = 0; i < paramCount; ++i) {
            String target = allocation.get(i);
            if (target == null) continue;
            if (i < 4) {
                if (target.charAt(0) == '/') {
                    env.indent().append("ASTORE SPILLEDARG ").append(target.substring(1));

                } else {
                    env.indent().append("MOVE ").append(target);
                }
                env.append(" a").append(i).append('\n');
            } else if (target.charAt(0) != '/'){
                env.indent().append("ALOAD ").append(target).append(" SPILLEDARG ");
                env.append(i - 4).append('\n');
            }
        }

        Map<Integer, String> lineToLabel = new HashMap<>();
        for (String label : labels.keySet())
            lineToLabel.put(labels.get(label), label);

        int line = 0;
        for (Statement stmt : stmts) {
            if (lineToLabel.keySet().contains(line)) {
                env.append(globalLabels.get(lineToLabel.get(line)));
            }
            try {
                stmt.generate(env);
            } catch (Exception e) {
                System.out.println(name + " " + line);
                throw e;
            }

            line++;
        }

        for (int i = 0; i < savedRegisters.size(); ++i) {
            env.indent().append("ALOAD ").append(savedRegisters.get(i));
            env.append(" SPILLEDARG ").append(stackSlots + i).append('\n');
        }

        env.append("END\n");

        return env.toString();
    }
}
