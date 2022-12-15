import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class PairList extends ArrayList<Pair> implements Serializavel{

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        //for(Pair p: this){
        for(int i = 0; i < this.size(); i++){
            Pair p = this.get(i);
            int occurrences = Collections.frequency(this, p);
            ret.append(occurrences).append(": ").append(p).append("\n");
            //ret.append(occurrences + ": " + p + "\n");
            i = this.lastIndexOf(p);
        }
        return ret.toString();
    }

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

