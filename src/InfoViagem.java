import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InfoViagem implements Serializavel{

    private boolean successful = true;
    private long duracao;
    private int distancia;
    private int custo;
    private Recompensa recompensa; // pode ser null.

    /**
     * Cria um objeto InfoEstacionamento sem informação que serve como código de insucesso.
     */
    public InfoViagem(){
        this.successful = false;
    }

    /**
     * Construtor que calcula automaticamente o preço, tendo em conta a duração e a distancia.
     * @param duracao
     * @param distancia
     */
    public InfoViagem(long duracao, int distancia) {
        this.duracao = duracao;
        this.distancia = distancia;
        this.custo = (int) duracao + distancia; // formula de calculo de custo provisória.
    }

    public InfoViagem(long duracao, int distancia, int custo, Recompensa recompensa) {
        this.duracao = duracao;
        this.distancia = distancia;
        this.custo = custo;
        this.recompensa = recompensa;
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
        sb.append("Informação da viagem\n");
        sb.append("Duração: ").append(duracao).append("\n");
        sb.append("Distância: ").append(distancia).append("\n");
        sb.append("Custo: ").append(custo).append("\n");
        if (recompensa != null){
            sb.append("Recompensa: ").append(recompensa).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(this.successful);
        if (this.successful){
            out.writeLong(duracao);
            out.writeInt(distancia);
            out.writeInt(custo);
            recompensa.serialize(out);
        }
    }

    @Override
    public Serializavel deserialize(DataInputStream in) throws IOException {
        boolean successful = in.readBoolean();
        if (successful) {
            long duracao = in.readLong();
            int distancia = in.readInt();
            int custo = in.readInt();
            Recompensa rec = new Recompensa();
            rec = rec.deserialize(in);
            return new InfoViagem(duracao, distancia, custo, rec);
        }
        else{
            return new InfoViagem();
        }
    }
}
