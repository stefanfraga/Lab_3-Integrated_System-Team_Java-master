
public interface ProgramPass {	
	
	/**Check line for syntax and operational errors
	 * @param line
	 * @update Output_Input.found_error when an error is produced
	 */
	public void parseLine(String line);
	
	
}
