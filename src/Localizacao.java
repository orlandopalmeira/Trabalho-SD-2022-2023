import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Localizacao {
    private final int x;
    private final int y;
    private ReentrantLock lock;
    private int ntrotinetes;
    private Condition cond;

    public Localizacao(int x, int y) {
        this.x = x;
        this.y = y;
        this.lock = new ReentrantLock();
        this.ntrotinetes = 0;
        this.cond = lock.newCondition();
    }

    public Localizacao(int x, int y, int ntrotinetes) {
        this.x = x;
        this.y = y;
        this.lock = new ReentrantLock();
        this.ntrotinetes = ntrotinetes;
        this.cond = lock.newCondition();
    }

    public void retirar(){
        lock.lock();
        try {
            if (this.ntrotinetes > 0){
                this.ntrotinetes--;
            }
            if (this.ntrotinetes == 0) this.cond.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void somar(){
        lock.lock();
        try {
            this.ntrotinetes++;
        }finally {
            lock.unlock();
        }
    }

    public int nPessoas(){
        this.lock.lock();
        try {
            return this.ntrotinetes;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void lockLocal (){
        lock.lock();
    }

    public void unlockLocal (){
        lock.unlock();
    }

    public int getNtrotinetes() {
        this.lock.lock();
        try {
            return ntrotinetes;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void setNtrotinetes(int ntrotinetes) {
        this.lock.lock();
        try{
            this.ntrotinetes = ntrotinetes;
        }finally {
            this.lock.unlock();
        }

    }

    public Condition getCond() {
        return cond;
    }

    public void setCond(Condition cond) {
        this.cond = cond;
    }

    public Localizacao clone() {
        return new Localizacao(this.getX(),this.getY(), this.getNtrotinetes());
    }

    public int getX() {
        try{
            //lock.lock();
            return x;
        }
        finally {
            //lock.unlock();
        }
    }


    public int getY() {
        try{
            //lock.lock();
            return y;
        }
        finally {
            //lock.unlock();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localizacao that = (Localizacao) o;
        return that.x == this.x && that.y == this.y && that.ntrotinetes == this.ntrotinetes;
    }

    @Override
    public String toString() {
        //return String.format("%d", ntrotinetes);
        return "Localizacao{" +
                "x=" + x +
                ", y=" + y +
                ", ntrotinetes=" + ntrotinetes +
                '}';
    }
}
