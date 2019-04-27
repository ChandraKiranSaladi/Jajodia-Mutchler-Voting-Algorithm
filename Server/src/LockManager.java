public class LockManager {
    private boolean locked;

    public LockManager() {
        this.locked = false;
    }

    public synchronized void lockRequest() throws InterruptedException {
        while (!locked){
            wait();
        }
        locked = true;
    }

    public synchronized void releaseRequest(){
        locked = false;
        notifyAll();
    }
}
