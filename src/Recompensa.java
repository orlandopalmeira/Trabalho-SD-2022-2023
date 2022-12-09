import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Recompensa {
    public Pair origem;
    public Pair destino;
    public int reward;

    public Recompensa (Pair origem, Pair destino){
        this.origem = origem;
        this.destino = destino;
        this.reward = this.calculaRecompensa();
    }


    /**
     * Função matemática que calcula o valor da recompensa tendo em conta a distância entre a origem e o destino.
     * O valor da recompensa é igual à distância entre a origem e o destino.
     */
    public int calculaRecompensa(){
        return origem.distance(destino); // por enquanto a recompensa é igual à distancia.
    }


    public static String toStringRecompensas(Collection<Recompensa> recs){
        List<Recompensa> sorted_ = recs.stream().sorted((r1, r2) -> {return r2.reward - r1.reward;}).toList();
        StringBuilder res = new StringBuilder();
        for(Recompensa r : sorted_){
            res.append(r).append("\n");
        }
        return res.toString();
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
}
