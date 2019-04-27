import java.io.Serializable;
import java.util.Date;

public class Message  implements Serializable, Comparable<Message>{
	
	private static final long serialVersionUID = 1L;
	private Date timeStamp;
	private int senderUID;
	private MessageType msgtype;
	private int versionNumber = 0;
	private int SC = 8;
	String DS = "";

	public Message(int senderUID, MessageType Msgtype) {
		this.senderUID = senderUID;
		this.msgtype = Msgtype;
	}
	
	public Message(Date timeStamp, int senderUID, MessageType messageType, int VersionNumber, int SC, String DS ) {
		this.timeStamp = timeStamp;
		this.senderUID = senderUID;
		this.msgtype = messageType;
		this.versionNumber = VersionNumber;
		this.SC = SC;
		this.DS = DS; 
	}
	
	public Message(MessageType msgtype) {
		this.msgtype = msgtype;
	}
	
	public Message(Message message) {
		this(message.timeStamp, message.senderUID, message.msgtype, message.versionNumber,message.SC, message.DS);
	}

	public Date getTimeStamp() {
		return this.timeStamp;
	}
	
	public int getsenderUID() {
		return this.senderUID;
	}

	public MessageType getMsgType() {
		return this.msgtype;
	}

	public int getVersionNumber() {
		return this.versionNumber;
	}

	public int getSC(){
		return this.SC;
	}

	public String getDS(){
		return this.DS;
	}

	// TODO: Set the priority of messages to handle
	@Override
	public int compareTo(Message msg) {
		if(msg.getMsgType() == MessageType.ABORT)
			return -1;
		else
			return 1;
	}
}
