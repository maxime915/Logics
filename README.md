# Logics

Handle logic boolean function

* Obtain a callable object from a descriptive String or a litteral expression
* Obtain the truth table for a logic function
* Get a representation as a sum of minterms or a product of maxterms

## `LogicFunction`

Main interface with different implementation listed in the next sections.

List of methods required by this interface : 

* `get(boolean... vs)` computes the result of the function for some inputs (**NB: the order of the input must follow the alphabetical order**)
* `getNames()` returns a `String` array where each element is only one character and represent the name of the corresponding variable
* `getTruthTable()` returns a boolean array of all value of the function for the 2^n possibles input combination
* `getTruthTableRepresentation()` returns a formatted String representing the truth-table
* `expressAsSumOfMinterms()` returns a String that can be used to create a new function that will behave exactly as this one using the sum of the minterms
* `getMinTermsIndex()` returns the indexs corresponding to the minterms of this function
* `expressAsProductOfMaxterms()` returns a String that can be used to create a new function that will behave exactly as this one using the product of the maxterms
* `getMaxTermsIndex()`returns the indexs corresponding to the maxterms of this function

### `LogicChain`

The constructor of this class takes a `String` argument storing the description of the function using letters (`a-z`) for variables and litterals (`0` or `1`) and limited boolean operators (`&`, `*`, `|`, `+`, `'`, `!`). The description is parsed with a `Parser` object and throws `RuntimeException` is the `String` isn't valid.

The object is built using `LogicNode` objects representing either a constant, a variable or a binary / unary operator.

### `LogicTable`

The constructor of this class is similar to the constructor of the `LogicChain` class, it first create a `LogicNode` object and then compute and store the truth table.

### `CallableHandler`

The constructor expects a `CallableLogic` object and store it. `CallableHandler` objects do not print names unless provided by the `setNames` method.

#### `CallableLogic`

Functional interface returning a boolean value for a list of boolean inputs.
