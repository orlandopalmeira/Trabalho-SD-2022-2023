import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Demultiplexer implements AutoCloseable {
    private final Connection c;
    private final ReentrantLock l;
    private final Map<Integer, TaggedMessages> map;
    private Exception exception = null;


    private class TaggedMessages {
        Queue<Serializavel> queue = new ArrayDeque<Serializavel>();
        Condition cond = l.newCondition();
    }

    /**
     * O construtor já chama o seu método start() automaticamente.
     * @param conn Objeto Connection com o respetivo socket associado.
     */
    public Demultiplexer(Connection conn) {
        this.c = conn;
        this.l = new ReentrantLock();
        this.map = new HashMap<Integer, TaggedMessages>();
        this.start();
    }

    public void start() {
        new Thread(() -> {
            try{
                while(true){
                    Frame frame = this.c.receive();
                    l.lock();
                    try{
                        TaggedMessages td = map.get(frame.tag);
                        if (td == null){
                            td = new TaggedMessages();
                            map.put(frame.tag, td);
                        }
                        td.queue.add(frame.data);
                        td.cond.signal();
                    } finally {
                        l.unlock();
                    }
                }
            } catch (IOException exc) {
                this.l.lock();
                try{
                    this.exception = exc;
                    this.map.forEach((k,v) -> v.cond.signalAll()); // acorda todos os receives em espera para que o programa termine em caso de uma excecao.
                } finally {
                    this.l.unlock();
                }
            }
        }).start();
    }

    public void send(Frame frame) throws IOException {
        c.send(frame);
    }

    public void send(int tag, Serializavel data) throws IOException {
        c.send(tag, data);
    }

    public Serializavel receive(int tag) throws Exception {
        l.lock();
        try{
            TaggedMessages td = map.get(tag);
            if (td == null){
                td = new TaggedMessages();
                map.put(tag, td);
            }
            while(true){
                if (exception != null){
                    throw this.exception;
                }
                if (!td.queue.isEmpty()){
                    Serializavel data = td.queue.poll();
                    return data;
                }
                td.cond.await();

            }
        } finally {
            l.unlock();
        }
    }

    public void close() throws IOException {
        c.close();
    }
}