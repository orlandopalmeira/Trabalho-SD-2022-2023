import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;


public class Client {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer m = new Demultiplexer(new Connection(s));
        //m.start();

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        HashSet<Thread> alarms = new HashSet<>(); // threads que irão estar à escuta de notificações.

        String username = null;
        while (username == null) {
            System.out.print("***TROTINETAS***\n"
                    + "\n"
                    + "Pretende:\n"
                    + "1) Registar nova conta.\n"
                    + "2) Iniciar sessão.\n"
                    + "\n"
                    + "Opção: ");
            String option = stdin.readLine();
            if (option.equals("1")) {
                System.out.print("***REGISTAR NOVA CONTA***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                String data = String.format("%s %s", uName, password);
                // User registration attempt = 0
                m.send(0, data.getBytes());
                String response = new String(m.receive(0));
                // Recebe 1 como sinal de sucesso.
                if(response.startsWith("1")) {
                    username = uName;
                    System.out.printf("\nRegistado com sucesso %s!%n", uName);
                }
                // Recebe 0 como sinal de insucesso (Username ja registado).
                else{
                    System.out.printf("\nErro - username (%s) já pertence a uma conta.%n", uName);
                }
            }
            else if(option.equals("2")) {
                System.out.print("***INICIAR SESSÃO***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                String data = String.format("%s %s", uName, password);
                // User log-in attempt = 1
                m.send(1, data.getBytes());
                String response = new String(m.receive(1));
                if(!response.startsWith("Erro")) {
                    username = uName;
                }
                else
                    System.out.println("\n" + response + "\n");
            }
        }

        // COMEÇAR MENU DE INTERAÇOES COM O MAPA.

    }
}
