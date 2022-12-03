/**
 * Esta é uma classe auxiliar que implementa pares.
 */
public class Pair {
    private int x;
    private int y;

    public Pair(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int x(){return x;}

    public int y(){return y;}

    
    public String toString(){
        return "(" +
                this.x + "," +
                this.y +
                ")";
    }
}
