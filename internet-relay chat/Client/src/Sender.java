import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender implements Runnable{
    private DatagramSocket socket;
    private String hostname;

    Sender(DatagramSocket socket, String hostname) {
        this.socket = socket;
        this.hostname = hostname;
    }

    private void sendMessage(String message) throws Exception {
        byte buf[] = message.getBytes();
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Client.PORT);
        socket.send(packet);
    }

    public void run() {
        do {
            try {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                System.out.println(Messages.INSERT_USERNAME);
                String sentence = inFromUser.readLine();
                sendMessage(sentence);
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        } while (!Client.connected);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean alive = true;
        while (alive) {
            try {
                String newSentence = in.readLine().trim();
                if(newSentence.getBytes().length <= Client.BUFFER_SIZE ){
                    if(newSentence.startsWith("/")){
                        if(newSentence.toLowerCase().startsWith("/nick") || newSentence.toLowerCase().startsWith("/create") || newSentence.toLowerCase().startsWith("/join")){
                            String[] command = newSentence.split(" ");
                            if(command.length <= 2){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1];
                                sendMessage(newSentence);
                            }else throw new ArrayIndexOutOfBoundsException();
                        }else if(newSentence.toLowerCase().startsWith("/list")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0,4);
                            sendMessage(newSentence);
                        }else if(newSentence.toLowerCase().startsWith("/quit")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0,4);
                            sendMessage(newSentence);
                            alive = false;
                        }//else if( newSentence.toLowerCase().startsWith("/remove"){
                         //}//
                            //
                            // || newSentence.toLowerCase().startsWith("/part")
                            // || newSentence.toLowerCase().startsWith("/names")
                            // || newSentence.toLowerCase().startsWith("/kick")
                            // || newSentence.toLowerCase().startsWith("/msg")
                            // || newSentence.toLowerCase().startsWith("/message")
                            // || newSentence.toLowerCase().startsWith("/quit")
                        //){

                        else{
                            System.out.println(Messages.INVALID_COMMAND);
                        }
                    }else{
                        sendMessage(newSentence);
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
