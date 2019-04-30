import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Message  implements Serializable, Comparable<Message>{
	
	private static final long serialVersionUID = 1L;
	private int senderUID;
	private MessageType msgType;
	private List<String> partitions;

	public Message(int senderUID, MessageType msgType) {
		this.senderUID = senderUID;
		this.msgType = msgType;
	}
	
	public Message(int senderUID, MessageType messageType, List<String> partitions ) {
		this.senderUID = senderUID;
		this.msgType = messageType;
		this.partitions = partitions;
	}
	
	public Message(MessageType msgType) {
		this.msgType = msgType;
	}
	
	public Message(Message message) {
		this(message.senderUID, message.msgType);
	}

	// public Date getTimeStamp() {
	// 	return this.timeStamp;
	// }
	
	public int getsenderUID() {
		return this.senderUID;
	}

	public MessageType getMsgType() {
		return this.msgType;
	}

	// public int getVersionNumber() {
	// 	return this.versionNumber;
	// }

	// public int getSC(){
	// 	return this.SC;
	// }

	// public HashSet<Integer> getDS(){
	// 	return this.DS;
	// }

	// TODO: Set the priority of messages to handle
	@Override
	public int compareTo(Message msg) {
		return this.msgType.compareTo(msg.msgType);
	}
}
