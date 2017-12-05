
public interface AU17Registers {

	public int getRegister(int registerNum);
	
	public void setRegister(int registerNum, int value);
	
	public void update_ccr(int value);
	
	public void printStatus();
}
