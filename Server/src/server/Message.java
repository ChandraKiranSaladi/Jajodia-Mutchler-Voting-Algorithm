package server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public class Message  implements Serializable, Comparable<Message>{
	
	private static final long serialVersionUID = 1L;
	private int senderUID;
	private MessageType msgType;
	private int versionNumber = 0;
	private int SC = 8;
	private HashSet<Integer> DS;
	private List<String> partitions;

	public Message(int senderUID, MessageType msgType) {
		this.senderUID = senderUID;
		this.msgType = msgType;
	}
	
	public Message(int senderUID, MessageType messageType, int VersionNumber, int SC, HashSet<Integer> DS ) {
		this.senderUID = senderUID;
		this.msgType = messageType;
		this.versionNumber = VersionNumber;
		this.SC = SC;
		this.DS = DS; 
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
		this(message.senderUID, message.msgType, message.versionNumber,message.SC, message.DS);
	}

	// public Date getTimeStamp() {
	// 	return this.timeStamp;
	// }

	public List<String> getPartitions() {
		return partitions;
	}

	public void setPartitions(List<String> partitions) {
		this.partitions = partitions;
	}

	public int getsenderUID() {
		return this.senderUID;
	}

	public MessageType getMsgType() {
		return this.msgType;
	}

	public int getVersionNumber() {
		return this.versionNumber;
	}

	public int getSC(){
		return this.SC;
	}

	public HashSet<Integer> getDS(){
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
