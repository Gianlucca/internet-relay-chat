import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject {
    protected Client() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int BUFFER_SIZE = 1024;
    public static boolean connected = false;
    public static User user;

    public static void main(String args[]) throws Exception{
        ChatServerInterface chatServerInterface = (ChatServerInterface) Naming.lookup("//192.168.0.10/Server");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
       
        do {
        	
            try {
              
                if (args.length == 0 || args.length >= 2) {
                	
                	Registry registry = LocateRegistry.createRegistry(args.length);
                	System.out.println("Iniciando o client...");
            		registry.rebind("ChatClient", new Client());
            		
        		}
                System.out.println(Messages.INSERT_USERNAME);
                String sentence = in.readLine().trim();
                
                if(!sentence.contains("*") && !sentence.contains(" ") && sentence.length() > 0) {
                	
                    user = new User(sentence);
                    Client.connected = true;
                    chatServerInterface.register(user.getId());

                }
                else  System.err.println(Messages.SET_NICKNAME_ERROR);
//                Thread.sleep(1000);
            } catch (Exception e) { }
        } while (!Client.connected);
        while (true) {
            try {
                String newSentence = in.readLine().trim();
                if(newSentence.getBytes().length <= Client.BUFFER_SIZE ){
                    if(newSentence.startsWith("/")){
                        if(newSentence.toLowerCase().startsWith("/nick")) chatServerInterface.nick(user, newSentence);
                        else if (newSentence.toLowerCase().startsWith("/create")) chatServerInterface.create(user, newSentence);
                                else if(newSentence.toLowerCase().startsWith("/join")) chatServerInterface.join(user, newSentence);
                                else if (newSentence.toLowerCase().startsWith("/kick")){
                                    chatServerInterface.kick(user, newSentence);
                                    String[] command = newSentence.split(" ");

//                            if(command.length == 2){
//                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1];
//                                System.out.println(chatServerInterface.message(user, newSentence));
//                            }

                        }
                        else if(newSentence.toLowerCase().startsWith("/list")){
                            chatServerInterface.list(user);
                        }
                        else if (newSentence.toLowerCase().startsWith("/part")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0,4);
                            chatServerInterface.message(user, newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/quit")) {
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 4);
                            chatServerInterface.message(user, newSentence);
                            System.exit(0);
                        }
                        else if(newSentence.toLowerCase().startsWith("/names")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 5);
                            chatServerInterface.message(user, newSentence);
                        }else if(newSentence.toLowerCase().startsWith("/remove")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 6);
                            chatServerInterface.message(user, newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/msg")){
                            String[] command = newSentence.split(" ", 3);
                            if(command.length == 3){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1] + " " + command[2];
                                chatServerInterface.message(user, newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
                        else{
                            System.out.println(Messages.INVALID_COMMAND);
                        }
                    }else{
                        chatServerInterface.message(user, newSentence);
                    }
                }else{
                    System.err.println(Messages.MESSAGE_TOO_LONG);
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                System.err.println(Messages.NO_PARAMETER);
            }catch(Exception e) {
                System.err.println(Messages.ERROR);
            }
        }

    }

	private static String part(Object object) {
		// TODO Auto-generated method stub
		return null;
	}
}
 