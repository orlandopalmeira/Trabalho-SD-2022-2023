import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe usada para a transmissão de informação de login.
 */
public class AccountInfo implements Serializavel{
    public final String username;
    public final String password;


    public AccountInfo(){
        this.username = null;
        this.password = null;
    }

    public AccountInfo(String username,String password){
        this.username = username;
        this.password = password;
    }


    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }

    @Override
    public Serializavel deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new AccountInfo(username, password);
    }
}
