import java.util.Map;
import java.util.TreeMap;

public class Memory {

	public int PC;
	public Map<Integer,Integer> memoryMap;
	public int size;
	public int startAddress;
	
	/* create memory object implemented by a Map */
	public Memory() {
		memoryMap = new TreeMap<Integer,Integer>();
	}
	
	/* creates a new element in the memoryMap or replaces an old element with the same address */
    /**
     * Updates a cell in memory to the new integer value
     * 
     * @param address the integer address between 0 and 65535 of the memory cell to update
     * @param value the value to update the memory cell to
     */
	public void setCell(int address, int value) {
		if (address <0) {
			//change sign extend from 0 to 1, interpret number as positive
			String binaryString = Interpreter.reduceTo16Bit(Integer.toBinaryString(address));
			address = Integer.parseInt(binaryString,2);
		}
		try {
			memoryMap.put(address, value);
		}
		catch(Exception e) {
			System.out.println("Warning: Unable to Write to Memory");
		}
	}
	
	/* returns the value in the memoryMap corresponding to the desired address */
    /**
     * Returns the int value inside the specified cell
     * 
     * @param address the integer address between 0 and 65535 of the memory cell to find
     * @return the value inside the memory cell
     */
	public int getCell(int address) {
		Integer value;
		if (address <0) {
			//change sign extend from 0 to 1, interpret number as positive
			String binaryString = Interpreter.reduceTo16Bit(Integer.toBinaryString(address));
			address = Integer.parseInt(binaryString,2);
		}
		value = memoryMap.get(address);
		if (value == null) {
			memoryMap.put(address, 0);
			value = 0;
		}
		return value;
	}
	
	/*set size of the memory and start address */
    /**
     * 
     * 
     * @param setSize
     * @param startAddress
     * @updates size
     */
	public void setSize(int segmentSize, int startAddress) {
		this.size = segmentSize;
	}
	
    /**
     * Updates PC to the new value
     * 
     * @param value the integer address between 0 and 65535 to update the PC to
     * @updates PC
     */
	public void setPC(int value) {
		if (value <0) {
			//change sign extend from 0 to 1, interpret number as positive
			String binaryString = Interpreter.reduceTo16Bit(Integer.toBinaryString(value));
			value = Integer.parseInt(binaryString,2);
		}
		this.PC = value;
	}
	
	/* get the value of the first 7 bits of the PC value */
    /**
     * Returns a string of the first 7 bits of the PC
     * 
     * @return page a string of the first 7 bits of the PC
     */
	public String getPCPage() {
		//PC value to binary string
		String binaryString = Integer.toBinaryString(this.PC);
		binaryString = Interpreter.zeroExtend(binaryString);
		//remove first 7 bits
		String page = binaryString.substring(0,7);
		return page;
	}
	
	/* increments the PC by one address */
    /**
     * Increments the PC by 1
     * 
     * @updates PC
     */
	public void incrementPC() {
		if(this.PC == 65535) {
			this.PC = 0;
		}
		else {
			this.PC++;
		}
	}

	/* returns the PC value to the caller */
    /**
     * Returns the current value of the PC
     * 
     * @return the current value of PC
     */
	public int getPC() {
		return this.PC;
	}

    /**
     * Prints the status of machine machine memory, lists every address currently in use and the value stored in it
     * 
     */
	public void printStatus() {
		System.out.println("Memory Segment");
		for (Map.Entry<Integer, Integer> cell : this.memoryMap.entrySet())
		{
		    System.out.println("Address: 0x" + hexZeroExtend(cell.getKey()) + " Value: 0x" + hexZeroExtend(cell.getValue()));
		}
		System.out.println("Program Counter: 0x"+ hexZeroExtend(this.PC));	
	}
	
	public static String hexZeroExtend(int value) {
		String hex = "";
		hex = Integer.toHexString(value);
		while (hex.length() < 4)
			hex = '0' + hex;

		return hex;
	}
	
}