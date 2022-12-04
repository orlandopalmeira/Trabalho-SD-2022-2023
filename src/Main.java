import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Mapa mapa = new Mapa(10);
        List<Localizacao> locals;
        System.out.println(mapa);

        System.out.println("Todas as recompensas:");
        Set<Recompensa> recompensas = mapa.getRewards();
        Recompensa.printRecompensas(recompensas);

        int x = 9, y = 5;
        System.out.printf("Recompensas com origem em (%d,%d):\n", x, y);
        Set<Recompensa> recompensasOrigin = mapa.getRewardsWithOrigin(x,y);
        Recompensa.printRecompensas(recompensasOrigin);

        System.out.println("Done!");
    }
}


/*
        Scanner in = new Scanner(System.in);
        System.out.println("Coordenada x: ");
        int x = in.nextInt();
        System.out.println("Coordenada y: ");
        int y = in.nextInt();

        locals = mapa.trotinetesArround(x,y);
        System.out.println(locals);

        locals = mapa.whereAreTrotinetes();
        for (Localizacao l: locals){
            System.out.println("Pos: (" + l.getX() + ", " + l.getY() + ") ; num_trotinetas-> " + l.getNtrotinetes());
        }
        locals = mapa.getSurroundings(mapa.getLocalizacao(9,9), 2);
        for (Localizacao l: locals){
            System.out.println(l);
        }
        */