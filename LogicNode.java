@FunctionalInterface
public interface LogicNode {

    public boolean call();

    public static LogicNode Variable(boolean[] a, int i, char name) {
        return new LogicNode() {
            private int index = i;
            private boolean[] arrays = a;

            @Override
            public boolean call() {
                return arrays[index];
            }

            @Override
            public String toString() {
                return "" + name;
            }
        };
    }

    public static LogicNode TRUE() {
        return new LogicNode() {
            @Override
            public boolean call() {
                return true;
            }

            @Override
            public String toString() {
                return "1";
            }
        };
    }

    public static LogicNode FALSE() {
        return new LogicNode() {
            @Override
            public boolean call() {
                return false;
            }

            @Override
            public String toString() {
                return "0";
            }
        };
    }

    public static LogicNode AND(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                return a.call() && b.call();
            }

            @Override
            public String toString() {
                return "( " + a.toString() + " AND " + b.toString() + " )";
            }
        };

    }

    public static LogicNode OR(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                return a.call() || b.call();
            }

            @Override
            public String toString() {
                return "( " + a.toString() + " OR " + b.toString() + " )";
            }
        };
    }

    public static LogicNode NOT(LogicNode a) {
        return new LogicNode() {
            @Override
            public boolean call() {
                return !a.call();
            }

            @Override
            public String toString() {
                return "!" + a.toString();
            }
        };
    }

    public static LogicNode NAND(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                return !(a.call() && b.call());
            }
        };
    }

    public static LogicNode NOR(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                return !(a.call() || b.call());
            }
        };
    }

    public static LogicNode XOR(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                boolean ra = a.call(), rb = b.call();
                return ra && !rb || !ra && rb;
            }
        };
    }

    public static LogicNode NXOR(LogicNode a, LogicNode b) {
        return new LogicNode() {
            @Override
            public boolean call() {
                boolean ra = a.call(), rb = b.call();
                return !ra && !rb || ra && rb;
            }
        };
    }
}
