import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServerInterface extends Remote {
    public String register(User user) throws RemoteException;
    public String nick(User user, String nickname) throws RemoteException;
    public String[] list(User user) throws RemoteException;
    public String create(User user, String command) throws RemoteException;
    public String remove(User user) throws RemoteException;
    public String join(User user, String channel) throws RemoteException;
    public String part(User user) throws RemoteException;
    public String[] names(User user) throws RemoteException;
    public String kick(User user, String nickname) throws RemoteException;
    public void msg(User user, String message) throws RemoteException;
    public String message(User user, String message) throws RemoteException;
    public void quit(User user) throws RemoteException;
}
