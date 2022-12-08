import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Localizacao {
    public final int x;
    public final int y;
    public ReentrantReadWriteLock lock;
    public int ntrotinetes;
    public Condition cond;

    public Localizacao(int x, int y) {
        this.x = x;
        this.y = y;
        this.lock = new ReentrantReadWriteLock();
        this.ntrotinetes = 0;
        this.cond = lock.writeLock().newCondition();
    }

    public Localizacao(int x, int y, int ntrotinetes) {
        this.x = x;
        this.y = y;
        this.lock = new ReentrantReadWriteLock();
        this.ntrotinetes = ntrotinetes;
        this.cond = lock.writeLock().newCondition();
    }

    public void retirar(){
        lock.writeLock().lock();
        try {
            if (this.ntrotinetes > 0){
                this.ntrotinetes--;
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void somar(){
        lock.writeLock().lock();
        try {
            this.ntrotinetes++;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public int nPessoas(){
        lock.readLock().lock();
        try {
            return this.ntrotinetes;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void lockLocal (){
        lock.readLock().lock();
    }

    public void unlockLocal (){
        lock.readLock().unlock();
    }

    public int getNtrotinetes() {
        this.lock.readLock().lock();
        try {
            return ntrotinetes;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public void setNtrotinetes(int ntrotinetes) {
        this.lock.writeLock().lock();
        try{
            this.ntrotinetes = ntrotinetes;
        }finally {
            this.lock.writeLock().unlock();
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
        return x;
    }

    public int getY() {
        return y;
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
        return "Localizacao{" +
                "x=" + x +
                ", y=" + y +
                ", ntrotinetes=" + ntrotinetes +
                "}";
    }
}
