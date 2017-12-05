
public interface AU17Loader {

	public Memory LoadFile(String fileName);
	
	public boolean isHexChar(char testChar);
	
	public String checkFourHex(String hexLine);
}
