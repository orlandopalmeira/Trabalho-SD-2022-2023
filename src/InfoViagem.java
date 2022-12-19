import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InfoViagem implements Serializavel{

    private boolean successful = true;
    private Pair origem;
    private Pair destino;
    private int distancia;
    private long duracao;
    private int custo;
    private Recompensa recompensa = null; // pode ser null.

    /**
     * Cria um objeto InfoEstacionamento sem informação que serve como código de insucesso.
     */
    public InfoViagem(){
        this.successful = false;
    }

    /**
     * Construtor que calcula automaticamente o preço, tendo em conta a duração e a distancia.
     * @param origem Origem da viagem.
     * @param destino Destino da viagem.
     * @param duracao Duraçao em segundos da viagem.y
     */
    public InfoViagem(Pair origem, Pair destino, long duracao) {
        this.origem = origem;
        this.destino = destino;
        this.distancia = origem.distance(destino);
        this.duracao = duracao;
        this.custo = (int) duracao + distancia; // formula de calculo de custo provisória.
    }

    public InfoViagem(Pair origem, Pair destino, int distancia, long duracao, int custo, Recompensa recompensa) {
        this.origem = origem;
        this.destino = destino;
        this.distancia = distancia;
        this.duracao = duracao;
        this.custo = custo;
        this.recompensa = recompensa;
    }

    public Pair getOrigem() {
        return origem;
    }

    public Pair getDestino() {
        return destino;
    }

    public void setRecompensa(Recompensa recompensa) {
        this.recompensa = recompensa;
        this.custo -= recompensa.reward;
    }

    public boolean isSuccessful(){
        return this.successful;
    }

    @Override
    public String toString() {
        if (!this.isSuccessful()){
            return "Insucesso no estacionamento.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n***** Informação da viagem *****\n");
        sb.append("Duração: ").append(duracao).append("\n");
        sb.append("Distância: ").append(distancia).append("\n");
        sb.append("Custo: ").append(custo).append("\n");
        if (recompensa != null){
            sb.append("Recompensa: ").append(recompensa).append("\n");
        }
        sb.append("**********************************\n");
        return sb.toString();
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(this.successful);
        if (this.successful){
            origem.serialize(out);
            destino.serialize(out);
            out.writeInt(distancia);
            out.writeLong(duracao);
            out.writeInt(custo);
            if (recompensa != null){
                out.writeBoolean(true);
                recompensa.serialize(out);
            }
            else {
                out.writeBoolean(false);
            }
        }
    }

    @Override
    public Serializavel deserialize(DataInputStream in) throws IOException {
        boolean successful = in.readBoolean();
        if (successful) {
            Pair origem = new Pair(), destino = new Pair();
            origem = (Pair) origem.deserialize(in);
            destino = (Pair) destino.deserialize(in);
            int distancia = in.readInt();
            long duracao = in.readLong();
            int custo = in.readInt();
            boolean hasrecompensa = in.readBoolean();
            Recompensa rec = null;
            if (hasrecompensa) {
                rec = new Recompensa();
                rec = rec.deserialize(in);
            }
            return new InfoViagem(origem, destino, distancia, duracao, custo, rec);
        }
        else{
            return new InfoViagem();
        }
    }
}
