/**
 * Esta é uma classe auxiliar que implementa pares.
 */
public class Pair<V1,V2> {
    private V1 fst;
    private V2 snd;

    public V1 fst(){return fst;}

    public V2 snd(){return snd;}
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(fst.toString()); sb.append(",");
        sb.append(snd.toString());
        sb.append(")");
        return sb.toString();
    }
}
