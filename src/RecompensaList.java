import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RecompensaList extends HashSet<Recompensa> implements Serializavel{

    @Override
    public String toString(){
        List<Recompensa> sorted_ = this.stream().sorted((r1, r2) -> {return r2.reward - r1.reward;}).toList();
        StringBuilder res = new StringBuilder();
        for(Recompensa r : sorted_){
            res.append(r).append("\n");
        }
        return res.toString();
    }

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
