import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class Mapa {

    ReentrantLock lock;
    Condition cond; // por enquanto ainda n esta utilizado, mas talvez se use para alertar em casos de escrita no mapa para alterar geração de recompensas.
    private Localizacao[][] mapa;   /** em cada posição do array temos a localizacao nessa posição */
    private int num_trotinetes;     /** número de trotinetes */
    int N;                          /** tamanho do mapa */

    public Mapa(int n) {
        this.lock = new ReentrantLock();
        this.cond = lock.newCondition();
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

    public Localizacao pairToLocalizacao(Pair p){
        return this.getLocalizacao(p.x(), p.y());
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

    /** Bloqueia devidamente todos os locais individuais de maneira a libertar as localizacoes mais rapidamente. (PERIGOSA)
     */
    private void lockAllLocais(){
        this.lock.lock();
        try{
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    mapa[i][j].lockLocal();
        } finally {
            this.lock.unlock();
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
     * Não faço locks aqui porque esta funcao é uma funcao auxiliar em que locks sao feitos previamente noutras funcoes /// talvez seja preciso alterar.
     */
    // fazer alterações para apenas receber pares de coordenadas e dar pares de coordenadas, para simplificar logica de obtencao, uma vez que esta funcão é puramente matematica, não havendo necessidade de haver locks ao obter coordenadas, to do later
    private List<Localizacao> getSurroundings(Localizacao l, int raio){
        List<Localizacao> surroundings = new ArrayList<Localizacao>();
        if (!this.validPos(l.getX(), l.getY())) return surroundings; // esta condição é tecnicamente impossivel uma vez que passo a localização como argumento.
        surroundings.add(l);
        List<Localizacao> nova = new ArrayList<Localizacao>();
        nova.add(l);
        int x, y;
        while (!(nova.isEmpty())) {
            Localizacao locl = nova.get(0);
            nova.remove(0);
            int new_d = distance(l.getX(), l.getY(), locl.getX(), locl.getY());
            if (new_d >= raio){
                break;
            }
            int [][] poses = {{locl.getX()+1, locl.getY()}, {locl.getX()-1, locl.getY()}, {locl.getX(), locl.getY()+1}, {locl.getX(), locl.getY()-1}};
            for (int[] pose : poses) {
                x = pose[0];
                y = pose[1];
                if (this.validPos(x, y) && !surroundings.contains(this.getLocalizacao(x, y))) {
                    nova.add(this.getLocalizacao(x, y));
                    surroundings.add(this.getLocalizacao(x, y));
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
            List<Localizacao> arround = this.getSurroundings(this.getLocalizacao(x,y), raio);
            trotinetes_livres = arround.stream().filter(p -> p.getNtrotinetes()>0).collect(Collectors.toList());;
            return trotinetes_livres;

        }finally {
            this.lock.unlock();
        }
    }

    /** Retorna uma List<Localizacao> onde indica a posição de trotinetes.
     */
    public List<Localizacao> whereAreTrotinetes(){
        List<Localizacao> trotinetes = new ArrayList<Localizacao>();
        this.lockAllLocais(); // é feito um lock do mapa de inicio para lockar todas as localizacoes individuais e é de seguida desbloqueado o lock do mapa.
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (getTrotinetasIn(i, j) > 0) {
                    trotinetes.add(getLocalizacao(i, j));
                }
                mapa[i][j].unlockLocal();// desbloqueio individual das localizacoes previamente bloquadas em lockAllLocais.
            }
        }
        return trotinetes;
    }

    /** Retorna uma List<Localizacao> onde indica a inexistência de trotinetes num raio de 2.
     */
    public List<Localizacao> getClearAreas(){
        List<Localizacao> clear = new ArrayList<Localizacao>();
        HashSet<Localizacao> withTrotArround = new HashSet<Localizacao>();
        this.lock.lock();
        try{
            List<Localizacao> trotinetes = this.whereAreTrotinetes();
            for (Localizacao l : trotinetes){
                withTrotArround.addAll(getSurroundings(l, 2));
            }
            for (int i=0; i<N; i++)
                for (int j=0; j<N; j++)
                    if (!withTrotArround.contains(mapa[i][j]))
                        clear.add(mapa[i][j]);
            return clear;
        } finally {
            this.lock.unlock();
        }
    }

    //public



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

}






/*
    /** Retorna uma List<Localizacao> onde indica a posição de trotinetes.
     // Faz lock total e n liberta antecipadamente as localizacoes.
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
*/