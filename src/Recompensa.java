import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Recompensa implements Serializavel {
    public Pair origem;
    public Pair destino;
    public int reward;

    public Recompensa (){
        this.origem = new Pair();
        this.destino = new Pair();
        this.reward = 0;
    }

    public Recompensa (Pair origem, Pair destino){
        this.origem = origem;
        this.destino = destino;
        this.reward = this.calculaRecompensa();
    }

    public Recompensa (Pair origem, Pair destino, int reward){
        this.origem = origem;
        this.destino = destino;
        this.reward = reward;
    }


    /**
     * Função matemática que calcula o valor da recompensa tendo em conta a distância entre a origem e o destino.
     * O valor da recompensa é igual à distância entre a origem e o destino.
     */
    public int calculaRecompensa(){
        return origem.distance(destino); // por enquanto a recompensa é igual à distancia.
    }


    @Override
    public String toString() {
        return "{" +
                "origem=" + origem +
                ", destino=" + destino +
                ", valor=" + reward +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (o == this){
            return true;
        }
        if (o.getClass() != Recompensa.class) {
            return false;
        }
        Recompensa r = (Recompensa) o;
        return origem.getX() == r.origem.getX() &&
               origem.getY() == r.origem.getY() &&
               destino.getX() == r.destino.getX() &&
               destino.getY() == r.destino.getY() &&
               reward == r.reward;
    }

    @Override
    public int hashCode() {
        Integer xo = origem.getX(),
                yo = origem.getY(),
                xd = destino.getX(),
                yd = destino.getY(),
                rewrd = this.reward;
        return xo.hashCode() + yo.hashCode() + xd.hashCode() + yd.hashCode() + rewrd.hashCode();
    }

    public void serialize (DataOutputStream out) throws IOException {
        this.origem.serialize(out);
        this.destino.serialize(out);
        out.writeInt(this.reward);
    }


    // @TODO
    public Recompensa deserialize (DataInputStream in) throws IOException {
        Pair origem = new Pair(0,0);
        Pair destino = new Pair(0,0);
        origem = (Pair) origem.deserialize(in);
        destino = (Pair) destino.deserialize(in);
        int reward = in.readInt();

        return new Recompensa(origem, destino, reward);
    }

}
