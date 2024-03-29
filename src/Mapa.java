import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        /*
        this.addTrotineta(2,1);
        this.addTrotineta(2,4);
        this.addTrotineta(4,1);
        this.addTrotineta(5,9);
        this.addTrotineta(6,1);
        this.addTrotineta(8,4);
        this.addTrotineta(8,9);
        this.addTrotineta(9,4);
         */
        this.randomTrotinetes(num_trotinetes);
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
     * Retorna o objeto Localizacao daquela coordenada.
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
     * Retorna o objeto Localizacao daquela coordenada.
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
     * Adiciona uma trotinete ao local indicado.
     * Retorna true se a operação correu com sucesso, falso caso a posição indicada não esteja no mapa.
     */
    public boolean addTrotineta(int x, int y){
        if (validPos(x,y)){
            num_trotinetes++;
            this.mapa[x][y].somar();
            return true;
        }
        return false;

    }

    /**
     * Adiciona uma trotinete ao local indicado.
     * Retorna true se a operação correu com sucesso, falso caso a posição indicada não esteja no mapa.
     */
    public boolean addTrotineta(Pair p){
        return addTrotineta(p.getX(), p.getY());
    }

    /**
     * Retira uma trotinete ao local indicado.
     * Retorna true se a operação correu com sucesso, falso caso a posição indicada não esteja no mapa.
     */
    public boolean retiraTrotineta(int x, int y){
        if (validPos(x,y)){
            num_trotinetes--;
            this.mapa[x][y].retirar();
            return true;
        }
        return false;
    }

    /**
     * Retira uma trotinete ao local indicado.
     * Retorna true se a operação correu com sucesso, falso caso a posição indicada não esteja no mapa.
     */
    public boolean retiraTrotineta(Pair p){
        return retiraTrotineta(p.getX(), p.getY());
    }

    /**
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
     * Sinaliza todas as localizaçoes da coleção.
     * @param pares coleção de objetos Pair.
     */
    public void signalLocations (Collection<Pair> pares){
        for (Pair p: pares){
            Localizacao l = getLocalizacao(p);
            l.lock.writeLock().lock();
            try{
                l.cond.signalAll();
            } finally {
                l.lock.writeLock().unlock();
            }
        }
    }

    /**
     * Indica as posições em que estão trotinetes num raio de 2 relativamente a uma determinada posição. REQUISITO 1
     * Quando ha mais que uma tronineta numa coordenada é repetido o Pair.
     */
    public PairList trotinetesArround(int x, int y){
        int raio = 2;
        PairList trotinetes_livres = new PairList();
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

    /**
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

    /**
     * Retorna uma List<Localizacao> onde indica a inexistência de trotinetes num raio de 2.
     * Funcao auxiliar.
     */
    private List<Pair> getClearAreas(){
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

    /**
     * Função que retorna todas as recompensas em vigor naquele determinado mapa.
     * O critério de recompensa atual é: o destino não tem nenhuma trotinete num raio de 2 unidades, e a origem tem que ter uma trotineta e no seu raio de 2 unidades também existir, pelo menos, uma outra trotineta.
     */
    public RecompensaList getRewards(){
        RecompensaList rewards = new RecompensaList();

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

    /**
     * Função que calcula as recompensas em vigor que têm origem num raio de 2 unidades de (x,y).
     * O critério de recompensa atual é: o destino não tem nenhuma trotinete num raio de 2 unidades, e a origem tem que ter uma trotineta e no seu raio de 2 unidades tbm existir, pelo menos, uma outra trotineta.
     */
    public RecompensaList getRewardsWithOrigin(int x, int y){
        RecompensaList rewards = new RecompensaList();

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
                    sum += this.getTrotinetasIn(sur.getX(), sur.getY());
                    if (sum > 1) { // se na area estiver mais que 1 trotineta, esta área é considerada cheia, sendo uma origem de recompensa.
                        for (Pair ca : clearAreas) {
                            rewards.add(new Recompensa(central, ca));
                        }
                        break;
                    }
                }
            }
            return rewards;
        } finally {
            unlockAllLocais();
        }
    }

    /**
     * Para verificação de recompensas na altura do estacionamento.
     * @param p Objeto Pair.
     * @return RecompensaList com as recompensas.
     */
    public RecompensaList getRewardsIn(Pair p){
        RecompensaList rewards = new RecompensaList();
        List<Pair> surroundings = getSurroundings(p, 2);
        int x = p.getX(), y = p.getY();
        lockAllLocais();
        try {
            // Locais destino de recompensas.
            List<Pair> clearAreas = this.getClearAreas();
            clearAreas = clearAreas.stream().filter(a -> !surroundings.contains(a)).toList();

            List<Pair> trotinetas = this.trotinetesArround(x,y);
            if (trotinetas.size() > 0){
                clearAreas.forEach(ca -> rewards.add(new Recompensa(p, ca)));
            }
        } finally {
            unlockAllLocais();
        }
        return rewards;
    }


    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(this.N > 10){
            for (int i=0; i<this.N; i++){
                for (int j = 0; j < this.N; j++) {
                    res.append(mapa[i][j].getNtrotinetes());
                }
                res.append("\n");
            }
            return res.toString();
        }
        // Printa o mapa com os indices caso o tamanho do mapa seja menor que 10
        res.append(" :");
        for (int i = 0; i < N; i++) {
            res.append(i%10);
        }
        res.append("\n");
        for (int i=0; i<this.N; i++){
            res.append(i%10).append(":");
            for (int j = 0; j < this.N; j++) {
                res.append(mapa[i][j].getNtrotinetes());
            }
            res.append("\n");
        }
        return res.toString();
    }

}
