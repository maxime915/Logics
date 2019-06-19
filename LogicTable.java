import java.util.Arrays;

public class LogicTable implements LogicFunction {

    private boolean[] table;
    private char[] names;
    private String query;
    private String logicDescription;

    public LogicTable(String description) {
        Builder b;
        // build the logic
        try {
            b = new Builder(description);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to parse string");
        }

        // get references

        // temp references
        boolean[] values = b.getValues();
        LogicNode first = b.getFirst();

        // members
        names = b.getNames();
        query = b.getCleanedQuery();
        logicDescription = first.toString();
        table = new boolean[1 << values.length]; // 2 ** l

        for (int i = 0; i < table.length; i++) {
            for (int k = 0; k < values.length; k++)
                values[k] = (i / (int) Math.pow(2, k)) % 2 == 1;
            table[i] = first.call();
        }
    }

    public String[] getNames() {
        String[] nms = new String[names.length];
        for (int i = 0; i < nms.length; i++)
            nms[i] = String.valueOf(names[i]);

        return nms;
    }

    public boolean call(boolean... vs) {
        if (vs.length != names.length)
            throw new RuntimeException("not the right number of values");

        int count = 0;
        for (int k = 0; k < vs.length; k++) {
            if (!vs[k])
                continue;
            count += 1 << k;
        }

        return table[count];
    }

    public String getTruthTableRepresentation() {
        String res = "";

        res += "•-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "----•\n";

        res += "| ";
        for (int i = 0; i < names.length; i++)
            res += names[i] + " ";
        res += "| F |\n";

        res += "|-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "+---|";

        for (int i = 0; i < table.length; i++) {
            res += "\n| ";
            for (int k = 0; k < names.length; k++)
                res += ((i >> k) % 2 == 1 ? "1 " : "0 ");

            res += "| " + (table[i] ? "1" : "0") + " |";
        }

        res += "\n•-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "----•";

        return res;
    }

    public boolean[] getTruthTable() {
        return (boolean[]) table.clone();
    }

    public String expressAsSumOfMinterms() {
        String s = "";

        for (int i = 0; i < table.length; i++) {
            if (table[i]) { // equiv to call(values)
                if (s != "")
                    s += "+";
                s += "(";
                for (int j = 0; j < names.length; j++) {
                    if (j != 0)
                        s += "*";
                    if ((i >> j) % 2 != 1) // if (!(values[j]))
                        s += "!";
                    s += "" + names[j];
                }
                s += ")";
            }
        }

        return s;
    }

    public int[] getMinTermsIndex() {
        int[] mins = new int[table.length];
        int counter = 0;
        int v = 0;

        for (int i = 0; i < table.length; i++) {
            if (table[i])
                mins[counter++] = v;
            v++;
        }

        return Arrays.copyOf(mins, counter);
    }

    public String expressAsProductOfMaxterms() {
        String s = "";

        for (int i = 0; i < table.length; i++) {
            if (!table[i]) {
                if (s != "")
                    s += "*";
                s += "(";
                for (int j = 0; j < names.length; j++) {
                    if (j != 0)
                        s += "+";
                    if ((i >> j) % 2 != 1)
                        s += "!";
                    s += "" + names[j];
                }
                s += ")";
            }
        }

        return s;
    }

    public int[] getMaxTermsIndex() {
        int[] mins = new int[table.length];
        int counter = 0;
        int v = 0;

        for (int i = 0; i < table.length; i++) {
            if (!table[i])
                mins[counter++] = v;
            v++;
        }

        return Arrays.copyOf(mins, counter);
    }

    @Override
    public String toString() {
        String s = "Logic function with variables " + Arrays.toString(names);
        s += "\n\torinigal query : [ " + query + " ]";
        return s + "\n\tand body : " + logicDescription;
    }
}