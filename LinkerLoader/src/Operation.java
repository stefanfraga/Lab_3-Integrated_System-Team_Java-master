
public class Operation implements Instruction{

	public String label = "";
	public String op = "";
	public String operands = "";
	public String originalLine = "";
	
	/**Operation Constructor
	 * @param line non parsed line
	 * @updates label element of line at indexes 0-5
	 * @updates op element of line at indexes 9-13
	 * @updates operands element of line starting at index 17
	 * @updates originalLine line parameter given
	 */
	public Operation(String line) {

		if(line.length()>17) {
			label = line.substring(0, 6);
			op = line.substring(9, 14);
			operands = line.substring(17);
		}
		else if (line.length()>9) {
			label = line.substring(0, 6);
			op = line.substring(9);
		}
		else 
			Input_Output.changeErrorFound(9);
		originalLine = line;

		
		op = removeSpaces(op);
		label = removeSpaces(label);	
		if(op.length() == 0) {
			Input_Output.changeErrorFound(32);
		}
		else {
			op = op.toUpperCase();
		}
		//removing comments and trimming whitespace
		if (operands.indexOf(';') >= 0 && !op.equals(".STRZ")) {
				operands = operands.substring(0, operands.indexOf(';'));//remove comments
		}
		operands = removeSpaces(operands);//remove spaces for all variables
	}
	
	/* (non-Javadoc)
	 * @see Instruction#isValidLabel()
	 */
	public boolean isValidLabel() {
		boolean validLabel = true;
		char c = label.charAt(0);//assume label.length != 0
		if ((c = label.charAt(0)) == 'R' || c == 'x') {//if first character is a R or x error and not valid label
			Input_Output.changeErrorFound(1);
			validLabel = false;
		}
		else if (!(Character.isLetterOrDigit(c))) {//if a first character is not alphanumeric error invalid label
			Input_Output.changeErrorFound(1);
			validLabel = false;
		}
		else if(op.equals(".END")) {//.END operations cannot have labels produce error
			Input_Output.changeErrorFound(39);
			validLabel = false;
		}
		for (int i = 0; i< label.length(); i++) {//is the label alphanumeric
			char nextChar = label.charAt(i);
			if(!(Character.isLetterOrDigit(nextChar))) {//check each character is it is alphanumeric
				Input_Output.changeErrorFound(10);//if not throw an error
				validLabel = false;
			}
		}
		return validLabel;
			
	}
	
	/** Parses String into Integer and determines if the integer falls in range from value type
	 * @param operands string representation of integer
	 * @param type indicating range  
	 * @return
	 */
	public static int isValidNumber(String operands, String type) {
		int value = 0;
		if(operands.length()>0) {// if the operand is not empty
			char firstChar = operands.charAt(0);//get first character
			try {
				if (firstChar == 'x' && operands.length() >1) 
					value = Integer.parseInt(operands.substring(1),16);//if hex indicator parse Int hex
				else if (firstChar == '#'&& operands.length() > 1)
					value = Integer.parseInt(operands.substring(1));// if decimal indicator parse int decimal
				else if (firstChar == 'x' || firstChar == '#' ) 
					Input_Output.changeErrorFound(4);
			}
			catch (NumberFormatException e) {
				Input_Output.changeErrorFound(4);;// produce error if exception is thrown during parsing
			}
			switch (type) {
			case "literal": 
				if (firstChar == '#' && (value < -32768 || value > 32767)) //if literal and decimal must fall in range
					Input_Output.changeErrorFound(34);
				else if (firstChar == 'x'&& (value < 0 || value > 0xFFFF)) //if literal and hex must fall in range
					Input_Output.changeErrorFound(34);
				break;
			case "address": 
				if (value < 0 || value > 65535)//if address same range for both hex and decimal
					Input_Output.changeErrorFound(34);
				break;
			case "immediate":
				if (firstChar == '#' && (value < -16 || value > 15))//if immediate and decimal must fall in range
					Input_Output.changeErrorFound(34);
				if (firstChar == 'x'&& (value < 0 || value > 0x1F))//if immediate and hex must fall in range 
					Input_Output.changeErrorFound(34);
				break;
			case "index":
				if (value < 0 || value > 63)//if index same range for both hex and decimal
					Input_Output.changeErrorFound(34);
				break;
			case "trap":
				if (value < 0 || value > 255)///if trap same range for both hex and decimal
					Input_Output.changeErrorFound(34);
				break;
			}
		}
		else
			Input_Output.changeErrorFound(30);
		return value;
	}
	
	/* (non-Javadoc)
	 * @see Instruction#removeSpaces(java.lang.String)
	 */
	public String removeSpaces(String text) {
		while (text.length() != 0 && text.charAt(text.length() -1) == ' ')//while spaces in first character remove
			text = text.substring(0,text.length() - 1);	
		return text;
	}
	
	
}
