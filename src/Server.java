import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(12345);

        final Accounts accounts;
        /* If the server has been run before, attempts to fetch the accounts and location history from the previous run. */
        File f = new File("accounts.ser");
        if(!f.exists())
            accounts = new Accounts();
        else
            accounts = Accounts.deserialize("accounts.ser");

        final Mapa mapa = new Mapa(10);

        while(true) {
            Socket s = ss.accept();
            Connection c = new Connection(s);

            /* The runnable that executes the processing of a client connection. */
            Runnable processing = () -> {
                try(c){
                    boolean isLoggedIn = false;
                    while(true){
                        Frame frame = c.receive();
                        // se não pretende registar-se ou fazer login, então não é permitido acesso às funcionalidades.
                        // TODO talvez nem seja preciso esta verificação se o cliente n permitir avancar.
                        if (frame.tag != 0 && frame.tag != 1 && !isLoggedIn){
                            c.send(frame.tag, "Erro. Não está registrado.".getBytes());
                            continue;
                        }
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
                        //
                        else if (frame.tag == 2){
                            // todo to be determined.
                        }

                    }
                } catch (IOException exc){
                    System.out.println(exc);
                }
            };
            new Thread(processing).start();
            }
    }
}
