import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

class Client {
    public static int BUFFER_SIZE = 1024;
    public static boolean connected = false;
    public static int userId;

    public static void main(String args[]) throws Exception{
        java.rmi.registry.LocateRegistry.createRegistry(1065);
        Naming.rebind("Client", new Chat());

        ChatServerInterface chatServerInterface = (ChatServerInterface) Naming.lookup("//localhost:1099/Server");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        do {
            try {
                System.out.println(Messages.INSERT_USERNAME);
                String sentence = in.readLine().trim();
                if(!sentence.contains("*") && !sentence.contains(" ") && sentence.length() > 0) {
                    Client.connected = true;
                    userId = chatServerInterface.register(sentence);
                }
                else  System.err.println(Messages.SET_NICKNAME_ERROR);

            } catch (Exception e) { }
        } while (!Client.connected);
        while (true) {
            try {
                System.out.println("Digite o comando");
                String newSentence = in.readLine().trim();
                if(newSentence.getBytes().length <= Client.BUFFER_SIZE ){
                    if(newSentence.startsWith("/")){
                        if(newSentence.toLowerCase().startsWith("/nick")) System.out.println(chatServerInterface.nick(userId, newSentence));

                        else if (newSentence.toLowerCase().startsWith("/create")) {
                            String[] channel = newSentence.split(" ");
                            System.out.println(chatServerInterface.create(userId, channel[1]));
                        }
                        else if(newSentence.toLowerCase().startsWith("/join")){
                            String[] channel = newSentence.split(" ");
                            System.out.println(chatServerInterface.join(userId, channel[1]));
                        }

                        else if (newSentence.toLowerCase().startsWith("/kick")){
                            String[] command = newSentence.split(" ");
                            System.out.println(chatServerInterface.kick(userId, command[1]));

//                            if(command.length == 2){
//                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1];
//                                System.out.println(chatServerInterface.message(userId, newSentence));
//                            }

                        }
                        else if(newSentence.toLowerCase().startsWith("/list")){
                            System.out.println(chatServerInterface.list());
                        }
                        else if (newSentence.toLowerCase().startsWith("/part")){
                            String[] channel = newSentence.split(" ");
                            System.out.println(chatServerInterface.part(userId, channel[1]));
                        }
                        else if(newSentence.toLowerCase().startsWith("/quit")) {
                            chatServerInterface.quit(userId);
                            System.exit(0);
                        }
                        else if(newSentence.toLowerCase().startsWith("/names")){
                            System.out.println(chatServerInterface.names(userId));
                        }else if(newSentence.toLowerCase().startsWith("/remove")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 6);
                            chatServerInterface.message(userId, newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/msg")){
                            String[] command = newSentence.split(" ", 3);
                            if(command.length == 3){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1] + " " + command[2];
                                chatServerInterface.message(userId, newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
                        else{
                            System.out.println(Messages.INVALID_COMMAND);
                        }
                    }else{
                        chatServerInterface.message(userId, newSentence);
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
}
 