
public interface AU17Simulator {

	public void simulateProgram(Memory fileMemory);
	
	public void printMachineStatus();
	
	public void haltExecution();
	
	public void changeMode(int newMode);
}
