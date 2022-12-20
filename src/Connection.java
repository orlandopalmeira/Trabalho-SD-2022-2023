import java.io.*;
import java.lang.reflect.InvocationTargetException;
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

    public void sendOld(int tag, Serializavel data) throws IOException {
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


    public void send(Frame frame) throws IOException {
        int tag = frame.tag;
        Serializavel ser = frame.data;
        send(tag, ser);
    }

    public void send(int tag, Serializavel data) throws IOException{
        wl.lock();
        try{
            this.dos.writeInt(tag);
            this.dos.writeUTF(data.getClass().getName()); // escreve o nome da classe
            data.serialize(this.dos);
            this.dos.flush();
        } finally {
            wl.unlock();
        }
    }


    public Frame receive() throws IOException {
        int tag;
        Serializavel data = null;
        rl.lock();
        try{
            tag = this.dis.readInt();
            String className = this.dis.readUTF();
            Class<?> classe = Class.forName(className);
            //Object obj = classe.getDeclaredConstructor().newInstance();
            data = (Serializavel) classe.getDeclaredConstructor().newInstance();
            data = data.deserialize(this.dis);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            rl.unlock();
        }
        return new Frame(tag, data);
    }

    public Frame receiveOld() throws IOException {
        int tag;
        Serializavel data = null;
        try {
            rl.lock();
            tag = this.dis.readInt();
            switch (tag) {
                case 0,1 -> {
                    AccountInfo ac = new AccountInfo();
                    data = (AccountInfo) ac.deserialize(dis);
                }
                case 2,3,4,6,7 -> {
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
                case 5, 14 -> {
                    CodigoReserva cr = new CodigoReserva();
                    data = cr.deserialize(dis);
                }
                case 15 -> {
                    InfoViagem infoViagem = new InfoViagem();
                    data = infoViagem.deserialize(dis);
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
