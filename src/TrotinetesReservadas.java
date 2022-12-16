import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;
import java.time.Duration;


// Classe que gere os códigos de reserva de trotinetes reservadas.
public class TrotinetesReservadas {

    /**
     * Contem informação sobre: <br>
     * Instant - instante de reserva. <br>
     * Pair - posição origem da reserva. <br>
     * String - username que fez a reserva.
     */
    private class InfoDeReserva{
        public Instant instanteDaReserva;
        public Pair origem;
        public String username;

        public InfoDeReserva(Pair origem, String username) {
            this.instanteDaReserva = Instant.now();
            this.origem = origem;
            this.username = username;
        }
    }

    private final HashMap<Integer, InfoDeReserva> infosDeReserva;
    private final ReentrantLock lockTrotinetesReservadas;

    public TrotinetesReservadas() {
        this.infosDeReserva = new HashMap<>();
        this.lockTrotinetesReservadas = new ReentrantLock();
    }

    /**
     * Adiciona informação sobre o novo código de reserva, bem como o utilizador que fez a reserva.
     * @param cr Contém informação sobre o código de reserva e a localização.
     * @param username Utilizador que fez a reserva.
     */
    public void add(CodigoReserva cr, String username){
        this.lockTrotinetesReservadas.lock();
        try{
            this.infosDeReserva.put(cr.getCodigo(), new InfoDeReserva(cr.getLocalizacao(), username));
        } finally {
            this.lockTrotinetesReservadas.unlock();
        }
    }

    /**
     * Retorna a informação calculada da viagem do utilizador, tendo em conta o código da reserva de trotinete, o username do cliente e o destino onde a trotinete foi estacionada.
     * @param cr Objeto CodigoReserva que contem o código de reserva e a localização destino.
     * @param username Para verificar que o username que enviou este código, foi o que reservou a trotinete em questão.
     * @return Retorna um objeto InfoEstacionameto, que irá conter a informação da viagem (podendo não levar informação nenhuma caso o pedido de estacionamento seja inválido).
     */
    public InfoViagem getInfoViagem(CodigoReserva cr, String username){
        Instant instanteDoEstacionamento = Instant.now();
        int code = cr.getCodigo();
        Pair destino = cr.getLocalizacao();
        InfoViagem returnValue;
        InfoDeReserva info;
        this.lockTrotinetesReservadas.lock();
        try{
            info = this.infosDeReserva.get(code);
            if (info == null || !info.username.equals(username)){
                returnValue = new InfoViagem();
                return returnValue;
            }
            this.infosDeReserva.remove(code); // Para retirar a reserva da trotinete, uma vez que ja foi estacionada.
        } finally {
            this.lockTrotinetesReservadas.unlock();
        }
        long duracao = Duration.between(info.instanteDaReserva, instanteDoEstacionamento).toSeconds();
        Pair origem = info.origem;
        returnValue = new InfoViagem(origem, destino, duracao);
        return returnValue;
    }
}
