
public interface STables {
	
	public final static int ADD_AND = 0;
	public final static int OFFSET9 = 1;
	public final static int  BLANK = 2;
	public final static int INDEX = 3;
	public final static int JUMPR = 4;
	public final static int NOT = 5;
	public final static int TRAP = 6;
	
	public final static int MAX_TABLE_SIZE = 100;
	public final static int VARIABLE = -1;
	
	/**
	 * Adds an element to the symbol table represented as a map with key, label, and value an array of parameters value and relocatable.
	 * does not add element if map already contains element with the same key. 
	 * @param label key for element in map, name  of symbol
	 * @param value value of the symbol
	 * @param relocatable bitwise indicator if the symbol value is relocatable {only if the program is relocatable}
	 * @updates Input_Output.found_error changes value when the symbol table exceeds maximum size or a symbol is redefined
	 */
	public void add (String label, int value, int relocatable, int external);
	
	/**
	 * returns the first value in the array of value for the symbol table represented as a map.
	 * returns null if the symbol does not exist in the table
	 * @param symbol the key of the symbol to find in the map
	 * @return first element of the array of values for symbol
	 */
	public Integer getValue (String symbol);
	
	/**
	 * returns the second value in the array of value for the symbol table represented as a map.
	 * returns 0 (absolute) if the symbol does not exist in the table
	 * @param symbol the key of the symbol to find in the map
	 * @return second element of the array of values for symbol, bitwise value representing relocation
	 */
	public Boolean isRelocatable(String symbol);
	
	/**
	 * returns the second value in the array of value for the machineOp table represented as a map.
	 * returns -1 if the key is not found in the map
	 * @param op the key for the element in the map
	 * @return int the size, second element of the array of value for a machineOp
	 */
	public int getOpSize(String op);
	
	/**
	 * returns the value for the psuedoOp table represented as a map.
	 * returns -1 if the key is not found in the map
	 * @param op the key for the element in the map
	 * @return the value corresponding to the key parameter
	 */
	public int getPsuedoOpSize(String op);
	
	/**
	 * returns the first value in the array of value for the machineOp table represented as a map.
	 * returns -1 if the key is not found in the map
	 * @param op the key for the element in the map
	 * @return int the opcode, first element of the array of value for a machineOp
	 */
	public int getOpcode(String op);
	
	/**
	 * returns the third value in the array of value for the machineOp table represented as a map.
	 * <pre> op must be a key to an element in the map, must exist in the table
	 * @param op the key for the element in the map
	 * @return String the type, third element of the array of value for a machineOp
	 */
	public Integer getType (String op);
}
