import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);

        final Accounts accounts = new Accounts();
        final Mapa mapa = new Mapa(10);

        while(true) {
            Socket s = ss.accept();
            Connection c = new Connection(s);

            /* The runnable that executes the processing of a message. */
            Runnable processing = () -> {
                try(c){
                    boolean isLoggedIn = false;
                    while(true){
                        Frame frame = c.receive();
                        // se não pretende registar-se ou fazer login, então não é permitido acesso às funcionalidades.
                        if (frame.tag != 0 && frame.tag != 1 && !isLoggedIn){
                            c.send(frame.tag, "Registo efetuado com sucesso!".getBytes());
                            continue;
                        }
                        // User registration attempt
                        if (frame.tag == 0) {
                            System.out.println("User registration attempt.");
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            String username = tokens[0];
                            String password = tokens[1];
                            boolean flag = accounts.addAccount(username, password);
                            if (accounts.addAccount(username, password)) {
                                isLoggedIn = true;
                                c.send(frame.tag, "Registo efetuado com sucesso!".getBytes()); // ALTERAR ISTO APOS SABER DESMULTIPLEXER.
                            }
                            else {
                                c.send(1, "Erro - username já pertence a uma conta.".getBytes());// ALTERAR ISTO APOS SABER DESMULTIPLEXER.
                            }
                        }
                        // User log-in attempt.
                        else if (frame.tag == 1) {
                            System.out.println("User log-in attempt.");
                            String data = new String(frame.data);
                            String [] tokens = data.split(" ");
                            String username = tokens[0];
                            String password = tokens[1];
                            String stored_password;
                            stored_password = accounts.getPassword(username);
                            if (stored_password != null) {
                                if (stored_password.equals(password)) {
                                    isLoggedIn = true;
                                    c.send(0, "Sessão iniciada com sucesso!".getBytes());// ALTERAR ISTO APOS SABER DESMULTIPLEXER.
                                }
                                else
                                    c.send(0, "Erro - palavra-passe errada.".getBytes());// ALTERAR ISTO APOS SABER DESMULTIPLEXER.
                            } else
                                c.send(0, "Erro - conta não existe.".getBytes());// ALTERAR ISTO APOS SABER DESMULTIPLEXER.
                        }
                        //
                        else if (frame.tag == 2){
                            // todo to be determined.
                        }

                    }
                } catch (IOException ignoring){ }
            };
            new Thread(processing).start();
            }
    }
}
