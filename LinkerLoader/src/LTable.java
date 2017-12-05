
public interface LTable {
	
	public static final int MAX_TABLE_SIZE = 50;

	/**
	 * Adds a element to the table of literals with the key as the parameter value and its value as 0.
	 * @updates Input_Output.found_error updates the value when the Literal table exceeds maximum size
	 * @param value the key value for the element to add to the table
	 */
	public void add (int value);
	
	/**
	 * returns the address value of the given key.
	 * @param value the numerical value representing the key for the table
	 * @return the address value stored with the key
	 */
	public Integer getAddress (int value);
	
	/**
	 * Changes the address values of the literal table so each literal has a different incremental address starting at parameter LocationCounter.
	 * @param locationCounter starting address to assign to literals in the table
	 * @return the address of the last literal + 1
	 */
	public int setAddresses(int locationCounter);
	
	/**returns the size of the table
	 * @return size of the table, number of elements
	 */
	public int getTableSize();
	
	/**
	 * Creates a text record for each literal in the table. The text records contain T, hex form of address value, and hex form of key value.
	 * @calls InputOutput.printRecords(String Record)
	 */
	public void printTextRecords();
	
	/**
	 * creates a listing record for each literal in the table. The record contains hex form of address value, hex form of key value, and binary form of key value.
	 *  The line will be blank.
	 * @calls Input_Output.printListing(String record, String Line)
	 */
	public void printListings();
	
	
}
