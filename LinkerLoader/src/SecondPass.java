public class SecondPass implements ProgramPass{
	

	public static Symbols symbolTables;
	public static Literals literalTable;
	public static int opcode;
	public static int locationCounter;
	public static String instructionBinary;
	public static String textRecordHex;
	public static boolean executing = true;
	public static Operation opLine;
	private static int jumps = 0;
	
	private final static int ADD_AND = 0;
	private final static int OFFSET9 = 1;
	private final static int INDEX = 3;
	private final static int JUMPR = 4;
	private final static int NOT = 5;
	private final static int TRAP = 6;
	private final static String SPACE = "                              ";
	private final static int[] TRAP_VECTOR = new int[] {0x21,0x22,0x23,0x25,0x31,0x33,0x43};
	private String relocatable;
	
	public static boolean relocatableLine;
	public static boolean linkable;
	
	public static Program segment;
	
	/**Second Pass Constructor
	 * @param program
	 */
	public SecondPass(Program program) {
		executing = true;
		segment = program;
		symbolTables = segment.getSymbols();//get program tables produced in FirstPass
		literalTable = segment.getLiterals();
		locationCounter = segment.getStartAddress();
	}
	
	/**Read the line and print a Text record to object file and information to listing file. When reading line find values for each operand
	 * @param line unparsed instruction line
	 * @updates Output_Input.found_error when an error is produced
	 */
	public void parseLine(String line) {
		
		opLine = new Operation(line);// create new operation which will separate out label, op and operands in constructor
		relocatableLine = false;//initially set relocatable to false
		linkable = false;
		textRecordHex = "";
		
		if (opLine.op.length() >0 && opLine.op.charAt(0) == '.') {//if a psuedoOp
			relocatable = "  ";
			switch (opLine.op) {
			case ".ORIG": 
				textRecordHex = segment.getHeaderRecord(); //create header record
				printRecords(SPACE);//print to files
				break;
			case ".END":
				literalTable.printTextRecords();//print to object file text records of literals
				
				textRecordHex = segment.getEndRecord();//create end record 
				printRecords(SPACE);//print to files
		
				Input_Output.lineNumber = -1;//set line number to -1 to represent literals
				literalTable.printListings();//print to Listing file value of literals 
				
//				if(jumps<0) {
//					Input_Output.changeErrorFound(37);
//				}
				
				break;
			case ".FILL":
				int value = getValueOperand(opLine.operands, "literal", opLine.op);//get value of operand 16 bit (address type)
				instructionBinary = adjustLength(Integer.toBinaryString(value),16);//get binary string of value
				String valueHex = adjustLength(Integer.toHexString(value),4);//get hexidecimal of value and adjust length to 4
				String addressHex = adjustLength(Integer.toHexString(locationCounter),4);//get hexidecimal of location in memory
				if (relocatableLine && !linkable) {
					relocatable = "M10";//set relocatabale indicator to hex 16
				}
				else if (linkable) {
					relocatable = "X10" + opLine.operands;
				}
				textRecordHex = 'T' + addressHex + valueHex + relocatable;//create text record adding together address value and relocatable indicator
				String listing = addressHex +"  "+valueHex+ relocatable + "  "+ instructionBinary;//create listing information including address, value, relocatable and binary form of value
				printRecords(listing);//print to files
				locationCounter = Program.increaseLocationCounter(opLine.op, opLine.operands, locationCounter);//increse the counter based on operation size
				break;
			case ".STRZ":
				if (opLine.operands.length()>0) {
					int index = opLine.operands.indexOf("\"");
					opLine.operands = opLine.operands.substring(index+1);//remove first quotation
					char nextChar;
					while((nextChar = opLine.operands.charAt(0)) != '\"') {//for each character until next quotation
						instructionBinary = adjustLength(Integer.toBinaryString(nextChar),16);//get 16 bit representation of ACII value of character 
						valueHex = adjustLength(Integer.toHexString(nextChar),4);//get hex of character
						addressHex = adjustLength(Integer.toHexString(locationCounter),4);//get hex of address
						textRecordHex = 'T' + addressHex + valueHex;//create text record
						locationCounter++;
						listing = addressHex +"  "+valueHex+"    "+instructionBinary;//create listing
						printRecords(listing);//print to files
						opLine.originalLine = "";// set line to only print once
						opLine.operands = opLine.operands.substring(1);//remove character
					}
					instructionBinary = adjustLength(Integer.toBinaryString(0),16);//null terminating value
					textRecordHex = adjustLength(Integer.toHexString(0),4);//hex of null terminating value
					addressHex = adjustLength(Integer.toHexString(locationCounter),4);//hex of address
					valueHex = textRecordHex;
					textRecordHex = 'T'+ addressHex + textRecordHex;//create text record
					listing = adjustLength(Integer.toHexString(locationCounter),4) +"  "+valueHex+"    "+instructionBinary;//create listing
					printRecords(listing);//print null terminating to files
					locationCounter++;
				}
				else
					Input_Output.changeErrorFound(30);
				break;
			case ".BLKW":
				listing = adjustLength(Integer.toHexString(locationCounter),4) + SPACE.substring(4);//create listing with just addresses of memory
				FileAssembler.printListing(listing, opLine.originalLine); //print to listing file not object file
				locationCounter = Program.increaseLocationCounter(opLine.op, opLine.operands, locationCounter);//increse the counter based on operation size
				break;
			case ".EQU":
				FileAssembler.printListing(SPACE, opLine.originalLine);//print to listing file not object file, just line
				break;
			case ".EXT":	
				FileAssembler.printListing(SPACE, opLine.originalLine);//print to listing file not object file, just line
				break;
			case ".ENT":
				opLine.label = opLine.operands;
				opLine.isValidLabel();
				Integer localValue = symbolTables.getValue(opLine.label);
				if (localValue == null) {
					Input_Output.changeErrorFound(45);
					localValue = 0;
					executing = false;
				}
//				else if (!symbolTables.isRelocatable(opLine.label)) {
//					Input_Output.changeErrorFound(46);
//				}
				if (symbolTables.isRelocatable(opLine.label) && !symbolTables.isExternal(opLine.label)) {//set relocatable indecator
					relocatable = "M10";
				}
				else if(symbolTables.isExternal(opLine.label)) {
					Input_Output.changeErrorFound(56);
				}
				while(opLine.label.length() <6) {
					opLine.label = opLine.label + "_";
				}
				String localHexValue = adjustLength(Integer.toHexString(localValue),4);
				textRecordHex = "N" + opLine.label + localHexValue + relocatable;
				printRecords(SPACE);//print to files
				break;
			}
		}
		else {
			int value = symbolTables.getOpcode(opLine.op);//if Machine OP
			if (value <0) {//if opcode is <0 not in machineOp table
				Input_Output.changeErrorFound(2);
				value = 0;
			}
			opcode = value;
			String opcodeBinary = Integer.toBinaryString(opcode);//binary of opcode
			opcodeBinary = adjustLength(opcodeBinary, 4);//adjust length to 4
			String operandBinary = "";
			relocatable = "  ";
			switch (symbolTables.getType(opLine.op)) {//get type of operands from MachineOp table
			case ADD_AND: 
				for (int i = 0; i<2; i++) {//Two registers 
					int registerNum = retrieveRegister(opLine.operands); //retrieve value of register
					operandBinary = operandBinary + adjustLength(Integer.toBinaryString(registerNum),3);// produce binary for for each register 
					if(opLine.operands.indexOf(',') >0)
						opLine.operands = opLine.operands.substring(opLine.operands.indexOf(',') + 1);
					else {
						Input_Output.changeErrorFound(30);
						opLine.operands = "";
						if(linkable)
							Input_Output.changeErrorFound(57);
					}
				}
				if (opLine.operands.length()>0 && opLine.operands.charAt(0) == 'R' && executing) {//if third operand id register  
					int registerNum = retrieveRegister(opLine.operands);//get register value
					operandBinary = operandBinary + '0' + adjustLength(Integer.toBinaryString(registerNum),5);//complete binary form of instruction
					if(linkable)
						relocatable = "S3" + opLine.operands;

				}
				else if (executing) {
					int immediate = getValueOperand(opLine.operands, "immediate", opLine.op);//get value of immediate or symbol
					operandBinary = operandBinary + '1' + adjustLength(Integer.toBinaryString(immediate),5);//complete instruction binary 
					if(linkable)
						relocatable = "S5" + opLine.operands;
				}
				if(relocatableLine && !linkable) //cannot get relocatable value in AND or ADD
					Input_Output.changeErrorFound(23);
				break;
			case OFFSET9:
				int registerNum;
				if(!opLine.op.startsWith("BR") && !opLine.op.startsWith("J")) {
					registerNum = retrieveRegister(opLine.operands);
					operandBinary = operandBinary + adjustLength(Integer.toBinaryString(registerNum),3);
					if(opLine.operands.indexOf(',') >0)
						opLine.operands = opLine.operands.substring(opLine.operands.indexOf(',') + 1);
					else {
						Input_Output.changeErrorFound(30);
						opLine.operands = "";
					}
				}else if (opLine.op.equals("BRP"))
					operandBinary = operandBinary + "001";
				else if (opLine.op.equals("BRZ"))
					operandBinary = operandBinary + "010";
				else if (opLine.op.equals("BRN")|| opLine.op.equals("JSR"))
					operandBinary = operandBinary + "100";
				else if (opLine.op.equals("BRNZP"))
					operandBinary = operandBinary + "111";
				else if (opLine.op.equals("BRNZ"))
					operandBinary = operandBinary + "110";
				else if (opLine.op.equals("BRNP"))
					operandBinary = operandBinary + "101";
				else if (opLine.op.equals("BRZP"))
					operandBinary = operandBinary + "011";
				else if (opLine.op.equals("BR"))
					operandBinary = operandBinary + "000";
				else 
					operandBinary = operandBinary + "000";
				
				if(opLine.op.equals("JSR"))
					jumps++;	
				
				int address = getValueOperand(opLine.operands, "address", opLine.op);//get value
				if (relocatableLine && !linkable) {//set relocatable indecator
					relocatable = "M9";
				}
				else if (linkable) {
					relocatable = "X9"+ opLine.operands; 
				}
				String offsetBinary = adjustLength(Integer.toBinaryString(address),9);//adjust binary length to 9 bits
				String pcPageAddressBinary = adjustLength(Integer.toBinaryString(locationCounter + 1), 16).substring(0,7) + offsetBinary;///add PC page 
				if(Integer.parseInt(pcPageAddressBinary,2) != address) {//compare addresses upon execution and desired address 
					Input_Output.changeErrorFound(16);;//error if not the same
				}
				if(Integer.parseInt(pcPageAddressBinary,2) < segment.getStartAddress() && Integer.parseInt(pcPageAddressBinary,2) > segment.getStartAddress()+Program.size && executing)
					Input_Output.changeErrorFound(8);//error if trying to access an address off segment
				operandBinary = operandBinary + offsetBinary;
				break;
			case INDEX:
				registerNum = retrieveRegister(opLine.operands);//retrieve rgister value
				operandBinary = operandBinary + adjustLength(Integer.toBinaryString(registerNum),3);//add register to binary 
				if(linkable)
					Input_Output.changeErrorFound(57);
				if(opLine.operands.indexOf(',') >0)
					opLine.operands = opLine.operands.substring(opLine.operands.indexOf(',') + 1);
				else {
					Input_Output.changeErrorFound(30);
					opLine.operands = "";
				}

			case JUMPR:
				if (opLine.op.equals("JMPR")) {
					operandBinary = operandBinary + "000";//JUMP doesnt use first register value
				}else if (opLine.op.equals("JSRR")) {
					operandBinary = operandBinary + "100";//JSRR doesnt use first register value
					jumps++;
				}
				registerNum = retrieveRegister(opLine.operands);//retrieve register 
				operandBinary = operandBinary + adjustLength(Integer.toBinaryString(registerNum),3);//add register to binary 
				if(linkable)
					Input_Output.changeErrorFound(57);
				if(opLine.operands.indexOf(',') >0)
					opLine.operands = opLine.operands.substring(opLine.operands.indexOf(',') + 1);
				else {
					Input_Output.changeErrorFound(30);
					opLine.operands = "";
				}
				if (executing && opLine.operands.indexOf(',') <=0) {
					int index6 = getValueOperand(opLine.operands, "index", opLine.op);//get value of index 
					operandBinary = operandBinary + adjustLength(Integer.toBinaryString(index6),6);//complete binary form
				}
				else if (opLine.operands.indexOf(',') >0) {
					Input_Output.changeErrorFound(9);
				}
				if(relocatableLine && !linkable) {
					Input_Output.changeErrorFound(26);//error is symbol was relocatable
				}
				if(linkable)
					relocatable = "S6" + opLine.operands;
				break;
			case NOT:
				for (int i = 0; i<2; i++) {
					registerNum = retrieveRegister(opLine.operands);//retrieve two register values
					if(linkable)
						Input_Output.changeErrorFound(57);
					operandBinary = operandBinary + adjustLength(Integer.toBinaryString(registerNum),3);//add both values to binary form of instruction
					if(opLine.operands.indexOf(',') >=0)
						opLine.operands = opLine.operands.substring(opLine.operands.indexOf(',') + 1);
					else if (i != 1){
						Input_Output.changeErrorFound(30);
						opLine.operands = "";
					}
					else {
						opLine.operands = "";
					}
				}
				if(opLine.operands.length() >0) {
					Input_Output.changeErrorFound(9);
				}
				operandBinary = operandBinary + adjustLength(Integer.toBinaryString(0),6);//add 000000 to the end of binary form
				break;
			case TRAP:
				int trapValue = getValueOperand(opLine.operands, "trap", opLine.op);// get value for trap operation
				if(relocatableLine && !linkable) {//cannot be a relocatable value for trap 
					Input_Output.changeErrorFound(24);
				}
				operandBinary = adjustLength(Integer.toBinaryString(trapValue),12);//set trap to binary 
				boolean trap = false;
				for(int i = 0; i< TRAP_VECTOR.length; i++) {//check if he value is one of the values in the TRAP_VECTOR array
					if(trapValue == TRAP_VECTOR[i])
						trap = true;
				}
				if(linkable) {
					relocatable = "T8" + opLine.operands;
					trap = true;
				}
				if(!trap) {//if not one of trap values produce error
					Input_Output.changeErrorFound(4);
				}
					
				break;
			default:
				if(opLine.operands.length()>0) {
					Input_Output.changeErrorFound(35);
				}
				if(opLine.op.equals("RET")) {
					jumps--;
				}
				operandBinary = adjustLength(Integer.toBinaryString(0),12);//instruction is all 0 
				break;
			}
			
			instructionBinary =  opcodeBinary + operandBinary;//add opcode binary to operandBinary 
			assert(instructionBinary.length() == 16);//binary form must be 16 bits
			String addressHex = adjustLength(Integer.toHexString(locationCounter),4);//get hex of locationCounter
			String instructionHex = adjustLength(Integer.toHexString(Integer.parseInt(instructionBinary, 2)),4);//change binary instruction to hex
			textRecordHex = 'T' + addressHex + instructionHex + relocatable;//create text record
			String listing = addressHex+"  "+instructionHex+ relocatable +"  "+instructionBinary;//create listing
			printRecords(listing);//print to files
			locationCounter = Program.increaseLocationCounter(opLine.op, opLine.operands, locationCounter);//increse the counter based on operation size
		}
		//Input_Output.errorMessage();//produce errror message
	}
	
	/**retrieve the number corresponding to the register operand ex R0 = 0
	 * @param operands contains a symbol or R operand
	 * @return int value of register number
	 */
	public static int retrieveRegister(String operands) {
		Integer value = 0;
		if (operands.length()>0) {
			if (operands.charAt(0) == ',' || operands.charAt(0) == ' ') {
				operands = operands.substring(1);//remove any spaces or commas
			}
			int index = operands.indexOf('R');
			if (index < 0) {//if no R character
				if(operands.indexOf(',')>0)
					operands = operands.substring(0,operands.indexOf(','));
				value = Program.getSymbolValue(operands);//get symbol value
				if (symbolTables.isRelocatable(operands)) {//if the reister number is relocatable produce an error
					Input_Output.changeErrorFound(15);
				}
				
			}
			else if (operands.length()>index+1) {
				value = operands.charAt(index+1) - '0';//The int value of the character 
			}
			else
				Input_Output.changeErrorFound(9);
			if((value < 0)||(value > 8)) {//if the value is not between 0-7 produce an error
				Input_Output.changeErrorFound(6);
				executing = false;
				value = 0;
			}
		}
		else
			Input_Output.changeErrorFound(30);
		return value;
	}
	
	/**Lengthens or reduces the string to the desired length by adding 0's
	 * @param binary original string
	 * @param length the desired length
	 * @return string of desied length
	 */
	public static String adjustLength(String binary, int length) {
		while (binary.length() <length) {//add zero's until length
			binary = '0' + binary;
		}
		while (binary.length() > length) {//remove first character until length
			binary = binary.substring(1);
		}
		return binary;
	}
	
	/**get the value of an operand numeric or symbol
	 * @param operands the final operand
	 * @param type the type of value expected
	 * @param op the operation name
	 * @return the int value of the operand
	 */
	public static int getValueOperand(String operands, String type, String op) {
		Integer value = 0;
		if(operands.length() > 0) {
			if (operands.charAt(0) == ',') {//remove commas
				operands = operands.substring(1);
			}
			if(operands.charAt(0) == 'x' || operands.charAt(0) == '#') {
				value = Operation.isValidNumber(operands, type);//get decimal or hex value in range
			}
			else if(operands.charAt(0) == '=') {//get literals
				if (op.equals("LD")) {
					operands = operands.substring(1);
					value = Operation.isValidNumber(operands, "address");//confirm literal value is in range
					value = literalTable.getAddress(value);//get address from litteral table
					if (value == null) {//error if literal was not in table
						value = 0;
						Input_Output.changeErrorFound(7);
						executing = false;
					}
					if(segment.relocatable)//if program is relocatable literal address is relocatable
						relocatableLine = true;
				}
				else {
					Input_Output.changeErrorFound(28);
				}
			}
			else {
				value = Program.getSymbolValue(operands);//get symbol value
				value = Operation.isValidNumber('#'+Integer.toString(value),type);

			}
		}
		else
			Input_Output.changeErrorFound(30);
		return value;
	}

	/**Print text record to Object file and parameter listing to Listing file
	 * @param listing
	 */
	public static void printRecords(String listing) {
		
		FileAssembler.printRecords(textRecordHex + '\n');//print to Object File
		FileAssembler.printListing(listing,opLine.originalLine);//print to listing file 
	}
}

