import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;
import java.time.Duration;


// Classe que gere os códigos de reserva de trotinetes reservadas.
public class TrotinetesReservadas {

    private class InfoDeReserva{
        public Instant instanteDaReserva;
        public Pair origem;

        public InfoDeReserva(Pair origem) {
            this.instanteDaReserva = Instant.now();
            this.origem = origem;
        }
    }

    private HashMap<Integer, InfoDeReserva> trotinetesReservadas;
    private final ReentrantLock lockTrotinetesReservadas;

    public TrotinetesReservadas() {
        this.trotinetesReservadas = new HashMap<>();
        this.lockTrotinetesReservadas = new ReentrantLock();
    }

    public void add(CodigoReserva cr){
        this.lockTrotinetesReservadas.lock();
        try{
            this.trotinetesReservadas.put(cr.getCodigo(), new InfoDeReserva(cr.getLocalizacao()));
        } finally {
            this.lockTrotinetesReservadas.unlock();
        }
    }

    // TODO - VAI RETORNAR A NOVA CLASSE QUE CONTEM INFORMAÇÃO DE CUSTO E DE RECOMPENSAS.
    // TODO - OU SIMPLESMENTE A DURACAO DA RESERVA PARA DEPOIS SER CRIADA A NOVA CLASSE.
    public void remove (int code){
        Instant instanteDoEstacionamento = Instant.now();
        this.lockTrotinetesReservadas.lock();
        try{
            InfoDeReserva info = this.trotinetesReservadas.get(code);
            long duracao = Duration.between(instanteDoEstacionamento, info.instanteDaReserva).toSeconds();

        } finally {
            this.lockTrotinetesReservadas.unlock();
        }
        //return
    }
}
