import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FileRequestAccess {

	Node dsNode;
	int csEntryCount = 0;
	int quorumNumber;


	public FileRequestAccess(Node _dsNode) {
		this.dsNode = _dsNode;
	}

	public void InitiateAlgorithm() {
		//TODO: Wait for LOCK_REQUEST from S0
		synchronized (Lock.getLockObject()){
		    while (dsNode.isLock()){
                try {
                    Lock.getLockObject().wait();
                    dsNode.waitingRequest = false;
                    dsNode.sendMessageToNeighbors(MessageType.VOTE_REQUEST);
                    dsNode.waitforVoteResponses();
                    if(!isDistinguished()){
                        dsNode.setLock(false);
                        dsNode.sendMessageToNeighbors(MessageType.ABORT);
                        continue;
                    }
                    boolean hasCurrent = Catch_Up();
                    if(!hasCurrent){
                        //TODO: Change version number to the latest
                    }
                    Do_Update();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	private int getRandomNumber(int number) {
		return 1 + new Random().nextInt(number);
	}

	public void Request_Resource() {
		// dsNode.messageGrantCount = 0;
		// dsNode.csStart = new Date();
		// dsNode.tempMessageCount = dsNode.sentMessageCount + dsNode.receivedMessageCount;
		// this.quorumNumber = getRandomNumber(dsNode.quorums.size());
		// System.out.println("Sending Request to Quorum: "+quorumNumber);
		// dsNode.sendMessageToQuorum(quorumNumber,MessageType.Request, this.csEntryCount);
		// dsNode.waitforGrantFromQuorum(quorumNumber);

	}

	synchronized private void CriticalSection() {
		this.csEntryCount++;
		// dsNode.latency[this.csEntryCount-1] = new Date().getTime() - dsNode.csStart.getTime();
		// dsNode.messageCountCS[this.csEntryCount-1] = dsNode.sentMessageCount+ dsNode.receivedMessageCount - dsNode.tempMessageCount; 
		System.out.println("IN Critical Section folks at:"+dsNode.getMyTimeStamp());
		// Write();
		// try {
		// 	Thread.sleep(300);
		// } catch (InterruptedException e) {
		// 	e.printStackTrace();
		// }
		System.out.println("Exiting the Critical Section");

	}

	public void Release_Resource() {
		// System.out.println("Resource Released");
		// dsNode.sendMessageToQuorum(this.quorumNumber, MessageType.Release, -1);
	}

	public void Write() {

		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(dsNode.filePath,true);
			fileWriter.write("Entering, timeStamp: "+ dsNode.getMyTimeStamp()
					+" VN: "+dsNode.VN + " RU: "+dsNode.RU+ " DS: " +dsNode.DS);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isDistinguished() {
		// TODO: Check whether the current partition is a distinguished one 
		// 
		return false;
	}

	public boolean Catch_Up() {
		// TODO: Update the outdated copy in your Server with a more recent one
	    return false;
	}

	public void Do_Update(){
	    dsNode.sendMessageToNeighbors(MessageType.COMMIT);
		// TODO: Sites commit the update
	}
}
