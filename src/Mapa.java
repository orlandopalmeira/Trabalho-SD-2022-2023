import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Mapa {

    ReentrantLock lock;
    Condition cond; // por enquanto ainda n esta utilizado, mas talvez se use para alertar em casos de escrita no mapa para alterar geração de recompensas.
    private Localizacao[][] mapa;   /** em cada posição do array temos a localizacao nessa posição */
    private int num_trotinetes;     /** número de trotinetes */
    int N;                          /** tamanho do mapa */

    /**
     * Construtor da classe Mapa.
     * DETALHE: Atualmente, o posicionamento inicial de trotinetes não está a ser feito aleatoriamente, para DEBUG.
     * @param n Tamanho do mapa.
     */
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
        // ESTOU A DEFINIR UMA ESTRUTURA MAIS EXATA, INVES DE INICIAR ALEATORIAMENTE AS TROTINETAS POR MOTIVOS DE DEBUG /////////////////
        this.addTrotineta(1,1);
        this.addTrotineta(2,4);
        this.addTrotineta(4,1);
        this.addTrotineta(5,9);
        this.addTrotineta(6,1);
        this.addTrotineta(8,4);
        this.addTrotineta(8,9);
        this.addTrotineta(9,4);
        //this.randomTrotinetes(num_trotinetes);
    }

    /**
     * Construtor da classe Mapa.
     * @param n Tamanho do mapa.
     * @param trotinetes Número de trotinetes a introduzir no mapa.
     */
    public Mapa(int n, int trotinetes) {
        this.lock = new ReentrantLock();
        this.cond = lock.newCondition();
        this.N = n; ///// vai ser 20 no final
        this.mapa = new Localizacao[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mapa[i][j] = new Localizacao(i,j);
            }
        }
        this.num_trotinetes = trotinetes;
        this.randomTrotinetes(num_trotinetes);
    }

    /**
     * Construtor da classe Mapa, com tamanho de 20 unidades e 10 trotinetes inicialmente colocados.
     */
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


    /**
     * Transforma um Pair na respetiva Localizacao.
     * @param p Objeto da classe Pair.
     * @return Objeto correspondente da classe Localizacao.
     */
    public Localizacao pairToLocalizacao(Pair p){
        return this.getLocalizacao(p.getX(), p.getY());
    }

    /** Retorna uma lista dos objetos Localizacao correspondentes às coordenadas fornecidas.
     */
    public List<Localizacao> returnLocals(List<Pair> pairs){
        List<Localizacao> ret = new ArrayList<Localizacao>();
        for (Pair p: pairs){
            ret.add(pairToLocalizacao(p));
        }
        return ret;
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

    /** Bloqueia devidamente todos os locais individuais de maneira a libertar as localizacoes mais rapidamente. (PERIGOSA)
     *//*
    private void lockTheseLocais(List<Localizacao> locals){
        this.lock.lock();
        try{
            for (Localizacao l : locals)
                l.lockLocal();
        } finally {
            this.lock.unlock();
        }
    }
    */


    /** Bloqueia devidamente todos os locais individuais de maneira a libertar as localizacoes mais rapidamente. (PERIGOSA)
     */
    private void lockTheseLocais(List<Pair> locals){
        this.lock.lock();
        try{
            for (Pair p : locals){
                Localizacao l = this.getLocalizacao(p.getX(), p.getY());
                l.lockLocal();
            }
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
     * Alternativa a getSurrondings que recebe dois inteiros que são as coordenadas.
     * DETALHE: A coordenada central que vem no argumento, é também retornada, sendo ela própria considerada como surronding coordinate dela mesmo.
     */
    private List<Pair> getSurroundings(Pair p, int raio){
        return this.getSurroundings(p.getX(), p.getY(), raio);
    }

    /** Indica as posições que estão a volta da Localizacao num determinado raio.
     * Não faço locks aqui porque esta funcao é uma funcao auxiliar em que locks sao feitos previamente noutras funcoes /// talvez seja preciso alterar.
     * DETALHE: A coordenada central que vem no argumento, é também retornada, sendo ela própria considerada como surronding coordinate dela mesmo.
     */
    private List<Pair> getSurroundings(int x, int y, int raio){
        List<Pair> surroundings = new ArrayList<Pair>();
        if (!this.validPos(x, y)) return surroundings;
        Pair l = new Pair(x,y);
        surroundings.add(l);
        List<Pair> nova = new ArrayList<Pair>();
        nova.add(l);
        int new_d;
        while (!(nova.isEmpty())) {
            Pair locl = nova.get(0);
            nova.remove(0);
            new_d = l.distance(locl);
            if (new_d >= raio){
                break;
            }
            Pair [] poses = {new Pair(locl.getX()+1, locl.getY()), new Pair(locl.getX()-1, locl.getY()), new Pair(locl.getX(), locl.getY()+1), new Pair(locl.getX(), locl.getY()-1)};
            for (Pair pose : poses) {
                x = pose.getX();
                y = pose.getY();
                Pair p = new Pair(x,y);
                if (this.validPos(x, y) && !surroundings.contains(p)) {
                    nova.add(p);
                    surroundings.add(p);
                }
            }
        }
        return surroundings;
    }

    /** Indica as posições em que estão trotinetes num raio de 2 relativamente a uma determinada posição. REQUISITO 1
     */
    public List<Pair> trotinetesArround(int x, int y){
        int raio = 2;
        List<Pair> trotinetes_livres = new ArrayList<Pair>();
        List<Pair> arround = this.getSurroundings(x, y, raio);
        this.lock.lock();
        try {
            this.lockTheseLocais(arround); // Faço lock apenas aos sitios à volta da coordenada fornecida, pois só irei ler esses valores do mapa.
        } finally {
            this.lock.unlock();
        }
        for (Pair p: arround){
            Localizacao l = this.getLocalizacao(p.getX(), p.getY());
            if (l.getNtrotinetes() > 0){
                trotinetes_livres.add(p);
            }
            l.unlockLocal(); // Faço unlock pois a funcao lockTheseLocais lockou estas Localizacao's.
        }
        return trotinetes_livres;
    }

    /** Retorna uma List<Localizacao> onde indica a posição de trotinetes.
     */
    public List<Pair> whereAreTrotinetes(){
        List<Pair> trotinetes = new ArrayList<Pair>();
        this.lockAllLocais(); // é feito um lock do mapa de inicio para lockar todas as localizacoes individuais e é de seguida desbloqueado o lock do mapa.
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (getTrotinetasIn(i, j) > 0) {
                    trotinetes.add(new Pair(i,j));
                }
                mapa[i][j].unlockLocal();// desbloqueio individual das localizacoes previamente bloquadas em lockAllLocais.
            }
        }
        return trotinetes;
    }

    /** Retorna uma List<Localizacao> onde indica a inexistência de trotinetes num raio de 2.
     * /// Pode ser melhorada a sua contenção de locks e talvez passe a retornar e trabalhar com Pair's invés de Localizacao's.
     */
    public List<Pair> getClearAreas(){
        List<Pair> clearLocals = new ArrayList<Pair>();
        HashSet<Localizacao> withTrotArround = new HashSet<Localizacao>();
        this.lock.lock();
        try{
            List<Pair> trotinetes = this.whereAreTrotinetes();
            for (Pair l : trotinetes){
                withTrotArround.addAll(returnLocals(getSurroundings(l.getX(), l.getY(), 2)));
            }
            for (int i=0; i<N; i++)
                for (int j=0; j<N; j++) {
                    Localizacao l = getLocalizacao(i, j);
                    if (!withTrotArround.contains(l))
                        clearLocals.add(new Pair(l.getX(), l.getY()));
                }
            return clearLocals;
        } finally {
            this.lock.unlock();
        }
    }

    /** Função que retorna todas as recompensas em vigor naquele determinado mapa.
     * O critério de recompensa atual é: o destino não tem nenhuma trotinete num raio de 2 unidades, e a origem tem que ter uma trotineta e no seu raio de 2 unidades tbm existir, pelo menos, uma outra trotineta.
     */
    public Set<Recompensa> getRewards(){
        Set<Recompensa> rewards = new HashSet<Recompensa>();
        this.lock.lock();
        try{
            // Locais destino de recompensas.
            List<Pair> clearAreas = this.getClearAreas();

            // Obtencao de locais de origem para recompensas.
            List<Pair> trotinetas = this.whereAreTrotinetes();
            for (Pair central: trotinetas){ // central = central trotinet em que está a ser feita a verificaçao inicial.
                List<Pair> surronding = this.getSurroundings(central.getX(), central.getY(), 2);
                for (Pair sur: surronding){ // sur = surrounding trotinet que está a ser verificada relativamente à trotinete central.
                    // talvez adicionar nº de trotinetes na area como fator no valor da recompensa. Para isso seria preciso alterar a classe recompensa para admitir um valor de nº de trotinetes na area de origem para usar como fator no valor da recompensa.
                    if (!sur.equals(central) && this.getTrotinetasIn(sur.getX(), sur.getY()) > 0){
                        for (Pair ca: clearAreas){
                            rewards.add(new Recompensa(central, ca));
                            //rewards.add(new Recompensa(sur, ca));
                        }
                        //trotinetas.remove(sur); // uma vez que já se faz a adiçao das recompensas quando se encontra uma trotineta surronding.
                    }
                }
            }
            return rewards;
        } finally {
            this.lock.unlock();
        }
    }

    /** Função que calcula as recompensas em vigor, mas que têm origem num raio de 2 unidades de (x,y).
     * TO BE COMPLETED.
     */
    public Set<Recompensa> getRewardsWithOrigin(int x, int y){
        Set<Recompensa> rewards = new HashSet<Recompensa>();
        //int raio = 2;
        this.lock.lock();
        try{
            // Locais destino de recompensas.
            List<Pair> clearAreas = this.getClearAreas();

            // Obtencao de locais de origem para recompensas.
            List<Pair> trotinetas = this.trotinetesArround(x,y);
            for (Pair central: trotinetas){ // central = central trotinet em que está a ser feita a verificaçao inicial.
                List<Pair> surronding = this.getSurroundings(central.getX(), central.getY(), 2);
                for (Pair sur: surronding){ // sur = surrounding trotinet que está a ser verificada relativamente à trotinete central.
                    // talvez adicionar nº de trotinetes na area como fator no valor da recompensa. Para isso seria preciso alterar a classe recompensa para admitir um valor de nº de trotinetes na area de origem para usar como fator no valor da recompensa.
                    if (!sur.equals(central) && this.getTrotinetasIn(sur.getX(), sur.getY()) > 0){
                        for (Pair ca: clearAreas){
                            rewards.add(new Recompensa(central, ca));
                            //rewards.add(new Recompensa(sur, ca));
                        }
                        //trotinetas.remove(sur); // uma vez que já se faz a adiçao das recompensas quando se encontra uma trotineta surronding.
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }


        return rewards;
    }


    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        // tem acrescentado indices
        res.append(" :");
        for (int i = 0; i < N; i++) {
            res.append(i);
        }
        res.append("\n");
        for (int i=0; i<this.N; i++){
            res.append(i+":");
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