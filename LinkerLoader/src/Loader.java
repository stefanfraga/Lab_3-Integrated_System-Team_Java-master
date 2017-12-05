import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Loader{

	String segment_name;
	public int MAX_MEMORY_SIZE = 65536;
	
	public Loader() {
		segment_name = "";
	}
	
	public Memory loadFile(String fileName) {
		Memory fileMemory = new Memory();
		//open file
		File file;
		try {
			file = new File(fileName);
		}
		catch(NullPointerException e) {
			System.out.println("Error: unable to open file");
			return null;// end current file load
		}
		try {
			FileReader file_reader = new FileReader(file);
			BufferedReader file_bufferedreader = new BufferedReader(file_reader);
			
			String fileLine = file_bufferedreader.readLine();
			fileLine = fileLine.toUpperCase();
			
			
			
			//read H from file
			//error if no H
			if (fileLine.charAt(0)!=72){
				System.out.println("Error: invalid header of file, need H");
				file_reader.close();// close reader
				return null;// end current file load return to caller with no memory
			}
			
			//read 6 character segment name
			this.segment_name = fileLine.substring(1,7);
			
			//read 4 hex initial address
			//error if character not hex
			String hexString = checkFourHex(fileLine.substring(7,11));
			
			//confirm that the length of the string is 4, if not invalid file
			if (hexString.length() < 4) {
				System.out.println("Error: invalid header of file, invalid hex value");
				file_reader.close();//close reader
				return null; //end current file load return to caller with no memory
			}
			int initial_address = Integer.parseInt(hexString, 16);
			
			//read 4 hex data size
			//error if character not hex
			hexString = checkFourHex(fileLine.substring(11,15));
			//confirm that the length of the string is 4, if not invalid file
			if (hexString.length() < 4) {
				System.out.println("Error: invalid header of file  in valid hex value");
				file_reader.close();
				return null; // end current file load
			}
			
			int size = Integer.parseInt(hexString, 16);
			
			
			//check if segment size is greater then machine memory
			if(size + initial_address>MAX_MEMORY_SIZE) {
				System.out.println("Error: Exceeded Max Memory Space");
				file_reader.close();
				return null;
			}
			
			fileLine = file_bufferedreader.readLine();
			fileLine = fileLine.toUpperCase();
			//for each text record that starts with T
			char firstChar = ' ';
			while(fileLine.length() > 0 && (firstChar = fileLine.charAt(0)) == 'T') {
				//get the hex value for the address in memory
				String addressString = checkFourHex(fileLine.substring(1,5));
				int address = Integer.parseInt(addressString, 16);
				
				//get the hex for the value to be saved in memory
				hexString = checkFourHex(fileLine.substring(5));
				//confirm that the length of the string is 4, if not invalid file
				if (hexString.length() < 4) {
					System.out.println("Warning: invalid text record discarded");
					System.out.println(fileLine);
					//get new line and continue through the while loop to next iteration
					fileLine = file_bufferedreader.readLine();
					firstChar = fileLine.charAt(0);
					continue;
				}
				int value = Integer.parseInt(hexString, 16);
				
				//make sure the address falls in the designated memory for the segment
				if (address < initial_address || address > (initial_address + size)) {
					System.out.println("Warning: invalid text record discarded");
					System.out.println(fileLine);
					fileLine = file_bufferedreader.readLine();
					firstChar = fileLine.charAt(0);
					continue;
				}
				fileMemory.setCell(address,value);
			
				
				fileLine = file_bufferedreader.readLine();
				firstChar = fileLine.charAt(0);
			}
			
		//if E does not start End record then invalid
		if (firstChar != 'E') {
			System.out.println("Error: invalid header file ");
			file_reader.close();
			return null; // end current file load
		}
		
		//get hex value for starting address of PC
		hexString = checkFourHex(fileLine.substring(1,5));
		if (hexString.length() < 4) {
			System.out.println("Error: invalid header file ");
			file_reader.close();
			return null; // end current file load
		}
		
		int pcAddress =  Integer.parseInt(hexString, 16);
		if (pcAddress < initial_address || pcAddress > (initial_address + size)) {
			System.out.println("Error: PC out of segment");
			file_reader.close();
			return null; // end current file load
		}
		fileMemory.setPC(pcAddress);
				
		file_reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + file.toString());
			return null;
		} catch (IOException e) {
            System.out.println("Unable to read file: " + file.toString());
            return null;
        } catch (NumberFormatException e){
        		System.out.println("Error: invalid header file ");
        		return null;
        }
		
		return fileMemory;
		
	}

	
	public boolean isHexChar(char testChar){
		if (!Character.isDigit(testChar) && !(testChar >= 41 && testChar <= 70) &&  !(testChar >= 97 && testChar <= 102)){
			return false;
		}
		return true;
	}
	
	public String checkFourHex(String hexLine) {
		String hexString = "";
		for (int i = 0; i<4; i++){
			if (!isHexChar(hexLine.charAt(i))) {
				break;
			}
			hexString = hexString + hexLine.charAt(i);
		}
		return hexString; 
	}
}
