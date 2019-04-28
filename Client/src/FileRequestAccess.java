import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class FileRequestAccess {

	Node dsNode;
	int csEntryCount = 0;
	int quorumNumber;

	public FileRequestAccess(Node _dsNode) {
		this.dsNode = _dsNode;
	}

	public void InitiateRequestGeneration() {

		while (this.csEntryCount < 20) {

			try {
				Thread.sleep(new Random().nextInt(500)+ 500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Request_Resource();
			CriticalSection();
			Release_Resource();


		}
		dsNode.sendCompletion();
	}

	private int getRandomNumber(int number) {
		return 1 + new Random().nextInt(number);
	}

	public void Request_Resource() {
		dsNode.messageGrantCount = 0;
		dsNode.csStart = new Date();
		dsNode.tempMessageCount = dsNode.sentMessageCount + dsNode.receivedMessageCount;
		this.quorumNumber = getRandomNumber(dsNode.quorums.size());
		System.out.println("Sending Request to Quorum: "+quorumNumber);
		dsNode.sendMessageToQuorum(quorumNumber,MessageType.Request, this.csEntryCount);
		dsNode.waitforGrantFromQuorum(quorumNumber);

	}

	synchronized private void CriticalSection() {
		this.csEntryCount++;
		dsNode.latency[this.csEntryCount-1] = new Date().getTime() - dsNode.csStart.getTime();
		dsNode.messageCountCS[this.csEntryCount-1] = dsNode.sentMessageCount+ dsNode.receivedMessageCount - dsNode.tempMessageCount; 
		System.out.println("IN Critical Section folks at:"+dsNode.getMyTimeStamp());
		Write();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Exiting the Critical Section");

	}

	public void Release_Resource() {
		System.out.println("Resource Released");
		dsNode.sendMessageToQuorum(this.quorumNumber, MessageType.Release, -1);
	}

	public void Write() {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(dsNode.filePath+"\\log.txt",true);
			fileWriter.write("Entering " + " UID: "+ (dsNode.UID)+ " timeStamp: "+ dsNode.getMyTimeStamp()+"\n");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
