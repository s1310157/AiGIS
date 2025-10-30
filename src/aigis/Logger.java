package aigis;

public class Logger {

	public static void Info(String msg) {
		System.out.println(msg);
	}
	
	public static void Debug(String msg) {
		System.out.println(msg);
	}
	
	public static void Error(String msg) {
		System.err.println(msg);
	}
	
	public static void Error(Exception e) {
		e.printStackTrace();
	}
	
	public static void Error(Error e) {
		e.printStackTrace();
	}
}
