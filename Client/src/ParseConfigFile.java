import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class NeighbourNode {
	String HostName;
	int PortNumber;

	NeighbourNode(String _hostName, int _portNumber) {
		this.HostName = _hostName;
		this.PortNumber = _portNumber;
	}
}

class ParseConfigFile {
	final static HashMap<Integer,Node> nodeList = new HashMap<>();

	public static Node read(String Path, String hostName, int hostNumIndex) throws IOException {
		System.out.println(hostNumIndex);
		BufferedReader b = new BufferedReader(new FileReader(Path));
		HashMap<Integer, NeighbourNode> map = new HashMap<>();
		HashMap<Integer, NeighbourNode> UIDofNeighbors = new HashMap<Integer, NeighbourNode>();
		String readLine = "";

		// Filtering the File 
		String entireFileinString = "";
		while ((readLine = b.readLine()) != null) {
			entireFileinString += readLine+"\n";
		}
		b.close();
		entireFileinString = entireFileinString.replaceAll("(?m)^#.*", "");
		entireFileinString = entireFileinString.replaceAll("(?m)^[ \t]*\r?\n","");

		String[] line = entireFileinString.split("\n");
		int no = 0;
		// b = new BufferedReader(new FileReader(Path));
		int nodeNumber = Integer.parseInt(line[no++]);
		Node node = new Node();
		int myUID = -1;

		for (int xyz = 0; xyz < nodeNumber; xyz++) {
			readLine = line[no++].trim();
			String[] s = readLine.split("\\s+");
			for(int i=0;i<s.length;i++)
				System.out.println(i+":"+s[i]);
			int UID = Integer.parseInt(s[0]);
			String Hostname = s[1];
			int Port = Integer.parseInt(s[2]);
			map.put(UID, new NeighbourNode(Hostname, Port));
			if (hostNumIndex == UID)
				myUID = UID;
			nodeList.put(UID, new Node(UID, Port, Hostname, null));
		}

		node = nodeList.get(hostNumIndex);

		int noofClientConnections = Integer.parseInt(line[no++]);

		for( int xyz = 0; xyz < noofClientConnections; xyz++) {
			String[] s = line[no++].trim().split("\\s+");
			if (myUID == Integer.parseInt(s[0])) {
				for (int i = 1; i < s.length; i++) {
					UIDofNeighbors.put(Integer.parseInt(s[i]), map.get(Integer.parseInt(s[i])));
					System.out.println(s[0] + s[i]);
				}
			}
		}
		
		node.uIDofNeighbors = UIDofNeighbors;
		
		return node;
	}
}