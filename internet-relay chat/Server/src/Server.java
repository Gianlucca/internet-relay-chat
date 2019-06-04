import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server extends Thread {
    private final static int PORT = 9876;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private HashSet<String> existingClients;
    private ArrayList<User> clients;
    private ArrayList<Channel> channels;

    private Server() throws IOException {
        socket = new DatagramSocket(PORT);
        existingClients = new HashSet<>();
        clients = new ArrayList<>();
        channels = new ArrayList<>();
    }

    public void run() {
        System.out.println(Messages.SERVER_RUNNING);
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
                    if(!content.contains(" ")){
                        echoMessage(content + Messages.LOGGED_IN);
                        existingClients.add(id);
                        clients.add(new User(content, IPAddress, port));
                        byte[] accepted = Messages.LOGGED_IN_PM.getBytes();
                        socket.send( new DatagramPacket(accepted, accepted.length, IPAddress, port));
                    } else{
                        byte[] denied = Messages.NAME_CONTAINS_SPACE.getBytes();
                        socket.send( new DatagramPacket(denied, denied.length, IPAddress, port));
                    }
                } else{
                    //usuario está no lobby
                    User sender = getUserById(IPAddress.toString(), port);
                    if(sender != null){
                        if(content.startsWith("NICK ")){
                            // /nick <nickname> - Solicita a alteração do apelido do usuário
                            String nick = content.split(" ", 2)[1].trim();
                            if(nick.contains(" ")){
                                byte[] denied = Messages.NAME_CONTAINS_SPACE.getBytes();
                                socket.send( new DatagramPacket(denied, denied.length, sender.getIPAddress(), sender.getPort()));
                            }else{
                                String oldNick = sender.getNickname();
                                sender.setNickname(nick);
                                System.out.println(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                                echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                            }
                        }else if(content.startsWith("CREATE ")){
                            // /create <channel> criar canal novo com o usuario que criou como admin, nome do admin deve ter * na frente
                            String channelName = content.split(" ", 2)[1].trim();
                            if(channelName.contains(" ")){
                                byte[] denied = Messages.CHANNEL_NAME_CONTAINS_SPACE.getBytes();
                                socket.send( new DatagramPacket(denied, denied.length, sender.getIPAddress(), sender.getPort()));
                            }else{
                                boolean channelExists = false;
                                for (Channel channel: channels) {
                                    if(channel.getName().equals(channelName)){
                                        channelExists = true;
                                        break;
                                    }
                                }
                                if(channelExists){
                                    byte[] denied = Messages.CHANNEL_ALREADY_EXISTS.getBytes();
                                    socket.send( new DatagramPacket(denied, denied.length, sender.getIPAddress(), sender.getPort()));
                                }
                                else{
                                    sender.setChannel(new Channel(sender, channelName));
                                    channels.add(sender.getChannel());
                                    clients.remove(sender);
                                    sender.getChannel().start();
                                }
                            }
                        }
                        else if(content.startsWith("LIST ")){
                            // /list mostra canais criados no servidor
                            StringBuilder channelList = new StringBuilder();
                            channelList.append(Messages.AVAILABLE_CHANNELS);
                            if(!channels.isEmpty()){
                                for (Channel channel : channels){
                                    String channelName = channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                                    channelList.append(channelName);
                                }
                                byte[] data = channelList.toString().getBytes();
                                socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                            }else{
                                byte[] data = Messages.NO_AVAILABLE_CHANNELS.getBytes();
                                socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                            }
                        }
                        else if(content.startsWith("JOIN ")){
                            // /join <channel> solicita a participação em um canal

                        }
                        else if(content.startsWith("QUIT ")){
                            // /quit encerra conexao do usuario

                        }
                        else{
                            String senderName = sender.getNickname();
                            String msg = senderName.trim() + ": " + content.trim();

                            System.out.println(msg);
                            echoMessage(msg);
                        }
                    }
                }
            }catch(Exception e) {
               System.err.println(Messages.INVALID_COMMAND);
            }
        }
    }

    private void echoMessage(String message) throws Exception{
        byte[] data = ("<"+message+">").getBytes();
        for (User user : clients) {
            DatagramPacket echoPacket = new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort());
            socket.send(echoPacket);
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

    public static void main(String[] args) throws Exception{
        Server s = new Server();
        s.start();
    }
}
