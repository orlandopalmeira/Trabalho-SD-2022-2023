import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements AutoCloseable {

    private Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Lock rl = new ReentrantLock();
    private final Lock wl = new ReentrantLock();

    public Connection(Socket socket) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(Frame frame) throws IOException {
        try {
            wl.lock();
            this.dos.writeInt(frame.tag);
            this.dos.writeInt(frame.data.length);
            this.dos.write(frame.data);
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    public void send(int tag, byte[] data) throws IOException {
        this.send(new Frame(tag, data));
    }

    public Frame receive() throws IOException {
        int tag;
        byte[] data;
        try {
            rl.lock();
            tag = this.dis.readInt();
            int len = this.dis.readInt();
            data = new byte[len];
            this.dis.readFully(data);
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
