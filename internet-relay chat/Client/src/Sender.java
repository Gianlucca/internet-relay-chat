import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender implements Runnable{
    private DatagramSocket socket;
    private String hostname;
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

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
                System.out.println(Messages.INSERT_USERNAME);
                String sentence = in.readLine().trim();
                if(!sentence.contains("*") && !sentence.contains(" ") && sentence.length() > 0)
                    sendMessage(sentence);
                else
                    System.err.println(Messages.SET_NICKNAME_ERROR);
                Thread.sleep(1000);
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
                                sendMessage(newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
                        else if(newSentence.toLowerCase().startsWith("/list")
                                || newSentence.toLowerCase().startsWith("/help")
                                || newSentence.toLowerCase().startsWith("/part")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0,4);
                            sendMessage(newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/quit")) {
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 4);
                            sendMessage(newSentence);
                            System.exit(0);
                        }
                        else if(newSentence.toLowerCase().startsWith("/names")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 5);
                            sendMessage(newSentence);
                        }else if(newSentence.toLowerCase().startsWith("/remove")){
                            newSentence = newSentence.replaceFirst("/", "").toUpperCase().substring(0, 6);
                            sendMessage(newSentence);
                        }
                        else if(newSentence.toLowerCase().startsWith("/msg")){
                            String[] command = newSentence.split(" ", 3);
                            if(command.length == 3){
                                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1] + " " + command[2];
                                sendMessage(newSentence);
                            }
                            else throw new ArrayIndexOutOfBoundsException();
                        }
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
