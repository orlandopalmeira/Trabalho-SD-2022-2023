import java.util.List;

/**
 * Esta é uma classe auxiliar que implementa pares.
 */
public class Pair implements Comparable<Pair>{
    public int x;
    public int y;

    public Pair(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){return x;}

    public int getY(){return y;}


    /** Calcula a distância de Manhatan.
     */
    public int distance(Pair p){
        int x0 = this.getX();
        int y0 = this.getY();
        int x1 = p.getX();
        int y1 = p.getY();
        int distance = Math.abs(x1-x0) + Math.abs(y1-y0);
        return distance;
    }

    /** Calcula a distância de Manhatan.
     */
    public int distance(Pair p0, Pair p1){
        int x0 = p0.getX();
        int y0 = p0.getY();
        int x1 = p1.getX();
        int y1 = p1.getY();
        int distance = Math.abs(x1-x0) + Math.abs(y1-y0);
        return distance;
    }

    public int compareTo(Pair p){
        if (this.x == p.x){
            return Integer.compare(this.y, p.y);
        }
        return Integer.compare(this.x, p.x);
    }

    public static String toStringPairs(List<Pair> pairs){
        StringBuilder ret = new StringBuilder();
        for(Pair p: pairs){
            ret.append(p).append("\n");
        }
        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair that = (Pair) o;
        return that.x == this.x && that.y == this.y;
    }

    @Override
    public int hashCode() {
        Integer xo = this.x,
                yo = this.y;
        return xo.hashCode() + yo.hashCode();
    }

    public String toString(){
        return "(" +
                this.x + "," +
                this.y +
                ")";
    }
}
