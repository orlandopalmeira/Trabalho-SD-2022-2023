import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe usada para a transmissão de pedidos de reserva e estacionamento.
 */
public class CodigoReserva implements Serializavel{

    /** -1 no caso de codigo de insucesso. Numero positivo para diferentes codigos possiveis atribuíveis. */
    private int codigo;
    /** Pair com a localizacao associada de origem ou destino conforme o contexto em que é utilizado. (Se código tiver o valor -1, este atributo é ignorável. */
    private Pair localizacao;

    public CodigoReserva (){
        this.codigo = 0;
        this.localizacao = new Pair();
    }

    /**
     * Se código de reserva for -1, significa insucesso na reserva, estando a null o Pair da localizacao.
     * @param codigo Inteiro que representa o código associado.
     */
    public CodigoReserva(int codigo) {
        this.codigo = codigo;
        this.localizacao = null;
    }

    public CodigoReserva(int codigo, Pair localizacao) {
        this.codigo = codigo;
        this.localizacao = localizacao;
    }

    public boolean isSuccess(){
        return codigo != -1;
    }

    public int getCodigo() {
        return codigo;
    }

    public Pair getLocalizacao() {
        return localizacao;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(codigo);
        if(codigo != -1){
            localizacao.serialize(out);
        }
    }

    @Override
    public Serializavel deserialize(DataInputStream in) throws IOException {
        int codigo = in.readInt();
        Pair localizacao = new Pair();
        if (codigo != -1){
            localizacao = (Pair) localizacao.deserialize(in);
            return new CodigoReserva(codigo, localizacao);
        }
        return new CodigoReserva(codigo);
    }
}
