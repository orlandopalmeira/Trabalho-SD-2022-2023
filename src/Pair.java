/**
 * Esta é uma classe auxiliar que implementa pares.
 */
public class Pair {
    public int x;
    public int y;

    public Pair(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){return x;}

    public int getY(){return y;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair that = (Pair) o;
        return that.x == this.x && that.y == this.y;
    }

    public String toString(){
        return "(" +
                this.x + "," +
                this.y +
                ")";
    }
}
