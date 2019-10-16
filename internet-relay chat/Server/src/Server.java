import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Server extends Thread implements ChatServerInterface{
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

    private void echoMessage(String message){
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
        s.setName(ServMessages.SERVER_NAME);
        s.start();
    }

    @Override
    public int register(String ipaddress, String nickname) throws RemoteException {
        return 0;
    }

    @Override
    public void nick(User sender, String nickname) throws RemoteException {
        getChannelByName()
        if (admin.getId() == sender.getId()) {
            String oldNick = sender.getNickname();
            sender.setNickname("*" + nickname);
            admin = sender;
            echoMessage(oldNick + ServMessages.CHANGED_NAME + sender.getNickname());
        } else {
            String oldNick = sender.getNickname();
            sender.setNickname(nickname);
            echoMessage(oldNick + ServMessages.CHANGED_NAME + sender.getNickname());
        }
    }

    @Override
    public String[] list(User user) throws RemoteException {
        String[] channelList = new String[100];
        channelList[0] = (ServMessages.AVAILABLE_CHANNELS);
        int i = 1;
        if(!Server.channels.isEmpty())
            for (Channel channel : Server.channels){
                String channelName = "#"+ channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList[i] = (channelName);
                i++;
            }

        return channelList;
    }

    @Override
    public int create(User user) throws RemoteException {
        return 0;
    }

    @Override
    public void remove(User user){
        Server.channels.remove(user.getChannel());
        for (User u : users) {
            if (u == user)
                user.setNickname(user.getNickname().substring(1));
            echoMessage(u.getNickname() + ServMessages.USER_DISCONNECTED);
            messageUser(u, ServMessages.CHANNEL_CLOSING);
            Server.partChannel(u);
        }
        stop = true;
    }

    @Override
    public int join(User sender, String channel) throws RemoteException {
        if (Server.channelExists(channel)) {
            if(admin == sender){
                sender.setNickname(sender.getNickname().substring(1));
                admin = sender;
            }
            users.remove(sender);
            Channel.inviteUser(sender, Server.getChannelByName(channel));
        } else
            messageUser(sender, ServMessages.CHANNEL_NOT_FOUND);
    }

    @Override
    public void part(User sender) throws RemoteException {
        users.remove(sender);
        if (admin == sender){
            sender.setNickname(sender.getNickname().substring(1));
            admin = sender;
        }
        sender.setChannel(null);
        echoMessage(sender.getNickname() + ServMessages.USER_DISCONNECTED);
        Server.partChannel(sender);


    }

    @Override
    public String[] names(User userU) throws RemoteException {
        String[] userList = new String[100];

        userList[0] = (ServMessages.ONLINE_USERS);
        int i = 1;
        for (User user : users){
            String nick = user.getNickname() + "\n";
            userList[i] = (nick);
            i++;
        }
        return userList;
    }

    @Override
    public int kick(User user, String nickname) throws RemoteException {
        return 0;
    }

    @Override
    public void msg(User user, String message) throws RemoteException {
        String[] command = message.split(" ",3);
        User u = getUserByNick(command[1]);
        if(u == null)
            messageUser(user, ServMessages.USER_NOT_FOUND);
        else if (u == user)
            messageUser(user, ServMessages.CANNOT_MESSAGE_YOURSELF);
        else
            messageUser(u, "<"+user.getNickname()+">:" + command[2]);
    }

    @Override
    public int message(User user, String message) throws RemoteException {
        return 0;
    }

    @Override
    public int quit(User user) throws RemoteException {
        users.remove(user);
        Server.existingClients.remove(user.getIPAddress().toString() + ":" + user.getPort());
        echoMessage(user.getNickname() + ServMessages.USER_DISCONNECTED);
    }
}
