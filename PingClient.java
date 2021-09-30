package PingServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class PingClient {

	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.out.println("Required arguments: host port");
			return;
		}
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		long[] rtts = new long[10]; 
		// Create a datagram socket object
		// for receiving and sending UDP packets
		// through the port specified on the command line.
		DatagramSocket socket = new DatagramSocket();
		// Set a timeout of 1000 ms for the client.
		socket.setSoTimeout(1000);
		Timestamp timeStamp;
		int recoveries = 0;
		String message;
		System.out.println(String.format("Pinging 8.8.8.8 with %d bytes of data:", 28));
		for (int i = 0; i < 10; i++) {
			// Prepare request.
			timeStamp = new Timestamp(System.currentTimeMillis());
			message = "PING" + i + timeStamp.toString();
			System.out.println(message);
			DatagramPacket request = new DatagramPacket(
					message.getBytes(),
					message.length(),
					InetAddress.getByName(host),
					port
					);
			// Create a placeholder for incoming UDP packet.
			DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
			try {
				int recovered = sendThenAwaitPacket(socket, request, response);
				rtts[i] = calculateRtt(timeStamp, new Timestamp(System.currentTimeMillis()));
				System.out.println(String.format("RTT = %d ms", rtts[i]));
				recoveries+=recovered;
				Thread.sleep(1000);
			} 
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		long minimum = Collections.min(Arrays.stream(rtts).boxed().collect(Collectors.toList()));
		long maximum = Collections.max(Arrays.stream(rtts).boxed().collect(Collectors.toList()));
		long average = (long) LongStream.of(rtts).average().getAsDouble();
		
		String output = String.format("Ping statistics for %s:\r\n"
				+ "    Packets: Sent = 10, Received = 10, Lost = 0, Retransmitted = %d \r\n"
				+ "Approximate round trip times in milli-seconds:\r\n"
				+ "    Minimum = %dms, Maximum = %dms, Average = %dms", host, recoveries, minimum, maximum, average);
		
		System.out.println(output);
		

	}
	
	private static int sendThenAwaitPacket(DatagramSocket socket, DatagramPacket request, DatagramPacket response) {
		//encapsulates sending and receiving UDP packet, counts and returns the number of retransmissions per packet.
		int recoveries = 0;
		while (true) {
			try {
				socket.send(request);
				socket.receive(response);
				// Print data received from server.
				printData(response);
				break;
				
			}
			catch (SocketTimeoutException e) {
				recoveries++;
				continue;
			} 
			catch (IOException e) {
				e.printStackTrace();
			} 
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return recoveries;
	}
	
	private static long calculateRtt(Timestamp a, Timestamp b) {
		long diff = b.getTime() - a.getTime();
		return diff;
	}
	
	private static void printData(DatagramPacket request) throws Exception {
		byte[] buf = request.getData();
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		// Print Information relating to received data.
		System.out.println("Received from " + request.getAddress().getHostAddress() + ": " + new String(line));
	} // end of printData method

}
