import java.util.*;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Node {
	int UID, port;
	String filePath;
	private String HostName;
	private HashMap<Integer, NeighbourNode> allServers;
	HashMap<Integer, NeighbourNode> uIDofNeighbors;
	private BlockingQueue<Message> msgQueue;
	private Map<Integer,TCPClient> connectedClients = (Map<Integer, TCPClient>) Collections.synchronizedMap(new HashMap<Integer,TCPClient>());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private LockManager lockManager;
	public int VN, SC;
	public HashSet<Integer> DS;
	private int voteResponseCount = 0;
	public HashMap<Integer,Message> voteResponseMessages;
	private TCPServer tcpServer;
	private FileRequestAccess fileRequestAccess;

	public Node(int UID, int port, String hostName) {
		this.UID = UID;
		this.port = port;
		this.HostName = hostName;
		this.msgQueue = new PriorityBlockingQueue<Message>();
		this.VN = 0;
		this.DS = new HashSet<>();
		this.lockManager = new LockManager();
		this.voteResponseMessages = new HashMap<>();
	}

	public Node() {
	}

	public void setuIDofNeighbors(HashMap<Integer, NeighbourNode> uIDofNeighbors) {
		this.allServers = uIDofNeighbors;
		this.uIDofNeighbors = new HashMap<>(uIDofNeighbors);
		this.SC = uIDofNeighbors.size()+1;
		System.out.println("this.SC = "+this.SC);
	}

	public void sendMessageToNeighbors(MessageType msgType) {
		synchronized (connectedClients) {
			for (Map.Entry<Integer, NeighbourNode> entry : uIDofNeighbors.entrySet()) {
				int recipientUID = entry.getKey();
				if(recipientUID == this.UID)
					continue;
				TCPClient client = connectedClients.get(entry.getKey());
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
	
	public void sendCommitMessageToComponent() {
		synchronized (connectedClients) {
			for (Map.Entry<Integer, NeighbourNode> entry : uIDofNeighbors.entrySet()) {
				int recipientUID = entry.getKey();
				if(recipientUID == this.UID)
					continue;
				TCPClient client = connectedClients.get(entry.getKey());
				try {
						System.out.println("Sending COMMIT to: "+recipientUID);
					client.getOutputWriter().writeObject(new Message(this.UID, MessageType.COMMIT,this.VN,this.SC,this.DS ));
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
		MessageType msgType = msg.getMsgType();
		if(msgType == MessageType.ABORT) {
			lockManager.releaseRequest();
		}
		else if(msgType == MessageType.VOTE_REQUEST){
			try {
				System.out.println("Message Handler. Vote_Request");
				lockManager.lockRequest();
				Message message = new Message(this.UID,MessageType.VOTE_RESPONSE,this.VN,this.SC,this.DS);
				sendMessage(msg.getsenderUID(),message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if(msgType == MessageType.VOTE_RESPONSE) {
			synchronized (Lock.getLockObject()) {
				this.voteResponseCount++;
				voteResponseMessages.put(msg.getsenderUID(),msg);
//				System.out.println("voteResponseMessageCount = "+ voteResponseCount);
				Lock.getLockObject().notifyAll();
			}
		}
		else if(msgType == MessageType.COMMIT){
			this.DS = msg.getDS();
			this.VN = msg.getVersionNumber();
			this.SC = msg.getSC();
			System.out.println("VN = "+ this.VN + " SC = "+ this.SC);
			for(Integer x: DS) {
				System.out.println(x+" ");
			}
			lockManager.releaseRequest();
		}else if(msgType == MessageType.REQUEST){
			try {
//				lockManager.lockRequest();
				System.out.println("MessageHandler. MessageType: "+msg.getMsgType());
				fileRequestAccess.addMessage(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else if(msgType == MessageType.PARTITION){
			partition(msg.getPartitions());
		}else if(msgType == MessageType.COMPLETION){
			synchronized (connectedClients) {
				try {
					fileRequestAccess.addMessage(new Message(-1,MessageType.ABORT));
					tcpServer.close();
					tcpServer.interrupt();
					for (TCPClient client : connectedClients.values()) {
						client.close();
						client.interrupt();
					}
				} catch (Exception e) {
	//				e.printStackTrace();
				}
			}
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

	public String getMyTimeStamp() {
		return sdf.format(new Date());
	}

	public void waitforVoteResponses() {
		int numberOfGrants = this.uIDofNeighbors.size() - 1;
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
		System.out.println("Setting Vote Response Count to zero");

	}

	public void sendMessage(int UID,Message message) {
		synchronized (connectedClients) {
			TCPClient client = connectedClients.get(UID);
			try {
				System.out.println("Sending "+message.getMsgType()+" to UID: "+UID);
				client.getOutputWriter().writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setTCPServer(TCPServer tcpServer) {
		this.tcpServer = tcpServer;
	}

	private void partition(List<String> partitions){
		synchronized (connectedClients) {
			System.out.print("Node.Parition Method");
			for (String partition : partitions) {
				if (partition.contains("" + getNodeUID())) {
					for (char server : partition.toCharArray()) {
						int serverToAdd = server - '0';
						System.out.println("Server to add: "+ serverToAdd);
						if(serverToAdd == getNodeUID()){
							continue;
						}
						uIDofNeighbors.put(serverToAdd,allServers.get(serverToAdd));
					}
				} else {
					for (char server : partition.toCharArray()) {
						int serverToRemove = server - '0';
						System.out.println("Server to remove: "+ serverToRemove);
						uIDofNeighbors.remove(serverToRemove);
					}
				}
			}
		}
	}

	public void setFileRequestAccess(FileRequestAccess fileRequestAccess) {
		this.fileRequestAccess = fileRequestAccess;
	}
}
