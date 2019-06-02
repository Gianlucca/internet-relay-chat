import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Channel implements Runnable{

    private User admin;
    private String name;
    private Set<User> users;

    public Channel (User admin, String name){
        this.admin = admin;
        this.name = name;
        this.users = new HashSet<>();
    }

    public void run(){
        try {
            DatagramSocket serverSocket = new DatagramSocket(9876);

            byte[] receiveData = new byte[1024];
            while (true) {
                // declara o pacote a ser recebido
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // recebe o pacote do cliente
                serverSocket.receive(receivePacket);

                // pega os dados, o endereço IP e a porta do cliente
                // para poder mandar a msg de volta
                String sentence = new String(receivePacket.getData());
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                System.out.println("Mensagem recebida: " + sentence);
            }
        }
        catch(Exception e){

        }
    }

}
