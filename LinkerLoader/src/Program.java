
public class Program implements ProgramI{
	
	public static Symbols symbolTables;
	public static Literals literalTable;
	public static int startAddress;
	public static int programCounter;
	public static int size; 
	public String programName;
	public boolean relocatable;
	
	/**
	 * Program Empty Constructor
	 */
	public Program () {
		symbolTables = new Symbols();
		literalTable = new Literals();
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#setRelocatable()
	 */
	public void setRelocatable() {
		symbolTables.relocatableProgram = true;//set relocatable variables to true
		relocatable = true;
		startAddress = 0;//set startAddress to 0
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#setStartAddress(int)
	 */
	public int setStartAddress(int address) {
		startAddress = address;//set start address to address parameter given
		return startAddress;
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#setProgramName(java.lang.String)
	 */
	public void setProgramName(String name) {
		programName = name;//set name to parameter given
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#getSymbols()
	 */
	public Symbols getSymbols() {
		return symbolTables;
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#getLiterals()
	 */
	public Literals getLiterals() {
		return literalTable;
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#getStartAddress()
	 */
	public int getStartAddress() {
		return startAddress;
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#isProgramRelocatable()
	 */
	public boolean isProgramRelocatable() {
		return relocatable;
	}
	
	/**
	 * Increases the locationAddress value given depending on the size of the operation. The sizes of the operations retrieved from MachineOpTable and PsuedoOpTable.
	 * For operations .STRZ and .BLKW sizes of elements are determined from parameter operands
	 * @param op name of the operation used to retrieve sizes from relative tables
	 * @param operands operation elements used to determine sizes for operations .STRZ and .BLKW
	 * @param locationAddress initial address 
	 * @updates locationAddress
	 * @updates Input_Output.found_error updates when error is found
	 * @return updated locationAddress
	 */
	public static int increaseLocationCounter (String op, String operands, int locationAddress) { //TODO adddresses still off
		int size = 0;
		if (op.length()>0&& op.charAt(0) == '.') {//is the op a psuedo op
			size = symbolTables.getPsuedoOpSize(op); //get size from psuedoOp table
			if (size < 0) {// if returned value was -1 .STRZ or >BLKW
				if(op.equals(".STRZ")){//if op is .STRZ
					int index = operands.indexOf("\"");//find first quotation
					if(index <0) {
						Input_Output.changeErrorFound(3);//if none found, error 
					}
					String tempString = operands.substring(index +1);//remove first quotation
					int index2 = tempString.indexOf("\"");//find index of second quotation
					if(index2 <0) {
						Input_Output.changeErrorFound(3);// if second quotation not found produce error
					}
					size = index2 - index +1;//size is the number of characters between two indexes
					
				}
				else if(op.equals(".BLKW")){//if op is .BLKW
					Integer value = 0;
					if (operands.length() >0 && operands.charAt(0) != 'x' && operands.charAt(0) != '#') {//if operand does not begin with immediate value indicators #,x must be symbol
						 value = symbolTables.getValue(operands);//get value of symbol in table
						if (value == null) {//if symbol is not in table produce error
							Input_Output.changeErrorFound(33);
							value = 0;//for value not found set to 0
						}
						if (symbolTables.isExternal(operands)) {
							//TODO could produce error
							Input_Output.changeErrorFound(47);
						}
					}
					else if (operands.length() == 0) {
						Input_Output.changeErrorFound(9);
					}
					else {
						value = Operation.isValidNumber(operands, "address");//if operand does begin with immediate #,x check value falls in range of addresses
						if(value == 0) {//if the value returned is 0 produce error BLKW cannot have 0 size
							Input_Output.changeErrorFound(25);
						}
					}
					size = value;
				}
				else {
					Input_Output.changeErrorFound(2);// if not found in PsuedoOp table and not .STRZ or .BLKW invalid operation
				}
			}
		}
		else {
			size = symbolTables.getOpSize(op);//if machineOp, get size
			if (size == -1) {
				size = 0;
				Input_Output.changeErrorFound(2);
			}
		}
		locationAddress = locationAddress + size; //add size to locationAddress
		if (locationAddress > 0xFFFF +1) {//if location Address exceeds max address wraps around to 0 error
			locationAddress = locationAddress % 0xFFFF;//find new address
			Input_Output.changeErrorFound(27);
		}
		return locationAddress;
		
	}
	/**returns the value of a symbol 
	 * @param operands the symbol name
	 * @return the value of the symbol found in the Symbol Tables or 0 if not found in the table
	 */
	public static int getSymbolValue(String operands) {
		 Integer value = symbolTables.getValue(operands);//get value of symbol in table
		if (value == null) {//if the value is null, symbol undefined
			Input_Output.changeErrorFound(5);//produce error and set value to 0
			value = 0;
			SecondPass.executing = false;//executing to false
		}
		else if (symbolTables.isRelocatable(operands)) {//if the symbol is relocatable set SecondPass relocatable line to true
			SecondPass.relocatableLine = true;
		}
		if (SecondPass.executing && symbolTables.isExternal(operands)) {//if the symbol is relocatable set SecondPass relocatable line to true
			SecondPass.linkable = true;
		}
		return value;
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#getHeaderRecord()
	 */
	public String getHeaderRecord() {
		String hexAddress = SecondPass.adjustLength(Integer.toHexString(startAddress),4);//get hex value length 4 for startAddress
		String hexSize = SecondPass.adjustLength(Integer.toHexString(size),4);//get hex value of program size with length 4
		programName = programName.replace(" ", "_");//Replace spaces with "_" for program Name
		String record = 'H' + programName + hexAddress + hexSize;//create header record
		if (isProgramRelocatable()) //add relocatable element if the program is relocatable
			record = record + 'M';
		return record;//return record
	}
	
	/* (non-Javadoc)
	 * @see ProgramI#getEndRecord()
	 */
	public String getEndRecord() {
		return 'E'+ SecondPass.adjustLength(Integer.toHexString(programCounter),4);//create end record from hex of program counter
	}
}
