import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO Determinar nestas funcões como é retornado a informação de quantas trotinetes existem num local, tendo em conta que pode haver mais que uma trotinete no mesmo sitio.
public class Mapa {

    private Localizacao[][] mapa;   /** em cada posição do array temos a localizacao nessa posição */
    private int num_trotinetes;     /** número de trotinetes */
    private int N;                  /** tamanho do mapa */

    /**
     * Construtor da classe Mapa.
     * DETALHE: Atualmente, o posicionamento inicial de trotinetes não está a ser feito aleatoriamente, para DEBUG.
     * @param n Tamanho do mapa.
     */
    public Mapa(int n) {
        this.N = n;
        this.mapa = new Localizacao[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mapa[i][j] = new Localizacao(i,j);
            }
        }
        this.num_trotinetes = 8;
        // ESTOU A DEFINIR UMA ESTRUTURA MAIS EXATA, INVES DE INICIAR ALEATORIAMENTE AS TROTINETAS POR MOTIVOS DE DEBUG /////////////////
        this.addTrotineta(2,1);
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
        this.N = n;
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
        this.N = 20;
        this.mapa = new Localizacao[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                mapa[i][j] = new Localizacao(i,j);
            }
        }
        this.num_trotinetes = 10;
        this.randomTrotinetes(num_trotinetes);
    }

    /** Coloca um certo número de trotinetes aleatoriamente no mapa, havendo o cuidado de não colocar trotinetes no mesmo sitio.
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

    /**
     * Retorna o tamanho do mapa.
     */
    public int getN(){
        return this.N;
    }

    /** Verifica se a posição está dentro dos limites do mapa estabelecido.
     */
    public boolean validPos (int x, int y){
        return x < this.N && y < this.N && x >= 0 && y >= 0;
    }

    /** Verifica se a posição está dentro dos limites do mapa estabelecido.
     */
    public boolean validPos (Pair p){
        int x = p.getX(), y = p.getY();
        return x < this.N && y < this.N && x >= 0 && y >= 0;
    }


    /** Retorna uma lista dos objetos Localizacao correspondentes às coordenadas fornecidas.
     */
    public List<Localizacao> returnLocals(List<Pair> pairs){
        List<Localizacao> ret = new ArrayList<Localizacao>();
        for (Pair p: pairs){
            ret.add(getLocalizacao(p));
        }
        return ret;
    }

    /**
     * Retorna a matriz do mapa - inutilizado
     */
    public Localizacao[][] getMapa() {
        Localizacao[][] newMapa = new Localizacao[N][N];
        lockAllLocais();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    try{
                        newMapa[i][j] = mapa[i][j].clone();
                    }finally {
                        mapa[i][j].lock.readLock().unlock();
                    }
                }
            }
        return newMapa;
    }


    /**
     * Retorna o objeto Localizacao naquela coordenada.
     * Retorna Null caso as coordenadas não sejam válidas.
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

    /**
     * Retorna o objeto Localizacao naquela coordenada.
     * Retorna Null caso as coordenadas não sejam válidas.
     */
    public Localizacao getLocalizacao(Pair p) {
        int x = p.getX(), y = p.getY();
        return getLocalizacao(x,y);
    }

    /**
     * Bloqueia o readLock de todos os locais individuais. (PERIGOSA)
     */
    private void lockAllLocais(){
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                mapa[i][j].lock.readLock().lock();
    }

    /**
     * Desbloqueia o readLock de todos os locais individuais.
     */
    private void unlockAllLocais(){
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                mapa[i][j].lock.readLock().unlock();
    }

    /**
     * Bloqueia devidamente todos os locais individuais de maneira a libertar as localizacoes mais rapidamente. (PERIGOSA)
     * É FEITO SÓ READLOCK.
     */
    private void lockTheseLocais(List<Pair> localizacoes){
        List<Pair> locals = new ArrayList<>(localizacoes);
        Collections.sort(locals);
        for (Pair p : locals){
            Localizacao l = this.getLocalizacao(p.getX(), p.getY());
            l.lock.readLock().lock();
        }
    }

    /**
     * Para notificacar aquando aparece uma trotinete num determinado local.
     * AVISO ((Não está totalmente implementado))
     */
    public void getNotifTrot(int x, int y) throws InterruptedException {
        mapa[x][y].lockLocal();
        try {
            while (mapa[x][y].getNtrotinetes() == 0){
                mapa[x][y].getCond().await();
            }
            throw new InterruptedException();
        }
        finally {
            mapa[x][y].unlockLocal();
        }
    }

    /**
     * Adiciona uma trotinete ao local indicado.
     */
    public void addTrotineta(int x, int y){
        // TODO lock local de variaveis do mapa, talvez.
        num_trotinetes++;
        this.mapa[x][y].somar();
        //this.mapa[x][y].getCond().signalAll(); //Não está corretamente implementado.
    }

    /**
     * Adiciona uma trotinete ao local indicado.
     */
    public void addTrotineta(Pair p){
        addTrotineta(p.getX(), p.getY());
    }

    /**
     * Retira uma trotinete ao local indicado.
     */
    public void retiraTrotineta(int x, int y){
        // TODO lock local de variaveis do mapa, talvez.
        num_trotinetes--;
        this.mapa[x][y].retirar();
    }

    /**
     * Retira uma trotinete ao local indicado.
     */
    public void retiraTrotineta(Pair p){
        retiraTrotineta(p.getX(), p.getY());
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Retorna o número de trotinetes num determinado local.
     * Não faço locks gerais do mapa nesta função uma vez que é auxiliar a funcoes que o fazem.
     */
    private int getTrotinetasIn(int x, int y){
        Localizacao l = this.getLocalizacao(x,y);
        l.lock.readLock().lock();
        try{
            return l.getNtrotinetes();
        } finally {
            l.lock.readLock().unlock();
        }
    }


    /**
     * Indica as posições que estão a volta do Pair num determinado raio.
     * Não se faz nenhum lock nesta função, pois é puramente matematica.
     * DETALHE: A coordenada central que vem no argumento, é também retornada, sendo ela própria considerada como vizinha dela mesmo.
     */
    public List<Pair> getSurroundings(Pair p, int raio){
        return this.getSurroundings(p.getX(), p.getY(), raio);
    }

    /**
     * Indica as posições que estão a volta das coordenadas num determinado raio.
     * Não se faz nenhum lock nesta função, pois é puramente matematica.
     * DETALHE: A coordenada central que vem no argumento, é também retornada, sendo ela própria considerada como vizinha dela mesmo.
     */
    public List<Pair> getSurroundings(int x, int y, int raio){
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

    /**
     * Sinaliza todas as localizaçoes vizinhas.
     * @param pares lista de objetos Pair.
     */
    public void signalLocations (Collection<Pair> pares){
        for (Pair p: pares){
            Localizacao l = getLocalizacao(p);
            l.lock.writeLock().lock();
            l.cond.signalAll();
            l.lock.writeLock().unlock();
        }
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Indica as posições em que estão trotinetes num raio de 2 relativamente a uma determinada posição. REQUISITO 1
     * Quando ha mais que uma tronineta numa coordenada é repetido o Pair.
     */
    public List<Pair> trotinetesArround(int x, int y){
        int raio = 2;
        List<Pair> trotinetes_livres = new ArrayList<Pair>();
        List<Pair> arround = this.getSurroundings(x, y, raio);
        this.lockTheseLocais(arround); // faz-se readLock das redondezas da coordenada de maneira ordenada.
        for (Pair p: arround){
            Localizacao l = this.getLocalizacao(p.getX(), p.getY());
            for (int i = 0; i < l.getNtrotinetes(); i++){ // repito a coordenada quando ha mais que uma trotinete no mesmo sitio.
                trotinetes_livres.add(p);
            }
            l.lock.readLock().unlock(); // Faço unlock pois a funcao lockTheseLocais lockou estas Localizacao's.
        }
        return trotinetes_livres;
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Retorna uma List<Localizacao> onde indica a posição de trotinetes.
     * Funcao auxiliar.
     */
    private List<Pair> whereAreTrotinetes(){
        List<Pair> trotinetes = new ArrayList<Pair>();
        this.lockAllLocais();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (getTrotinetasIn(i, j) > 0) {
                    trotinetes.add(new Pair(i,j));
                }
                mapa[i][j].lock.readLock().unlock();// desbloqueio individual das localizacoes previamente bloquadas em lockAllLocais.
            }
        }
        return trotinetes;
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Retorna uma List<Localizacao> onde indica a inexistência de trotinetes num raio de 2.
     * Funcao auxiliar.
     */
    public List<Pair> getClearAreas(){
        List<Pair> clearLocals = new ArrayList<Pair>();
        HashSet<Localizacao> withTrotArround = new HashSet<Localizacao>();
        lockAllLocais();
        List<Pair> trotinetes = this.whereAreTrotinetes();
        for (Pair p : trotinetes){
            withTrotArround.addAll(returnLocals(getSurroundings(p, 2)));
        }
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                Localizacao l = getLocalizacao(i, j);
                if (!withTrotArround.contains(l))
                    clearLocals.add(new Pair(l.getX(), l.getY()));
                l.lock.readLock().unlock(); // desbloqueio individual das localizacoes previamente bloquadas em lockAllLocais.
            }
        return clearLocals;
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Função que retorna todas as recompensas em vigor naquele determinado mapa.
     * O critério de recompensa atual é: o destino não tem nenhuma trotinete num raio de 2 unidades, e a origem tem que ter uma trotineta e no seu raio de 2 unidades também existir, pelo menos, uma outra trotineta.
     */
    public HashSet<Recompensa> getRewards(){
        HashSet<Recompensa> rewards = new HashSet<Recompensa>();

        lockAllLocais();
        try {
            // Locais destino de recompensas.
            List<Pair> clearAreas = this.getClearAreas();

            // Obtencao de locais de origem para recompensas.
            List<Pair> trotinetas = this.whereAreTrotinetes();
            //for (Pair central: trotinetas){
            for (int i = 0; i < trotinetas.size(); i++) {
                Pair central = trotinetas.get(i);
                List<Pair> surronding = this.getSurroundings(central.getX(), central.getY(), 2);
                int sum = 0;
                for (Pair sur : surronding) {
                    Localizacao l = getLocalizacao(sur.getX(), sur.getY());
                    sum += this.getTrotinetasIn(sur.getX(), sur.getY());
                    if (sum > 1) { // se na area estiver mais que 1 trotineta, esta área é considerada cheia, sendo uma origem de recompensa.
                        for (Pair ca : clearAreas) {
                            rewards.add(new Recompensa(central, ca));
                            rewards.add(new Recompensa(sur, ca)); //
                        }
                        trotinetas.remove(sur); // uma vez que já se faz a adiçao das recompensas quando se encontra uma trotineta surronding.
                        break;
                    }
                }
            }
            return rewards;
        } finally {
            unlockAllLocais();
        }
    }

    /** FIXME ARRANJADOS OS LOCKS
     * Função que calcula as recompensas em vigor que têm origem num raio de 2 unidades de (x,y).
     * O critério de recompensa atual é: o destino não tem nenhuma trotinete num raio de 2 unidades, e a origem tem que ter uma trotineta e no seu raio de 2 unidades tbm existir, pelo menos, uma outra trotineta.
     */
    public Set<Recompensa> getRewardsWithOrigin(int x, int y){
        Set<Recompensa> rewards = new HashSet<Recompensa>();

        lockAllLocais();
        try {
            // Locais destino de recompensas.
            List<Pair> clearAreas = this.getClearAreas();

            // Obtencao de locais de origem para recompensas.
            List<Pair> trotinetas = this.trotinetesArround(x, y);
            for (int i = 0; i < trotinetas.size(); i++) {
                Pair central = trotinetas.get(i);
                List<Pair> surronding = this.getSurroundings(central.getX(), central.getY(), 2);
                int sum = 0;
                for (Pair sur : surronding) {
                    Localizacao l = getLocalizacao(sur.getX(), sur.getY());
                    sum += this.getTrotinetasIn(sur.getX(), sur.getY());
                    if (sum > 1) { // se na area estiver mais que 1 trotineta, esta área é considerada cheia, sendo uma origem de recompensa.
                        for (Pair ca : clearAreas) {
                            rewards.add(new Recompensa(central, ca));
                            rewards.add(new Recompensa(sur, ca)); //
                        }
                        trotinetas.remove(sur); // uma vez que já se faz a adiçao das recompensas quando se encontra uma trotineta surronding.
                        break;
                    }
                }
            }
            return rewards;
        } finally {
            unlockAllLocais();
        }
    }


    @Override
    // TODO talvez implementar gestao de concorrencia no toString de mapa.
    public String toString() {
        StringBuilder res = new StringBuilder();
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