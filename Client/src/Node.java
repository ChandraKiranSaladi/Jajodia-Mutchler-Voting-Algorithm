import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class Node {
	int UID, port;
	String path;
	String HostName;
	HashMap<Integer, NeighbourNode> uIDofNeighbors;
	ServerSocket serverSocket;
	Map<Integer, TCPClient> connectedClients = (Map<Integer, TCPClient>) Collections
			.synchronizedMap(new HashMap<Integer, TCPClient>());
	BlockingQueue<Message> msgQueue;
	HashMap<Integer, List<String>> partitions;
	private boolean completion = false;

	public Node(int UID, int port, String hostName, HashMap<Integer, NeighbourNode> uIDofNeighbors) {
		this.UID = UID;
		this.port = port;
		this.HostName = hostName;
		this.uIDofNeighbors = uIDofNeighbors;
		this.msgQueue = new PriorityBlockingQueue<Message>();
	}

	public Node() {
	}

	public Message getHeadMessageFromQueue() {
		if (this.msgQueue.peek() != null) {
			Message msg = this.msgQueue.peek();
			this.msgQueue.remove();
			return msg;
		}
		return null;
	}

	public Message getMessageFromQueue() {
		Message msg = null;
		try {
			msg = this.msgQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return msg;
	}

	synchronized public void addMessageToQueue(Message msg) {
		// setMyTimeStamp(Math.max(msg.timeStamp,getMyTimeStamp()));
		msgQueue.add(msg);
	}

	public void sendRequest(int UID) {
		setCompletion(false);
		synchronized (connectedClients) {
			TCPClient client = connectedClients.get(UID);
			try {
				System.out.println("Sending Request to UID: " + UID);
				client.getOutputWriter().writeObject(new Message(this.UID, MessageType.REQUEST));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendPartitionMessageToServers(List<String> Components) {
		MessageType msgType = MessageType.PARTITION;
		synchronized (connectedClients) {
			for (Map.Entry<Integer, TCPClient> entry : connectedClients.entrySet()) {
				int recipientUID = entry.getKey();
				TCPClient client = entry.getValue();
				try {
					System.out.println("Sending " + msgType + " to Server: " + recipientUID);
					client.getOutputWriter().writeObject(new Message(this.UID, msgType, Components));
					// System.out.println("Connection Closed for UID:"+getsenderUID);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendCompletion(int UID) {
		synchronized (connectedClients) {
			TCPClient client = connectedClients.get(UID);
			try {
				System.out.println("Sending Completion to UID: " + client.getServerUID());
				client.getOutputWriter().writeObject(new Message(this.UID, MessageType.COMPLETION));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void attachServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public int getNodeUID() {
		return this.UID;
	}

	public int getNodePort() {
		return this.port;
	}

	public String getNodeHostName() {
		return this.HostName;
	}

	public HashMap<Integer, NeighbourNode> getNeighbors() {
		return this.uIDofNeighbors;
	}

	public void addClient(int UID, TCPClient client) {
		synchronized (connectedClients) {
			connectedClients.put(UID, client);
		}
	}

	synchronized public void messageHandler(Message msg) {
		if (msg.getMsgType() == MessageType.COMPLETION) {
			setCompletion(true);
		}
	}

	synchronized public void setCompletion(boolean val) {
		this.completion = val;
	}

	public boolean getCompletionStatus(){
		return this.completion;
	}
	public Map<Integer, TCPClient> getAllConnectedClients() {
		return this.connectedClients;
	}

	public void waitForCompletion() {
		System.out.println("Waiting for Completion Message");
		while (getCompletionStatus() == false) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
