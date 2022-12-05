import java.io.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that stores information about accounts.
 */
public class Accounts implements Serializable {
    private final HashMap<String, String> contas;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();

    public Accounts() {
        this.contas = new HashMap<>();
    }

    /**
     * Fetches a user's password.
     * @param username the user's username.
     * @return the user's password, or <i>null</i> if the user is not registered in the system.
     */
    public String getPassword(String username) {
        l.readLock().lock();
        try{
            return contas.get(username);
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Adds a new account to the system with the specified credentials.
     * @param username the user's username.
     * @param password the user's password.
     * @return <i>false</i> if account already exists, <i>true</i> in case of sucessful add.
     */
    public boolean addAccount(String username, String password) {
        l.writeLock().lock();
        try {
            if (!accountExists(username)) return false;
            contas.put(username, password);
            return true;
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Checks if an account is registered in the system.
     * @param username the username to check.
     * @return <i>true</i> if an account with the specified username exists. <i>false</i> otherwise.
     */
    private boolean accountExists(String username) {
        return contas.containsKey(username);
    }

    /**
     * Serializing methods that may not be used.
     */
    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public static Accounts deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Accounts accounts = (Accounts) ois.readObject();
        ois.close();
        fis.close();
        return accounts;
    }
}