import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Server {
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(12345);

        final Accounts accounts = new Accounts();
        final Mapa mapa = new Mapa(10);
        System.out.println(mapa); // Printa o estado inicial do mapa.

        // Trata da geração de códigos de reserva de trotinetes.
        UniqueCode codeGenerator = new UniqueCode();

        // Gere a reserva de trotinetes.
        TrotinetesReservadas trotinetesReservadas = new TrotinetesReservadas();

        // Estruturas auxiliares de suporte ao mecanismo de geração de recompensas.
        final ReentrantReadWriteLock rewardslock = new ReentrantReadWriteLock(); // TODO talvez pensar se um simples lock seria melhor.
        final Condition rewardsCond = rewardslock.writeLock().newCondition();
        final HashSet<Recompensa> recompensas = mapa.getRewards();

        // Geração de recompensas em background.
        Thread evalRewards = new Thread(() -> {
            while(true) {
                HashSet<Recompensa> newRecompensas = mapa.getRewards();
                HashSet<Pair> toSignal = new HashSet<>();
                HashSet<Recompensa> difRec = new HashSet<>(); // FIXME FOR DEBUG. ELIMINATE LATER.
                for (Recompensa nr : newRecompensas) {
                    if (!recompensas.contains(nr)){
                        toSignal.addAll(mapa.getSurroundings(nr.origem, 2)); // adiciona as novas posicoes vizinhas de origem que têm novas recompensas.
                        difRec.add(nr); // FIXME FOR DEBUG. ELIMINATE LATER.
                    }
                }
                for (Pair p: toSignal){
                    mapa.signalLocations(toSignal); // sinaliza as novas posicoes originais de recompensa.
                }
                rewardslock.writeLock().lock();
                try {
                    recompensas.clear();
                    recompensas.addAll(newRecompensas);
                    rewardsCond.await(); // fica à espera que haja uma mudanca no mapa (reservamento ou estacionamento de trotinete).
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    rewardslock.writeLock().unlock();
                }
            }
        });
        evalRewards.start();


        while(true) {
            Socket s = ss.accept();
            Connection c = new Connection(s);

            /* The runnable that executes the processing of a client connection. */
            Runnable processing = () -> {
                try(c){

                    HashMap<Pair, Thread> notificationThreadsMap = new HashMap<>(); // para armazenar as threads que tratam das notificações.
                    boolean isLoggedIn = false;

                    while(true){
                        Frame frame = c.receive();

                        // User registration attempt
                        if (frame.tag == 0) {
                            System.out.println("User registration attempt."); // pequeno log.
                            AccountInfo acc = (AccountInfo) frame.data;
                            String username = acc.username;
                            String password = acc.password;
                            boolean flag = accounts.addAccount(username, password);
                            if (flag) {
                                isLoggedIn = true;
                                c.send(13, new Mensagem(1)); // "1" é mensagem de sucesso.
                            }
                            else {
                                c.send(13, new Mensagem(0));// "0" é mensagem de conta ja existente.
                            }
                        }
                        // User log-in attempt.
                        else if (frame.tag == 1) {
                            System.out.println("User log-in attempt."); // pequeno log.
                            AccountInfo acc = (AccountInfo) frame.data;
                            String username = acc.username;
                            String password = acc.password;
                            String stored_password;
                            stored_password = accounts.getPassword(username);
                            if (stored_password != null) {
                                if (stored_password.equals(password)) {
                                    isLoggedIn = true;
                                    c.send(13, new Mensagem(1)); // "1" é mensagem de sucesso.
                                }
                                else
                                    c.send(13, new Mensagem(0)); // "0" é mensagem de password errada.
                            } else
                                c.send(13, new Mensagem(2)); // "2" é mensagem de conta nem sequer existe.
                        }

                        // Probing de trotinetes à volta duma área.
                        if (frame.tag == 2){
                            Pair data = (Pair) frame.data;
                            int x = data.getX(), y = data.getY();
                            System.out.printf("Probing de trotinetes em (%d,%d).%n", x,y); // LOG
                            PairList ls = mapa.trotinetesArround(x,y);
                            c.send(11, ls);
                        }
                        // Probing de recompensas com origem numa localizacao.
                        else if (frame.tag == 3) {
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Probing de recompensas em (%d,%d).%n", x,y); // LOG
                            RecompensaList rs = mapa.getRewardsWithOrigin(x,y);
                            c.send(12, rs);
                        }
                        // Reservar trotinete
                        else if (frame.tag == 4){
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Pedido de reserva de trotinete em (%d,%d).%n", x,y); // LOG
                            List<Pair> lista = mapa.trotinetesArround(x,y);
                            if(lista.size() > 0){ // se houver trotinetes disponiveis.
                                Pair closest = lista.get(0);
                                mapa.retiraTrotineta(closest);
                                rewardslock.writeLock().lock();
                                try{
                                    rewardsCond.signalAll(); // sinaliza uma alteração no mapa para o gerador de recompensas. todo talvez passar so a signal.
                                } finally {
                                    rewardslock.writeLock().unlock();
                                }
                                // Enviar codigo de sucesso e localizacao
                                int myCode = codeGenerator.getCode();
                                /////// ADICIONAR ESTE NÚMERO A ALGUMA ESTRUTURA DE DADOS, PARA POSTERIOR VERIFICAÇAO NO ESTACIONAMENTO.//////
                                CodigoReserva cr = new CodigoReserva(myCode, closest);
                                trotinetesReservadas.add(cr);
                                c.send(14, cr);
                            }
                            else{
                                // Enviar codigo de insucesso.
                                CodigoReserva cr = new CodigoReserva(-1);
                                c.send(frame.tag, cr);
                            }
                        }
                        // Estacionar trotinete
                        else if (frame.tag == 5){
                            // todo Falta lógica de códigos de reserva.
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Estacionamento de trotinete em (%d,%d).%n", x,y); // LOG
                            boolean flag = mapa.addTrotineta(x,y);
                            if(flag){
                                rewardslock.writeLock().lock();
                                rewardsCond.signalAll(); // sinaliza uma alteração no mapa para o gerador de recompensas.
                                rewardslock.writeLock().unlock();
                                // FIXME Envio de codigo de sucesso.
                                //c.send(frame.tag, "1".getBytes());
                            }
                            else{
                                // FIXME Envio de codigo de insucesso.
                                //c.send(frame.tag, "0".getBytes());
                            }
                        }
                        // Pedido de notificacao
                        else if (frame.tag == 6){
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Pedido de notificação de recompensas na área de (%d,%d).%n", x,y); // LOG
                            Pair watchedLocal = new Pair(x,y);
                            if (notificationThreadsMap.containsKey(watchedLocal)){
                                c.send(13, new Mensagem(0));
                                continue;
                            }
                            Thread sendNotifications = new Thread(() -> {
                                try {
                                    while(true){
                                        boolean flag = true;
                                        RecompensaList newl = null, ant = mapa.getRewardsWithOrigin(watchedLocal.getX(),watchedLocal.getY());
                                        while(flag){
                                            Localizacao l = mapa.getLocalizacao(watchedLocal);
                                            l.lock.writeLock().lock();
                                            try{
                                                l.cond.await();
                                            } finally {
                                                l.lock.writeLock().unlock();
                                            }
                                            newl = mapa.getRewardsWithOrigin(watchedLocal.getX(),watchedLocal.getY());
                                            flag = ant.containsAll(newl);
                                        }
                                        newl.removeAll(ant);
                                        c.send(30, newl);
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            notificationThreadsMap.put(watchedLocal, sendNotifications);
                            sendNotifications.start();
                            // Envio de codigo de pedido de notificação bem sucedido.
                            c.send(13, new Mensagem(1));
                        }
                        System.out.println(mapa);

                    }
                } catch (IOException exc){
                    //System.out.println(exc); // FIXME remove
                    //throw exc;
                }
            };
            new Thread(processing).start();
            }
    }
}
