import java.util.Arrays;
import java.util.regex.*;

public class Parser {

    private int[] indexMapping;

    private LogicNode first; // root node
    private LogicNode[] nodes; // store the variable
    private boolean[] values; // values on which the variable depends

    private char[] names; // names of each variable

    private String query;

    private static char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static Pattern pattern = Pattern.compile("^[\t \\[\\]\\(\\)A-Z0-1\\+\\*&\\|!']+$");

    Parser(String s) {
        query = Parser.clean(s);

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

        first = parse(sequence);
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

    private LogicNode parse(char[] sequence) {
        if (sequence.length < 1)
            throw new RuntimeException("Empty sequence");

        int count = 0;
        // search for OR in max scope
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == '(')
                count++;
            else if (sequence[i] == ')')
                count--;
            else if (count == 0 && sequence[i] == '|')
                return LogicNode.OR(parse(Arrays.copyOfRange(sequence, 0, i - 1 + 1)),
                        parse(Arrays.copyOfRange(sequence, i + 1, sequence.length)));

            if (count < 0)
                throw new RuntimeException("Unbalenced ()");
        }

        if (count != 0)
            throw new RuntimeException("Unbalenced ()");

        // search for AND in max scope
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == '(')
                count++;
            else if (sequence[i] == ')')
                count--;
            else if (count == 0 && sequence[i] == '&')
                return LogicNode.AND(parse(Arrays.copyOfRange(sequence, 0, i - 1 + 1)),
                        parse(Arrays.copyOfRange(sequence, i + 1, sequence.length)));
        }

        // check for NOT
        if (sequence[0] == '!')
            return LogicNode.NOT(parse(Arrays.copyOfRange(sequence, 1, sequence.length)));

        // check for bracket
        if ((sequence[0] == '(') != (sequence[sequence.length - 1] == ')')) // check for () at outer most pos
            throw new RuntimeException("not anticipated");
        if (sequence[0] == '(')
            return parse(Arrays.copyOfRange(sequence, 1, sequence.length - 1));

        // should only have a signle variable now (A-Z0-1)
        if (sequence.length != 1)
            throw new RuntimeException("didn't expect that...");

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

    private static String clean(String s) {
        s = s.toUpperCase();
        if (!pattern.matcher(s).matches())
            throw new RuntimeException("Invalid String format");

        // remove white space
        s = Pattern.compile("[ \t]").matcher(s).replaceAll("");

        // transform ' -> !
        s = Pattern.compile("([A-Z0-1]){1}'").matcher(s).replaceAll("!$1"); // signle value
        s = Pattern.compile("(\\([\\(\\)A-Z0-1\\+\\*&\\|!']+\\))'").matcher(s).replaceAll("!$1"); // expression

        // transform NOT -> !
        s = Pattern.compile("NOT").matcher(s).replaceAll("!");

        // transform + -> |
        s = Pattern.compile("\\+").matcher(s).replaceAll("|");

        // transform OR -> |
        s = Pattern.compile("OR").matcher(s).replaceAll("|");

        // transform * -> &
        s = Pattern.compile("\\*").matcher(s).replaceAll("&");

        // transform AND -> &
        s = Pattern.compile("AND").matcher(s).replaceAll("&");

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
        Matcher m = Pattern.compile("^\\(([^()]*)\\)$").matcher(s);
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
