
public class FirstPass implements ProgramPass{

	
	public static Operation opLine;
	
	public static int locationCounter;
	public static Symbols symbolTables;
	public static Literals literalTable;
	public static Program segment;
	
	private static final int ABSOLUTE = 0;
	private static final int RELOCATE = 1;
	private static final int EXT = 1;
	private static final int LOCAL = 0;
	private static final int MAX_PAGE_SIZE = 512;
	private static boolean orig;
	public static boolean end;
	private static boolean ent;
	
	
	public FirstPass () {
		segment = new Program();
		symbolTables = segment.getSymbols();
		literalTable = segment.getLiterals();
		end = false;
		orig = false;
		ent = true;
	}
	
	/**
	 * Read the line to add symbols to symbol table and get program size
	 * @param line unparsed instruction line
	 * @updates Output_Input.found_error when an error is produced
	 */
	public void parseLine(String line) {
//
		opLine = new Operation(line);// create new operation which will separate out label, op and operands in constructor
		if (end) {
			Input_Output.changeErrorFound(22);//operation after .END
		}
		
		if (opLine.op.equals(".ORIG")) { //if the op is.ORIG set start address and LC
			if (orig) {
				Input_Output.changeErrorFound(19);
			}
			orig = true;
			if (opLine.operands.length() == 0) {//if no start address set relocatable
				segment.setRelocatable();
				locationCounter = 0;
			}
			else {
				locationCounter = segment.setStartAddress(Operation.isValidNumber(opLine.operands, "address"));
			}
			if (opLine.label.length() == 0) //need program name
				Input_Output.changeErrorFound(17);
			else
				segment.setProgramName(line.substring(0,6));//set name 
			
		}
		if ((opLine.op.equals(".ENT")||opLine.op.equals(".EXT")) ) {
			if(opLine.label.length() >0)
				Input_Output.changeErrorFound(55);
			if(opLine.operands.length()==0)
				Input_Output.changeErrorFound(30);
		}
		if(opLine.op.equals(".EXT")) {
			opLine.label = opLine.operands;
			opLine.isValidLabel();
			symbolTables.add(opLine.label, 0, RELOCATE, EXT);
			if(!segment.relocatable)
				Input_Output.changeErrorFound(48);
			opLine.label = "";
		}
		if(opLine.op.equals(".ENT") && !segment.relocatable && orig)
				Input_Output.changeErrorFound(48);
		else if ((opLine.op.equals(".ENT")||opLine.op.equals(".EXT")) && !ent && orig)
			Input_Output.changeErrorFound(49);
		else if (ent && !opLine.op.equals(".ENT") && !opLine.op.equals(".EXT") &&  !opLine.op.equals(".ORIG"))
			ent = false;
		
		//is there a label
		if (opLine.label.length() !=0 && opLine.isValidLabel()) {//is there a valid label
			
			if(opLine.op.equals(".EQU")) {//is the operation .EQU
				int relocate = ABSOLUTE;
				if (opLine.operands.length() > 0 && opLine.operands.charAt(0) != 'x' && opLine.operands.charAt(0) != '#') {//is the operand for .EQU a symbol
					if(opLine.operands.charAt(0) == '=')
						Input_Output.changeErrorFound(28);
					 Integer value = symbolTables.getValue(opLine.operands);//get value of symbol in table
						if (value == null) {//if symbol is not in table produce error
							Input_Output.changeErrorFound(33);
							value = 0;//for value not found set to 0
						}
						value = Operation.isValidNumber('#'+Integer.toString(value),"address");
					if (symbolTables.isRelocatable(opLine.operands)) {
						relocate = RELOCATE;
					}
					if (symbolTables.isExternal(opLine.operands)) {
						//TODO could produce error
						Input_Output.changeErrorFound(47);
					}
					symbolTables.add(opLine.label, value, relocate, LOCAL );//add .EQU label with the value from the symbol
				}
				else if (opLine.operands.length() > 0) {
					symbolTables.add(opLine.label, Operation.isValidNumber(opLine.operands, "address"), ABSOLUTE, LOCAL);// add .EQU label with operand value confirmed a valid number 
				}
				else 
					Input_Output.changeErrorFound(30);
				
			}
			else {
				symbolTables.add(opLine.label, locationCounter, RELOCATE, LOCAL);// if there is label not with .EQU add to symbol table with value of LC
			}
		}
		else {
			if (opLine.op.equals(".EQU")) {//EQU must have a label
				Input_Output.changeErrorFound(38);
			}
		}
		
		if(opLine.op.equals("LD")) {//check for literals and add them to the literal table
			int index = opLine.operands.indexOf("=");
			if(index > 0 && opLine.operands.length()>index+1) {
				int value = Operation.isValidNumber(opLine.operands.substring(index+1), "literal");
				literalTable.add(value);
			}
			else if(opLine.operands.length() != 0 && opLine.operands.length()==index+1) {
				Input_Output.changeErrorFound(4);
			}
		}
		else if (opLine.op.equals(".END")) {
			end = true;
			locationCounter = literalTable.setAddresses(locationCounter);//set addresses for literals after all lines are read
			Program.size = locationCounter - segment.getStartAddress();
			if(segment.relocatable && Program.size >MAX_PAGE_SIZE) {
				Input_Output.changeErrorFound(18);
			}
			Integer address = 0;
			if (opLine.operands.length()>0 && opLine.operands.charAt(0) != 'x' && opLine.operands.charAt(0) != '#') {//if not decimal or hex indicator
				 address = Program.getSymbolValue(opLine.operands);// get symbol 
			}
			else if(opLine.operands.length()==0)
				address = segment.getStartAddress();
			else {
				address = Operation.isValidNumber(opLine.operands, "address");
			}
//			if(address < Program.startAddress || address>Program.startAddress + Program.size)
//				Input_Output.changeErrorFound(40);
			Program.programCounter = address;
		}
		
		locationCounter = Program.increaseLocationCounter(opLine.op, opLine.operands, locationCounter);//increse the counter based on operation size
		//Input_Output.errorMessage();

	}

	/**
	 * return the program created
	 * @return Program segment
	 */
	public Program getProgram() {
		return segment;
	}

	
}
