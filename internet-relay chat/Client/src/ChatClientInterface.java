import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClientInterface extends Remote {
    public String message(String message) throws RemoteException;
    public int kick() throws RemoteException;
}
