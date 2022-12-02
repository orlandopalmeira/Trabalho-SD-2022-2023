import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    private String username;
    private String password;
    private int x;
    private int y;
    private Map<LocalTime,Localizacao> historico;
    ReentrantLock l = new ReentrantLock();

    public User(String username, String password, int x, int y, int flagEspecial) {
        this.username = username;
        this.password = password;
        this.x = x;
        this.y = y;
        this.historico = new TreeMap<>();
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

    public Map<LocalTime, Localizacao> getHistorico() {
        try{
            l.lock();
            return historico;
        }
        finally {
            l.unlock();
        }
    }

    public void setHistorico(Map<LocalTime, Localizacao> historico) {
        try {
            l.lock();
            this.historico = historico;
        }
        finally {
            l.unlock();
        }
    }

    public void addHistorico(Localizacao local, LocalTime instante){
        try {
            l.lock();
            this.historico.put(instante, local.clone());
        }
        finally {
            l.unlock();
        }
    }


    /**
     * método que atualiza a localizacao e mete a anterior no historico
     * @return
     */

    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(this.username).append(";");
        builder.append(this.password).append(";");
        builder.append(this.x).append(";");
        builder.append(this.y).append(";");
        builder.append(this.historico).append(";");
        return builder.toString();
    }
}
