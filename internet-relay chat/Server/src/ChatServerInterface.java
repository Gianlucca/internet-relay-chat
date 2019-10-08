import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatServerInterface extends Remote {
    public int register(String ipaddress, String nickname) throws RemoteException;
    public int nick(int id, String nickname) throws RemoteException;
    public String[] list(int id) throws RemoteException;
    public int create(int id, String channel) throws RemoteException;
    public int remove(int id, String channel) throws RemoteException;
    public int join(int id, String channel) throws RemoteException;
    public int part(int id, String channel) throws RemoteException;
    public List<String> names(int id, String channel) throws RemoteException;
    public int kick(int id, String channel, String nickname) throws RemoteException;
    public int msg(int id, String channel, String nickname, String message) throws RemoteException;
    public int message(int id, String channel, String message) throws RemoteException;
    public int quit(int id) throws RemoteException;
}
