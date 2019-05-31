import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        // FIXME "!(VALID)" makes it fail!
        String s = (args.length > 0 ? args[0] : "!SCB+CBJ+SC!B+!SJ+S!J");

        LogicChain ln = LogicChain.fromString(s);
        System.out.println(ln);
        System.out.println(ln.getTruthTable());

        System.out.println("min terms: " + Arrays.toString(ln.getMinTerms()));
        System.out.println("max terms: " + Arrays.toString(ln.getMaxTerms()));
    }
}
