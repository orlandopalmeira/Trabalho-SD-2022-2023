import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class PairList extends ArrayList<Pair> implements Serializavel{

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        int size = this.size();
        out.writeInt(size);
        for(Pair p: this) {
            p.serialize(out);
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

