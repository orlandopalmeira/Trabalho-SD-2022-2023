import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Mapa mapa = new Mapa(10);
        List<Localizacao> locals;
        System.out.println(mapa);
        //List<Pair> coordenadas = mapa.whereAreTrotinetes();
        //System.out.println(coordenadas);

        Set<Recompensa> recompensas = mapa.getRewards();
        Recompensa.printRecompensas(recompensas);

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