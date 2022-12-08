import java.util.List;
import java.util.Set;

import static java.lang.Thread.sleep;

// SO PARA TESTES.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Mapa mapa = new Mapa(10);
        System.out.println(mapa);

        Runnable workers = () -> {
            mapa.addTrotineta(3,3);
            List<Pair> l = mapa.trotinetesArround(2,3);
            String res = Pair.toStringPairs(l);
            System.out.println(res);

            mapa.getRewardsWithOrigin(8, 4);
            mapa.trotinetesArround(5,4 );
            mapa.getRewardsWithOrigin(4,3);
            mapa.getRewardsWithOrigin(4,3);



        };
        for (int i = 0; i < 9; i++){
            new Thread(workers).start();
        }
        sleep(1*1000);
        System.out.println(mapa);
        System.out.println("Done!");
    }
}