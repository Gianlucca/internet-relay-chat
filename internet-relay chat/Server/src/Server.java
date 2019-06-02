import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server extends Thread {
    private final static int PORT = 9876;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private HashSet<Channel> channels;
    private HashSet<String> existingClients;
    private ArrayList<User> clients;

    private Server() throws IOException {
        socket = new DatagramSocket(PORT);
        existingClients = new HashSet<String>();
        clients = new ArrayList<User>();
        channels = new HashSet<>();
    }

    public void run() {
        System.out.println("Server is now running. ");
        byte[] receiveData = new byte[BUFFER];
        while(true) {
            try{
                Arrays.fill(receiveData, (byte)0);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);

                String content = new String(packet.getData()).trim();
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();
                String id = IPAddress.toString() + ":" + port;

                if(!existingClients.contains(id)){
                    System.out.println(" conectado: " + id + " " + content);
                    existingClients.add( id );
                    clients.add(new User(content, IPAddress, port));
                } else{
                    //usuario está no lobby
                    User sender = getUserById(IPAddress.toString(), port);
                    if(sender != null){

                        // /nick <nickname>Solicita a alteração do apelido do usuário
                        if(content.startsWith("NICK ")){
                            System.out.println(" Velho nick: " + sender.getNickname());
                            sender.setNickname(content.split(" ", 2)[1].trim());
                            System.out.println(" Novo nick: " + sender.getNickname());
                        }
                        // /create <channel> criar canal novo com o usuario que criou como admin, nome do admin deve ter * na frente
                        else if(content.startsWith("CREATE ")){

                        }
                        // /list mostra canais criados no servidor
                        else if(content.startsWith("LIST ")){

                        }
                        // /join <channel> solicita a participação em um canal
                        else if(content.startsWith("JOIN ")){

                        }
                        // /quit encerra conexao do usuario
                        else if(content.startsWith("QUIT ")){

                        }
                        else{
                            String senderName = sender.getNickname();
                            String msg = senderName.trim() + ": " + content.trim();

                            System.out.println(msg);
                            byte data[] = (msg).getBytes();
                            for (User user : clients) {
                                DatagramPacket echoPacket = new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort());
                                socket.send(echoPacket);
                            }
                        }
                    }
                }
            }catch(Exception e) {
               System.err.println(e);
            }
        }
    }


    private User getUserById(String ip, int port){
        for (User user : clients) {
            if(user.getIPAddress().toString().equals(ip) && user.getPort() == port){
                return user;
            }
        }
        return null;
    }

    public static void main(String args[]) throws Exception{
        Server s = new Server();
        s.start();
    }
}
