import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

/**
 * Interpreter executes the instructions pointed to by the PC within memory.
 *
 * @author Team Java
 *
 */
public class Interpreter {

    Registers registers;
    Memory memory;
    Simulator simulator;
    public int MAX_MEMORY_ADDRESS = 65535;

    /**
     * Constructor that takes a Registers, Memory and Simulator object
     *
     * @param fileRegisters
     *            Registers object used to manipulate registers 0-7
     * @param memoryMap
     *            Memory object used to access cells in memory
     * @param fileSimulator
     *            Simulator object the provides the mode of simulation
     * @updates registers
     * @updates memory
     * @updates simulator
     *
     */
    public Interpreter(Registers fileRegisters, Memory memoryMap,
            Simulator fileSimulator) {
        this.registers = fileRegisters;
        this.memory = memoryMap;
        this.simulator = fileSimulator;
    }

    /**
     * Fetches the next line of code to be executed. Parses the machine code and
     * decides which Operation to call.
     */
    public void interpretInstruction() {
        // Fetches the PC
        int pc = this.memory.getPC();
        // Increments the PC
        this.memory.incrementPC();
        // Fetch instruction from memory
        int instruction = this.memory.getCell(pc);

        // interpret opcode
        // call corresponding function
        String instructionString = Integer.toBinaryString(instruction);

        //zero extend instruction to correct length
        instructionString = zeroExtend(instructionString);

        if (instructionString.startsWith("0001")) {
            this.add(instructionString);
        } else if (instructionString.startsWith("0101")) {
            this.and(instructionString);
        } else if (instructionString.startsWith("0000")) {
            this.brx(instructionString);
        } else if (instructionString.startsWith("1000")) {
            this.dbug(instructionString);
        } else if (instructionString.startsWith("0100")) {
            this.jsr(instructionString);
        } else if (instructionString.startsWith("1100")) {
            this.jsrr(instructionString);
        } else if (instructionString.startsWith("0010")) {
            this.ld(instructionString);
        } else if (instructionString.startsWith("1010")) {
            this.ldi(instructionString);
        } else if (instructionString.startsWith("0110")) {
            this.ldr(instructionString);
        } else if (instructionString.startsWith("1110")) {
            this.lea(instructionString);
        } else if (instructionString.startsWith("1001")) {
            this.not(instructionString);
        } else if (instructionString.startsWith("1101")) {
            this.ret(instructionString);
        } else if (instructionString.startsWith("0011")) {
            this.st(instructionString);
        } else if (instructionString.startsWith("1011")) {
            this.sti(instructionString);
        } else if (instructionString.startsWith("0111")) {
            this.str(instructionString);
        } else if (instructionString.startsWith("1111")) {
            this.trap(instructionString);
        } else {
            System.out.print("Warning: Invalid Instruction Discarded");
        }
    }

    /**
     * Executes the LD instruction, sets the value in the register specified in
     * the instruction parameter to the value in memory at the address created
     * from operands in the instruction parameter.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     * @updates registers
     * @updates memory
     */
    private void ld(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        String drString = instruction.substring(4, 7);
        int destinationRegister = Integer.parseInt(drString, 2);
        addOperand(destinationRegister, aReg);
        //characters 15:9 of PC concatenated with pgoffset9;
        String addressString = (this.memory.getPCPage()
                + instruction.substring(7));
        int address = Integer.parseInt(addressString, 2);
        addOperand(address, aMem);
        //set the value of the destination register to the value stored at the calculated address;
        int value = this.memory.getCell(address);
        this.registers.setRegister(destinationRegister, value);
        this.registers.updateCCR(value);
        
		if (this.simulator.mode != 0)
			printInstruction(" LD", instruction, aReg, aMem);
        if (this.simulator.mode == 2)
            step();
    }

    /**
     * Executes the LDI instruction. Loads the value stored in memory using
     * indirect addressing mode and stores in the DR. Updates the CCRs based on
     * the value stored in DR
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     * @updates registers
     * @updates memory
     */
    private void ldi(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        String drString = instruction.substring(4, 7);
        int destinationRegister = Integer.parseInt(drString, 2);
        addOperand(destinationRegister, aReg);
        //characters 15:9 of PC concatenated with pgoffset9;
        String addressString = (this.memory.getPCPage()
                + instruction.substring(7));
        int address = this.memory.getCell(Integer.parseInt(addressString, 2));
        addOperand(address, aMem);
        //set the value of the destination register to the value stored at the calculated address;
        int value = this.memory.getCell(address);
        this.registers.setRegister(destinationRegister,value);
        this.registers.updateCCR(value);
        //print instruction
		if (this.simulator.mode != 0)
			printInstruction(" LDI", instruction, aReg, aMem);
        if (this.simulator.mode == 2)
            step();
    }

    /**
     * Executes the ST instruction. Stores the value specified in SR in memory
     * using direct addressing mode.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     * @updates registers
     * @updates memory
     */
    private void st(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        String srString = instruction.substring(4, 7);
        int sourceRegister = Integer.parseInt(srString, 2);
        addOperand(sourceRegister, aReg);
        //characters 15:9 of PC concatenated with pgoffset9;
        String addressString = (this.memory.getPCPage()
                + instruction.substring(7));
        int address = Integer.parseInt(addressString, 2);
        addOperand(address, aMem);
        //set the value of the destination register to the value stored at the calculated address;
        int value = this.registers.getRegister(sourceRegister);
        this.memory.setCell(address, value);

        if (this.simulator.mode != 0) {
            printInstruction(" ST", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the STI instruction. Stores the value specified in SR in memory
     * using indirect addressing mode.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void sti(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        String srString = instruction.substring(4, 7);
        int sourceRegister = Integer.parseInt(srString, 2);
        addOperand(sourceRegister, aReg);
        //characters 15:9 of PC concatenated with pgoffset9;
        String addressString = (this.memory.getPCPage()
                + instruction.substring(7));
        int address = this.memory.getCell(Integer.parseInt(addressString, 2));
        addOperand(address, aMem);
        //set the value of the destination register to the value stored at the calculated address;
        int value = this.registers.getRegister(sourceRegister);
        this.memory.setCell(address, value);

        if (this.simulator.mode != 0) {
            printInstruction(" STI", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Updates the R7 register to the current PC value
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void ret(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        this.memory.setPC(this.registers.getRegister(7));
        addOperand(7, aReg);

        if (this.simulator.mode != 0) {
            printInstruction(" RET", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the NOT instruction. Takes this bitwise value of the SR register
     * and stores in DR
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void not(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        int destR = Integer.parseInt(instruction.substring(4, 7), 2);
        addOperand(destR, aReg);
        int srcR = Integer.parseInt(instruction.substring(7, 10), 2);
        addOperand(srcR, aReg);
        int sr = this.registers.getRegister(srcR);
        String srBinary = zeroExtend(Integer.toBinaryString(sr));//return positive int <16 Bit or negative in 32 Bit
        System.out.println(srBinary);
        String bitWiseComp = "";
        for (int i = 0; i < 16; i++) {
            if (srBinary.substring(i, i + 1).equals("1")) {
                bitWiseComp = bitWiseComp + "0";
            } else {
                bitWiseComp = bitWiseComp + "1";
            }
        }
        this.registers.setRegister(destR, Integer.parseInt(bitWiseComp, 2));

        if (this.simulator.mode != 0) {
            printInstruction(" NOT", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the JSRR instruction. Updates the PC using register indexed
     * addressing mode. If the L bit is set the R7 register is updated to the
     * current PC before it is updated.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     * @updates PC
     */
    private void jsrr(String instruction) {//test case base register value is negative is it possible?
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        int L = Integer.parseInt(instruction.substring(4, 5));
        int index6 = Integer.parseInt(zeroExtend(instruction.substring(10)), 2);
        int baseR = Integer.parseInt(instruction.substring(7, 10), 2);
        aReg.add(baseR);
        if (L == 1) {
            this.registers.setRegister(7, this.memory.getPC());
            addOperand(7, aReg);
        }
        int register = this.registers.getRegister(baseR);
        int value = index6 + register;
        if (value > this.MAX_MEMORY_ADDRESS) {

            this.memory.setPC((index6 + register) - this.MAX_MEMORY_ADDRESS);// the value wraps around to 0
        } else {
            this.memory.setPC(index6 + register);
        }

        if (this.simulator.mode != 0) {
            printInstruction(" JSRR", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the JSR instruction. Updates the PC using direct addressing
     * mode. If the L bit is set the R7 register is updated to the current PC
     * before it is updated.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void jsr(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        int L = Integer.parseInt(instruction.substring(4, 5));
        String pcPage = this.memory.getPCPage();
        String pgOffset9 = instruction.substring(7);
        String newAddress = pcPage + pgOffset9;
        if (L == 1) {
            this.registers.setRegister(7, this.memory.getPC());
            addOperand(7, aReg);
        }
        this.memory.setPC(Integer.parseInt(newAddress, 2));

        if (this.simulator.mode != 0) {
            printInstruction(" JSR", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    //print PC, registers, ccrs
    /**
     * Executes the DBUG instruction. Displays the contents of the machine
     * registers to system.out
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void dbug(String instructionString) {
        System.out.println("PC: " + this.memory.PC);
        this.registers.printStatus();
    }

    /**
     * Executes the BRX instruction. The branch is taken if any of the specified
     * CCRs given in the instruction are set to 1.
     *
     * The destination of the branch is formed by concatenating bits [15:9] of
     * the PC with bits [8:0] of the PG offset specified in the instruciton
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     * @updates PC
     */
    private void brx(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get pc page aka PC[15:9]
        String pcPage = this.memory.getPCPage();
        //get destination register number
        String destinationString = pcPage + instruction.substring(7);
        int destinationNum = Integer.parseInt(destinationString, 2);
       	int nzp = Integer.parseInt(instruction.substring(4,7));
       	switch (nzp) {
       		case 100://100
       			if(this.registers.CCR_N) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRN
       		case 10://010
       			if(this.registers.CCR_Z) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRZ
       		case 1://001
       			if(this.registers.CCR_P) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRP
       		case 110://110
       			if(this.registers.CCR_N || this.registers.CCR_Z) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRNZ
       		case 101://101
       			if(this.registers.CCR_N || this.registers.CCR_P) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRNP
       		case 111://111
       			this.memory.setPC(destinationNum);
                addOperand(-1, aReg);
       			break;//BRNPZ
       		case 11://011
       			if(this.registers.CCR_Z || this.registers.CCR_P) {
       				this.memory.setPC(destinationNum);
       				addOperand(-1, aReg);
       			}
       			break;//BRZP
       	default:
       	
       	}
        
        if (this.simulator.mode != 0) {
            printInstruction(" BRX", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the two ADD instructions
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void add(String instruction) {

        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get destination register number
        String destinationString = instruction.substring(4, 7);
        int destinationNum = Integer.parseInt(destinationString, 2);

        addOperand(destinationNum, aReg);//add register number to operand

        //get source register number
        String sourceString = instruction.substring(7, 10);
        int sourceNum = Integer.parseInt(sourceString, 2);

        addOperand(sourceNum, aReg);// add register number to operand

        //determine how the operands will be retrieved from 10th bit
        char tenthBit = instruction.charAt(10);
        String sum = "";
        int addedValue;
        if (tenthBit == '0') {
            //get second source register number
            String source2String = instruction.substring(13);
            int source2Num = Integer.parseInt(source2String, 2);

            addOperand(source2Num, aReg);//add register number to operand

            //add together value from both source registers
            sum = binaryAddition(Integer.toBinaryString(this.registers.getRegister(source2Num)), Integer.toBinaryString(this.registers.getRegister(sourceNum)));
            addedValue = Integer.parseInt(sum, 2);
        } else {
            //get binary form of immediate operand sign extended
            String source2String = instruction.substring(11);
            while (source2String.length() < 16) {
            		source2String = source2String.charAt(0) + source2String;
            }
            //add the value of the register and the immediate operand
            sum = binaryAddition(source2String, Integer.toBinaryString(this.registers.getRegister(sourceNum)));
            addedValue = Integer.parseInt(sum, 2);
        }
        if (addedValue > MAX_MEMORY_ADDRESS) {
        		addedValue = addedValue - MAX_MEMORY_ADDRESS;
        }

        //set destination register to the value received
        this.registers.setRegister(destinationNum, addedValue);
        //update the CCRS
        this.registers.updateCCR(addedValue);

        if (this.simulator.mode != 0) {
            printInstruction(" ADD", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the two AND instructions
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void and(String instruction) {

        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get destination register number
        String destinationString = instruction.substring(4, 7);
        int destinationNum = Integer.parseInt(destinationString, 2);
        addOperand(destinationNum, aReg);

        //get source register number
        String sourceString = instruction.substring(7, 10);
        int sourceNum = Integer.parseInt(sourceString, 2);
        addOperand(sourceNum, aReg);

        //get binary string for source value
        int value1 = this.registers.getRegister(sourceNum);
        sourceString = Integer.toBinaryString(value1);
        //sign extend the binary string
        sourceString = zeroExtend(sourceString);
        //if negative reduce from 32 bit to 16 
        sourceString = reduceTo16Bit(sourceString);

        //determine how the operands will be retrieved from 10th bit
        char tenthBit = instruction.charAt(10);

        String source2String;
        int andedValue;
        if (tenthBit == '0') {
            //get second source register number
            source2String = instruction.substring(13);
            int source2Num = Integer.parseInt(source2String, 2);
            aReg.add(source2Num);

            //get the binary form of second source value
            int value2 = this.registers.getRegister(source2Num);
            source2String = Integer.toBinaryString(value2);
            //sign extend the binary source 2 string
            source2String = zeroExtend(source2String);//if value is positive zero extend to 16 bit
            source2String = reduceTo16Bit(source2String);// if value is netative 32 bit, reduce to 16 bit

        } else {
            //get second source value
            source2String = instruction.substring(11);
            while (source2String.length() < 16) {
            		source2String = source2String.charAt(0) + source2String;
            }
            //operands.add(new BigInteger(signExtend32(source2String),2).intValue());
        }
            String andedBinary = "";
            for (int i = 0; i < 16; i++) {
                char nextChar = '0';
                if (sourceString.charAt(i) == source2String.charAt(i)
                        && sourceString.charAt(i) == '1') {
                    nextChar = '1';
                }
                andedBinary = andedBinary + nextChar;
            }
            andedBinary = this.signExtend32(andedBinary);// extend to 32 bit for correct integer parsing
            andedValue = new BigInteger(andedBinary, 2).intValue();


        this.registers.setRegister(destinationNum, andedValue);
        this.registers.updateCCR(andedValue);

        if (this.simulator.mode != 0) {
            printInstruction(" AND", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the LDR instruction. Loads the value stored in memory using
     * register indexed addressing mode and stores in the DR. Updates the CCRs
     * based on the value stored in DR
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void ldr(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get destination register number
        String destinationString = instruction.substring(4, 7);
        int destinationNum = Integer.parseInt(destinationString, 2);
        addOperand(destinationNum, aReg);
        //get base register number from bits 8-6
        String baseString = instruction.substring(7, 10);
        int baseNum = Integer.parseInt(baseString, 2);
        //get register value from base register
        int baseR = this.registers.getRegister(baseNum);
        addOperand(baseNum, aReg);
        //get index as string from bits 5-0
        String indexString = instruction.substring(10);
        //convert index string to int value
        int indexNum = Integer.parseInt(indexString, 2);
        //add index to base register
        int resultAddr = indexNum + baseR;
        if (resultAddr > this.MAX_MEMORY_ADDRESS) {

            resultAddr = indexNum + baseR - this.MAX_MEMORY_ADDRESS;// the value wraps around to 0
        }
        //load destination register with new address
        this.registers.setRegister(destinationNum, this.memory.getCell(resultAddr));
        this.registers.updateCCR(resultAddr);

        if (this.simulator.mode != 0) {
            printInstruction(" LDR", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the LEA instruction. Loads the value in memory using immediate
     * addressing mode and stores in DR. Updates CCRs based on the value stored
     * in DR.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void lea(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get destination register number
        String destinationString = instruction.substring(4, 7);
        int destinationNum = Integer.parseInt(destinationString, 2);
        addOperand(destinationNum, aReg);

        //characters 15:9 of PC concatenated with pgoffset9;
        String addressString = (this.memory.getPCPage()
                + instruction.substring(7));
        //get integer of address to find value
        int address = Integer.parseInt(addressString, 2);
        //get value at memory address
        //int value = this.memory.getCell(address);
        //aMem.add(address);
        //set destination register with the value found
        //registers.setRegister(destinationNum, value);
        this.registers.setRegister(destinationNum, address);

        if (this.simulator.mode != 0) {
            printInstruction(" LEA", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the STR instruction. Stores the value specified in SR in memory
     * using register indexed addressing mode.
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void str(String instruction) {
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();

        //get source register number
        String sourceString = instruction.substring(4, 7);
        int sourceNum = Integer.parseInt(sourceString, 2);
        addOperand(sourceNum, aReg);

        //get base register number from bits 8-6
        String baseString = instruction.substring(7, 10);
        int baseNum = Integer.parseInt(baseString, 2);
        //get register value from base register
        int baseR = this.registers.getRegister(baseNum);
        addOperand(baseR, aReg);
        //get index as string from bits 5-0
        String indexString = instruction.substring(10);
        //convert index string to int value
        int indexNum = Integer.parseInt(indexString, 2);
        //add index to base register
        int resultAddr = indexNum + baseR;
        //save value in register to memory
        if (resultAddr > this.MAX_MEMORY_ADDRESS) {

            resultAddr = indexNum + baseR - this.MAX_MEMORY_ADDRESS;// the value wraps around to 0
        }
        int value = this.registers.getRegister(sourceNum);
        this.memory.setCell(resultAddr, value);
        addOperand(resultAddr, aMem);

        if (this.simulator.mode != 0) {
            printInstruction(" STR", instruction, aReg, aMem);
        }
        if (this.simulator.mode == 2) {
            this.step();
        }
    }

    /**
     * Executes the TRAP instruction. Parses the instruction for the trap
     * vector. Trap vector 0x21 - OUT writes the character in R0[7:0] to the
     * System.out Trap vector 0x22 - PUTS writes the null-terminated string
     * pointed to by R0 System.out Trap vector 0x23 - IN prompts the screen
     * asking for a single character of input the character is copied to the
     * screen and its ASCII value is stored in R0 the high 8 bits of R0 are set
     * to zero. Trap vector 0x25 - HALT execution is halted and a message is
     * written to System.out Trap vector 0x31 - OUTN the value of R0 is written
     * to System.out as a decimal integer Trap vector 0x33 - INN prompts the
     * screen asking for a decimal number input. the decimal integer is echoed
     * to the screen and store in R0 Trap vector 0x43 - RND a random number is
     * stored in R0
     *
     * @param instruction
     *            the 16 character string representing the 16bit OP code
     */
    private void trap(String instruction) {
        //determine the value of bits 7-0
        int trapNum = Integer.parseInt(instruction.substring(9), 2);
        this.registers.setRegister(7, this.memory.getPC());
        ArrayList<Integer> aReg = new ArrayList<Integer>();
        ArrayList<Integer> aMem = new ArrayList<Integer>();
        switch (trapNum) {
            case 0x21:
                //print the hex character at r0 [7:0]
                int regValue = this.registers.getRegister(0);
                //add to operands
                addOperand(0, aReg);
                String registerhex = Integer.toHexString(regValue);
                while (registerhex.length() > 2) {
                		registerhex = registerhex.substring(1);
                }
                char c = (char) Integer.parseInt(registerhex, 16); 
                System.out.println(c);
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP OUT", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                break;
            case 0x22:
                String output = "";
                //set char at non null value
                char nextChar = 'A';
                //get the address in Register 0
                int address = this.registers.getRegister(0);
                //get the string pointed to by the address in register 0
                while (nextChar != 0) {
                    int valueAtAddress = this.memory.getCell(address);
                    //convert to binary string
                    String binaryString = Integer
                            .toBinaryString(valueAtAddress);
                    //sign extend
                    binaryString = zeroExtend(binaryString);//if positive zero extend
                    binaryString = reduceTo16Bit(binaryString);// if negative reduce from 32 bit to 16 bit
                    //take the last 8 bits to represent ASCII character
                    binaryString = binaryString.substring(8);
                    //convert binary to ASCII character
                    nextChar = (char) Integer.parseInt(binaryString, 2);
                    output = output + nextChar;
                    if (address > this.MAX_MEMORY_ADDRESS) {
                        address = address - this.MAX_MEMORY_ADDRESS;
                    } else {
                        address++;
                    }
                    if (this.simulator.userTimeLimit
                            + this.simulator.startTime < System
                                    .currentTimeMillis()) {
                        break;
                    }
                }
                addOperand(0, aReg);
                
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP PUTS", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                System.out.println(output);
                break;
            case 0x23:
                System.out.println("Please Enter One Character: ");
                char input;
                try {
                    input = (char) System.in.read();
                    System.in.read(new byte[System.in.available()]);
                } catch (IOException e) {
                    System.out.println("Warning could not read in character");
                    input = '\0';
                }
                System.out.println("Characer Saved: " + input);
                addOperand(0, aReg);
                this.registers.setRegister(0, 0);
                this.registers.setRegister(0,input);
                this.registers.updateCCR(input);
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP IN", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                break;
            case 0x25:
                this.simulator.haltExecution();
                System.out.println("Simulation has been halted.");
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP HALT", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                break;
            case 0x31:
            		addOperand(0, aReg);
                int regVal = this.registers.getRegister(0);
                String integerBin = signExtend16(Integer.toBinaryString(regVal));
                int integer = Integer.parseInt(integerBin,2);
                if (integer > Short.MAX_VALUE)
            			integer = integer - 65536;
                System.out.println(integer);
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP OUTN", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                break;
            case 0x33:
                System.out.print(
                        "Please enter a value greater than " + Short.MIN_VALUE
                                + " and less than " + Short.MAX_VALUE);
                // read in integer value
                //create bufferedReader
                BufferedReader inputReader = new BufferedReader(
                        new InputStreamReader(System.in));
                //create String
                String intString = "";
                int value = 0;
                try {
                    intString = inputReader.readLine();
                } catch (IOException ioe) {
                    System.out.println("Unable to read input");
                    intString = "0";
                }
                try {
                		value = Integer.parseInt(intString);
                } catch (NumberFormatException e) {
                		System.out.println("Invalid number format number changed to 0");
                		value = 0;
                }
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            			System.out.println("Invalid number: Exceded Bounds");
            			System.out.println("Input number changed to 0");
            			value = 0;
                }
                System.out.println("Input number: " + value);
                	if (value < 0)
                		value = value + 65536;
                	this.registers.setRegister(0,value);
                addOperand(0, aReg);
                this.registers.updateCCR(value);
                if (this.simulator.mode != 0) {
                    printInstruction("TRAP INN", instruction, aReg, aMem);
                }
                if (this.simulator.mode == 2) {
                    this.step();
                }
                break;
            case 0x43:
            		Random rand = new Random();
                int rnd = rand.nextInt(65535);
				this.registers.setRegister(0,rnd);
				addOperand(0, aReg);
				this.registers.updateCCR(rnd);
				if (this.simulator.mode != 0)
					printInstruction("TRAP RND", instruction, aReg, aMem);
		        if (this.simulator.mode == 2) {
		            this.step();
		        }
				break;
			default:
				break;
				
		}
		//error if not one of trap calls 
	}

    /**
     * Pads a binary string with zeros until it is 16 characters long
     *
     * @return the given binaryString zero extended to be 16 characters long
     */
    public static String zeroExtend(String binaryString) {
        //increase the length of a binary string by adding 0's to the front
        while (binaryString.length() < 16) {
            binaryString = "0" + binaryString;
        }
        return binaryString;
    }

    private static String binaryAddition(String value1, String value2) {
    		String sum = "";
    		String op1 = zeroExtend(value1);
    		String op2 = zeroExtend(value2);
    		boolean carry = false;
    		for (int i = 15; i >= 0; i--) {
    			if (carry) {
    				if (op1.charAt(i)=='0' && op2.charAt(i)=='0') {
    					sum = '1' + sum;
    					carry = false;
    				}
    				else if (op1.charAt(i)=='1' && op2.charAt(i)=='1') {
    					sum = '1' + sum;
    					carry = true;
    				} else  {
    					sum = '0' + sum;
    					carry = true;
    				}
    			} else {
    				if (op1.charAt(i)=='0' && op2.charAt(i)=='0')
    					sum = '0' + sum;
    				else if (op1.charAt(i)=='1' && op2.charAt(i)=='1') {
    					sum = '0' + sum;
    					carry = true;
    				} else 
    					sum = '1' + sum;
    			}
    		}
    		return sum;
    }
    /**
     * Pads a binary string with 1's or 0's depending on sign until it is 16
     * characters long
     *
     * @return the given binaryString sign extended to be 16 characters long
     */
    public static String signExtend16(String binaryString) {
        //increase the length of the binary string by adding the sign bit, first bit repetitively to the front
		char signBit = '0';
		if (binaryString.length() == 16)
			signBit = binaryString.charAt(0);
        while (binaryString.length() < 16) {
            binaryString = signBit + binaryString;
        }
        return binaryString;
    }

    /**
     * Pads a binary string with 1's or 0's depending on sign until it is 32
     * characters long
     *
     * @return the given binaryString sign extended to be 32 characters long
     */
    public String signExtend32(String binaryString) {
        //increase the length of the binary string by adding the sign bit, first bit repetitively to the front
    		char signBit = '0';
    		if (binaryString.length() == 16)
    			signBit = binaryString.charAt(0);
        while (binaryString.length() < 32) {
            binaryString = signBit + binaryString;
        }
        return binaryString;
    }

    /**
     * Removes the first 16 characters of binary string
     *
     * @return the given binaryString without it's first 16 characters
     */
    public static String reduceTo16Bit(String binaryString) {
        return binaryString.substring(binaryString.length() - 16);
    }

    private void step() {
        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(System.in));
        String input = "";
        System.out.print("Press enter to continue to next instruction.");
        try {
            input = inputReader.readLine();
        } catch (IOException ioe) {
            System.out.println("Error reading input.");
        }
        switch (input) {
            case "q":
                this.simulator.mode = 0;
                break;
            case "t":
                this.simulator.mode = 1;
                break;
            case "m":
                this.memory.printStatus();
                this.registers.printStatus();
                System.out
                        .print("Press enter to continue to next instruction.");
                try {
                    input = inputReader.readLine();
                } catch (IOException ioe) {
                }
                break;
            default:
        }
        System.out.println();
    }

    private static void addOperand(int value, ArrayList<Integer> operandArray) {
        if (!operandArray.contains(value)) {
            operandArray.add(value);
        }
    }

    private static void printInstruction(String instructionName,
            String instructionBinary, ArrayList<Integer> aReg,
            ArrayList<Integer> aMem) {
        boolean affectedPC = false;
        String inst = "0x"
                + Memory.hexZeroExtend(Integer.parseInt(instructionBinary, 2));
        System.out.println(inst + "\t" + instructionName + " Operation");
        int operand1 = 0, operand2 = 0;
        if (aReg.size() > 0) {
            operand1 = aReg.remove(0);
            if (operand1 < 0) {
                affectedPC = true;
            } else {
                System.out
                        .print("\t" + instructionName + " used register(s): ");
                System.out.print("R" + operand1);
                if (aReg.size() == 0) {
                    System.out.print("\n");
                }
            }
        }
        while (aReg.size() > 0) {
            operand2 = aReg.remove(0);
            if (operand1 != operand2) {
                if (operand2 < 0) {
                    affectedPC = true;
                } else {
                    System.out.print(", R" + operand2);
                }
            }
            if (aReg.size() == 0) {
                System.out.print("\n");
            }
            operand1 = operand2;
        }
        if (aMem.size() > 0) {
            int operand = aMem.remove(0);
            System.out.println("\t" + instructionName + " used memory address: "
                    + zeroExtend(Integer.toBinaryString(operand)));
        }
        if (affectedPC) {
            System.out.println("\t" + instructionName + " modified the PC");
        }

    }
}

