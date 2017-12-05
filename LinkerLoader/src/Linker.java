
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Linker {

	private final static int HEADER_LOC = 0;
	private final static int ORIG_ADDR = 11;
	private final static int HEX_RADIX = 16;
	private final static int ADDR_LOC = 5;
	private final static int EXT_LOC = 9;
	private final static int X9_STRLEN = 2;
	private final static int X10_STRLEN = 3;
	private final static String X9_STR = "X9";
	private final static String X10_STR = "X10";
	private final static int S_STRLEN = 2;
	private final static int R = 1;
	private final static int A = 0;
	private final static int[] TRAP_VECTOR = new int[] {0x21,0x22,0x23,0x25,0x31,0x33,0x43};
	

	private ArrayList<ObjectFile> objectFiles;
	private ObjectFile linkedFile;
	private int memoryFootprint;
	private int initialProgLoadAddr;
	private int initialProgStartAddr;
	private Map<String, int[]> externalTable;
	private boolean isRelocatable;
	
	public Linker() {
		objectFiles = new ArrayList<ObjectFile>();
		memoryFootprint = 0;
		initialProgLoadAddr = 0;
		externalTable = new TreeMap<String, int[]>();
		isRelocatable = false;
	}
	
	
	/**
	 * Link object files using a two-pass algorithm
	 * 
	 * @return the name of the Linked file
	 */
	public String createLinkedFile(String linkedName) {
		String name = linkedName.replace(".o","");
		while (name.length() != 6) {
			name = name + "_";
		}
		this.linkedFile = new ObjectFile(name);
		
		firstPass();
		if (Input_Output.executing)secondPass();
		
		createFile(linkedName);
		
		return linkedName;//linkedFile.getFileName();
	}
	
	
	/**
	 * First pass of the Linker
	 * 1.	Calculate the total memory size of the program
	 * 2.	Get the Initial Program Load Address
	 * 3.	For each segment:
	 * 		a)	Add Symbols to the External Symbol Table
	 * 		b)	Calculate the Program Load Address
	 * 	4. Check that all external symbols are defined
	 */
	private void firstPass() {
		String curRecord = "";
		String extension = "";
		int progLoadAddr = 0;
		
		/*
		 * 1. Calculate total mem size
		 */
		memoryFootprint = totalMemSize();
		/*
		 * 2. Get IPLA
		 */
		checkRelocatable();
		if (isRelocatable)
			initialProgLoadAddr = getIPLA();
		else
			initialProgLoadAddr = getAbsoluteAddress();
		setIPSA();
		progLoadAddr = initialProgLoadAddr;
		
		/*
		 * 3. for each segment, add symbols to external symbol table
		 * and calculate PLA
		 */
		for (ObjectFile file : objectFiles) {
			
			/*
			 * Add prog name to symbol table with PLA
			 */
			curRecord = file.getRecord(0);
			String segName = curRecord.substring(1, 7);
			if (externalTable.containsKey(segName)) {
				/*
				 * TODO
				 * Error: program name cannot have already been defined
				 */
				Input_Output.changeErrorFound(50);
			}
			externalTable.put(segName, new int[]{progLoadAddr, A});
			
			/*
			 * Add each entry record to the table with its address
			 * (address = PLA + offset)
			 */
			for (int i=1; i<file.getNumRecords(); i++) {
				curRecord = file.getRecord(i);
				if (curRecord.charAt(0)=='N') {
					String symbolName = curRecord.substring(1,7);
					while(symbolName.charAt(symbolName.length()-1) == '_')
						symbolName = symbolName.substring(0,symbolName.length()-1);
					
					if (externalTable.containsKey(symbolName)) {
						/*
						 * TODO
						 * Error: external symbol cannot have already been defined
						 */
						Input_Output.lineNumber = i+1;
						Input_Output.fileToRead = file.getFileName();
						Input_Output.changeErrorFound(51);
					}
					
					int symbolOffset;
					int symbolAddr;
					if (curRecord.length() > ORIG_ADDR && curRecord.charAt(ORIG_ADDR)=='M') {
						symbolOffset = Integer.parseInt(curRecord.substring(7,ORIG_ADDR), HEX_RADIX);
						symbolAddr = progLoadAddr + symbolOffset;	
						externalTable.put(symbolName, new int[] {symbolAddr,R});
					}
					else {
						symbolAddr = Integer.parseInt(curRecord.substring(7), HEX_RADIX);
						externalTable.put(symbolName, new int[] {symbolAddr,A});
					}
				}
			}
			
			/*
			 * increment PLA by program size for next program's PLA
			 */
			progLoadAddr += file.getMemSize();
		}
		
		/*
		 * Check that all extension symbols in text records 
		 * have been defined
		 */
		for (ObjectFile file : objectFiles) {
			for (int i=1; i<file.getNumRecords(); i++) {
				curRecord = file.getRecord(i);
				if (curRecord.charAt(0)=='T' && curRecord.length()>EXT_LOC) {
					extension = curRecord.substring(EXT_LOC);
					if (extension.startsWith(X9_STR)) {
						extension = extension.substring(X9_STRLEN);
					} else if (extension.startsWith(X10_STR)) {
						extension = extension.substring(X10_STRLEN);
					}
					else if (extension.startsWith("S") || extension.startsWith("T")) {
						extension = extension.substring(S_STRLEN);
					}
					if (!extension.startsWith("M") && !(externalTable.containsKey(extension))) {
						/*
						 * TODO
						 * Error: all extensions must now be in 
						 * external symbol table
						 */
						Input_Output.lineNumber = i + 1;
						Input_Output.fileToRead = file.getFileName();
						Input_Output.changeErrorFound(54);
					}
				}
			}
		}
		
		Input_Output.changeErrorFound(0);	
	}
	
	
	
	/**
	 * Second pass of the linker
	 * 1.	Set IPLA
	 * 2.	For each segment:
	 * 		a) 	Save PLA
	 * 		b) 	For each Text record
	 * 			(i) 	Calculate memory relocation
	 * 			(ii) 	Relocate record: Absolute, Relative, External
	 * 		c) 	Load the word ( Done in Loader )
	 * 3.	Transfer control to first segment ( Done in Loader )
	 * 
	 */
	private void secondPass() {
		String progHeader = "";
		String linkedHeader = "";
		String entryRecord = "";
		String textRecord = "";
		String modifiedText = "";
		String extension = "";
		String modifiedExt = "";
		String linkedEnd = "";
		int textAddr = 0;
		int pla =  0;
		
		/*
		 * 1. Set IPLA
		 * 		Add header record to linked file
		 */
		linkedHeader = "H" + linkedFile.getFileName() 
				+ adjustLength(Integer.toHexString(initialProgLoadAddr),4) 
				+ adjustLength(Integer.toHexString(memoryFootprint),4);
		linkedFile.appendRecord(linkedHeader);
		
		/*
		 * Add entry records to linked file
		 */
//		for (Map.Entry<String, Integer> symbol : externalTable.entrySet()) {
//			entryRecord = "N" + symbol.getKey() 
//					+ adjustLength(Integer.toHexString(symbol.getValue()),4);
//			System.out.println(entryRecord);
//			linkedFile.appendRecord(entryRecord);
//		}
		
		/*
		 * 3. For each segment
		 */
		for (ObjectFile file : objectFiles) {
			/*
			 * 3.a save PLA
			 */
			progHeader = file.getRecord(0);
			progHeader = progHeader.substring(1, 7);
			pla = externalTable.get(progHeader)[0];

			/*
			 * 3.b For each text record
			 */
			for (int i=1; i<file.getNumRecords(); i++) {
				/*
				 * 3.b.i Calculate mem relocation
				 */
				if(file.getRecord(i).charAt(0) == 'T') {
					textRecord = file.getRecord(i).substring(ADDR_LOC);
					int addr = Integer.parseInt(file.getRecord(i).substring(1,ADDR_LOC),HEX_RADIX);
					modifiedText = "T" + adjustLength(Integer.toHexString(addr+pla),4) + textRecord;
					file.modifyRecord(i, modifiedText);
				}
				
				/*
				 * 3.b.ii Relocate record instruction address
				 */
				int bitValue = 16;
				boolean absolute = false;
				boolean trap = false;
				if (file.getRecord(i).length()>EXT_LOC && file.getRecord(i).charAt(0) == 'T') {
					modifiedExt = file.getRecord(i).substring(0,EXT_LOC);//-1
					extension = file.getRecord(i).substring(EXT_LOC);
					if (extension.startsWith(X9_STR)) {
						bitValue = 9;
						extension = extension.substring(X9_STRLEN);
					} else if (extension.startsWith(X10_STR)) {
						bitValue = 16;
						extension = extension.substring(X10_STRLEN);
					}
					else if (extension.startsWith("S")) {
						bitValue = Integer.parseInt(extension.substring(1,2));
						extension = extension.substring(S_STRLEN);
						absolute = true;
					}
					else if(extension.startsWith("T")) {
						bitValue = Integer.parseInt(extension.substring(1,2));
						extension = extension.substring(S_STRLEN);
						trap = true;
					}
					else {
						extension = progHeader;
					}
					textAddr = Integer.parseInt(modifiedExt.substring(ADDR_LOC), HEX_RADIX);
					if (externalTable.containsKey(extension)) {
						if(absolute && externalTable.get(extension)[1] == 0) {
							textAddr +=Integer.parseInt(adjustLength(Integer.toBinaryString(externalTable.get(extension)[0]),bitValue),2);
						}
						else if(trap && externalTable.get(extension)[1] == 0) {
							int trapValue = Integer.parseInt(adjustLength(Integer.toBinaryString(externalTable.get(extension)[0]),bitValue),2);

							trap = false;
							for(int j = 0; j< TRAP_VECTOR.length; j++) {//check if he value is one of the values in the TRAP_VECTOR array
								if(trapValue == TRAP_VECTOR[j])
									trap = true;
							}
							if(trap) textAddr += trapValue;
							else {
								Input_Output.lineNumber = i + 1;
								Input_Output.fileToRead = file.getFileName();
								Input_Output.changeErrorFound(59);
							}
							
						}
						else if (absolute || trap) {
							System.out.println(file.getRecord(i));
							Input_Output.lineNumber = i + 1;
							Input_Output.fileToRead = file.getFileName();
							Input_Output.changeErrorFound(58);
						}
						else 
							textAddr +=Integer.parseInt(adjustLength(Integer.toBinaryString(externalTable.get(extension)[0]),bitValue),2);
					}
					modifiedText = modifiedExt.substring(0,ADDR_LOC) + adjustLength(Integer.toHexString(textAddr),4);
					file.modifyRecord(i, modifiedText);
				}
				
				/*
				 * Add text records to linked file
				 */
				if(file.getRecord(i).charAt(0) == 'T')linkedFile.appendRecord(file.getRecord(i));	
			}
			Input_Output.changeErrorFound(0);
		}
		
		/*
		 * Add end record to linked file
		 * 
		 * TODO
		 * Not sure about where to assign the END record address.
		 * Maybe get the address of the END record in the 'main'
		 * object file.
		 */
		linkedEnd = "E" + adjustLength(Integer.toHexString(initialProgStartAddr),4);
		linkedFile.appendRecord(linkedEnd);
		
	}
	
	/**
	 * Get the Initial Program Load Address by prompting the user 
	 * through the UI
	 * 
	 * @return user given start address as an int
	 */
	private int getIPLA() {
		int ipla = UI.retrieveStartAddress();
		return ipla;
	}
	
	/**
	 * Get the Initial Program Start Address by prompting the user 
	 * through the UI
	 * 
	 * @return user given start address as an int
	 */
	private void setIPSA() {
		String endRecord = objectFiles.get(0).getRecord(objectFiles.get(0).getNumRecords()-1);
		if(isRelocatable) initialProgStartAddr = Integer.parseInt(endRecord.substring(1), HEX_RADIX) + initialProgLoadAddr;
		else initialProgStartAddr = Integer.parseInt(endRecord.substring(1), HEX_RADIX);
	}
	
	
	
	/**
	 * Sum the program sizes of each of the object files
	 * 
	 * @param files
	 * @return the total memory size of all object files as a short
	 */
	private int totalMemSize() {
		int totalSize = 0;
		for (int i=0; i<objectFiles.size(); i++) {
			totalSize += progSize(objectFiles.get(i));
		}
		return totalSize;
	}
	
	/**
	 * Get the program memory size as specified by the 
	 * 
	 * @param file
	 * @return the memory size of the program
	 */
	public int progSize(ObjectFile file) {
		int size = 0;
		String head = file.getRecord(HEADER_LOC);
		if(head.length()>15) head = head.substring(ORIG_ADDR, 15);
		else head = head.substring(ORIG_ADDR);

		try {
			size = (int) Integer.parseInt(head, HEX_RADIX);
		}
		catch (Exception e) {
			/*
			 * TODO
			 * Error: parse string error
			 */
			Input_Output.changeErrorFound(52);
		}
		file.setMemSize(size);
		return size;
	}

	/**
	 * Produces error is if the file is not relocatable and there are multiple files
	 * 
	 * @param file
	 * @return the memory size of the program
	 */
	public void checkRelocatable() {
		for (ObjectFile file : objectFiles) {
			String head = file.getRecord(HEADER_LOC);
			if ((!(head.length() > 15) || !(head.charAt(15) == 'M')) && objectFiles.size() != 1 )
				Input_Output.changeErrorFound(53);
			else if (head.length() > 15 && head.charAt(15) == 'M') 
				isRelocatable = true;
			else if (objectFiles.size() == 1)
				isRelocatable = false;
		}
	}
	
	public int getAbsoluteAddress() {
		int address = 0;
		ObjectFile file = objectFiles.get(0);
		String head = file.getRecord(HEADER_LOC);
		head = head.substring(8,11);
		try {
			address = (int) Integer.parseInt(head, HEX_RADIX);
		}
		catch (Exception e) {
			/*
			 * TODO
			 * Error: parse string error
			 */
			Input_Output.changeErrorFound(52);
		}
		return address;
	}
	
	/**
	 * Read object file(s) from Assembler output
	 * 
	 * @param file
	 */
	public void addFile(String fileName) {
		ObjectFile file = new ObjectFile(fileName);
		BufferedReader reader = null;
		int lineCount = 0;
        String line;
        /*
         * Basic file opening and error catching for a file
         */
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
        		Input_Output.lineNumber = lineCount;
            Input_Output.changeErrorFound(41);
        }
        /*
         * Basic error catching for reading from a file
         */
        try {
        	while ((line = reader.readLine()) != null) {		
        		lineCount++;
        		file.appendRecord(line);
            }
        } catch (Exception e) {
    		Input_Output.lineNumber = lineCount;
            Input_Output.changeErrorFound(41);
        }
        try {
        	reader.close();
        } 
        catch (Exception e) {
    		Input_Output.lineNumber = lineCount;
        	Input_Output.changeErrorFound(41);
        }
        objectFiles.add(file);
        

	}
        
    

    private boolean createFile(String fileName) {    
		boolean created = false;
    	BufferedWriter objectFileWriter = null;
		try {
			File objectFile = new File(fileName);
		    objectFileWriter = new BufferedWriter(new FileWriter(objectFile));
		} catch (Exception e) {
			Input_Output.changeErrorFound(42);
		}
	    
		for (int i=0; i<linkedFile.getNumRecords(); i++) {
			try {
				objectFileWriter.write(linkedFile.getRecord(i));
				objectFileWriter.newLine();
			} catch (IOException e) {
				/*
				 * TODO
				 * Error: write failed
				 */
				Input_Output.changeErrorFound(43);
			}
		}
		
		try {
			objectFileWriter.close();
		} catch (Exception e) {
			Input_Output.changeErrorFound(42);
		}
		return created;
		
	}
    
	/**Lengthens or reduces the string to the desired length by adding 0's
	 * @param binary original string
	 * @param length the desired length
	 * @return string of desied length
	 */
	public static String adjustLength(String binary, int length) {
		while (binary.length() <length) {//add zero's until length
			binary = '0' + binary;
		}
		while (binary.length() > length) {//remove first character until length
			binary = binary.substring(1);
		}
		return binary;
	}
}		

