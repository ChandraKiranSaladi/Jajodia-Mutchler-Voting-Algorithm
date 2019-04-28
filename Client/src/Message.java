import java.io.Serializable;
import java.util.Date;

public class Message  implements Serializable, Comparable<Message>{ 
	private static final long serialVersionUID = 1L;
	private Date timeStamp;
	private int senderUID;
	private MessageType msgtype;
	
	
	public Message(int senderUID, MessageType Msgtype) {
		this.senderUID = senderUID;
		this.msgtype = Msgtype;
	}
	
	public Message(Date timeStamp, int senderUID,MessageType messageType ) {
		this.timeStamp = timeStamp;
		this.senderUID = senderUID;
		this.msgtype = messageType;
	}
	
	public Message(MessageType msgtype) {
		this.msgtype = msgtype;
	}
	
//	public Message(Message message) {
//		this(message.timeStamp, message.senderUID, message.msgtype);
//	}

	public Date getTimeStamp() {
		return this.timeStamp;
	}
	
	public int getsenderUID() {
		return this.senderUID;
	}

	public MessageType getMsgType() {
		return this.msgtype;
	}

	@Override
	public int compareTo(Message msg) {
		return 1;
	}
}
