import java.net.InetAddress;

public class InvokeMain {
	public static void main(String[] args) {
		try {

			// build a node for each terminal

			// logic for assigning nodes - temporary
			//			Scanner scanner = new Scanner(System.in);

			//			int hostNumIndex = scanner.nextInt();
			// Integer.parseInt(args[0]);
			int hostNumIndex = Integer.parseInt(args[0]);;

			Node dsNode = BuildNode(hostNumIndex);

			System.out.println("Initializing Server with UID: " + dsNode.UID);

			// Start server thread

			Runnable serverRunnable = new Runnable() {
				public void run() {
					TCPServer server = new TCPServer(dsNode);
					// start listening for client requests
					server.listenSocket();
				}
			};
			Thread serverthread = new Thread(serverRunnable);
			serverthread.start();

			System.out.println("Server started and listening to client requests.........");

			Thread.sleep(3000);
			if(dsNode.getNodeUID() == 1) {
				// Iterate through the node neighbors to send the Client Requests
				dsNode.uIDofNeighbors.entrySet().forEach((neighbour) -> {
					Runnable clientRunnable = new Runnable() {
						public void run() {
							TCPClient client = new TCPClient(dsNode.UID,
									neighbour.getValue().PortNumber, neighbour.getValue().HostName, dsNode.getNodeHostName(), neighbour.getKey(),
									dsNode);
							System.out.println("Client Connection sent from "+dsNode.UID+" to UID: "+neighbour.getKey()+" at Port: "+neighbour.getValue().PortNumber);
							// The following function calls starts the Socket Connections, and adds the client to a list to access
							// it later. Listen Messages is an infinite loop to preserve the socket connection
							client.listenSocket();
							client.sendHandShakeMessage();
							dsNode.addClient(neighbour.getKey(),client);
							client.listenToMessages();
						}
					};
					Thread clientthread = new Thread(clientRunnable);
					clientthread.start();
				});
			}
			// Sleep so that all the Client connections are established		
			Thread.sleep(3000);

			new FileRequestHandler(dsNode).listen();
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public static Node BuildNode(int hostNumIndex) {
		Node dsNode = new Node();
		try {
			dsNode = ParseConfigFile.read(
					"C:\\Users\\kiran\\OneDrive - The University of Texas at Dallas\\"
							+ "CS 6378 ( Advanced Operating Systems )\\Projects\\"
							+ "Tree Based Quorum\\Server\\src\\readFile.txt",
							InetAddress.getLocalHost().getHostName(), hostNumIndex);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get nodeList", e);
		}
		return dsNode;
	}
}
