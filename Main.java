import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void doSomething(String s) {
        LogicFunction lf = new LogicChain(s);
        System.out.println(lf);
        System.out.println(lf.getTruthTableRepresentation());

        System.out.println("min terms: " + Arrays.toString(lf.getMinTermsIndex()));
        System.out.println(lf.expressAsSumOfMinterms());
        System.out.println("max terms: " + Arrays.toString(lf.getMaxTermsIndex()));
        System.out.println(lf.expressAsProductOfMaxterms());
    }

    public static void main(String[] args) {
        if (args.length > 0)
            doSomething(args[0]);

        String s;

        Scanner scan = new Scanner(System.in);

        for (;;) {
            System.out.print("Enter a new boolean expression: ");
            s = scan.nextLine();
            if (s.equals("no"))
                break;
            doSomething(s);
        }

        scan.close();
    }
}
