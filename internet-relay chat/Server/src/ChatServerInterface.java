import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServerInterface extends Remote {
    public int register(String ipaddress, String nickname) throws RemoteException;
    public void nick(User user, String nickname) throws RemoteException;
    public String[] list(User user) throws RemoteException;
    public int create(User user) throws RemoteException;
    public void remove(User user) throws RemoteException;
    public int join(User user, String channel) throws RemoteException;
    public void part(User user) throws RemoteException;
    public String[] names(User user) throws RemoteException;
    public int kick(User user, String nickname) throws RemoteException;
    public void msg(User user, String message) throws RemoteException;
    public int message(User user, String message) throws RemoteException;
    public int quit(User user) throws RemoteException;
}
