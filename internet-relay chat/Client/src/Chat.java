import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Chat extends UnicastRemoteObject implements ChatClientInterface {

    protected Chat() throws RemoteException {
    }

    @Override
    public String message(String message) throws RemoteException {
        return message;

    }

    @Override
    public int kick() throws RemoteException {
        return 0;
    }
}
