package server;

import common.Message;
import common.MessageType;

import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileRequestAccess extends Thread {
	private BlockingQueue<Message> messages;
	Node dsNode;
	private int M;
	private int N;
	private HashSet<Integer> I;
	private HashSet<Integer> P;

	public FileRequestAccess(Node _dsNode) {
		this.messages = new LinkedBlockingQueue<>();
		this.dsNode = _dsNode;
		this.M = 0;
		HashSet<Integer> DS = new HashSet<>();
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.println("waiting on queue");
				Message message = messages.take();
				System.out.println("server.Message with msgType " + message.getMsgType());
				if (message.getMsgType() == MessageType.ABORT) {
					break;
				}
				InitiateAlgorithm();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void InitiateAlgorithm() throws InterruptedException {
		/*
		 * Receive Request from S0 Set lock = true send vote request Master = true wait
		 * for responses if( received vote request) Master = false; set lock = true send
		 * vote responses waits for any other message
		 * 
		 * !is_Distinguished() set lock = false send abort
		 * 
		 * if received abort set lock = false
		 * 
		 * is Distinguished = true Catch_Up() if S does not belong to Set I set recent
		 * copy from any site in I to your copy
		 * 
		 * Do_ Update(Master) sets the recent version commits send commit message along
		 * with all the values ( all missing or just recent ) Do_Update(!Master) if
		 * commit message received, updates the values releases lock
		 * 
		 * 
		 */

		// synchronized (server.Lock.getLockObject()) {
		dsNode.getLockManager().lockRequest();
		System.out.println("Received server.Lock");
		dsNode.voteResponseMessages.clear();
		System.out.println("Vote requests sent");
		dsNode.sendMessageToNeighbors(MessageType.VOTE_REQUEST);
		dsNode.waitforVoteResponses();

		if (!isDistinguished()) {
			System.out.println("Not Distinguished Partition");
			dsNode.sendMessageToNeighbors(MessageType.ABORT);
			dsNode.getLockManager().releaseRequest();
			Message response = new Message(dsNode.getNodeUID(), MessageType.COMPLETION);
			response.setRequestSatisfied(false);
			dsNode.sendMessage(0, response);
			System.out.println("Write unsuccessful");
			System.out.println("VN= " + dsNode.VN + " SC= " + dsNode.SC);
			System.out.println("Values inside DSi");
			for (Integer x : dsNode.DS) {
				System.out.print(x + " ");
			}
			System.out.println();
			return;
		}
		System.out.println("Catching Up()");
		Catch_Up();
		System.out.println("Do_Update()");
		Do_Update();
		System.out.println("Write successful. Version number = "+dsNode.VN);
		Message response = new Message(dsNode.getNodeUID(), MessageType.COMPLETION);
		response.setRequestSatisfied(true);
		dsNode.sendMessage(0, response);
		// }
	}

	// private void Write() {
	//
	// FileWriter fileWriter;
	// try {
	// fileWriter = new FileWriter(dsNode.filePath, true);
	// fileWriter.write("Entering, timeStamp: " + dsNode.getMyTimeStamp() + " VN: "
	// + dsNode.VN + " SC: "
	// + dsNode.SC + " DS: " + dsNode.DS);
	// fileWriter.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public void addMessage(Message message) throws InterruptedException {
		messages.put(message);
	}

	private boolean isDistinguished() {
		/*
		 * P = neighbors + me M = max ( VN ) I = set of max VN N = SC(any site in I) if(
		 * count(I) > N/2) return true else if( count(I) == N/2) let any one site in I
		 * be X if( I.contains(DS(X))) return true else if( N == 3) P contains 2 or 3 in
		 * DS(I) ( IN this case count(I) == 1) return true else return false;
		 */
		P = new HashSet<>(dsNode.uIDofNeighbors.keySet());
		I = new HashSet<>();
		P.add(dsNode.UID);
		System.out.println("Partition Size = " + P.size());
		if (P.size() == 1)
			return false;
		M = dsNode.VN;
		for (Message msg : dsNode.voteResponseMessages.values()) {
			if (M < msg.getVersionNumber()) {
				M = msg.getVersionNumber();
			}
		}
		N = dsNode.SC;
		Message memberOfIMsg = null;
		for (Message msg : dsNode.voteResponseMessages.values()) {
			if (msg.getVersionNumber() == M) {
				I.add(msg.getsenderUID());
				memberOfIMsg = msg;
			}
		}
		HashSet<Integer> DSIMsg = memberOfIMsg.getDS();
		if (dsNode.VN == M) {
			I.add(dsNode.getNodeUID());
			DSIMsg.add(dsNode.getNodeUID());
		}
		System.out.println("I.size() = " + I.size());
		N = memberOfIMsg == null ? dsNode.UID : memberOfIMsg.getSC();
		System.out.println("N = " + N + " M = " + M);
		if (I.size() > N / 2)
			return true;
		else if (I.size() == N / 2) {
			if (I.containsAll(DSIMsg)) {
				System.out.println(" I am in the Distinguished Partition");
				return true;
			}
		} else if (N == 3) {
			Set<Integer> PunionI = new HashSet<Integer>(memberOfIMsg.getDS());
			PunionI.retainAll(I);
			if (PunionI.size() >= 2)
				return true;
		}
		return false;
	}

	private void Catch_Up() {
		// TODO: Update the outdated copy in your Server with a more recent one
		if (!I.contains(dsNode.UID)) {
			dsNode.VN = M;
		}
		System.out.println("New VN of this server.Node: " + dsNode.VN);
	}

	private void Do_Update() {
		int sizeofP = P.size();
		int VNi = this.M + 1;
		if (N == 3 && sizeofP == 2)
			return;
		int SCi = sizeofP;
		HashSet<Integer> DSi = new HashSet<>();
		// System.out.println("sizeofP = "+sizeofP);
		if (sizeofP == 3) {
			for (Map.Entry<Integer, NeighbourNode> map : dsNode.uIDofNeighbors.entrySet()) {
				DSi.add(map.getKey());
			}
		} else if (sizeofP % 2 == 0) {
			DSi.add(Collections.min(dsNode.uIDofNeighbors.keySet()));
		}
		dsNode.DS = DSi;
		dsNode.VN = VNi;
		dsNode.SC = SCi;
		System.out.println("VN= " + VNi + " SC= " + SCi);
		System.out.println("Values inside DSi");
		for (Integer x : DSi) {
			System.out.print(x + " ");
		}
		System.out.println();

		/*
		 * VN = M + 1 if( N ==3 && card(P) == 2) return; else VN = M + 1 SC = size(P) DS
		 * = { least UID in P if( size(p) is even) set(P) if size(P) = 3 }
		 */
		System.out.println("Ready to Commit");
		dsNode.sendCommitMessageToComponent();
		dsNode.getLockManager().releaseRequest();
	}
}
