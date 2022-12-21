import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
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

    /**
     * Pede do standard input informação sobre um Pair no formato "x y", retornando o Pair processado.
     * @param stdin Buffered Reader do stdin.
     * @return Retorna o objeto Pair obtido pelo standard input.
     * @throws IOException
     */
    public static Pair inputPair (BufferedReader stdin) throws IOException {
        String location;
        while (true) {
            System.out.print("Insira a localização no formato \"x y\": ");
            location = stdin.readLine();
            if (validLocation(location)) break;
            System.out.println("Input inválido.");
        }
        return parsePair(location);
    }

    public static void main(String[] args) throws Exception {
        Socket s = null;
        try{
            s = new Socket("localhost", 12345);
        } catch (ConnectException exc){
            System.out.println("Servidor não está ativo!");
            System.out.println("A terminar execução...");
            System.exit(1);
        }
        Demultiplexer m = new Demultiplexer(new Connection(s));

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        // TODO não implementado em todos os Prints. (preciso assegurar momentos em que notificaçoes podem ser apresentados).
        ReentrantLock printLock = new ReentrantLock(); // lock para prints de notificações não atropelarem o resto das funcionalidades.

        String username = null;
        boolean hasReservedTrotinete = false;
        ArrayList<Pair> notificacoesAtivas = new ArrayList<>(); // guarda as localizações em que o cliente tem notificações ativas.

        while (username == null) {
            System.out.print("\n*** LOGIN ***\n"
                    + "\n"
                    + "Pretende:\n"
                    + "1) Registar nova conta.\n"
                    + "2) Iniciar sessão.\n"
                    + "\n"
                    + "Opção: ");
            String option = stdin.readLine();
            if (option.equals("1")) {
                System.out.print("\n***REGISTAR NOVA CONTA***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                AccountInfo acc = new AccountInfo(uName, password);
                m.send(0, acc); // User registration attempt. tag = (0)
                Mensagem response = (Mensagem) m.receive(0);
                // Recebe 1 como sinal de sucesso.
                if(response.equals(1)) {
                    username = uName;
                    System.out.printf("\nRegistado com sucesso %s!%n", uName);
                }
                // Recebe 0 como sinal de insucesso (Username ja registado).
                else{
                    System.out.printf("\nErro - username \"%s\" já pertence a uma conta.%n", uName);
                }
            }
            else if(option.equals("2")) {
                System.out.print("\n***INICIAR SESSÃO***\n"
                        + "\n"
                        + "Introduza o seu username: ");
                String uName = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                AccountInfo acc = new AccountInfo(uName, password);
                // User log-in attempt.  tag = (1)
                m.send(1, acc);
                Mensagem response = (Mensagem) m.receive(0);
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
                    System.out.println("Conexão perdida com o servidor.");
                    System.exit(1);
                }
                printLock.lock();
                try {
                    System.out.println("\n(Notificação) Surgiu a seguinte recompensa:\n" + recompensas);
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
                    + (notificacoesAtivas.size() > 0 ? "6) Desativar notificação.\n" : "")
                    + "\n"
                    + "0) Sair.\n"
                    + "\n"
                    + "Opção: ");
            String option = stdin.readLine();
            String location;
            int code;
            Pair par;
            switch (option) {
                case "0" -> { // Sair da aplicação.
                    exit = true;
                }
                case "1" -> { // "1) Probing de trotinetes livres -> tag(2)
                    par = inputPair(stdin);
                    m.send(2, par);
                    PairList trotinetes = (PairList) m.receive(0);
                    if (trotinetes.size() == 0)
                        System.out.println("Não há trotinetes na área.");
                    else {
                        System.out.println("\nLocalizações:");
                        System.out.print(trotinetes);
                    }
                }
                case "2" -> { // 2) Probing de Recompensas -> tag(3)
                    par = inputPair(stdin);
                    m.send(3, par);
                    RecompensaList recompensas = (RecompensaList) m.receive(0);
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
                    par = inputPair(stdin);
                    m.send(4, par);
                    CodigoReserva myCode = (CodigoReserva) m.receive(0);

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
                    par = inputPair(stdin);
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
                    CodigoReserva cr = new CodigoReserva(code, par);
                    m.send(5, cr);
                    InfoViagem infoViagem = (InfoViagem) m.receive(0);
                    System.out.println(infoViagem);
                    if (infoViagem.isSuccessful()){
                        hasReservedTrotinete = false;
                    }
                }
                case "5" -> { // 5) Ativar notificação -> tag(6)
                    par = inputPair(stdin);
                    m.send(6, par);
                    Mensagem response = (Mensagem) m.receive(0);
                    if (response.equals(0)){
                        System.out.println("Ativação de notificação inválida.");
                    }
                    else{
                        System.out.println("Notificação do local ativada com sucesso.");
                        notificacoesAtivas.add(par);
                    }

                }
                case "6" -> { // 6) Desativar notificação -> tag(7)
                    System.out.println("Tem as seguintes posições com notificações ativas:\n" + notificacoesAtivas);
                    par = inputPair(stdin);
                    m.send(7, par);
                    Mensagem response = (Mensagem) m.receive(0);
                    if (response.equals(0)){
                        System.out.println("Notificação do local foram desativadas com sucesso.");
                        notificacoesAtivas.remove(par);
                    }
                    else{
                        System.out.println("Cancelamento de notificações inválido.");
                    }
                }
                default -> {
                    System.out.println("Opção inválida!");
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
