import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class RecompensaList extends HashSet<Recompensa> implements Serializavel{

        @Override
        public void serialize(DataOutputStream out) throws IOException {
            int size = this.size();
            out.writeInt(size);
            for(Recompensa r: this) {
                r.serialize(out);
            }
        }

        @Override
        public Serializavel deserialize(DataInputStream in) throws IOException {
            PairList res = new PairList();
            Pair pair = new Pair(0,0);
            int len = in.readInt();
            for (int i = 0; i<len; i++){
                res.add((Pair) pair.deserialize(in));
            }
            return res;
        }

}
