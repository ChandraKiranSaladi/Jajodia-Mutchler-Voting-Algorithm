package server;

public class LockManager {
    private boolean locked;

    public LockManager() {
        this.locked = false;
    }

    public synchronized void lockRequest() throws InterruptedException {
    	System.out.println("Locked in LockedManager");
        while (locked){
            wait();
        }
        locked = true;
    }

    public synchronized void releaseRequest(){
    	System.out.println("Releasing Locked in server.LockManager");
        locked = false;
        notifyAll();
    }
}
