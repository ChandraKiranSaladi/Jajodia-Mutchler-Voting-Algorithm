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
	/*
	 	Receive Request from S0
		Set lock = true
		send vote request	
		Master = true						
		wait for responses 								if( received vote request)
														Master = false;
														set lock = true
														send vote responses
														waits for any other message

		!is_Distinguished()								
		set lock = false
		send abort

														if  received abort
														 set lock = false
		
		is Distinguished = true
			Catch_Up()
				if S does not belong to Set I 
				set recent copy from any site in I 
				to your copy

			Do_ Update(Master)											
				sets the recent version commits
				send commit message along with all the
				values ( all missing or just recent )	
														Do_Update(!Master)
														if commit message received, 
														updates the values
														releases lock

				 
	*/

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
		/* TODO: Check whether the current partition is a distinguished one 
		P = neighbors + me
		M = max ( VN )
		I = set of max VN
		N = SC(any site in I)
		if( count(I) > N/2)
			 return true
		else if( count(I) == N/2)
			let any one site in I be X
			if( I.contains(DS(X)))
				return true
		else if( N == 3)
			P contains 2 or 3 in DS(I) ( IN this case count(I) == 1)
			return true
		*/
		else
			return false;
	}

	public boolean Catch_Up() {
		// TODO: Update the outdated copy in your Server with a more recent one
	    return false;
	}

	public void Do_Update(){
		/*
		VN = M + 1
		if( N ==3 && card(P) == 2)
			return;
		else
			VN = M + 1
			SC = size(P)
			DS = {
				least UID in P	if( size(p) is even)
				set(P)  		if size(P) = 3
			}
		*/
	    dsNode.sendMessageToNeighbors(MessageType.COMMIT);
		// TODO: Sites commit the update

		
	}
}
