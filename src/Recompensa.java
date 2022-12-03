import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Recompensa {
    private Pair origem;
    private Pair destino;
    private int reward;

    public Recompensa (Pair origem, Pair destino){
        this.origem = origem;
        this.destino = destino;
        this.reward = this.calculaRecompensa();
    }

    /** Função matemática que calcula o valor da recompensa tendo em conta a distância entre a origem e o destino.
     */
    public int calculaRecompensa(){
        return origem.distance(destino); // por enquanto a recompensa é igual à distancia.
    }

    public static void printRecompensas(Collection<Recompensa> recs){
        List<Recompensa> sorted_ = recs.stream().sorted((r1, r2) -> {return r2.reward - r1.reward;}).collect(Collectors.toList());
        for(Recompensa r : sorted_){
            System.out.println(r);
        }
    }

    @Override
    public String toString() {
        return "Recompensa{" +
                "origem=" + origem +
                ", destino=" + destino +
                ", recompensas=" + reward +
                '}';
    }
}
