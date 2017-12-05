

public class Simulator {

    public boolean executing;
    public int mode;
    public long userTimeLimit;
    public long startTime;

    Registers registers;
    Memory memory;

    public int QUIET = 0;
    public int TRACE = 1;
    public int STEP = 2;

    public int MAX_INSTRUCTIONS = 200;
    public int current_instruction = 1;
    
    /**
     * constructor of simulator object
     * @param firstMode
     * @param timeLimit
     */
    public Simulator(int firstMode, long timeLimit) {
        //set executing to true, mode and timeLimit to those arguments specified
        this.executing = true;
        this.mode = firstMode;
        this.userTimeLimit = timeLimit;
    }

    public void updateMaxInstructions(int max) {
		this.MAX_INSTRUCTIONS = max;
    }
    
    /**
     * 
     * @param
     */
    public void simulateProgram(Memory fileMemory) {
    		//create new registers and memory objects
    		registers = new Registers();
    		memory = fileMemory;
    		Interpreter fileInterpreter = new Interpreter(this.registers, this.memory, this);
    		
    		//for trace and step print initial machine status
    		if (this.mode != QUIET) {
				printMachineStatus();
			}
    		
    		
    		//run interpreter until halt instruction and until timelimit
    		startTime = System.currentTimeMillis(); 
    		while ((this.MAX_INSTRUCTIONS >= this.current_instruction)&&(System.currentTimeMillis()- startTime < userTimeLimit && this.executing) || (this.mode == STEP && this.executing)) {
    			
    			//call first line interpreter
    			fileInterpreter.interpretInstruction();
    			this.current_instruction++;
    		}

    		if (this.MAX_INSTRUCTIONS < this.current_instruction) {
    			System.out.println("Your program has exceeded the maximum number of instrucitons");
    			System.out.println("It ran " + this.current_instruction + " instructions");
    		}
    		if (this.mode != QUIET) {
				printMachineStatus();
			}
    }

    /**
     * 
     * @param
     * @return
     */
    public void printMachineStatus() {
        //print memory page
        this.memory.printStatus();
        //print registers
        this.registers.printStatus();
    }
    
    /**
     * 
     * @param
     * @return
     */
    public void haltExecution() {
        //used for step mode ?
        this.executing = false;
    }

    public void changeMode(int newMode) {
        this.mode = newMode;
    }

}
