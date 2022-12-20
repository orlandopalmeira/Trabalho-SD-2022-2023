import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Server {
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(12345);

        final Mapa mapa = new Mapa(10);

        System.out.println(mapa); // Printa o estado inicial do mapa.

        // Gere as contas existentes.
        final Accounts accounts = new Accounts();

        // Trata da geração de códigos de reserva de trotinetes.
        UniqueCode codeGenerator = new UniqueCode();

        // Gere a reserva de trotinetes.
        TrotinetesReservadas trotinetesReservadas = new TrotinetesReservadas();

        // Estruturas auxiliares de suporte ao mecanismo de geração de recompensas.
        final ReentrantReadWriteLock rewardslock = new ReentrantReadWriteLock(); // TODO talvez pensar se um simples lock seria melhor.
        final Condition rewardsCond = rewardslock.writeLock().newCondition();
        final RecompensaList recompensas = mapa.getRewards();

        // Geração de recompensas em background.
        Thread evalRewards = new Thread(() -> {
            while(true) {
                rewardslock.writeLock().lock(); //// talvez limitar o começo do lock, so na parte de alteração da estrutura de dados.
                try {
                    HashSet<Recompensa> newRecompensas = mapa.getRewards();
                    HashSet<Pair> toSignal = new HashSet<>();
                    //HashSet<Recompensa> difRec = new HashSet<>();

                    for (Recompensa nr : newRecompensas) {
                        if (!recompensas.contains(nr)){
                            toSignal.addAll(mapa.getSurroundings(nr.origem, 2)); // adiciona as novas posicoes vizinhas de origem que têm novas recompensas.
                            //difRec.add(nr);
                        }
                    }
                    // sinaliza as novas posicoes com uma recompensa na origem.
                    mapa.signalLocations(toSignal);

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
                String thisUsername = "";
                HashMap<Pair, Thread> notificationThreadsMap = new HashMap<>();
                try (c) {

                    while (true) {
                        Frame frame = c.receive();

                        // User registration attempt
                        if (frame.tag == 0) {
                            AccountInfo acc = (AccountInfo) frame.data;
                            String username = acc.username;
                            String password = acc.password;
                            System.out.printf("User %s registration attempt.\n", username); // LOG
                            boolean flag = accounts.addAccount(username, password);
                            if (flag) {
                                thisUsername = username;
                                c.send(0, new Mensagem(1)); // "1" é mensagem de sucesso.
                            } else {
                                c.send(0, new Mensagem(0));// "0" é mensagem de conta ja existente.
                            }
                        }
                        // User log-in attempt.
                        else if (frame.tag == 1) {
                            AccountInfo acc = (AccountInfo) frame.data;
                            String username = acc.username;
                            String password = acc.password;
                            System.out.printf("User %s log-in attempt.\n", username); // LOG
                            String stored_password = accounts.getPassword(username);
                            if (stored_password != null) {
                                if (accounts.isLogged(username)) {
                                    c.send(0, new Mensagem(3)); // "3" é mensagem que indica que alguem já esta a usar a conta.
                                }
                                else if (stored_password.equals(password)) {
                                    thisUsername = username;
                                    c.send(0, new Mensagem(1)); // "1" é mensagem de sucesso.
                                } else
                                    c.send(0, new Mensagem(0)); // "0" é mensagem de password errada.
                            } else
                                c.send(0, new Mensagem(2)); // "2" é mensagem de conta não existe.

                        }

                        // Probing de trotinetes à volta duma área.
                        if (frame.tag == 2) {
                            Pair data = (Pair) frame.data;
                            int x = data.getX(), y = data.getY();
                            System.out.printf("Probing de trotinetes em (%d,%d).%n", x, y); // LOG
                            PairList ls = mapa.trotinetesArround(x, y);
                            c.send(0, ls);
                        }
                        // Probing de recompensas com origem numa localizacao.
                        else if (frame.tag == 3) {
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Probing de recompensas em (%d,%d).%n", x, y); // LOG
                            RecompensaList rs = mapa.getRewardsWithOrigin(x, y);
                            c.send(0, rs);
                        }
                        // Reservar trotinete
                        else if (frame.tag == 4) {
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Pedido de reserva de trotinete em (%d,%d).%n", x, y); // LOG
                            List<Pair> lista = mapa.trotinetesArround(x, y);
                            if (lista.size() > 0) { // se houver trotinetes disponiveis.
                                Pair closest = lista.get(0);
                                mapa.retiraTrotineta(closest);
                                rewardslock.writeLock().lock();
                                try {
                                    rewardsCond.signalAll(); // sinaliza uma alteração no mapa para o gerador de recompensas. todo talvez passar so a signal.
                                } finally {
                                    rewardslock.writeLock().unlock();
                                }
                                // Enviar codigo de sucesso e localizacao
                                int myCode = codeGenerator.getCode();
                                CodigoReserva cr = new CodigoReserva(myCode, closest);
                                trotinetesReservadas.add(cr, thisUsername);
                                c.send(0, cr);
                            } else {
                                // Enviar codigo de insucesso.
                                CodigoReserva cr = new CodigoReserva(-1);
                                c.send(0, cr);
                            }
                        }
                        // Estacionar trotinete
                        else if (frame.tag == 5) {
                            CodigoReserva cr = (CodigoReserva) frame.data;
                            int x = cr.getLocalizacao().getX();
                            int y = cr.getLocalizacao().getY();
                            System.out.printf("Pedido de estacionamento de trotinete em (%d,%d).%n", x, y); // LOG
                            if (!mapa.validPos(x, y)) {
                                c.send(0, new InfoViagem());
                                continue;
                            }
                            InfoViagem infoviagem = trotinetesReservadas.getInfoViagem(cr, thisUsername); // Verifica na informação sobre trotinetes reservadas.
                            boolean flag = false;
                            if (infoviagem.isSuccessful()) {
                                // Verificação de recompensas
                                RecompensaList rl = mapa.getRewardsIn(infoviagem.getOrigem());
                                Recompensa situation = new Recompensa(infoviagem.getOrigem(), infoviagem.getDestino());
                                if (rl.contains(situation)) {
                                    infoviagem.setRecompensa(situation);
                                }
                                // Estacionamento de trotinete no mapa.
                                flag = mapa.addTrotineta(x, y);
                            }
                            if (flag) {
                                rewardslock.writeLock().lock();
                                rewardsCond.signalAll(); // sinaliza uma alteração no mapa para o gerador de recompensas.
                                rewardslock.writeLock().unlock();
                                c.send(0, infoviagem);
                            } else {
                                c.send(0, new InfoViagem());
                            }
                        }
                        // Pedido de notificacao
                        else if (frame.tag == 6) {
                            Pair watchedLocal = (Pair) frame.data;
                            System.out.printf("Pedido de notificação de recompensas na área de %s por %s.%n", watchedLocal, thisUsername); // LOG
                            if (!mapa.validPos(watchedLocal) || notificationThreadsMap.containsKey(watchedLocal)) {
                                c.send(0, new Mensagem(0));
                                continue;
                            }
                            Thread sendNotifications = new Thread(() -> {
                                try {
                                    while (true) {
                                        boolean flag = true;
                                        RecompensaList newl = null, ant = mapa.getRewardsWithOrigin(watchedLocal.getX(), watchedLocal.getY());
                                        while (flag) {
                                            Localizacao l = mapa.getLocalizacao(watchedLocal);
                                            l.lock.writeLock().lock();
                                            try {
                                                l.cond.await();
                                            } finally {
                                                l.lock.writeLock().unlock();
                                            }
                                            newl = mapa.getRewardsWithOrigin(watchedLocal.getX(), watchedLocal.getY());
                                            flag = ant.containsAll(newl);
                                        }
                                        newl.removeAll(ant);
                                        c.send(30, newl);
                                    }
                                } catch (Exception e) {
                                    System.out.printf("Notificação do local %s cancelada.\n", watchedLocal);
                                }
                            });
                            notificationThreadsMap.put(watchedLocal, sendNotifications);
                            sendNotifications.start();
                            // Envio de mensagem de pedido de notificação bem sucedido.
                            c.send(0, new Mensagem(1));
                        }
                        // Desativação de notificação
                        else if (frame.tag == 7) {
                            Pair data = (Pair) frame.data;
                            int x = data.getX();
                            int y = data.getY();
                            System.out.printf("Desativação de notificação de recompensas na área de (%d,%d) por %s.%n", x, y, thisUsername); // LOG
                            if (notificationThreadsMap.containsKey(data)) {
                                Thread toKill = notificationThreadsMap.get(data);
                                toKill.interrupt();
                                notificationThreadsMap.remove(data);
                                c.send(0, new Mensagem(0));
                            } else {
                                c.send(0, new Mensagem(1));
                            }
                        }
                        System.out.println(mapa);
                    }
                } catch (IOException exc) {
                    // Quando um cliente se desconecta do servidor.
                    System.out.printf("Log-out do utilizador %s.\n", thisUsername);
                    accounts.logOutUser(thisUsername);
                    notificationThreadsMap.forEach((k, v) -> v.interrupt()); // interrompe as notificações que estão ativas.
                }
            };
            new Thread(processing).start();
            }
    }
}
