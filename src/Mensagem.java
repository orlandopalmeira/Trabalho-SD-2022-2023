import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe que permite enviar um unico byte para confirmações do genero: Informação de login aceite pelo servidor, Reply do servidor sobre notificação aceite...
 */
public class Mensagem implements Serializavel{
    public byte mensagem;


    public Mensagem(){
        this.mensagem = (byte) 0;
    }

    public Mensagem(byte message){
        this.mensagem = message;
    }

    public Mensagem(int message){
        this.mensagem = (byte) message;
    }

    public boolean equals (int m){
        return this.mensagem == (byte) m;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeByte(mensagem);
    }

    @Override
    public Serializavel deserialize(DataInputStream in) throws IOException {
        byte mess = in.readByte();
        return new Mensagem(mess);
    }
}
