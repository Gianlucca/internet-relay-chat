import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServerInterface extends Remote {
    public int register(String nickname, String args) throws RemoteException;
    public String nick(int user, String nickname) throws RemoteException;
    public String list() throws RemoteException;
    public String create(int user, String command) throws RemoteException;
    public String remove(int user) throws RemoteException;
    public String join(int user, String channel) throws RemoteException;
    public String part(int user, String channel) throws RemoteException;
    public String names(int user) throws RemoteException;
    public String kick(int user, String nickname) throws RemoteException;
    public String msg(int user, String nickname, String message) throws RemoteException;
    public void message(int user, String message) throws RemoteException;
    public void quit(int user) throws RemoteException;
}
