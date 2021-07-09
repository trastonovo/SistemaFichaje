package gui;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class Log {

	public Log(String fileName) throws SecurityException, IOException {
		
		File f = new File(fileName);
		
		if(!f.exists())
			f.createNewFile();
		
		fh = new FileHandler(fileName, true);
		
		logger.addHandler(fh);
		logger.setUseParentHandlers(false);
		
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}
	
	 public String stackTraceToString(Throwable e) {
		    StringBuilder sb = new StringBuilder();
		    for (StackTraceElement element : e.getStackTrace()) {
		        sb.append(element.toString());
		        sb.append("\n");
		    }
		    return sb.toString();
		}
	
	//public?
	public Logger logger = Logger.getLogger(Logger.class.getName());
	private static FileHandler fh;
}
