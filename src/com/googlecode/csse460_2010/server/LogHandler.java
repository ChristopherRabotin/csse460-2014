package com.googlecode.csse460_2010.server;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

/**
 * This is the logging class. It will enable the game to be launched as a
 * background application. The server admin can then access the log file and see
 * all the logging information. The output is an XML file. Based on <a>http://publib
 * .boulder.ibm.com/infocenter/wasinfo/v6r0/index.jsp?topic=/com.ibm
 * .websphere.express.doc/info/exp/ae/rtrb_createhandler.html</a>
 * 
 * @author Christopher Rabotin
 * 
 */
public class LogHandler extends Handler {

	FileOutputStream fileOutputStream;
	PrintWriter printWriter;
/**
 * Constructor.
 * @param fn String representing the filename of the log 
 */
	public LogHandler(String fn) {
		super();
		if (fn == null || fn == "")
			fn = "serverlog.xml";
		try {
			// initialize the file
			fileOutputStream = new FileOutputStream(fn);
			printWriter = new PrintWriter(fileOutputStream);
			setFormatter(new XMLFormatter());
			printWriter.println("<Records>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws SecurityException {
		printWriter.println("</Records>");
		printWriter.close();
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
		printWriter.flush();
	}

	@Override
	public void publish(LogRecord record) {
		/*
		 * Ensure that this log record should be logged by this Handler
		 */
		if (!isLoggable(record))
			return;

		/*
		 * Output the formatted data to the file
		 */
		printWriter.println(getFormatter().format(record));

	}

}
