public class FileRequestHandler {

	Node dsNode;
	int completionMessageCount;
	
	public FileRequestHandler(Node node) {
		this.dsNode = node;
	}

	public void listen() {
		// Listen for Requests
		while(true) {
			if(dsNode.isLocked() == false && dsNode.msgQueue.size() != 0 ) {
				Message msg = dsNode.getHeadMessageFromQueue();
				if(msg.getMsgType() == MessageType.Request) {
					dsNode.setLocked(true);
					dsNode.sendGrant(msg.getsenderUID());
				}
				else if(dsNode.getNodeUID() == 1 && msg.getMsgType() == MessageType.Completion) {
					if(5 == ++completionMessageCount) {
						dsNode.sendCompletion();
						dsNode.printReport();
						break;
					}
				}
				else if(msg.getMsgType() == MessageType.Completion) {
					dsNode.printReport();
					break;
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}