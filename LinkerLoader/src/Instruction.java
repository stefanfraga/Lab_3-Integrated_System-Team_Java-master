
public interface Instruction {

	/** determines if the label is a valid label, containing only alphanumeric and not starting with R or x
	 * @return true if label meets all requirements
	 */
	public boolean isValidLabel();
	
	/**removes all spaces from the front the given String
	 * @param text provided String
	 * @return suffix of text
	 */
	public String removeSpaces(String text);
}
