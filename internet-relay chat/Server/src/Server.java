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
                    echoMessage("<" +content+ ">" + Messages.LOGGED_IN);
                    existingClients.add(id);
                    User u = new User(content, IPAddress, port);
                    clients.add(u);
                    messageUser(u, Messages.LOGGED_IN_PM);
                }
                else{
                    User sender = getUserById(IPAddress.toString(), port);

                    if(content.startsWith("NICK ") || content.startsWith("CREATE ") || content.startsWith("JOIN ") ) {
                        String command = content.split(" ")[1].trim();
                        if (content.startsWith("NICK ")) {
                            String oldNick = sender.getNickname();
                            sender.setNickname(command);
                            echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                        }
                        else if (content.startsWith("CREATE "))
                            if (channelExists(command))
                                messageUser(sender, Messages.CHANNEL_NAME_ALREADY_EXISTS);
                            else createChannel(sender, command);
                        else if (content.startsWith("JOIN "))
                            if(channelExists(command)){
                                clients.remove(sender);
                                Channel.inviteUser(sender, getChannelByName(command));
                            }
                            else createChannel(sender, command);
                    }
                    else if(content.startsWith("LIST"))
                        messageUser(sender, listServers());
                    else if(content.startsWith("QUIT")){
                        clients.remove(sender);
                        existingClients.remove(id);
                        echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                    }
                    else if(content.startsWith("HELP"))
                        messageUser(sender, Messages.LOBBY_HELP);
                    else{
                        System.err.println(Messages.INVALID_COMMAND);
                        messageUser(sender, Messages.INVALID_COMMAND);}
                }
            }catch(Exception e) { System.err.println(Messages.INVALID_COMMAND); }
        }
    }

    private void echoMessage(String message){
        System.err.println(message);
        for (User user : clients)
            messageUser(user,message);
    }

    private static void messageUser(User user, String message){
        byte[] data = message.getBytes();
        try{ socket.send(new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));}
        catch (Exception e){ }
    }

    private User getUserById(String ip, int port){
        for (User user : clients)
            if(user.getIPAddress().toString().equals(ip) && user.getPort() == port)
                return user;
        return null;
    }

    public String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(Messages.AVAILABLE_CHANNELS);
        channelList.append(getName() + " - " + clients.size() + " online users \n");
        if(!channels.isEmpty())
            for (Channel channel : channels){
                String channelName = "#" + channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }
        return channelList.toString();
    }

    private void createChannel(User user, String command){
        try {
            Channel c = new Channel(user, command);
            user.setChannel(c);
            channels.add(c);
            clients.remove(user);
            echoMessage(user.getNickname() + Messages.CHANNEL_CREATED + "#" + command);
            user.getChannel().start();
        }catch(Exception e){messageUser(user, Messages.CHANNEL_SOCKET_ERROR);}
    }

    public static void partChannel(User user){
        clients.add(user);
        Server.messageUser(user, user.getNickname() + " " + Messages.PART_CHANNEL);
    }

    public static Channel getChannelByName(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return c;
        return null;
    }

    public static boolean channelExists(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return true;
        return false;
    }

    public static void main(String[] args) throws Exception{
        Server s = new Server();
        s.setName(Messages.SERVER_NAME);
        s.start();
    }
}
