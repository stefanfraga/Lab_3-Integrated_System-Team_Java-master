public class Registers{
	
	public int[] registerArray = new int[8];
	public boolean CCR_N;
	public boolean CCR_Z;
	public boolean CCR_P;
	
	public Registers() {
		for(int i = 0; i<8; i++) {
			this.registerArray[i] = 0;
		}
		this.CCR_N = false;
		this.CCR_Z = true;
		this.CCR_P = false;
	}
	
    /**
     * Returns the integer value inside the specified register
     * 
     * @param registerNum the number of the register to be accessed (0-7)
     * @return the value inside the register
     */
	public int getRegister(int registerNum) {
		return this.registerArray[registerNum];
	}
	
    /**
     * Updates the value of the specified register 
     * 
     * @param registerNum the number of the register to be updated (0-7)
     * @param value the value to set the register to
     * @updates this.registerArray
     */
	public void setRegister(int registerNum, int value) {
		this.registerArray[registerNum] = value;
	}
	
    /**
     * Updates the CCRs based on the integer input
     * 
     * @param value the value of the last operation
     * @updates CCR_N
     * @updates CCR_Z
     * @updates CCR_P
     */
	public void updateCCR(int value) {
		
		this.CCR_N = (value >= 32768);//set N to 1 or 0 depending on if the value is negative

		this.CCR_Z = (value == 0); //set Z to 1 or 0 depending on if the value is zero or not

		this.CCR_P = (value != 0 && value < 32768); //set P to 1 or 0 depending on if that value is positive or negative

	}

    /**
     * Prints the status of the machine registers and CCRs to System.out
     */
	public void printStatus() {
		//print r0 - r7
		System.out.println("Register values");
		for(int i = 0;i<8;i++) {
			//print register number and value in the register
			System.out.println("R"+i+":   0x" + Memory.hexZeroExtend(this.getRegister(i)));
		}
		System.out.println();
		//get ccr int values
		int n = 0;
		if (this.CCR_N == true) n=1;
		int z = 0;
		if (this.CCR_Z == true) z=1;
		int p = 0;
		if (this.CCR_P == true) p=1;
		
		//print ccrs
		System.out.println("CCR Values");
		System.out.println("N = " + n);
		System.out.println("Z = " + z);
		System.out.println("P = " + p);
		System.out.println();
	}
	
}