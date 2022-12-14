import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public interface Serializavel {
    public void serialize (DataOutputStream out) throws IOException;
    public Serializavel deserialize(DataInputStream in) throws IOException;
}
