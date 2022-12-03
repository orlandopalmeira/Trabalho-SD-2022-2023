import java.util.Scanner;

// Só para visualisar o mapa e adicionar trotinetes nele de maneira mais grafica.
public class ShowMapa {
    public static void main(String[] args) {
        int size = 10;
        Mapa mapa = new Mapa(size, 0);
        //mapa.addTrotineta(x,y);
        //mapa.addTrotineta();
        Scanner in = new Scanner(System.in);
        int opc, x, y;
        System.out.println(mapa);
        while (true){
            System.out.println("Adicionar(1) ou remover(0) ou remover tudo(2)?");
            opc = in.nextInt();
            while (opc != 0 && opc != 1 && opc != 2) {
                System.out.println("Adicionar(1) ou remover(0)?");
                opc = in.nextInt();
            }
            if(opc == 2){
                mapa = new Mapa(size,0);
                System.out.println(mapa);
                continue;
            }
            System.out.println("Imprime coordenada: ");
            x = in.nextInt();
            y = in.nextInt();
            if (opc == 0) mapa.retiraTrotineta(x,y);
            else mapa.addTrotineta(x,y);
            System.out.println(mapa);
        }
    }
}
