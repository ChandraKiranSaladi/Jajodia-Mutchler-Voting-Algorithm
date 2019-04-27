import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Node {
	int UID, port;
	String filePath;
	String HostName;
	HashMap<Integer, NeighbourNode> uIDofNeighbors;
	BlockingQueue<Message> msgQueue;
	ServerSocket serverSocket;
	Map<Integer,TCPClient> connectedClients = (Map<Integer, TCPClient>) Collections.synchronizedMap(new HashMap<Integer,TCPClient>());
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private boolean isLocked = false;
	public int VN, RU;
	public String DS;
	public int maxVersionNumberinPartition;
	HashSet<Integer> I = new HashSet<>();
	private int voteResponseCount = 0;
	
	public Node(int UID, int port, String hostName, HashMap<Integer, NeighbourNode> uIDofNeighbors) {
		this.UID = UID;
		this.port = port;
		this.HostName = hostName;
		this.uIDofNeighbors = uIDofNeighbors;
		this.msgQueue = new PriorityBlockingQueue<Message>();
		this.VN = 0;
		this.RU = uIDofNeighbors.size();
		this.DS = "";
	}

	public Node() {
	}

	public void sendMessageToNeighbors(MessageType msgType) {
		synchronized (connectedClients) {
			for (Map.Entry<Integer, TCPClient> entry : connectedClients.entrySet()) {
				int recipientUID = entry.getKey();
				TCPClient client = entry.getValue();
				try {
						System.out.println("Sending "+msgType+" to: "+recipientUID);
					client.getOutputWriter().writeObject(new Message(this.UID, msgType ));
					//						System.out.println("Connection Closed for UID:"+getsenderUID);
				} catch (IOException e) {
					e.printStackTrace();
				}
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


	public 	Map<Integer,TCPClient> getAllConnectedClients() {
		return this.connectedClients;
	}

	synchronized public void messageHandler(Message msg) {
		MessageType msgType = msg.getMsgType();
		if(msgType == MessageType.ABORT) {
			setLocked(false);
		}
		else if(msgType == MessageType.VOTE_RESPONSE)
			this.voteResponseCount++;
		else
			msgQueue.add(msg);
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

	synchronized public boolean isLocked() {
		return this.isLocked;
	}

	synchronized public void setLocked( boolean val){
		this.isLocked = val;
	}

	public String getMyTimeStamp() {
		return sdf.format(new Date());
	}

	public void waitforVoteResponses() {
		int numberOfGrants = this.uIDofNeighbors.size();
		System.out.println("Waiting For Vote Responses");
		while(numberOfGrants != this.voteResponseCount)
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
