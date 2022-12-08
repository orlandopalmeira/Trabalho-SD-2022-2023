import java.util.List;
import java.util.Set;

// SO PARA TESTES.
public class Main {
    public static void main(String[] args) {
        Mapa mapa = new Mapa(10);
        System.out.println(mapa);

        System.out.println("");
        List<Pair> l = mapa.trotinetesArround(2,3);
        String res = Pair.toStringPairs(l);
        System.out.println(res);

        System.out.println("Done!");
    }
}