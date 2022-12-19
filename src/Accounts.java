import java.io.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that stores information about accounts. // FIXME - remove serializable methods and interface implementation.
 */
public class Accounts implements Serializable {

    class PassAndIslogged{
        String password;
        boolean isLogged;

        public PassAndIslogged(String password) {
            this.password = password;
            this.isLogged = true; // porque sempre que se cria uma conta, o utilizador entra automaticamente na aplicação.
        }

        public PassAndIslogged(String password, boolean isLogged) {
            this.password = password;
            this.isLogged = isLogged;
        }
    }
    private ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    private final HashMap<String, PassAndIslogged> contas;

    public Accounts() {
        this.contas = new HashMap<>();
    }

    /**
     * Verifies if a user with that username is logged in the system.
     * @param username Name of the user.
     * @return True if he´s logged, False otherwise.
     */
    public boolean isLogged(String username){
        l.readLock().lock();
        try{
            return contas.get(username).isLogged;
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Logs out a user when he disconnects from the server.
     * @param username Name of the user.
     */
    public void logOutUser(String username){
        l.writeLock().lock();
        try{
            contas.get(username).isLogged = false;
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Fetches a user's password.
     * @param username the user's username.
     * @return the user's password, or <i>null</i> if the user is not registered in the system.
     */
    public String getPassword(String username) {
        l.readLock().lock();
        try{
            if (accountExists(username)){
                return contas.get(username).password;
            }
            return null;
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
            if (accountExists(username)) return false;
            contas.put(username, new PassAndIslogged(password));
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
     * Serializing methods não são usados.
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
