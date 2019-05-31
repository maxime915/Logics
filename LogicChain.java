import java.util.Arrays;
import java.util.Random;
import java.util.regex.*;

public class LogicChain {

    private LogicNode first;
    private boolean[] values;
    private char[] names;

    private LogicChain() {
    }

    public static LogicChain fromString(String description) {
        String s = Builder.formatCheck(description);

        Builder b = new Builder(s);
        int numberOfVariable = b.numberOfVariable;

        LogicChain lc = new LogicChain();
        lc.values = new boolean[numberOfVariable];
        lc.names = b.names;
        LogicNode[] nodes = new LogicNode[numberOfVariable];

        for (int i = 0; i < numberOfVariable; i++)
            nodes[i] = LogicNode.Variable(lc.values, i, b.names[i]);

        lc.first = b.build(nodes);

        return lc;
    }

    public boolean get(boolean... vs) {
        if (vs.length != values.length)
            throw new RuntimeException("not the right number of values");

        for (int i = 0; i < values.length; i++)
            values[i] = vs[i];

        return first.call();
    }

    public String getTruthTable() {
        String res = "";

        resetValues();

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

        do {
            res += "\n| ";
            for (int i = 0; i < values.length; i++)
                res += (values[i] ? "1 " : "0 ");
            res += "| " + (get(values) ? "1" : "0") + " |";
        } while (incrementValues());

        res += "\n•-";
        for (int i = 0; i < names.length; i++)
            res += "--";
        res += "----•";

        return res;
    }

    public int[] getMinTerms() {
        int[] mins = new int[(int) Math.pow(2, values.length)];
        int counter = 0;
        int v = 0;

        resetValues();
        do {
            if (get(values))
                mins[counter++] = v;
            v++;
        } while (incrementValues());

        return Arrays.copyOf(mins, counter);
    }

    public int[] getMaxTerms() {
        int[] mins = new int[(int) Math.pow(2, values.length)];
        int counter = 0;
        int v = 0;

        resetValues();
        do {
            if (!get(values))
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

    @Override
    public String toString() {
        return "Logic function with variables " + Arrays.toString(names) + " and body : " + first.toString();
    }

}

class Builder {

    public int first;
    public int[] indexMapping;

    public LogicNode[] nodes;
    public char[] names;

    public int numberOfVariable;
    public char[] initialSequence;

    public static char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static Pattern pattern = Pattern.compile("^[\t \\[\\]\\(\\)A-Z0-1\\+\\*&\\|!']+$");

    Builder(String s) {
        initialSequence = s.toCharArray();

        int count = 0;
        for (int i = 0; i < initialSequence.length; i++)
            if (initialSequence[i] == '(')
                count++;
            else if (initialSequence[i] == ')')
                count--;
        if (count != 0)
            throw new RuntimeException("Unbalenced ()");

        indexMapping = new int[letters.length];
        names = new char[letters.length];
        first = (int) letters[0]; // should be 65

        for (int i = 0; i < letters.length; i++)
            indexMapping[i] = -1;

        numberOfVariable = 0;

        for (int j = 0; j < letters.length; j++) {
            for (int i = 0; i < initialSequence.length; i++) {
                if (initialSequence[i] == letters[j] && indexMapping[(int) letters[j] - first] == -1) {
                    indexMapping[(int) letters[j] - first] = numberOfVariable++;
                    names[numberOfVariable - 1] = letters[j];
                }
            }
        }

        names = Arrays.copyOf(names, numberOfVariable);
    }

    LogicNode build(LogicNode[] nodes) {
        this.nodes = nodes;
        return buildRec(initialSequence);
    }

    private LogicNode buildRec(char[] sequence) {
        boolean noBracket = true;
        for (int i = 0; i < sequence.length && noBracket; i++)
            if (sequence[i] == '(')
                noBracket = false;

        if (noBracket)
            return buildRecBracketLess(sequence);
        else if (new Random().nextInt(3) > 4) // always true but the compiler doesn't know it
            throw new RuntimeException("/!\\ rest is not implemented ");

        int count = 0;
        int i0 = -1;
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == '(') {
                if (count == 0)
                    i0 = i;
                count++;
            } else if (sequence[i] == ')') {
                count--;

                if (count != 0)
                    continue;

                // end of bracket !

                LogicNode n = buildRec(Arrays.copyOfRange(sequence, i0 + 1, i - 1 + 1));

                if (i0 > 0 && sequence[i0 - 1] == '!') {
                    n = LogicNode.NOT(n);
                    i0--;
                }

                // get first part if &
                if (i0 > 0 && sequence[i0 - 1] == '&') {
                    int tempI = i0 - 1;
                    while (tempI > 0 && sequence[tempI] != '|')
                        tempI--;
                    if (tempI == 0) {
                        n = LogicNode.AND(buildRec(Arrays.copyOfRange(sequence, 0, i0 - 1 - 1 + 1)), n);
                        // let i0 such that [i0 - 1] == '&'
                    } else {
                        LogicNode mid = buildRec(Arrays.copyOfRange(sequence, tempI + 1, i0 - 1 - 1 + 1));
                        n = LogicNode.AND(mid, n);
                        i0 = tempI + 1; // position right after |
                    }
                } else if (i0 > 0 && sequence[i0 - 1] != '|')
                    throw new RuntimeException(
                            "unexpected sequence at position " + (i0 - 1) + " in sequence" + Arrays.toString(sequence));

                if (i + 1 >= sequence.length) {
                    if (i0 > 0 && sequence[i0 - 1] == '|')
                        n = LogicNode.OR(buildRec(Arrays.copyOfRange(sequence, 0, i0 - 1 + 1)), n);
                    return n;
                }

                if (sequence[i + 1] == '|') {
                    if (i0 > 0 && sequence[i0 - 1] == '|') {
                        LogicNode pre = buildRec(Arrays.copyOfRange(sequence, 0, i0 - 1 - 1 + 1));
                        n = LogicNode.OR(pre, n);
                    }
                    return LogicNode.OR(n, buildRec(Arrays.copyOfRange(sequence, i + 2, sequence.length - 1 + 1)));
                } else if (sequence[i + 1] != '&')
                    throw new RuntimeException(
                            "unexpected sequence at position " + (i0 - 1) + " in sequence" + Arrays.toString(sequence));

                // sequence[i+1] == '&'
                int tempI = i + 1;
                while (tempI < sequence.length && sequence[tempI] != '|')
                    tempI++;
                if (tempI == sequence.length) {
                    n = LogicNode.AND(n, buildRec(Arrays.copyOfRange(sequence, i + 1 + 1, sequence.length - 1 + 1)));
                } else {
                    LogicNode mid = buildRec(Arrays.copyOfRange(sequence, i + 1 + 1, tempI - 1 + 1));
                    LogicNode post = buildRec(Arrays.copyOfRange(sequence, tempI + 1, sequence.length - 1));
                    n = LogicNode.OR(LogicNode.AND(n, mid), post);
                }

                if (i0 > 0 && sequence[i0 - 1] == '|') {
                    LogicNode pre = buildRec(Arrays.copyOfRange(sequence, 0, i0 - 1 - 1 + 1));
                    n = LogicNode.OR(pre, n);
                }

                return n;
            }
        }
        return null;
    }

    private LogicNode buildRecBracketLess(char[] sequence) {
        // System.out.println("building for : " + new String(sequence));

        // only : A-Z0-1!|&

        if (sequence.length == 0)
            throw new RuntimeException("null sequence");

        if (sequence.length == 1) {
            if (sequence[0] == '0')
                return LogicNode.FALSE();
            if (sequence[0] == '1')
                return LogicNode.TRUE();
            try {
                return nodes[indexMapping[(int) sequence[0] - first]];
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                throw new RuntimeException("Unrocognized token: " + sequence[0]);
            }
        }

        // find first |
        for (int i = 0; i < sequence.length; i++)
            if (sequence[i] == '|')
                return LogicNode.OR(buildRecBracketLess(Arrays.copyOfRange(sequence, 0, i - 1 + 1)),
                        buildRecBracketLess(Arrays.copyOfRange(sequence, i + 1, sequence.length - 1 + 1)));

        // only & left
        for (int i = 0; i < sequence.length; i++)
            if (sequence[i] == '&')
                return LogicNode.AND(buildRecBracketLess(Arrays.copyOfRange(sequence, 0, i - 1 + 1)),
                        buildRecBracketLess(Arrays.copyOfRange(sequence, i + 1, sequence.length - 1 + 1)));

        if (sequence.length != 2)
            throw new RuntimeException("Houston, we have a probleme up here...");

        if (sequence[0] != '!')
            throw new RuntimeException("Houston, we have a probleme up here (2)...");

        return LogicNode.NOT(buildRecBracketLess(Arrays.copyOfRange(sequence, 1, 2)));
    }

    static String formatCheck(String s) {
        s = s.toUpperCase();
        if (!pattern.matcher(s).matches())
            throw new RuntimeException("Invalid String format");

        // remove white space
        s = Pattern.compile("[ \t]").matcher(s).replaceAll("");

        // transform ' -> !
        s = Pattern.compile("([A-Z0-1]){1}'").matcher(s).replaceAll("!$1"); // signle value
        s = Pattern.compile("(\\([\\(\\)A-Z0-1\\+\\*&\\|!']+\\))'").matcher(s).replaceAll("!$1"); // expression

        // transform + -> |
        s = Pattern.compile("\\+").matcher(s).replaceAll("|");

        // transform * -> &
        s = Pattern.compile("\\*").matcher(s).replaceAll("&");

        // transform "!!" -> ""
        s = Pattern.compile("!!").matcher(s).replaceAll("");

        // transform [] -> ()
        s = Pattern.compile("\\[").matcher(s).replaceAll("(");
        s = Pattern.compile("\\]").matcher(s).replaceAll(")");

        // check for bad syntax

        // consecutive operators
        if (Pattern.compile("^.*[\\|]{2,}.*$").matcher(s).matches())
            throw new RuntimeException("two or more consecutive binary operators found");

        // remove some un-necessary brackets

        // global brackets
        Matcher m = Pattern.compile("^\\((.*)\\)$").matcher(s);
        while (m.matches()) {
            s = m.replaceAll("$1");
            m = Pattern.compile("^\\((.*)\\)$").matcher(s);
        }

        // repetitive brackets with no other bracket inside
        s = Pattern.compile("[\\(]+([^\\(\\)]*)[\\)]+").matcher(s).replaceAll("($1)");

        // remove empty ()
        s = Pattern.compile("\\(\\)").matcher(s).replaceAll("");

        // consecutive variables

        if (Pattern.compile("^.*[A-Z0-1\\)]{1}[!]?[A-Z0-1\\(]{1}.*$").matcher(s).matches()) {
            // AB -> twice bc othewhise ABCD -> A&BC&D
            s = Pattern.compile("([A-Z0-1]{1})([!]?[A-Z0-1]{1})").matcher(s).replaceAll("$1&$2");
            s = Pattern.compile("([A-Z0-1]{1})([!]?[A-Z0-1]{1})").matcher(s).replaceAll("$1&$2");
            // A(
            s = Pattern.compile("([A-Z0-1]{1})([!]?[\\(]{1})").matcher(s).replaceAll("$1&$2");
            // )A
            s = Pattern.compile("([\\)]{1})([!]?[A-Z0-1]{1})").matcher(s).replaceAll("$1&$2");

            // System.err.println("WARNING - 2 consecutive variables / litterals found,
            // implying & operation");
        }

        return s;
    }
}
