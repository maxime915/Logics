public class LogicFunctionHandler {

    private boolean[] values;
    private LogicFunction f;

    public LogicFunctionHandler(LogicFunction fun, int nInputs) {
        values = new boolean[nInputs];
        f = fun;
    }

    public boolean get(boolean[] vs) {
        if (vs.length != values.length)
            throw new RuntimeException("not the right number of values");
        return f.call(vs);
    }

    public static String verifyTable(int n, LogicFunction... fs) {
        String res = "Logic function of " + n + " inputs:";
        LogicFunctionHandler lfh = new LogicFunctionHandler(fs[0], n);
        lfh.resetValues();

        do {
            res += "\n";
            for (int i = 0; i < lfh.values.length; i++)
                res += " " + (lfh.values[i] ? "1" : "0");
            res += " |";
            for (int i = 0; i < fs.length; i++)
                res += " " + (fs[i].call(lfh.values) ? "1" : "0");
        } while (lfh.incrementValues());

        return res;
    }

    public String verityTable() {
        String res = "";
        resetValues();

        do {
            for (int i = 0; i < values.length; i++)
                res += " " + (values[i] ? "1" : "0");
            res += " | " + (f.call(values) ? "1\n" : "0\n");
        } while (incrementValues());

        return res;
    }

    private void resetValues() {
        values = new boolean[values.length];
    }

    private boolean incrementValues() {
        boolean reminder = values[values.length - 1];
        boolean temp;
        values[values.length - 1] = !values[values.length - 1];

        for (int k = values.length - 2; k >= 0 && reminder; k--) {
            temp = reminder && values[k];
            if (k == 0 && reminder && values[0])
                return false;
            if (reminder)
                values[k] = !values[k];
            reminder = temp;
        }
        return true;
    }
}
