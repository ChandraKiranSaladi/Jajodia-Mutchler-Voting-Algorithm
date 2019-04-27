public class Lock {
    private static Object lock;
    static {
        lock = new Object();
    }

    public static Object getLockObject(){
        return lock;
    }
}
