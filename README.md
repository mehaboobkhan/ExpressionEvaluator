# ExpressionEvaluator
Time Series Expression along with data is fed to the algorithm. 
Algorithm detects the chronology of execution of expressions to be executed and evaluate with the given data.

InputData:
-------------------------
  col1	| col2	| col3 |
  ------|-------|------|
  7	    |   -1	|   1  |
  -11	    |   9	|   2  |
  17	    |   14	|   3  |
  25	    |   21	|   4  |
  35	    |   30	|   5  |

If first row is 0th index than current row i.e. startRow = 5. And endRow = 10 (9th index)

Expressions	
---------------
  LHS	| RHS	|
  ------|-------|
  col1	    |   ([col2]{t}+[col3]{t})	|
  col2	    |   ([col1]{t-1}+[col3]{t})	|
  col3	    |   ([col3]{t-1}+1)	|

Output Data		
-------------------------
  col1	| col2	| col3 |
  ------|-------|------|
  7	    |   -1	|   1  |
  -11	    |   9	|   2  |
  17	    |   14	|   3  |
  25	    |   21	|   4  |
  35	    |   30	|   5  |
  45	    |   41	|   6  |
  -61	    |   54	|   7  |
  77	    |   69	|   8  |
  95	    |   86	|   9  |
  115	    |   105	|   10  |
