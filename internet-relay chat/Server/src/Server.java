import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server implements ChatServerInterface{
    private final static int PORT = 9876;
    private final static int BUFFER = 1024;


    private static DatagramSocket socket;
    private static ArrayList<User> clients;
    public static HashSet<String> existingClients;
    public static ArrayList<Channel> channels;

    private Server() throws IOException {
        socket = new DatagramSocket(PORT);
        existingClients = new HashSet<>();
        clients = new ArrayList<>();
        channels = new ArrayList<>();
    }



    public void run() {
        System.out.println(ServMessages.SERVER_RUNNING);
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
                    echoMessage("<" +content+ ">" + ServMessages.LOGGED_IN);
                    existingClients.add(id);

                    User u = new User(content, IPAddress, port);
                    clients.add(u);
                    messageUser(u, ServMessages.LOGGED_IN_PM);
                }
                else{
                    User sender = getUserById(IPAddress.toString(), port);

                    if(content.startsWith("NICK ") || content.startsWith("CREATE ") || content.startsWith("JOIN ") ) {
                        String command = content.split(" ")[1].trim();
                        if (content.startsWith("NICK ")) {
                            String oldNick = sender.getNickname();
                            sender.setNickname(command);
                            echoMessage(oldNick + ServMessages.CHANGED_NAME + sender.getNickname());
                        }
                        else if (content.startsWith("CREATE "))
                            if (channelExists(command))
                                messageUser(sender, ServMessages.CHANNEL_NAME_ALREADY_EXISTS);
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
                        echoMessage(sender.getNickname() + ServMessages.USER_DISCONNECTED);
                    }
                    else if(content.startsWith("HELP"))
                        messageUser(sender, ServMessages.LOBBY_HELP);
                    else{
                        System.err.println(ServMessages.INVALID_COMMAND);
                        messageUser(sender, ServMessages.INVALID_COMMAND);}
                }
            }catch(Exception e) { System.err.println(ServMessages.INVALID_COMMAND); }
        }
    }

    public static boolean channelExists(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return true;
        return false;
    }

    private void createChannel(User user, String command){
        try {
            Channel c = new Channel(user, command);
            user.setChannel(c);
            channels.add(c);
            clients.remove(user);
            echoMessage(user.getNickname() + ServMessages.CHANNEL_CREATED + "#" + command);
            user.getChannel().start();
        }catch(Exception e){messageUser(user, ServMessages.CHANNEL_SOCKET_ERROR);}
    }

    private static void echoMessage(String message){
        System.err.println(message);
        for (User user : clients)
            messageUser(user,message);
    }

    public static Channel getChannelByName(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return c;
        return null;
    }

    private User getUserById(String ip, int port){
        for (User user : clients)
            if(user.getIPAddress().toString().equals(ip) && user.getPort() == port)
                return user;
        return null;
    }

    public String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(ServMessages.AVAILABLE_CHANNELS);
        channelList.append(getName() + " - " + clients.size() + " online users \n");
        if(!channels.isEmpty())
            for (Channel channel : channels){
                String channelName = "#" + channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }
        return channelList.toString();
    }

    private static void messageUser(User user, String message){
        byte[] data = message.getBytes();
        try{ socket.send(new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));}
        catch (Exception e){ }
    }

    public static void partChannel(User user){
        clients.add(user);
        Server.messageUser(user, user.getNickname() + " " + ServMessages.PART_CHANNEL);
    }

    public static void main(String[] args) throws Exception{
        Server s = new Server();
        java.rmi.registry.LocateRegistry.createRegistry(9);

        Naming.rebind("Chat", new Chat());

        Arrays.fill(receiveData, (byte) 0);
        Channel channel = new Channel();
        User admin = channel.getAdmin();


        String content = new String(packet.getData()).trim();

        User sender = channel.getUserById();

        if (!content.equals("")) {
            if (content.startsWith("NICK ") || content.startsWith("JOIN")) {
                String command = content.split(" ")[1].trim();
                if (content.startsWith("NICK ")) {
                    channel.nick(sender, command);
                } else if (content.startsWith("JOIN")) {
                    channel.join(sender, command);
                }
            } else if (content.startsWith("LIST"))
                messageUser(sender, channel.list(sender).toString());
            else if (content.startsWith("PART")) {
                channel.part(sender);
            } else if (content.startsWith("MSG")) {
                channel.msg(sender, content);
            } else if (content.startsWith("HELP"))
                messageUser(sender, ServMessages.CHANNEL_HELP);
            else if (content.startsWith("QUIT")) {
                channel.quit(sender);
            } else if (content.startsWith("NAMES"))
                messageUser(sender, channel.names(sender).toString());
            else if (admin == sender) {
                if (!sender.getNickname().substring(0, 1).equals("*"))
                    sender.setNickname("*" + admin.getNickname());
                if (content.startsWith("REMOVE"))
                    channel.remove(sender);
                else if (content.startsWith("KICK ")) {
                    sender.setNickname(sender.getNickname().substring(1));
                    if (sender.getNickname().equals(content.split(" ")[1].trim()))
                        messageUser(sender, ServMessages.CANT_KICK_YOURSELF);
                    else
                        channel.kick(sender,content.split(" ")[1].trim());
                    sender.setNickname("*" + sender.getNickname());
                } else echoMessage("<" + sender.getNickname() + ">: " + content);
            } else echoMessage("<" + sender.getNickname() + ">: " + content);
        }
    }


}
