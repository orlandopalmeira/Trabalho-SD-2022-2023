import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


////////////////////// TALVEZ INACABADO //////////////////////
public class Demultiplexer implements AutoCloseable {
    private final Connection c;
    private final ReentrantLock l;
    private final Map<Integer, FrameValue> map;
    private IOException exception = null;

    private class FrameValue {
        Queue<byte[]> queue = new ArrayDeque<>();
        Condition cond = l.newCondition();
    }

    public Demultiplexer(Connection conn) {
        this.c = conn;
        this.l = new ReentrantLock();
        this.map = new HashMap<Integer, FrameValue>();
    }

    // Vai estar sempre a receber dados e a coloca-los no respetivo lugar no map.
    public void start() {
        new Thread(() -> {
            try{
                while(true){
                    Frame frame = this.c.receive();
                    l.lock();
                    try{
                        FrameValue fv = map.get(frame.tag);
                        if (fv == null){
                            fv = new FrameValue();
                            map.put(frame.tag, fv);
                        }
                        fv.queue.add(frame.data);
                        fv.cond.signalAll();
                    } finally {
                        l.unlock();
                    }
                }
            } catch (IOException exc) {exception = exc;}
        }).start();
    }

    public void send(Frame frame) throws IOException {
        c.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        c.send(tag, data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        l.lock();
        try{
            FrameValue fv = map.get(tag);
            if (fv == null){
                fv = new FrameValue();
                map.put(tag, fv);
            }
            while(true){
                if (!fv.queue.isEmpty()){
                    byte[] data = fv.queue.poll();
                    return data;
                }
                else{
                    fv.cond.await();
                }
            }
        } finally {
            l.unlock();
        }
    }

    public void close() throws IOException {
        c.close();
    }
}
