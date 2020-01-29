import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static Scanner scan = new Scanner(System.in);

    public static void describeSentence(String s) {
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
            describeSentence(args[0]);

        String s;

        while (true) {
            System.out.print("Enter a new boolean expression: ");
            s = scan.nextLine();
            if (shouldQuit(s)) {
                System.out.print("Would you like to quit ? [y/*] : ");
                String line = scan.nextLine();
                if (line.equals("y"))
                    break;
                // otherwise do description
            }
            describeSentence(s);
        }

        scan.close();
    }

    private static boolean shouldQuit(String query) {
        query = query.toLowerCase();
        if (query.equals("no"))
            return true;
        if (query.equals("quit"))
            return true;
        if (query.equals("exit"))
            return true;
        return false;
    }
}
