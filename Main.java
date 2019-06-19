import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        String s = (args.length > 0 ? args[0] : "!(VALID)");

        LogicFunction lf = new LogicChain(s);
        System.out.println(lf);
        System.out.println(lf.getTruthTableRepresentation());

        System.out.println("min terms: " + Arrays.toString(lf.getMinTermsIndex()));
        System.out.println(lf.expressAsSumOfMinterms());
        System.out.println("max terms: " + Arrays.toString(lf.getMaxTermsIndex()));
        System.out.println(lf.expressAsProductOfMaxterms());
    }
}
