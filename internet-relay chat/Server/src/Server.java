import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server extends Thread {
    public final static int PORT = 9876;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private HashSet<Channel> channels;
    private HashSet<String> existingClients;
    private ArrayList<User> clients;

    public Server() throws IOException {
        socket = new DatagramSocket(PORT);
        existingClients = new HashSet();
        clients = new ArrayList();
    }

    public void run() {
        System.out.println("Server is now running. ");
        byte[] receiveData = new byte[BUFFER];
        while(true) {
            try{
                Arrays.fill(receiveData, (byte)0);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);

                String content = new String(packet.getData());
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();
                String id = IPAddress.toString() + ":" + port;

                if(!existingClients.contains(id)){
                    System.out.println(" conectado: " + id + " " + content);
                    existingClients.add( id );
                    clients.add(new User(content, IPAddress, port));
                } else{//usuario está no lobby

                    //tratar mensagens que podem ser usadas no lobby
                    System.out.println(id + " " + content);

                    // /nick <nickname>Solicita a alteração do apelido do usuário
                    if(content.startsWith("NICK ")){
                        for (User user : clients) {
                            if(user.getIPAddress().equals(IPAddress) && user.getPort() == port){
                                System.out.println(" Velho nick: " + user.getNickname());
                                user.setNickname(content.split(" ", 2)[1]);
                                System.out.println(" Novo nick: " + user.getNickname());
                            }
                        }
                    }



                    // /create <channel> criar canal novo com o usuario que criou como admin, nome do admin deve ter * na frente
                    // /list mostra canais criados no servidor
                    // /join <channel> solicita a participação em um canal
                    // /quit encerra conexao do usuario

                }
            }catch(Exception e) {
                System.err.println(e);
            }
        }
    }

    public static void main(String args[]) throws Exception{
        Server s = new Server();
        s.start();
    }
}
