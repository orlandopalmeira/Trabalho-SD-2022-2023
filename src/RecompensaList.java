import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class RecompensaList extends HashSet<Recompensa> implements Serializavel{

    @Override
    public String toString(){
        // Agrupa recompensas por origem(NOT IMPLEMENTED).
        StringBuilder res = new StringBuilder();
        HashMap<Pair, ArrayList<Recompensa>> grouped = new HashMap<>();
        for (Recompensa r: this){
            if (!grouped.containsKey(r.origem)){
                grouped.put(r.origem, new ArrayList<>());
            }
            grouped.get(r.origem).add(r);
        }
        grouped.forEach((k, v) -> {
            res.append("\nCom origem ").append(k).append(":\n");
            List<Recompensa> sorted_ = v.stream().sorted((r1, r2) -> {return r2.reward - r1.reward;}).toList();
            for (Recompensa r: sorted_){
                res.append(r).append("\n");
            }
        });
        /*
        List<Recompensa> sorted_ = this.stream().sorted((r1, r2) -> {return r2.reward - r1.reward;}).toList();
        for(Recompensa r : sorted_){
            res.append(r).append("\n");
        }
         */
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
        RecompensaList res = new RecompensaList();
        Recompensa rec = new Recompensa();
        int len = in.readInt();
        for (int i = 0; i<len; i++){
            res.add(rec.deserialize(in));
        }
        return res;
    }

}
