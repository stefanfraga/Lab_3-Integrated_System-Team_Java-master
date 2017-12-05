
public interface AU17Memory {

	public void setCell(int address, int value);
	
	public int getCell(int address);
	
	public void setSize(int segmentSize, int startAddress);
	
	public void setPC(int value);
	
	public String getPCPage();
	
	public void incrementPC();
	
	public int getPC();
	
	public void printStatus();
}
