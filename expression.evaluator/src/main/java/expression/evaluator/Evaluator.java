package expression.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Evaluator {

	public static void main (String args[]) {
		//Expressions
		Map<String, String> expsMap = new HashMap<String, String>(){
			{
				put("col1","([col2]{t}+[col3]{t})");
				put("col2","([col1]{t-1}+[col3]{t})");
				put("col3","([col3]{t-1}+{1})");
			}
		};

		//Get order in which expressions has to be evaluated
		SortExpression sortExpression = new SortExpression(expsMap);
		List<String> sortedOrder = sortExpression.sort();

		// Data for the expressions
		final List<Double> col1 = new ArrayList<Double>(){
			{
				add(7.0);
				add(-11.0);
				add(17.0);
				add(25.0);
				add(35.0);
			}
		};

		final List<Double> col2 = new ArrayList<Double>(){
			{
				add(-6.0);
				add(9.0);
				add(14.0);
				add(21.0);
				add(30.0);
			}
		};

		final List<Double> col3 = new ArrayList<Double>(){
			{
				add(1.0);
				add(2.0);
				add(3.0);
				add(4.0);
				add(5.0);
			}
		};

		Map<String, List<Double>> dataMap = new HashMap<String, List<Double>>() {
			{
				put("col1", col1);
				put("col2", col2);
				put("col3", col3);
			}
		};

		//startRow is position of data value from where to start. Here dataMap contains 5 data in every column. Here startRow = 5
		int startRow = 5;

		//endRow is the end position till which one wants to generate data. Generating data till 10th row
		int endRow = 10;

		Map<String, List<Double>> result = evaluate(expsMap, sortedOrder, dataMap, startRow, endRow);
		System.out.println(result);
		//below is the Expected Result
		/*{col2=[-6.0, 9.0, 14.0, 21.0, 30.0, 41.0, 54.0, 69.0, 86.0, 105.0], 
		col3=[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0], 
		col1=[7.0, -11.0, 17.0, 25.0, 35.0, 47.0, 61.0, 77.0, 95.0, 115.0]}*/
	}

	public static Map<String, List<Double>> evaluate (Map<String, String> expsMap, List<String> sortedOrder, Map<String, List<Double>> dataMap, int startRow, int endRow) {
		try {
			for(int i=startRow; i<endRow; i++) {
				for (String exp: sortedOrder) {
					double res = evaluteExpression(expsMap.get(exp), dataMap, i);
					List<Double> temp = new ArrayList<Double>();
					if(dataMap.containsKey(exp))
						temp = dataMap.get(exp);
					temp.add(res);
					dataMap.put(exp, temp);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataMap;
	}

	public static strictfp double evaluteExpression(String exp, Map<String, List<Double>> data, int tValue) {
		try {
			//considering there is no space between variable names
			exp=exp.trim();
			char[] tokens = exp.toCharArray();

			for(int i=0; i< tokens.length; i++) {
				String replaceingExp = "";
				if(tokens[i]== '[') {
					replaceingExp+=tokens[i];
					i++;
					StringBuffer variable = new StringBuffer();
					while (i < tokens.length && tokens[i] != ']' ) {
						variable.append(tokens[i]);
						replaceingExp+=tokens[i];
						i++;
					}
					replaceingExp+=tokens[i];
					i++;
					StringBuffer timeSeries = new StringBuffer();
					if(tokens[i] == '{') {
						replaceingExp+=tokens[i];
						i++;
						while (i < tokens.length && tokens[i] != '}' ) {
							timeSeries.append(tokens[i]);
							replaceingExp+=tokens[i];
							i++;
						}
						replaceingExp+=tokens[i];
					} else {
						i--;
						timeSeries.append("t");
					}

					List<Double> dataData = data.get(variable.toString());
					String ts = timeSeries.toString().replace("t", tValue+"");
					int position = (int)evaluateExpressionWithData(ts, false);
					Double value = 0.0;
					if(position>=0) {
						value = dataData.get(position);
						if(value == null) {
							value = 0.0;
						}
					}

					exp=exp.replace(replaceingExp, "{"+value+"}"); 
				}
			}
			return evaluateExpressionWithData(exp, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	public static double evaluateExpressionWithData(String expression, boolean hasNegative) 
	{ 
		char[] tokens = expression.toCharArray(); 

		// Stack for numbers: 'values' 
		Stack<Double> values = new Stack<Double>(); 

		// Stack for Operators: 'ops' 
		Stack<Character> ops = new Stack<Character>(); 

		for (int i = 0; i < tokens.length; i++) 
		{ 
			// Current token is a whitespace, skip it 
			if (tokens[i] == ' ') 
				continue; 

			if(hasNegative) {
				if (tokens[i] == '{') { 
					StringBuffer sbuf = new StringBuffer(); 
					i++;
					// There may be more than one digits in number 
					while (tokens[i]!='}') 
						sbuf.append(tokens[i++]); 
					if(sbuf.toString().contains(".")) {
						values.push(Double.parseDouble(sbuf.toString()));
					} else {
						values.push(Double.valueOf(Integer.parseInt(sbuf.toString())));
					}
					i--;
				}
			} else {
				// Current token is a number, push it to stack for numbers 
				if (tokens[i] >= '0' && tokens[i] <= '9') { 
					StringBuffer sbuf = new StringBuffer(); 
					// There may be more than one digits in number 
					while ((i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9') || (i < tokens.length && tokens[i] == '.')) 
						sbuf.append(tokens[i++]); 
					if(sbuf.toString().contains(".")) {
						values.push(Double.parseDouble(sbuf.toString()));
					} else {
						values.push(Double.valueOf(Integer.parseInt(sbuf.toString())));
					}
					i--;
				}
			}

			// Current token is an opening brace, push it to 'ops' 
			if (tokens[i] == '(') 
				ops.push(tokens[i]); 

			// Closing brace encountered, solve entire brace 
			else if (tokens[i] == ')') { 
				while (ops.peek() != '(') 
					values.push(applyOp(ops.pop(), values.pop(), values.pop())); 
				ops.pop(); 
			} 

			// Current token is an operator. 
			else if (tokens[i] == '+' || tokens[i] == '-' || 
					tokens[i] == '*' || tokens[i] == '/') { 
				// While top of 'ops' has same or greater precedence to current 
				// token, which is an operator. Apply operator on top of 'ops' 
				// to top two elements in values stack 
				while (!ops.empty() && hasPrecedence(tokens[i], ops.peek())) 
					values.push(applyOp(ops.pop(), values.pop(), values.pop())); 

				// Push current token to 'ops'. 
				ops.push(tokens[i]); 
			} 
		} 

		// Entire expression has been parsed at this point, apply remaining 
		// ops to remaining values 
		while (!ops.empty()) 
			values.push(applyOp(ops.pop(), values.pop(), values.pop())); 

		// Top of 'values' contains result, return it 
		return values.pop(); 
	} 

	// Returns true if 'op2' has higher or same precedence as 'op1', 
	// otherwise returns false. 
	public static boolean hasPrecedence(char op1, char op2) { 
		if (op2 == '(' || op2 == ')') 
			return false; 
		if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) 
			return false; 
		else
			return true; 
	} 

	// A utility method to apply an operator 'op' on operands 'a'  
	// and 'b'. Return the result. 
	public static double applyOp(char op, double b, double a) { 
		switch (op) { 
		case '+': 
			return a + b; 
		case '-': 
			return a - b; 
		case '*': 
			return a * b; 
		case '/': 
			if (b == 0) 
				throw new
				UnsupportedOperationException("Cannot divide by zero"); 
			return a / b; 
		} 
		return 0; 
	}
}
