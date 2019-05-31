import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        String s = (args.length > 0 ? args[0] : "!(VALID)");

        LogicChain ln = LogicChain.fromString(s);
        System.out.println(ln);
        System.out.println(ln.getTruthTable());

        System.out.println("min terms: " + Arrays.toString(ln.getMinTermsIndex()));
        System.out.println(ln.asSumOfMinterms());
        System.out.println("max terms: " + Arrays.toString(ln.getMaxTermsIndex()));
        System.out.println(ln.asProdfMaxterms());
    }
}
