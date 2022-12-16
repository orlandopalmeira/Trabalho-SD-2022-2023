import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
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
        //username = "re"; //  TODO remove in final version. (Simula login já efetuado)
        boolean hasReservedTrotinete = false;
        while (username == null) {
            System.out.print("*** LOGIN ***\n"
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
                AccountInfo acc = new AccountInfo(uName, password);
                //String data = String.format("%s %s", uName, password);
                m.send(0, acc); // User registration attempt. tag = (0)
                Mensagem response = (Mensagem) m.receive(13);
                // Recebe 1 como sinal de sucesso.
                if(response.equals(1)) {
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
                AccountInfo acc = new AccountInfo(uName, password);
                //String data = String.format("%s %s", uName, password);
                // User log-in attempt.  tag = (1)
                m.send(1, acc);
                Mensagem response = (Mensagem) m.receive(13);
                if(response.equals(1)) {
                    username = uName;
                    System.out.printf("\nBem-vindo %s!%n", uName);
                }
                else if (response.equals(0))
                    System.out.println("\nPassword incorreta.\n");
                else if (response.equals(3))
                    System.out.println("\nConta já está a ser utilizada.\n");
                else
                    System.out.println("\nConta inexistente.\n");

            }
        }

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
                    + "Opção: ");
            String option = stdin.readLine();
            String location;
            int code;
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
                        System.out.println("\nLocalizações:");
                        System.out.print(trotinetes);
                    }
                }
                case "2" -> { // 2) Probing de Recompensas -> tag(3)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(3, par);
                    RecompensaList recompensas = (RecompensaList) m.receive(12);
                    if (recompensas.size() == 0)
                        System.out.println("\nNão há recompensas na área.\n");
                    else {
                        System.out.println("\nRecompensas:");
                        System.out.print(recompensas);
                    }
                }
                case "3" -> { // 3) Reservar trotinete -> tag(4)
                    if (hasReservedTrotinete){ // Restrição de reserva de apenas 1 trotinete.
                        System.out.println("Já tem uma trotinete reservada!\n");
                        System.out.println("Prime Enter para continuar.");
                        stdin.readLine();
                        continue;
                    }
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(4, par);
                    CodigoReserva myCode = (CodigoReserva) m.receive(14);

                    if (myCode.isSuccess()){
                        System.out.println("Reservada trotinete na posição " + myCode.getLocalizacao() + ", com o código " + myCode.getCodigo() + ".");
                        hasReservedTrotinete = true;
                    }
                    else {
                        System.out.println("Não foi possível a reserva.");
                    }
                }

                case "4" -> { // 4) Estacionar trotinete -> tag(5)
                    if (!hasReservedTrotinete){ // Restrição de reserva de apenas 1 trotinete.
                        System.out.println("Ainda não tem uma trotinete reservada!\n");
                        System.out.println("Prime Enter para continuar.");
                        stdin.readLine();
                        continue;
                    }
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    while (true){
                        System.out.print("Insira o código de reserva: ");
                        try{
                            code = Integer.parseInt(stdin.readLine());
                            if (code < 1){
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException exc){
                            System.out.println("Input inválido.");
                            continue;
                        }
                        break;
                    }
                    par = parsePair(location);
                    CodigoReserva cr = new CodigoReserva(code, par);
                    m.send(5, cr);
                    InfoViagem infoViagem = (InfoViagem) m.receive(15);
                    System.out.println(infoViagem);
                    if (infoViagem.isSuccessful()){
                        hasReservedTrotinete = false;
                    }
                }
                case "5" -> { // 5) Ativar notificação -> tag(6)
                    while (true) {
                        System.out.print("Insira a localização no formato \"x y\": ");
                        location = stdin.readLine();
                        if (validLocation(location)) break;
                        System.out.println("Input inválido.");
                    }
                    par = parsePair(location);
                    m.send(6, par);
                    Mensagem response = (Mensagem) m.receive(13);
                    if (response.equals(0)){
                        System.out.println("Notificações sobre esta localização já estavam ativadas.");
                    }
                    else{
                        System.out.println("Notificação do local ativada com sucesso.");
                    }

                }
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
