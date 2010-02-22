package src.com.googlecode.csse460_2010.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {
	static private int port, timeout;
	static private String host, name, room, inputLn;
	static private InetAddress addr;
	static private SocketAddress sockaddr;
	static private Socket skt;
	static private boolean processRaw = false;
	static private BufferedReader readFromSkt;
	static private PrintWriter writeToSkt;

	public static void main(String[] args) {
		/*
		 * We start by reading the XML file to communicate with the server.
		 */
		try {
			XMLParser.loadNParse("clientConf.xml");
		} catch (Throwable e) {
			System.err.println("Error while loading the XML file!");
			e.printStackTrace();
			System.exit(0);
		}
		port = XMLParser.getCnxPort();
		timeout = XMLParser.getCnxTimeout();
		host = XMLParser.getCnxHost();
		/*
		 * Then we print the welcome message to the user.
		 */
		System.out.println(XMLParser.getClientMsgs().get("welcome"));
		name = System.in.toString();
		System.out.println(XMLParser.parseMsg(XMLParser.getClientMsgs().get("welcome"),XMLParser.class));
		try {
			addr = InetAddress.getByName(host);
			sockaddr = new InetSocketAddress(addr, port);

			skt = new Socket();

			/*
			 *  This method will block no more than timeoutMs.
			 *  If the timeout occurs, SocketTimeoutException is thrown.
			 */
			skt.connect(sockaddr, timeout);
			readFromSkt = new BufferedReader(new InputStreamReader(skt
					.getInputStream()));
			writeToSkt = new PrintWriter(skt.getOutputStream(), true);

			writeToSkt.println(name);
			while ((inputLn = readFromSkt.readLine()) != null) {
				process(inputLn);
			}
			
			readFromSkt.close();
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void process(String str) {
		if (processRaw) { // if we are processing a raw input then we simply
			// output it, no questions asked
			System.out.println(str);
		} else if (str.startsWith("endraw")) {
			processRaw = false; // we don't print out anything as endraw is on
			// it own line
		} else if (!str.startsWith("raw")) {
			processRaw = true; // we don't print out anything as raw is on it
			// own line
		} else {
			// then we look up the meaning in the XMLParser class.
		}
	}
}