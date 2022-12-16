import java.util.concurrent.locks.ReentrantLock;

public class UniqueCode {
    private int code;
    private final ReentrantLock codeLock;

    public UniqueCode() {
        this.code = 1;
        this.codeLock = new ReentrantLock();
    }

    public int getCode(){
        codeLock.lock();
        int myCode;
        try{
            myCode = this.code++;
        } finally {
            codeLock.unlock();
        }
        return myCode;
    }
}
