import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

class Client {
    public static int BUFFER_SIZE = 1024;
    public static boolean connected = false;

    public static void main(String args[]) throws Exception{
        ChatClientInterface chatClientInterface = (ChatClientInterface) Naming.lookup("//local/Chat");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            try {
                System.out.println(Messages.INSERT_USERNAME);
                String sentence = in.readLine().trim();
                if(!sentence.contains("*") && !sentence.contains(" ") && sentence.length() > 0)
                    chatClientInterface.message(sentence);
                else
                    System.err.println(Messages.SET_NICKNAME_ERROR);
//                Thread.sleep(1000);
            } catch (Exception e) { }
        } while (!Client.connected);
        while (true) {
            try {
                String newSentence = in.readLine().trim();
                if(newSentence.getBytes().length <= Client.BUFFER_SIZE ){
                    if(newSentence.startsWith("/")){
                        if(newSentence.toLowerCase().startsWith("/nick")
                                || newSentence.toLowerCase().startsWith("/create")
                                || newSentence.toLowerCase().startsWith("/join")
                                || newSentence.toLowerCase().startsWith("/kick")){
                            String[] command = newSentence.split(" ");
                            if(command.length == 2){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1];
                                chatClientInterface.message(newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
                        else if(newSentence.toLowerCase().startsWith("/list")
                                || newSentence.toLowerCase().startsWith("/help")
                                || newSentence.toLowerCase().startsWith("/part")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0,4);
                            chatClientInterface.message(newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/quit")) {
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 4);
                            chatClientInterface.message(newSentence);
                            System.exit(0);
                        }
                        else if(newSentence.toLowerCase().startsWith("/names")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 5);
                            chatClientInterface.message(newSentence);
                        }else if(newSentence.toLowerCase().startsWith("/remove")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 6);
                            chatClientInterface.message(newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/msg")){
                            String[] command = newSentence.split(" ", 3);
                            if(command.length == 3){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1] + " " + command[2];
                                chatClientInterface.message(newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
                        else{
                            System.out.println(Messages.INVALID_COMMAND);
                        }
                    }else{
                        chatClientInterface.message(newSentence);
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
 