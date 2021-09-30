
/**
 * 
 */

package PingServer;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Assaad
 *
 */

//Server capable of processing ping requests over UDP.
public class PingServer {
	private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100; // ms

	public static void main(String[] args) throws Exception {
		// Get command line arguments.
		if (args.length != 1) {
			System.out.println("Required arguments: port");
			return;
		}
		int port = Integer.parseInt(args[0]);
		// Create Random object to
		// simulate packet loss and network delay.
		Random random = new Random();
		// Create a datagram socket object
		// for receiving and sending UDP packets
		// through the port specified on the command line.
		DatagramSocket socket = new DatagramSocket(port);
		
		// Main infinite loop.
		while (true) {
			// Create a placeholder for incoming UDP packet.
			DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
			socket.receive(request);
			// Print data received from client.
			printData(request);
			// Simulate packet loss.
			if (random.nextDouble() < LOSS_RATE) {
				System.out.println("Reply not sent.");
				continue;
			}
			// Simulate network delay.
			Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
			// Send reply.
			InetAddress clientHost = request.getAddress();
			int clientPort = request.getPort();
			byte[] buf = request.getData();
			DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
			socket.send(reply);
			System.out.println("Reply sent.");
		} // end while(true)
		
	} // end main method
		// Print received data out.

	private static void printData(DatagramPacket request) throws Exception {
		byte[] buf = request.getData();
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		// Print Information relating to received data.
		System.out.println("Received from " + request.getAddress().getHostAddress() + ": " + new String(line));
	} // end of printData method
} // end of public class
