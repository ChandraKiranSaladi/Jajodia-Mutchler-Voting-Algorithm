import java.io.*;
import java.net.*;

public class TCPServer {
	String HostName;
	int PortNumber, UID;
	ServerSocket serversocket;
	Node dsNode;

	public TCPServer(int UID, int serverPort, String hostName) {
		this.HostName = hostName;
		this.PortNumber = serverPort;
		this.UID = UID;
	}

	public TCPServer(Node _dsNode) {
		// hard coded local host for now
		this(_dsNode.UID, _dsNode.port, _dsNode.getNodeHostName());
		this.dsNode = _dsNode;
	}

	public void listenSocket() {
		try {
			serversocket = new ServerSocket(PortNumber);
		} catch (IOException e) {
			// System.out.println("Could not listen on port 4444");
			System.out.println(e);
			System.exit(-1);
		}
		while (true) {
			TCPClient reqHandler;
			try {
				// server.accept returns a client connection
				Socket clientreqSocket = serversocket.accept();
				reqHandler = new TCPClient(clientreqSocket, this.dsNode);

				// assign each client request to a separate thread
				Thread t = new Thread(reqHandler);
				t.start();

			} catch (IOException e) {
				System.out.println("Accept failed");
				System.exit(-1);
			}
		}
	}

}