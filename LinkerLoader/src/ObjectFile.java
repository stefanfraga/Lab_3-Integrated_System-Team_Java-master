

import java.util.ArrayList;

class ObjectFile {
	
	private String fileName;
	private ArrayList<String> fileRecords;
	private int memoryFootprint;
	
	public ObjectFile(String fileName) {
		this.fileName = fileName;
		this.fileRecords = new ArrayList<String>();
		this.memoryFootprint = 0;
	}
	
	
	/**
	 * Get the name of the object file
	 * 
	 * @return the name of the file as a String
	 */
	public String getFileName() {
		return fileName;
	}
	
	
	/**
	 * Add a String record to the object file
	 * 
	 * @param record
	 * @return status of the append operation as a boolean
	 */
	public boolean appendRecord(String record) {
		boolean isAppended = false;
		if (!record.isEmpty()) {
			fileRecords.add(record);
			isAppended = true;
		}
		return isAppended;
	}
	
	
	/**
	 * Get the record of the object file at the specified line number index
	 * 
	 * @param index
	 * @return the desired record as a String
	 */
	public String getRecord(int index) {
		String record = "";
		if (index<fileRecords.size() && index>-1) {
			record =  fileRecords.get(index);
		}
		else {
			/*
			 * TODO
			 * Error: index out of bounds
			 */
		}
		return record;
	}
	
	
	/**
	 * Removes the record at the index and replaces it with the 
	 * modification record
	 */
	public boolean modifyRecord(int index, String modification) {
		boolean isModified = false;
		if (index>0 && index<fileRecords.size()) {
			fileRecords.set(index, modification);
			isModified = true;
		}
		return isModified;
	}
	
	
	public int getNumRecords() {
		return fileRecords.size();
	}
	
	public boolean setMemSize(int size) {
		boolean isSet = false;
		if 	(size>0 && size<65536)
			this.memoryFootprint = size;
			isSet = true;
		return isSet;
	}
	
	public int getMemSize() {
		return this.memoryFootprint;
	}
	
}
