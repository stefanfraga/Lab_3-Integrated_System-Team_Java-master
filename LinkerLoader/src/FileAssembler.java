import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class FileAssembler {
	
	public static final int RECORD_MAX_NUM = 200;
	public static int records;
	public static Writer objectFileWriter;
	public static Writer listingFileWriter;
	public static String fileToRead;

	public void FileAssember() {
	
	}
	
	/**compiles the assembly code into a executable that can be read by the simulator
	 * @param file
	 * @return The name of the object file created
	 */
	public String assembleFile (String file) {
		fileToRead = file;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileToRead);//create file reader
		}
		catch(Exception e) {
			Input_Output.changeErrorFound(41);
		}
		
		BufferedReader bufferedReader = null; 
		try {
			bufferedReader = new BufferedReader (fileReader);
		}catch(Exception e) {
			Input_Output.changeErrorFound(41);
		}
		
		Input_Output.lineNumber = 1;//start at line 1
		String line = null;
		FirstPass passOne = new FirstPass ();// create first pass
		
		try {
			line = bufferedReader.readLine();
		}catch(Exception e) {
			Input_Output.changeErrorFound(41);
		}
		while((line.length() == 0 || line.charAt(0) == ';') && Input_Output.executing) {
			if(line.length() == 0) {
				Input_Output.changeErrorFound(29);
			}
			if (records == RECORD_MAX_NUM) {
				Input_Output.changeErrorFound(14);
			}
			try {
				line = bufferedReader.readLine();
			}catch(Exception e) {
				Input_Output.changeErrorFound(41);
			}
			Input_Output.lineNumber++;
		}
		if( Input_Output.executing && (line.length() <13 || !(line.toUpperCase().substring(9,14).equals(".ORIG")))) {
			Input_Output.executing = false;
			Input_Output.changeErrorFound(21);
		}
		else if (Input_Output.executing) {
			passOne.parseLine(line);//read .ORIG
			if (records == RECORD_MAX_NUM) {
				Input_Output.changeErrorFound(14);
			}
			Input_Output.lineNumber++;//increase counter
		}
		try {
			while((line = bufferedReader.readLine()) != null && Input_Output.executing) {//read each line in first pass 
				Input_Output.changeErrorFound(0);
				if(line.length() == 0) 
					Input_Output.changeErrorFound(29);
				else if(line.charAt(0) != ';') {//read unless comment
					passOne.parseLine(line);
				}	
				//errorMessage();
				if (records == RECORD_MAX_NUM) {
					Input_Output.changeErrorFound(14);
				}
				if (Input_Output.executing) Input_Output.lineNumber++;
			}
		}catch(Exception e) {
			Input_Output.changeErrorFound(41);
		}
			if(Input_Output.executing && !FirstPass.end) {//no .END since reader finished produce error
				Input_Output.changeErrorFound(20);
			}
			Program program = passOne.getProgram();
		try {
			bufferedReader.close();//close reader
			fileReader.close();
		}	catch(Exception e) {
			Input_Output.changeErrorFound(41);
		}
		try {
			fileReader = new FileReader(fileToRead);//open new reader for same file
			bufferedReader = new BufferedReader (fileReader);
		}	catch(Exception e) {
			Input_Output.changeErrorFound(42);
		}
		
		int periodIndex = fileToRead.indexOf('.');
		if (periodIndex >= 0) {
			fileToRead = fileToRead.substring(0,periodIndex);
		}
		

		try {
			File objectFile = new File(fileToRead +".o");
		    objectFileWriter = new BufferedWriter(new FileWriter(objectFile));//create writers for Object and Listing file
		    
		    File listingFile = new File(fileToRead + ".lst");
		    listingFileWriter = new BufferedWriter(new FileWriter(listingFile));
		} catch (Exception ex) {
			Input_Output.changeErrorFound(42);
		}
		    SecondPass passTwo = new SecondPass(program);//create pass two object
		    //System.out.println(bufferedReader.readLine());
		try {
		    if(Input_Output.executing) {
		    		Input_Output.changeErrorFound(0);
		    		Input_Output.lineNumber = 1;
//		    		line = bufferedReader.readLine();
//		    		while(line.length()== 0 || line.charAt(0) == ';') { //until .ORIG
//		    			line =  bufferedReader.readLine();
//		    			Input_Output.lineNumber++;
//				}
//				passTwo.parseLine(line);//.ORIG
				while((line = bufferedReader.readLine()) != null && Input_Output.executing) {//read each line in first pass
					Input_Output.changeErrorFound(0);
					if(line.length() > 0 && line.charAt(0) != ';') 
						passTwo.parseLine(line);
					Input_Output.changeErrorFound(0);
					if (Input_Output.executing) Input_Output.lineNumber++;
				}
		    }
			bufferedReader.close();//close readers
			fileReader.close();
			objectFileWriter.close();//close writers
			listingFileWriter.close();
		} catch (Exception ex) {
			Input_Output.changeErrorFound(42);
		} finally {
		   try {objectFileWriter.close();} catch (Exception ex) {/*ignore*/}
		}
		Input_Output.changeErrorFound(0);	
		return fileToRead +".o";
	}
	
	
	
	/**print string parameter to Object file
	 * @param record
	 */
	public static void printRecords(String record) {
		record = record.replace(" ","");
		try {
				objectFileWriter.write(record);
				records++;
		}
		catch (Exception e) {
			Input_Output.changeErrorFound(43);
		}
	}
	
	/**print string parameters to Listing file, using line number indicate the line being read
	 * line number = -1 line number will be L
	 * @param record
	 * @param line
	 */
	public static void printListing(String record, String line) {
		String space = "     ";
		try {
			if (Input_Output.lineNumber < 0)
				listingFileWriter.write(record + "\t Line: " + 'L' + space.substring(1) + line + '\n');
			else
				listingFileWriter.write(record + "\t Line: " + Input_Output.lineNumber + space.substring(Integer.toString(Input_Output.lineNumber).length()) + line + '\n');
		}
		catch (Exception e) {
			Input_Output.changeErrorFound(43);
			System.out.println(e);
		}
	}
}
