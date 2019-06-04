import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel extends Thread{
    private final static int BUFFER = 1024;

    private User admin;
    private String name;
    private ArrayList<User> users;
    private DatagramSocket socket;

    public Channel(User admin, String name){
        this.admin = admin;
        this.name = "#"+name;
        this.users = new ArrayList<>();
        this.admin.setNickname("*" + this.admin.getNickname());
        users.add(this.admin);
        try {
            socket = new DatagramSocket();
            socket.send(new DatagramPacket((Messages.CHANNEL_CREATE_MESSAGE + name).getBytes(),
                    (Messages.CHANNEL_CREATE_MESSAGE + name).getBytes().length,
                    admin.getIPAddress(),
                    admin.getPort())
            );
        }catch(Exception e){
            System.out.println(Messages.CHANNEL_SOCKET_ERROR);
        }
    }

    public void run(){
        byte[] receiveData = new byte[BUFFER];
        try{
            while(true) {
                Arrays.fill(receiveData, (byte)0);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);

                String content = new String(packet.getData()).trim();
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();

                echoMessage(content);

                System.out.println("Mensagem recebida: " + content);
            }
        }catch(Exception e){
            System.err.println(e);
        }
    }

    public int getUsersOnline(){
        return users.size();
    }

    private void echoMessage(String message) throws Exception{
        byte[] data = ("<"+message+">").getBytes();
        for (User user : users) {
            DatagramPacket echoPacket = new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort());
            socket.send(echoPacket);
        }
    }
}
