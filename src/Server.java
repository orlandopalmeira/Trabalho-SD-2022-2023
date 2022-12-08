import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;

public class Server {
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(12345);

        final Accounts accounts = new Accounts();
        final Mapa mapa = new Mapa(10);
        System.out.println(mapa); // DEBUG

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
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            int x = Integer.parseInt(tokens[0]);
                            int y = Integer.parseInt(tokens[1]);
                            System.out.printf("Pedido de reserva de trotinete em (%d,%d).%n", x,y); // LOG
                            List<Pair> l = mapa.trotinetesArround(x,y);
                            if(l.size() > 0){
                                Pair closest = l.get(0);
                                // Enviar codigo de sucesso e localizacao
                                c.send(frame.tag, closest.toString().getBytes());
                            }
                            else{
                                // Enviar codigo de insucesso.
                                c.send(frame.tag, "".getBytes());

                            }
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
