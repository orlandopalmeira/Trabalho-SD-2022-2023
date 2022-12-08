import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;


public class Client {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer m = new Demultiplexer(new Connection(s)); // TODO Demultiplexer não implementado, mas em principio esta será a estrutura.

        HashSet<Thread> alarms = new HashSet<>(); // threads que irão estar à escuta de notificações.

        m.start();

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        String username = null;
        while (username == null) {
            System.out.print("***TROTINETAS***\n"
                    + "\n"
                    + "Pretende:\n"
                    + "1) Iniciar sessão.\n"
                    + "2) Registar nova conta.\n"
                    + "\n"
                    + "Opção desejada: ");
            String option = stdin.readLine();
            if(option.equals("1")) {
                System.out.print("***INICIAR SESSÃO***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                String data = String.format("%s %s", uName, password);
                m.send(0, data.getBytes());
                String response = new String(m.receive(0));
                if(!response.startsWith("Erro")) {
                    username = uName;
                }
                System.out.println("\n" + response + "\n");
            }
            else if (option.equals("2")) {
                System.out.print("***REGISTAR NOVA CONTA***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                String data = String.format("%s %s", uName, password);
                m.send(1, data.getBytes());
                String response = new String(m.receive(1));
                if(!response.startsWith("Erro")) { // to be determined!!
                    username = uName;
                }
                System.out.println("\n" + response + "\n");
            }
        }

    }
}
