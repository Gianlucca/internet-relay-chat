import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server extends Thread {
    private final static int PORT = 9876;
    private final static int BUFFER = 1024;

    private static DatagramSocket socket;
    public static HashSet<String> existingClients;
    private static ArrayList<User> clients;
    public static ArrayList<Channel> channels;

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
                        System.out.println("<" +content+ ">" + Messages.LOGGED_IN);
                        echoMessage("<" +content+ ">" + Messages.LOGGED_IN);
                        existingClients.add(id);
                        clients.add(new User(content, IPAddress, port));
                        byte[] accepted = Messages.LOGGED_IN_PM.getBytes();
                        socket.send( new DatagramPacket(accepted, accepted.length, IPAddress, port));
                    }
                    else {
                        byte[] accepted = Messages.SET_NICKNAME_ERROR.getBytes();
                        socket.send( new DatagramPacket(accepted, accepted.length, IPAddress, port));
                    }
                } else{
                    //usuario está no lobby
                    User sender = getUserById(IPAddress.toString(), port);

                    if(content.startsWith("NICK ") || content.startsWith("CREATE ") || content.startsWith("JOIN ") ) {
                        String command = content.split(" ")[1].trim();
                        if (content.startsWith("NICK ")) {
                            // /nick <nickname> - Solicita a alteração do apelido do usuário
                            String oldNick = sender.getNickname();
                            sender.setNickname(command);
                            System.out.println(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                            echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                        }
                        else if (content.startsWith("CREATE ")) {
                            // /create <channel> criar canal novo com o usuario que criou como admin, nome do admin deve ter * na frente
                            if (channelExists(command)) {
                                byte[] denied = Messages.CHANNEL_NAME_ALREADY_EXISTS.getBytes();
                                socket.send(new DatagramPacket(denied, denied.length, sender.getIPAddress(), sender.getPort()));
                            }
                            else createChannel(sender, command);
                        }
                        else if (content.startsWith("JOIN ")) {
                            // /join <channel> solicita a participação em um canal
                            if(channelExists(command)){
                                Channel.inviteUser(sender, getChannelByName(command));
                                System.out.println( sender.getNickname() + Messages.USER_JOINING_CHANNEL  + command);
                            }
                            else createChannel(sender, command);
                        }
                    }
                    else if(content.startsWith("LIST")){
                        // /list mostra canais criados no servidor

                        byte[] data = listServers().getBytes();
                        socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                    }
                    else if(content.startsWith("QUIT")){
                        // /quit encerra conexao do usuario
                        clients.remove(sender);
                        existingClients.remove(id);
                        echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                        System.out.println(sender.getNickname() + Messages.USER_DISCONNECTED);
                    }
                    else if(content.startsWith("HELP")){
                        byte[] data = Messages.LOBBY_HELP.getBytes();
                        socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                    }
                    else{
                        System.err.println(Messages.INVALID_COMMAND);
                        byte[] data = Messages.INVALID_COMMAND.getBytes();
                        socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                    }
                }
            }catch(Exception e) {
               System.err.println(Messages.INVALID_COMMAND);
            }
        }
    }

    private void echoMessage(String message) throws Exception{
        byte[] data = (message).getBytes();
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

    public String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(Messages.AVAILABLE_CHANNELS);
        channelList.append(getName() + " - " + clients.size() + " online users \n");
        if(!channels.isEmpty()){
            for (Channel channel : channels){
                String channelName = channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }
        }
        return channelList.toString();
    }

    private void createChannel(User user, String command){
        try {
            user.setChannel(new Channel(user, command));
            channels.add(user.getChannel());
            clients.remove(user);
            System.out.println(user.getNickname() + Messages.CHANNEL_CREATED + "#" + command);
            echoMessage(user.getNickname() + Messages.CHANNEL_CREATED + "#" + command);
            user.getChannel().start();
        }
        catch(Exception e){

        }
    }

    public static void partChannel(User user){
        clients.add(user);
        byte[] data = (user.getNickname() + " " + Messages.PART_CHANNEL).getBytes();
        try {
            socket.send(new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));
        }catch(Exception e){}
    }



    public static Channel getChannelByName(String channel){
        for (Channel c : channels) {
            if (c.getName().equals(channel)) {
                return c;
            }
        }
        return null;
    }

    public static boolean channelExists(String channel){
        for (Channel c : channels) {
            if (c.getName().equals(channel)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception{
        Server s = new Server();
        s.setName(Messages.SERVER_NAME);
        s.start();
    }
}
