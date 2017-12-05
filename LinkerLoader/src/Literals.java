import java.util.Map;
import java.util.TreeMap;

public class Literals implements LTable{

	public static final int MAX_TABLE_SIZE = 50;
	public static int literals = 0;
	
	public TreeMap<Integer, Integer> literalTable;
	
	/**
	 * Literals empty constructor
	 */
	public Literals() {
		literalTable = new TreeMap<Integer, Integer>(); 
	}
	
	/* (non-Javadoc)
	 * @see LTable#add(int)
	 */
	public void add (int value) {
		if (literals == MAX_TABLE_SIZE)
			Input_Output.changeErrorFound(13);//Error exceeded max table size
		else if(!literalTable.containsKey(value))//add literal to table without repeats
			literalTable.put(value, 0); //set value to 0 because address is not known
		literals++;
	}
	
	/* (non-Javadoc)
	 * @see LTable#getAddress(int)
	 */
	public Integer getAddress (int value) {
		Integer address  = literalTable.get(value);//returns the address value
		return address;
	}
	
	/* (non-Javadoc)
	 * @see LTable#setAddresses(int)
	 */
	public int setAddresses(int locationCounter) {
		for (Map.Entry <Integer, Integer> literal :literalTable.entrySet()) {//for each literal in the table
			literalTable.put(literal.getKey(),locationCounter);//set the value of each element to locationCounter, an address in memory
			locationCounter++;//increment locationCounter
		}
		return locationCounter; //address of last literal + 1
	}
	
	/* (non-Javadoc)
	 * @see LTable#getTableSize()
	 */
	public int getTableSize() {
		return literalTable.size();
	}

	/* (non-Javadoc)
	 * @see LTable#printTextRecords()
	 */
	public void printTextRecords() {
		String textRecord = "";
		for (Map.Entry <Integer, Integer> literal :literalTable.entrySet()) {//for each literal in the table
			int key = literal.getKey();//get values
			int address = literal.getValue();
			//form the text record 
			textRecord = 'T' + SecondPass.adjustLength(Integer.toHexString(address),4) + SecondPass.adjustLength(Integer.toHexString(key),4) + '\n';
			FileAssembler.printRecords(textRecord);//print to Object file
		}
		
	}
	/* (non-Javadoc)
	 * @see LTable#printListings()
	 */
	public void printListings() {
		String listing = "";
		for (Map.Entry <Integer, Integer> literal :literalTable.entrySet()) {//for each literal in the table
			int key = literal.getKey();//get values
			int address = literal.getValue();
			//form the listing record for a literal
			listing = SecondPass.adjustLength(Integer.toHexString(address),4) +"  "+SecondPass.adjustLength(Integer.toHexString(key),4)+ "    "+ SecondPass.adjustLength(Integer.toBinaryString(key),16);
			FileAssembler.printListing(listing, "");//print to Listing file
		}
		
	}
	
}
