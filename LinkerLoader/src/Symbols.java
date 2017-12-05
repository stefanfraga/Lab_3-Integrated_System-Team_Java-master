import java.util.TreeMap;

public class Symbols implements STables{

	public final static int ADD_AND = 0;
	public final static int OFFSET9 = 1;
	public final static int  BLANK = 2;
	public final static int INDEX = 3;
	public final static int JUMPR = 4;
	public final static int NOT = 5;
	public final static int TRAP = 6;
	
	public final static int MAX_TABLE_SIZE = 100;
	public final static int VARIABLE = -1;
	
	private final static int ABSOLUTE = 0;
	
	public boolean relocatableProgram = false;
	
	public static int symbols = 0;
	
	
	static final TreeMap<String, int[]> machineOpTable = new TreeMap<String, int[]>();
	static final TreeMap<String, Integer> psuedoOpTable = new TreeMap<String, Integer>();
	
	public TreeMap<String, int[]> symbolTable;
	
	/**
	 * Symbols empty constructor
	 * creates three tables
	 * 		symbolTables contains elements added throughout execution
	 * 		machineOpTable contains operation names, opcodes, sizes, and format but does not change 
	 * 		psuedoOpTable contains psuedo operation names, and sizes but does not change
	 * adds all elements to machineOpTable and psuedoOpTable
	 */
	public Symbols() {
		symbolTable = new TreeMap<String, int[]>();
		
		machineOpTable.put("ADD", new int[] {1,1,ADD_AND});
		machineOpTable.put("AND", new int[] {5,1,ADD_AND});
		machineOpTable.put("BRP", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRZP", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRN", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRZ", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRNZ", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRNP", new int[] {0,1,OFFSET9});
		machineOpTable.put("BRNZP", new int[] {0,1,OFFSET9});
		machineOpTable.put("BR", new int[] {0,1,OFFSET9});
		machineOpTable.put("DBUG", new int[] {8,1,BLANK});
		machineOpTable.put("JSR", new int[] {4,1,OFFSET9});
		machineOpTable.put("JMP", new int[] {4,1,OFFSET9});
		machineOpTable.put("JSRR", new int[] {12,1,JUMPR});
		machineOpTable.put("JMPR", new int[] {12,1,JUMPR});
		machineOpTable.put("LD", new int[] {2,1,OFFSET9});
		machineOpTable.put("LDI", new int[] {10,1,OFFSET9});
		machineOpTable.put("LDR", new int[] {6,1,INDEX});
		machineOpTable.put("LEA", new int[] {14,1,OFFSET9});
		machineOpTable.put("NOT", new int[] {9,1,NOT});
		machineOpTable.put("RET", new int[] {13,1,BLANK});
		machineOpTable.put("ST", new int[] {3,1,OFFSET9});
		machineOpTable.put("STI", new int[] {11,1,OFFSET9});
		machineOpTable.put("STR", new int[] {7,1,INDEX});
		machineOpTable.put("TRAP", new int[] {15,1,TRAP});
		
		psuedoOpTable.put(".ORIG",0);
		psuedoOpTable.put(".END",0);
		psuedoOpTable.put(".EXT",0);
		psuedoOpTable.put(".ENT",0);
		psuedoOpTable.put(".EQU",0);
		psuedoOpTable.put(".FILL",1);
		psuedoOpTable.put(".STRZ",VARIABLE);
		psuedoOpTable.put(".BLKW",VARIABLE);
		
		
		
	}
	
	
	/* (non-Javadoc)
	 * @see STables#add(java.lang.String, int, int)
	 */
	public void add (String label, int value, int relocatable, int external) {
		for (int i = 0; i< label.length(); i++) {//is the label alphanumeric
			char nextChar = label.charAt(i);
			if(!(Character.isLetterOrDigit(nextChar))) {//check each character is it is alphanumeric
				Input_Output.changeErrorFound(10);//if not throw an error
				continue;
			}
		}
		if (relocatableProgram == false) {//if program is not relocatable each symbol will be absolute
			relocatable = ABSOLUTE;
		}
		if(symbolTable.containsKey(label)) {//redefined symbol, same label, throw error
			Input_Output.changeErrorFound(11);
		}
		else if (symbols == MAX_TABLE_SIZE) {//determine if adding symbol will exceed maximum table size
			Input_Output.changeErrorFound(12);
		}
		else {
			symbolTable.put(label, new int[] {value, relocatable, external});// add symbol to table
			symbols++;//increment table size counter
		}
	}
	
	/* (non-Javadoc)
	 * @see STables#getValue(java.lang.String)
	 */
	public Integer getValue (String symbol) {
		int[] value = symbolTable.get(symbol);//get value for symbol in table
		if(value == null) {// if the value is null, symbol is not in table
			return null;
		}
		return value[0];//return value of the symbol
		
	}
	
	/* (non-Javadoc)
	 * @see STables#isRelocatable(java.lang.String)
	 */
	public Boolean isRelocatable(String symbol) {
		boolean relocate = false;
		if(symbolTable.get(symbol) != null && symbolTable.get(symbol)[1] == 1) //get the value of the symbols relocatable element
			relocate = true;
		return relocate;
	}
	
	public Boolean isExternal(String symbol) {
		boolean relocate = false;
		if(symbolTable.get(symbol) != null && symbolTable.get(symbol)[2] == 1) //get the value of the symbols external element
			relocate = true;
		return relocate;
	}
	
	/* (non-Javadoc)
	 * @see STables#getOpSize(java.lang.String)
	 */
	public int getOpSize(String op) {
		int[] values = machineOpTable.get(op);//get values ofr key:op
		if(values == null)
			return -1;// if the op does not exist return -1
		return values[1];//return the size of the op 
	}
	/* (non-Javadoc)
	 * @see STables#getPsuedoOpSize(java.lang.String)
	 */
	public int getPsuedoOpSize(String op) {
		 Integer value = psuedoOpTable.get(op);//get the value for a psuedoOp
		 if (value == null)
			 value = -1;//if the psuedoOp does not exist return -1
		 return value;
	}
	/* (non-Javadoc)
	 * @see STables#getOpcode(java.lang.String)
	 */
	public int getOpcode(String op) {
		int[] values = machineOpTable.get(op);//get first element of value for MachineOp
		if(values == null) {
			return -1;//if the Op does not exist return -1
		}
		return values[0];
	}
	/* (non-Javadoc)
	 * @see STables#getType(java.lang.String)
	 */
	public Integer getType (String op) {
		 return machineOpTable.get(op)[2]; // return third element of the value for MachineOp, the type of operation
		
	}
	
	
}
