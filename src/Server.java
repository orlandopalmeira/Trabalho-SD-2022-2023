import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(12345);

        final Accounts accounts = new Accounts();
        final Mapa mapa = new Mapa(10);
        System.out.println(mapa); // DEBUG

        final ReentrantReadWriteLock rewardslock = new ReentrantReadWriteLock();
        final Condition rewardsCond = rewardslock.writeLock().newCondition();
        final HashSet<Recompensa> recompensas = mapa.getRewards();

        // Geração de recompensas em background.
        Thread evalRewards = new Thread(() -> {
            while(true) {
                HashSet<Recompensa> newRecompensas = mapa.getRewards();
                HashSet<Pair> toSignal = new HashSet<>();
                for (Recompensa nr : newRecompensas) {
                    if (!recompensas.contains(nr)){
                        toSignal.addAll(mapa.getSurroundings(nr.origem, 2)); // adiciona as novas posicoes de origem que têm novas recompensas
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
                    boolean isLoggedIn = false;
                    while(true){
                        Frame frame = c.receive();
                        /*
                        // se não pretende registar-se ou fazer login, então não é permitido acesso às funcionalidades.
                        if (frame.tag != 0 && frame.tag != 1 && !isLoggedIn){
                            c.send(frame.tag, "Erro. Não está registrado.".getBytes());
                            continue;
                        }
                         */
                        // User registration attempt
                        if (frame.tag == 0) {
                            System.out.println("User registration attempt."); // pequeno log.
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            String username = tokens[0];
                            String password = tokens[1];
                            boolean flag = accounts.addAccount(username, password);
                            if (flag) {
                                isLoggedIn = true;
                                c.send(frame.tag, "1".getBytes()); // "1" é mensagem de sucesso.
                            }
                            else {
                                c.send(frame.tag, "0".getBytes());// "0" é mensagem de conta ja existente.
                            }
                        }
                        // User log-in attempt.
                        else if (frame.tag == 1) {
                            System.out.println("User log-in attempt."); // pequeno log.
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            String username = tokens[0];
                            String password = tokens[1];
                            String stored_password;
                            stored_password = accounts.getPassword(username);
                            if (stored_password != null) {
                                if (stored_password.equals(password)) {
                                    isLoggedIn = true;
                                    c.send(frame.tag, "1".getBytes()); // "1" é mensagem de sucesso.
                                }
                                else
                                    c.send(frame.tag, "Erro-palavra-passe errada.".getBytes()); // Talvez alterar para mensagem mais curta.
                            } else
                                c.send(frame.tag, "Erro-conta não existe.".getBytes()); // Talvez alterar para mensagem mais curta.
                        }
                        // Probing de trotinetes à volta duma área.
                        else if (frame.tag == 2){
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            int x = Integer.parseInt(tokens[0]);
                            int y = Integer.parseInt(tokens[1]);
                            System.out.printf("Probing de trotinetes em (%d,%d).%n", x,y); // LOG
                            List<Pair> ls = mapa.trotinetesArround(x,y);
                            c.send(frame.tag, Pair.toStringPairs(ls).getBytes());
                        }
                        // Probing de recompensas com origem numa localizacao.
                        else if (frame.tag == 3) {
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            int x = Integer.parseInt(tokens[0]);
                            int y = Integer.parseInt(tokens[1]);
                            System.out.printf("Probing de recompensas em (%d,%d).%n", x,y); // LOG
                            Set<Recompensa> rs = mapa.getRewardsWithOrigin(x,y);
                            c.send(frame.tag, Recompensa.toStringRecompensas(rs).getBytes());
                        }
                        // Reservar trotinete
                        else if (frame.tag == 4){
                            // todo to be determined.
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            int x = Integer.parseInt(tokens[0]);
                            int y = Integer.parseInt(tokens[1]);
                            System.out.printf("Pedido de reserva de trotinete em (%d,%d).%n", x,y); // LOG
                            List<Pair> lista = mapa.trotinetesArround(x,y);
                            if(lista.size() > 0){
                                Pair closest = lista.get(0);
                                mapa.retiraTrotineta(closest);
                                rewardsCond.signalAll(); // sinaliza uma alteração no mapa para o gerador de recompensas.
                                // Enviar codigo de sucesso e localizacao
                                c.send(frame.tag, closest.toString().getBytes());
                            }
                            else{
                                // Enviar codigo de insucesso.
                                c.send(frame.tag, "Erro".getBytes());
                            }
                        }
                        // Estacionar trotinete
                        else if (frame.tag == 5){
                            String data = new String(frame.data);

                            // todo to be determined.
                        }
                        // Pedido de notificacao
                        else if (frame.tag == 6){
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            int x = Integer.parseInt(tokens[0]);
                            int y = Integer.parseInt(tokens[1]);
                            System.out.printf("Pedido de notificação de recompensas na área de (%d,%d).%n", x,y); // LOG
                            Pair watchedLocal = new Pair(x,y);

                            // todo to be determined.
                        }

                    }
                } catch (IOException exc){
                    System.out.println(exc);
                    //throw exc;
                }
            };
            new Thread(processing).start();
            }
    }
}
