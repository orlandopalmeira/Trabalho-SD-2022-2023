import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class Mapa {

    ReentrantLock lock;
    private Localizacao[][] mapa;   /** em cada posição do array temos a localizacao nessa posição */
    private int num_trotinetes;     /** número de trotinetes */
    int N;                          /** tamanho do mapa */

    public Mapa(int n) {
        this.lock = new ReentrantLock();
        this.N = n; ///// vai ser 20 no final
        this.mapa = new Localizacao[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mapa[i][j] = new Localizacao(i,j);
            }
        }
        this.num_trotinetes = 8;
        this.randomTrotinetes(num_trotinetes);
    }

    public Mapa() {
        this.lock = new ReentrantLock();
        this.N = 20; ///// vai ser 20 no final
        this.mapa = new Localizacao[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mapa[i][j] = new Localizacao(i,j);
            }
        }
        this.num_trotinetes = 10;
        this.randomTrotinetes(num_trotinetes);
    }

    /** Coloca um certo número de trotinetes aleatoriamente no mapa.
     */
    private void randomTrotinetes(int num){
        if (num > N*N) num = N*N; // no caso de haver mais trotinetes pra colocar do que espaços disponíveis.
        Random rand = new Random();
        for (int i = 0; i<num; ) {
            int x = rand.nextInt(this.N);
            int y = rand.nextInt(this.N);
            if (getTrotinetasIn(x, y) == 0) {
                this.addTrotineta(x, y);
                i++;
            }
        }
    }

    /** Verifica se a posição está dentro dos limites do mapa estabelecido.
     */
    public boolean validPos (int x, int y){
        return x < this.N && y < this.N && x >= 0 && y >= 0;
    }

    /** Calcula a distância de Manhatan.
     */
    public int distance(int x0, int y0, int x1, int y1){
        int distance = Math.abs(x1-x0) + Math.abs(y1-y0);
        return distance;
    }

    /** Retorna a matriz do mapa - inutilizado
     */
    public Localizacao[][] getMapa() {
        Localizacao[][] newMapa = new Localizacao[N][N];
        try {
            lock.lock();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    newMapa[i][j] = mapa[i][j].clone();
                }
            }
        }
        finally {
            lock.unlock();
        }
        return newMapa;
    }
    /** Retorna o tamanho do mapa.
     */
    public int getN(){
        try {
            lock.lock();
            return this.N;
        }
        finally {
            lock.unlock();
        }
    }

    /** Retorna o objeto Localizacao naquela coordenada.
     */
    public Localizacao getLocalizacao(int x, int y) {
        if(!this.validPos(x,y)) return null;
        try {
            this.mapa[x][y].lockLocal();
            return this.mapa[x][y];
        }
        finally {
            this.mapa[x][y].unlockLocal();
        }
    }

    /** Para notificacar aquando um local passe a estar vazio. NOT SURE DA UTILIDADE.
     */
    public void getLocalVazio(int x, int y) throws InterruptedException {
        mapa[x][y].lockLocal();
        try {
            while (mapa[x][y].getNtrotinetes() > 0){
                mapa[x][y].getCond().await();
            }
            throw new InterruptedException();
        }
        finally {
            mapa[x][y].unlockLocal();
        }
    }

    /** Adiciona uma trotinete ao local indicado.
     */
    public void addTrotineta(int x, int y){
        this.mapa[x][y].somar();
    }

    /** Retira uma trotinete ao local indicado.
     */
    public void retiraTrotineta(int x, int y){
        this.mapa[x][y].retirar();
    }

    /** Retorna o número de trotinetes num determinado local.
     */
    public int getTrotinetasIn(int x, int y){
        return this.mapa[x][y].getNtrotinetes();
    }


    /** Indica as posições que estão a volta da Localizacao num determinado raio.
     */
    public List<Localizacao> getSurroundings(Localizacao l, int raio){
        List<Localizacao> surroundings = new ArrayList<Localizacao>();
        if (!this.validPos(l.getX(), l.getY())) return surroundings;
        surroundings.add(l);
        List<Localizacao> nova = new ArrayList<Localizacao>();
        nova.add(l);
        int x, y;
        for( ; !(nova.isEmpty()); ){
            Localizacao locl = nova.get(0);
            nova.remove(0);
            int new_d = distance(l.getX(), l.getY(), locl.getX(), locl.getY());
            if (new_d >= raio){
                break;
            }
            int [][] poses = {{locl.getX()+1, locl.getY()}, {locl.getX()-1, locl.getY()}, {locl.getX(), locl.getY()+1}, {locl.getX(), locl.getY()-1}};
            for(int j = 0; j < poses.length; j++){
                x = poses[j][0];
                y = poses[j][1];
                if (this.validPos(x,y) && !surroundings.contains(this.getLocalizacao(x,y))){
                    nova.add(this.getLocalizacao(x,y));
                    surroundings.add(this.getLocalizacao(x,y));
                }
            }
        }
        return surroundings;
    }

    /** Indica as posições em que estão trotinetes num raio de 2 relativamente a uma determinada posição. REQUISITO 1
     */
    public List<Localizacao> trotinetesArround(int x, int y){
        int raio = 2;
        List<Localizacao> trotinetes_livres = new ArrayList<Localizacao>();
        this.lock.lock();
        try{
            List<Localizacao> pos_arround = this.getSurroundings(this.getLocalizacao(x,y), raio);
            trotinetes_livres = pos_arround.stream().filter(p -> p.getNtrotinetes()>0).collect(Collectors.toList());;
            return trotinetes_livres;

        }finally {
            this.lock.unlock();
        }
    }


    /** Retorna uma List<Localizacao> onde indica a posição de trotinetes.
     */
    public List<Localizacao> whereAreTrotinetes(){
        List<Localizacao> trotinetes = new ArrayList<Localizacao>();
        this.lock.lock();
        try {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (getTrotinetasIn(i, j) > 0) {
                        trotinetes.add(getLocalizacao(i, j));
                    }
                }
            }
            return trotinetes;
        } finally {
            this.lock.unlock();
        }
    }

    /** Retorna uma List<Localizacao> onde indica a inexistência de trotinetes num raio de 2.
     */
    public List<Localizacao> getClearAreas(){
        List<Localizacao> clear = new ArrayList<Localizacao>();
        this.lock.lock();
        try{
            List<Localizacao> trotinetes = this.whereAreTrotinetes();
            HashSet<Localizacao> withTrArround = new HashSet<Localizacao>();
            for (Localizacao l : trotinetes){
                withTrArround.addAll(getSurroundings(l, 2));
            }
            for (int i=0; i<N; i++)
                for (int j=0; j<N; j++)
                    if (!withTrArround.contains(mapa[i][j]))
                        clear.add(mapa[i][j]);
            return clear;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i=0; i<this.N; i++){
            for (int j = 0; j < this.N; j++) {
                res.append(mapa[i][j].getNtrotinetes());
            }
            res.append("\n");
        }
        return res.toString();
    }


    // Just testing.
    public static void main(String[] args) {
        Mapa mapa = new Mapa(10);
        List<Localizacao> locals;
        System.out.println(mapa);
        locals = mapa.getClearAreas();
        System.out.println(locals);

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