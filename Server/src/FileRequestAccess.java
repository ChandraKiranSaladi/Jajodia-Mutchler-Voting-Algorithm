import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class FileRequestAccess {

	Node dsNode;
	int csEntryCount = 0;
	int quorumNumber;
    private int M;
    private int N;
    private HashSet<Integer> I;
    private HashSet<Integer> P;

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
                try {
                    Lock.getLockObject().wait();
					dsNode.waitingRequest = false;
					dsNode.voteResponseMessages.clear();
                    dsNode.sendMessageToNeighbors(MessageType.VOTE_REQUEST);
					dsNode.waitforVoteResponses();
					
                    if(!isDistinguished()){
                        dsNode.setLock(false);
                        dsNode.sendMessageToNeighbors(MessageType.ABORT);
                        return;
                    }
                    Catch_Up();
                    Do_Update();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
					+" VN: "+dsNode.VN + " SC: "+dsNode.SC + " DS: " +dsNode.DS);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isDistinguished() {
		/*
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
		else
			return false;
		*/
		Set<Integer> P = new HashSet<>(dsNode.uIDofNeighbors.keySet());
		I = new HashSet<>();
		P.add(dsNode.UID);
		M = dsNode.VN;
        for (Message msg : dsNode.voteResponseMessages.values()) {
            if(M<msg.getVersionNumber()){
                M = msg.getVersionNumber();
            }
        }
        N = dsNode.SC;
        Message memberOfIMsg = null;
        for (Message msg:dsNode.voteResponseMessages.values()) {
            if(msg.getVersionNumber() == M){
                I.add(msg.getsenderUID());
                memberOfIMsg = msg;
            }
        }
        N = memberOfIMsg==null?dsNode.UID:memberOfIMsg.getSC();
        if(I.size() > N/2)
			 return true;
		else if(I.size() == N/2)
			if( I.containsAll(memberOfIMsg.DS))
				return true;
		else if( N == 3) {
		    Set<Integer> PunionI = new HashSet<Integer>(memberOfIMsg.DS);
            PunionI.retainAll(I);
		    if(PunionI.size()>=2)
                return true;
		}
		return false;
	}

	public void Catch_Up() {
		// TODO: Update the outdated copy in your Server with a more recent one
        if(!I.contains(dsNode.UID)){
            dsNode.VN = M;
        }
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
