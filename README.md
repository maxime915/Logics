# Logics

Handle logic boolean function (basics)

* Obtain a callable object from a descriptive String
* Obtain the truth table for a logic function
* Get a representation as a sum of minterms or a product of maxterms

## LogicChain

This is the most useful class, accessible methods are listed below

* `static LogicChain fromString(String description)` creates a logic function from a String
* `boolean get(boolean... vs)` computes the result of the function for some inputs (**NB: the order of the input must follow the alphabetical order**)
* `String getTruthTable()` returns a formatted String representing the truth-table
* `String asSumOfMinterms()` returns a String that can be used to create a new LogicChain that will behave exactly as this one using the sum of the minterms
* `int[] getMinTermsIndex()` returns the indexs corresponding to the minterms of this LogicChain
* `String asProdfMaxterms()` returns a String that can be used to create a new LogicChain that will behave exactly as this one using the product of the maxterms
* `int[] getMaxTermsIndex()`returns the indexs corresponding to the maxterms of this LogicChain
