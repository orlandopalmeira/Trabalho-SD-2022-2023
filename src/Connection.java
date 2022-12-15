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
     * Case 2: Pair
     * @return
     * @throws IOException
     */
    public Frame receive() throws IOException {
        int tag;
        //Serializavel data = null;
        //Serializavel data = new Pair(0,0);
        try {
            rl.lock();
            tag = this.dis.readInt();
            //data = data.deserialize(dis);
            // TODO implementar verificações da tag para fazer o correto deserialize.
            switch (tag){
                case 2:
                    Pair p = new Pair();
                    p = (Pair) p.deserialize(dis);

                    break;
                case 11:

                    break;
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
