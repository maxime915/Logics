import java.util.Arrays;
import java.util.Random;
import java.util.regex.*;

public class Builder {

    private int[] indexMapping;

    private LogicNode first; // root node
    private LogicNode[] nodes; // store the variable
    private boolean[] values; // values on which the variable depends

    private char[] names; // names of each variable

    private String query;

    private static char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static Pattern pattern = Pattern.compile("^[\t \\[\\]\\(\\)A-Z0-1\\+\\*&\\|!']+$");

    Builder(String s) {
        query = Builder.clean(s);

        char[] sequence = query.toCharArray();

        int count = 0;
        for (int i = 0; i < sequence.length; i++)
            if (sequence[i] == '(')
                count++;
            else if (sequence[i] == ')')
                count--;
        if (count != 0)
            throw new RuntimeException("Unbalenced ()");

        indexMapping = new int[letters.length];
        names = new char[letters.length];

        for (int i = 0; i < letters.length; i++)
            indexMapping[i] = -1;

        int numberOfVariable = 0;

        for (int j = 0; j < letters.length; j++) {
            for (int i = 0; i < sequence.length; i++) {
                if (sequence[i] == letters[j] && indexMapping[(int) letters[j] - (int) letters[0]] == -1) {
                    indexMapping[(int) letters[j] - (int) letters[0]] = numberOfVariable++;
                    names[numberOfVariable - 1] = letters[j];
                }
            }
        }

        // trim down end of array
        names = Arrays.copyOf(names, numberOfVariable);

        values = new boolean[numberOfVariable];
        nodes = new LogicNode[numberOfVariable];

        for (int i = 0; i < numberOfVariable; i++)
            nodes[i] = LogicNode.Variable(values, i, names[i]);

        first = buildRec(sequence);
    }

    // getters

    LogicNode getFirst() {
        return first;
    }

    char[] getNames() {
        return names;
    }

    boolean[] getValues() {
        return values;
    }

    String getCleanedQuery() {
        return query;
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
                return nodes[indexMapping[(int) sequence[0] - (int) letters[0]]];
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

    private static String clean(String s) {
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
