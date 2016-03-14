package file_utils;

public class ProtocolEnum {
	public static final int UNKNOWN = -1;
	public static final int BACKUP = 1;
	public static final int STORED = 2;
	public static final int GETCHUNK = 3;
	public static final int CHUNK = 4;
	public static final int DELETE = 5;
	public static final int REMOVED = 6;
	
	public static final int min = 1;
	public static final int max = 6;
}