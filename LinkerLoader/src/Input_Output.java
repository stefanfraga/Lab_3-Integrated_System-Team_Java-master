import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class Input_Output {

	
	public static boolean executing = true;
	public static Integer lineNumber;
	public static String fileToRead;
	
	public static void main(String args[]){
		UI simulator = new UI();
		int inputLength  = 0;
		String objectFileName = "";
		FileAssembler assembler = new FileAssembler ();
		Linker fileLinker = new Linker();
		String simulationFile = "";
		
		//If there was no file names provided
		if (args.length == 0) {
			changeErrorFound(44);
		}
		
		//for each String/file name provided
		while (args.length != 0 && inputLength != args.length && executing) {
			
			//set the current file
			fileToRead = args[inputLength];
			
			//compile assembly code into an object file
			objectFileName = assembler.assembleFile(fileToRead);	
			
			//add the object file to the linker to be compbined
			fileLinker.addFile(objectFileName);
			
			//increase counter
			inputLength++;
		}
		
		//If no error were found during assembly of each file
		if (executing) { 
			//prompt user for file name
			String linkedFileName = UI.getLinkedFileName();
			
			//combine all object files into executable
			simulationFile = fileLinker.createLinkedFile(linkedFileName);
		}
			
			//simulate execution of executable file
		if (executing)	simulator.beginSimulation(simulationFile);
		
	}
	
	
	/**
	 * print error message indicated by value of found_error
	 */
	public static int found_error = 0; //is this the same as is_executing referenced above?
	
	/**Compares the error found to the previous error, if they are the same or one of the indicated warnings the error is not printed until a new error is found
	 * @param error
	 */
	public static void changeErrorFound(int error) {
		//was the previous error repeated or a warning
		if (found_error == 0 || found_error == 29 || found_error == error || found_error == 35 || found_error == 36 || found_error == 37)
			//change error
			found_error = error;
		else {
			//print previous error message and change to new error
			errorMessage();
			found_error = error;
			
		}
	}
	
	//Whenever someone wants to define an error at your new error int value to the switch case
	/** Compares the value of the error found and prints the corresponding error message. 
	 * @return the error message printed
	 */
	public static String errorMessage() {
		int lineNum = lineNumber;
		String error = "";
		
		
		switch (found_error) {//print error message indicated by value of found_error
		case 0:
			break;
		case 1:
			error = fileToRead +" Line: "+ lineNum + " Error (" + found_error + "): Invalid first character of label ";
			break;
		case 2: 
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Operation Name";
			break;
		case 3: 
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid .STRZ operand";
			break;
		case 4:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Numerical Operand";
			break;
		case 5:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Symbol Operand, not in symbol table";
			break;
		case 6:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Register Number";
			break;
		case 7:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Literal Not defined";
			break;
		case 8:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Address operand outside program segment";
			break;
		case 9:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid instruction structure";
			break;
		case 10:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Label";
			break;
		case 11:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Redefining Symbol Label";
			break;
		case 12:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Exceeding Maximum Symbol Table Size";
			break;
		case 13:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Exceeding Maximum Literal Table Size";
			break;
		case 14:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Exceeding Maximum Record Number";
			break;
		case 15:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): relocatable value for register operand";
			break;
		case 16:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): desired load address changes with page of PC";
			break;
		case 17:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): no program name ";
			break;
		case 18:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): relocatable program exceeds page size ";
			break;
		case 19:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): duplicate .ORIG operantion ";
			break;
		case 20:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): missing .END operation ";
			break;
		case 21:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): operation before .ORIG ";
			break;
		case 22:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): operation after .END ";
			break;
		case 23:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): ADD/AND operation using relocatable value ";
			break;
		case 24:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Trap operation using relocatable value ";
			break;
		case 25:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): BLKW operand value 0";
			break;
		case 26:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): invalid use of relocatable value for index";
			break;
		case 27:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Program separated in memory";
			break;
		case 28:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Litteral used in operation other than LD";
			break;
		case 29:
			error = fileToRead +" Line: " + lineNum + " Warning: Blank line";
			break;
		case 30:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): no operand";
			break;
		case 31:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): no file name entered";
			break;
		case 32:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): no operation";
			break;
		case 33:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): invalid use of forward referance, symbol not in table";
			break;
		case 34:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid numerical operand, out of range";
			break;
		case 35:
			error = fileToRead +" Line: " + lineNum + " Warning: Operand values not used for operation";
			break;
		case 37:
			error = fileToRead +" Warning: More calls to RET then JSR and JSRR";
			break;
		case 38:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): No Label for .EQU where label is required";
			break;
		case 39:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Label for .END psuedo operation";
			break;
		case 40:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): start address outside page ";
			break;
		case 41:
			error = fileToRead +" Line: " + lineNum + " Error Reading file";
			break;
		case 42:
			error = fileToRead +" Line: " + lineNum + " Error Writing file";
			break;
		case 43:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Unable to write to new file";
			break;
		case 44:
			error = fileToRead +" Error: No files provided";
			break;
		case 45:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Error Local ENT Symbol not defined";
			break;
		case 46:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Error Local ENT Symbol is absolute";
			break;
		case 47:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Error exteral symbol used for .EQU or .BLKW";
			break;
		case 48:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Error exteral symbol used in Absolute program, must be relocatable";
			break;
		case 49:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Error ENT/EXT must proceed all instructions";
			break;
		case 50:
			error = "Error : Segment name in " + fileToRead + "  already defined";
			break;
		case 51:
			error =  "Error : External symbol in  " + fileToRead + " Line: " + lineNum + "  already defined";
			break;
		case 52:
			error =  "Error : Invalid size value in  " + fileToRead;
			break;
		case 53:
			error =  " Error : File is not relocatable,  " + fileToRead;
			break;
		case 54:
			error =  "Error : External symbol in  " + fileToRead + " Line: " + lineNum + "  not defined";
			break;
		case 55:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Label for .ENT/.EXT psuedo operation";
			break;
		case 56:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Definition of .ENT uses .EXT label";
			break;
		case 57:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): External Symbol used for Register value in AND/ADD";
			break;
		case 58:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Relative External Symbol used were absolute value is necessary";
			break;
		case 59:
			error = fileToRead +" Line: " + lineNum + " Error (" + found_error + "): Invalid Trap Operation Value";
			break;
		default:
			System.out.println(found_error);
			error = "Unknown Error";
			
		}
		
		//if the error was a warning do not stop execution
		if (found_error == 29 || found_error == 35|| found_error == 36|| found_error == 37)
			System.out.println(error);
		else if (found_error > 0) {
			executing = false;
			System.out.println(error);
		}
		return error;
	}
	
	
}
