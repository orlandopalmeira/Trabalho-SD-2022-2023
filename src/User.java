import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    private String username;
    private String password;
    private int x;
    private int y;
    ReentrantLock l = new ReentrantLock();

    public User(String username, String password, int x, int y, int flagEspecial) {
        this.username = username;
        this.password = password;
        this.x = x;
        this.y = y;
    }

    public String getUsername() {
        try{
            l.lock();
            return username;
        }
        finally {
            l.unlock();
        }
    }

    public void setUsername(String username) {
        try {
            l.lock();
            this.username = username;
        }
        finally {
            l.unlock();
        }
    }

    public String getPassword() {
        try{
            l.lock();
            return password;
        }
        finally {
            l.unlock();
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getX() {
        try{
            l.lock();
            return x;
        }
        finally {
            l.unlock();
        }
    }

    public int getY() {
        try{
            l.lock();
            return y;
        }
        finally {
            l.unlock();
        }
    }

    public void setX(int x) {
        try{
            l.lock();
            this.x = x;
        }
        finally {
            l.unlock();
        }
    }

    public void setY(int y) {
        try{
            l.lock();
            this.y = y;
        }
        finally {
            l.unlock();
        }
    }


    public void atualizaLocalizacaoAnterior(int[] locs){
        x = locs[0];
        y = locs[1];
    }


    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(this.username).append(";");
        builder.append(this.password).append(";");
        builder.append(this.x).append(";");
        builder.append(this.y).append(";");
        return builder.toString();
    }
}
