import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Lock rl = new ReentrantLock();
    private final Lock wl = new ReentrantLock();

    public Connection(Socket sock) throws IOException {
        this.socket = sock;
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }


    public void send(Frame frame) throws IOException {
        int tag = frame.tag;
        Serializavel ser = frame.data;
        send(tag, ser);
    }


    public void send(int tag, Serializavel data) throws IOException {
        try {
            wl.lock();
            this.dos.writeInt(tag);
            data.serialize(this.dos);
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    /**
     *
     * Tag 2: Pair <br>
     * Tag 3: Pair <br>
     * Tag 4: Pair <br>
     * Tag 5: Pair <br>
     * Tag 11: PairList <br>
     * Tag 12: RecompensaList <br>
     * Tag 30: RecompensaList (Notificações) <br>
     * @return Frame
     */
    public Frame receive() throws IOException {
        int tag;
        Serializavel data = null;
        try {
            rl.lock();
            tag = this.dis.readInt();
            // TODO implementar verificações da tag para fazer o correto deserialize.
            switch (tag) {
                case 0,1 -> {
                    AccountInfo ac = new AccountInfo();
                    data = (AccountInfo) ac.deserialize(dis);
                }
                case 2,3,4,5,6 -> {
                    Pair p = new Pair();
                    data = (Pair) p.deserialize(dis);
                }
                case 11 -> {
                    PairList pl = new PairList();
                    data = (PairList) pl.deserialize(dis);
                }
                case 12, 30 -> {
                    RecompensaList recompensas = new RecompensaList();
                    data = recompensas.deserialize(dis);
                }
                case 13 -> {
                    Mensagem mess = new Mensagem();
                    data = mess.deserialize(dis);
                }
            }
        }
        finally {
            rl.unlock();
        }
        return new Frame(tag,data);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
