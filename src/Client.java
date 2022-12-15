import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;


public class Client {

    /**
     * Verifica se o formato da string corresponde ao formato "%d %d" em que %d é um inteiro.
     * @param location String para ser analisada.
     * @return
     */
    public static boolean validLocation(String location){
        String[] tokens = location.split(" ");
        try{
            if (tokens.length != 2){
                throw new NumberFormatException();
            }
            Integer.parseInt(tokens[0]);
            Integer.parseInt(tokens[1]);
        } catch (NumberFormatException exc){
            return false;
        }
        return true;
    }

    public static Pair parsePair (String localizacao) {
        String[] tokens = localizacao.split(" ");
        int x = Integer.parseInt(tokens[0]);
        int y = Integer.parseInt(tokens[1]);
        return new Pair(x,y);
    }

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer m = new Demultiplexer(new Connection(s));
        //m.start();

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        // TODO não implementado em todos os Prints. (preciso assegurar momentos em que notificaçoes podem ser apresentados).
        ReentrantLock printLock = new ReentrantLock(); // lock para prints de notificações não atropelarem o resto das funcionalidades.

        String username = null;
        username = "re"; //  TODO remove in final version.
        /*

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
                // User registration attempt. tag = (0)
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
                // User log-in attempt.  tag = (1)
                m.send(1, data.getBytes());
                String response = new String(m.receive(1));
                if(!response.startsWith("Erro")) {
                    username = uName;
                    System.out.printf("\nBem-vindo %s!%n", uName);
                }
                else
                    System.out.println("\n" + response + "\n");
            }
        }
         */

        // Inicio de thread que responde fica à escuta de eventuais notificações.
        Thread receivingNotifications = new Thread(()->{
            RecompensaList recompensas = null;
            while(true){
                try {
                    recompensas = (RecompensaList) m.receive(30); // tag(30) usada para notificações.
                } catch (Exception ignored) {
                    System.exit(0);
                }
                printLock.lock();
                try {
                    System.out.println("\n(Notificação) Surgiu a seguinte recompensa: " + recompensas);
                } finally {
                    printLock.unlock();
                }
            }
        });
        receivingNotifications.start();

        // COMEÇAR MENU DE INTERAÇOES COM O MAPA.
        boolean exit = false;
        while (!exit) {
            System.out.print("\n***TROTINETAS***\n\n"
                    + "O que pretende?\n"
                    + "1) Indicar trotinetes livres numa área.\n"
                    + "2) Indicar recompensas com origem numa área.\n"
                    + "3) Reservar trotinete.\n"
                    + "4) Estacionar trotinete.\n"
                    + "5) Ativar notificação.\n"
                    + "\n"
                    + "0) Sair.\n"
                    + "\n"
                    + "Insira a opção que pretende: ");
            String option = stdin.readLine();
            String location, response;
            Pair par;
            switch (option) {
                case "0" -> // Sair da aplicação.
                    //m.send(99, new byte[0]);
                        exit = true;
                case "1" -> { // "1) Probing de trotinetes livres -> tag(2)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(2, par);
                    PairList trotinetes = (PairList) m.receive(11);
                    if (trotinetes.size() == 0)
                        System.out.println("Não há trotinetes na área.");
                    else {
                        //System.out.println("\nLocalizações:");
                        System.out.print(trotinetes);
                    }
                }
                case "2" -> { // 2) Porbing de Recompensas -> tag(3)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(3, par);
                    RecompensaList recompensas = (RecompensaList) m.receive(3);
                    if (recompensas.size() == 0)
                        System.out.println("\nNão há recompensas na área.\n");
                    else {
                        //System.out.println("\nRecompensas:");
                        System.out.print(recompensas);
                    }
                }
                case "3" -> { // 3) Reservar trotinete -> tag(4)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(4, par);
                }
                // TODO receber CODIGO + PAIR
                //RecompensaList recompensas = (RecompensaList) m.receive(4);
                //response = new String(m.receive(4));
                    /*
                    if ("0".equals(response)){
                        System.out.println("Não foi possível a reserva.");
                    }
                    else {
                        System.out.println("Reservado.");
                    }
                     */

                case "4" -> { // 4) Estacionar trotinete -> tag(5)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    // TODO TRATAR DA LOGICA DE ENVIO DE CODIGO + PAIR
                    par = parsePair(location);
                    m.send(5, par);
                }
                    /*
                    response = new String(m.receive(5));
                    if (response.equals("0")){
                        System.out.println("Posição inválida, operação recusada pelo servidor.");
                    }
                    else{
                        System.out.println("Pedido enviado com sucesso.");
                    }
                     */
                case "5" -> { // 5) Ativar notificação -> tag(6)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(6, par);
                }
                // TODO receber MENSAGEM DE SUCESSO OU INSUCESSO DE NOTIFICAÇÃO
                    /*
                    response = new String(m.receive(6));
                    if (response.equals("0")){
                        System.out.println("Notificações sobre esta localização já estavam ativadas.");
                    }
                    else{
                        System.out.println("Pedido enviado com sucesso.");
                    }
                     */
            }
            if (!option.equals("0")){
                System.out.println("Prime Enter para continuar.");
                stdin.readLine();
            }
            }

        receivingNotifications.interrupt();
        receivingNotifications.join();
        s.close();
    }
}
