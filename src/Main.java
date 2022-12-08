import java.util.List;
import java.util.Set;

// SO PARA TESTES.
public class Main {
    public static void main(String[] args) {
        Mapa mapa = new Mapa(10);
        List<Localizacao> locals;
        System.out.println(mapa);

        System.out.println("Todas as recompensas:");
        Set<Recompensa> recompensas = mapa.getRewards();
        String string = Recompensa.toStringRecompensas(recompensas);
        System.out.println(string);

        int x = 5, y = 2;
        System.out.printf("\nRecompensas com origem em (%d,%d):\n", x, y);
        Set<Recompensa> recompensasOrigin = mapa.getRewardsWithOrigin(x,y);
        string = Recompensa.toStringRecompensas(recompensas);
        System.out.println(string);

        System.out.println("Done!");
    }
}