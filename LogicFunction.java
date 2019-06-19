public interface LogicFunction extends CallableLogic {

    public String[] getNames();

    public boolean[] getTruthTable();

    public String getTruthTableRepresentation();

    public String expressAsSumOfMinterms();

    public int[] getMinTermsIndex();

    public String expressAsProductOfMaxterms();

    public int[] getMaxTermsIndex();
}
