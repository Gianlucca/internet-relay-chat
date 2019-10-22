import java.rmi.RemoteException;

public class Chat implements ChatClientInterface {
    @Override
    public String message(String message) throws RemoteException {
        return message;

    }

    @Override
    public int kick() throws RemoteException {
        return 0;
    }
}
