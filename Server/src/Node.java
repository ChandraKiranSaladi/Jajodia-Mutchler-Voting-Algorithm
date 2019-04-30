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
	private String HostName;
	HashMap<Integer, NeighbourNode> uIDofNeighbors;
	private BlockingQueue<Message> msgQueue;
	private ServerSocket serverSocket;
	private Map<Integer,TCPClient> connectedClients = (Map<Integer, TCPClient>) Collections.synchronizedMap(new HashMap<Integer,TCPClient>());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private LockManager lockManager;
	private boolean lock = false;
	public int VN, SC;
	public HashSet<Integer> DS;
	private int voteResponseCount = 0;
	public HashMap<Integer,Message> voteResponseMessages;
	
	public Node(int UID, int port, String hostName, HashMap<Integer, NeighbourNode> uIDofNeighbors) {
		this.UID = UID;
		this.port = port;
		this.HostName = hostName;
		this.uIDofNeighbors = uIDofNeighbors;
		this.msgQueue = new PriorityBlockingQueue<Message>();
		this.VN = 0;
		this.SC = uIDofNeighbors.size();
		this.DS = new HashSet<>();
		this.lockManager = new LockManager();
		this.voteResponseMessages = new HashMap<>();
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

	public LockManager getLockManager(){
		return this.lockManager;
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
			lockManager.releaseRequest();
		}
		else if(msgType == MessageType.VOTE_REQUEST){
			try {
				lockManager.lockRequest();
				Message message = new Message(new Date(),this.UID,MessageType.VOTE_RESPONSE,this.VN,this.SC,this.DS);
				sendMessage(msg.getsenderUID(),message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if(msgType == MessageType.VOTE_RESPONSE) {
			synchronized (Lock.getLockObject()) {
				this.voteResponseCount++;
				voteResponseMessages.put(msg.getsenderUID(),msg);
				Lock.getLockObject().notifyAll();
			}
		}
		else if(msgType == MessageType.COMMIT){
			this.DS = msg.getDS();
			this.VN = msg.getVersionNumber();
			this.SC = msg.getSC();
			System.out.println("VN = "+ this.VN + " SC = "+ this.SC);
			lockManager.releaseRequest();
		}else if(msgType == MessageType.UPDATE_REQUEST){

		}else if(msgType == MessageType.DISABLE_CONNECTION){

		}else if(msgType == MessageType.ENABLE_CONNECTION){

		}
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

	synchronized public boolean isLock() {
		return this.lock;
	}

	synchronized public void setLock(boolean val){
		this.lock = val;
	}

	public String getMyTimeStamp() {
		return sdf.format(new Date());
	}

	public void waitforVoteResponses() {
		int numberOfGrants = this.uIDofNeighbors.size();
		System.out.println("Waiting For Vote Responses");
		synchronized (Lock.getLockObject()) {
			while (numberOfGrants != this.voteResponseCount) {
				try {
					Lock.getLockObject().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.voteResponseCount = 0;
		}

	}

	public void sendMessage(int UID,Message message) {
		synchronized (connectedClients) {
			TCPClient client = connectedClients.get(UID);
			try {
				System.out.println("Sending Grant to UID: "+ UID);
				client.getOutputWriter().writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
