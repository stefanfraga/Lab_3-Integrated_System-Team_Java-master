
public interface ProgramI {

	/**sets the element relocatable and symbolTables.relocatableProgram equal to true
	 * sets the start address of the program to 0.
	 */
	public void setRelocatable();
	
	/**Set the the startAddress of program to the parameter address
	 * @param address the  address to set StartAddress
	 * @return the StartAddress after change of value
	 */
	public int setStartAddress(int address);
	
	/**Set the ProgramName of the program to the parameter name
	 * @param name the name of the program to set the value of ProgramName
	 */
	public void setProgramName(String name);
	
	/**return the symbolTable
	 * @return Symbols symbolTables variable 
	 */
	public Symbols getSymbols();
	
	/**return the literalTable 
	 * @return Literals LiteralTable variable
	 */
	public Literals getLiterals();
	
	/**return startAddress
	 * @return startAddress variable
	 */
	public int getStartAddress();
	
	/**return value of relocatable
	 * @return value of reloctable variable
	 */
	public boolean isProgramRelocatable();
	
	/** create header record of the Object file of the program includes 'H', ProgramName,hex of startAddress, and hex of size of program
	 * @return String header record for object file
	 */
	public String getHeaderRecord();
	
	/** creates end records of the object file of the program. includes 'E' hex of program counter 
	 * @return String end record for the object file
	 */
	public String getEndRecord();
	
	
}
