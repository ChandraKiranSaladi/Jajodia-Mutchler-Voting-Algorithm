package client;

import java.util.List;
import java.util.Random;

public class FileRequestAccess {

    Node dsNode;

    public FileRequestAccess(Node _dsNode) {
        this.dsNode = _dsNode;
    }

    public void InitiateAlgorithm() {
        System.out.println(" Client Started Execution of Requests");
        for (int i = 0; i < dsNode.partitions.size(); i++) {
            List<String> Components = dsNode.partitions.get(i);
            dsNode.sendPartitionMessageToServers(Components);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("\nCurrent Partitions = " + String.join(",",Components));

            for (int j = 0; j < Components.size(); j++) {
                String component = Components.get(j);
                System.out.println("2 writes on partition " + component);
                for (int k = 0; k < 2; k++) {
                    Random rand = new Random();
                    int index = rand.nextInt(component.length());
                    int UID = Character.getNumericValue(component.charAt(index));
                    dsNode.sendRequest(UID);
                    dsNode.waitForCompletion();
                }
            }
        }
        for (int j : dsNode.uIDofNeighbors.keySet()) {
            dsNode.sendCompletion(j);
        }
    }
}
