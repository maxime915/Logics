import java.util.Arrays;

public class LogicFunctionHandler implements LogicFunction {

    private boolean[] values;
    private char[] names;
    private CallableLogic f;

    public LogicFunctionHandler(CallableLogic fun, int nInputs) {
        values = new boolean[nInputs];
        f = fun;
        names = null;
    }

    public LogicFunctionHandler(CallableLogic fun, int nInputs, char... names) {
        this(fun, nInputs);
        this.setNames(names);
    }

    public boolean call(boolean... vs) {
        if (vs.length != values.length)
            throw new RuntimeException("not the right number of values");
        return f.call(vs);
    }

    public String[] getNames() {
        if (names == null)
            return null;
        String[] nms = new String[names.length];
        for (int i = 0; i < nms.length; i++)
            nms[i] = String.valueOf(names[i]);

        return nms;
    }

    public void setNames(char... names) {
        if (names.length != values.length)
            throw new RuntimeException("not the right number of names");
        if (this.names == null)
            this.names = new char[names.length];
        for (int i = 0; i < names.length; i++)
            this.names[i] = names[i];
    }

    public static String verifyTable(int n, CallableLogic... fs) {
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

    public static String getTruthTableRepresentation(int n, CallableLogic... fs) {
        if (fs.length < 1)
            throw new RuntimeException("Empty list");
        String res = fs.length + " logic functions of " + n + " inputs\n";
        LogicFunctionHandler lfh = new LogicFunctionHandler(fs[0], n);

        if (fs.length == 1)
            return res + lfh.getTruthTableRepresentation();

        lfh.resetValues();

        res += "•-";
        for (int i = 0; i < lfh.names.length; i++)
            res += "--";
        res += "----•\n"; // FIXME: not the right amount of ---

        if (lfh.names != null) {
            res += "| ";
            for (int i = 0; i < lfh.names.length; i++)
                res += lfh.names[i] + " ";
            res += "| ";
            for (int i = 1; i <= fs.length; i++)
                res += i + " ";
            res += "|\n";

            res += "|-";
            for (int i = 0; i < lfh.names.length; i++)
                res += "--";
            res += "+---|";
        }

        do {
            // entry
            res += "\n| ";
            for (int i = 0; i < lfh.values.length; i++)
                res += (lfh.values[i] ? "1 " : "0 ");

            // outs
            res += "| ";
            for (int i = 0; i < fs.length; i++)
                res += (fs[i].call(lfh.values) ? "1 " : "0 ");
            res += "|\n";

        } while (lfh.incrementValues());

        res += "\n•-";
        for (int i = 0; i < lfh.names.length; i++)
            res += "--";
        res += "----•"; // FIXME: not the right amount of ---

        return res;
    }

    public String getTruthTableRepresentation() {
        String res = "";

        resetValues();

        res += "•-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "----•\n";

        if (names != null) {
            res += "| ";
            for (int i = 0; i < names.length; i++)
                res += names[i] + " ";
            res += "| F |\n";

            res += "|-";
            for (int i = 0; i < names.length; i++)
                res += "--";
            res += "+---|";
        }

        do {
            res += "\n| ";
            for (int i = 0; i < values.length; i++)
                res += (values[i] ? "1 " : "0 ");
            res += "| " + (call(values) ? "1" : "0") + " |";
        } while (incrementValues());

        res += "\n•-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "----•";

        return res;
    }

    public boolean[] getTruthTable() {
        boolean[] table = new boolean[1 << values.length];

        int count = 0;
        do {
            table[count++] = call(values);
        } while (incrementValues());

        return table;
    }

    public String expressAsSumOfMinterms() {
        String s = "";

        resetValues();
        do {
            if (call(values)) {
                if (s != "")
                    s += "+";
                s += "(";
                for (int i = 0; i < values.length; i++) {
                    if (i != 0)
                        s += "*";
                    if (!values[i])
                        s += "!";
                    s += "" + (names != null ? names[i] : i);
                }
                s += ")";
            }
        } while (incrementValues());

        return s;
    }

    public int[] getMinTermsIndex() {
        int[] mins = new int[1 << values.length];
        int counter = 0;
        int v = 0;

        resetValues();
        do {
            if (call(values))
                mins[counter++] = v;
            v++;
        } while (incrementValues());

        return Arrays.copyOf(mins, counter);
    }

    public String expressAsProductOfMaxterms() {
        String s = "";

        resetValues();
        do {
            if (!call(values)) {
                if (s != "")
                    s += "*";
                s += "(";
                for (int i = 0; i < values.length; i++) {
                    if (i != 0)
                        s += "+";
                    if (values[i])
                        s += "!";
                    s += "" + (names != null ? names[i] : i);
                }
                s += ")";
            }
        } while (incrementValues());

        return s;
    }

    public int[] getMaxTermsIndex() {
        int[] mins = new int[1 << values.length];
        int counter = 0;
        int v = 0;

        resetValues();
        do {
            if (!call(values))
                mins[counter++] = v;
            v++;
        } while (incrementValues());

        return Arrays.copyOf(mins, counter);
    }

    private void resetValues() {
        for (int i = 0; i < values.length; i++)
            values[i] = false;
    }

    private boolean incrementValues() {
        if (values.length < 1)
            return false;

        if (values.length == 1) {
            if (values[0])
                return false;

            values[0] = !values[0];
            return true;
        }

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
