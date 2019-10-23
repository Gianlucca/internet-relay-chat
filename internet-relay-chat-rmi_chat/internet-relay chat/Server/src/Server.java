import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class Server {
    private final static int PORT = 9876;
    private final static int BUFFER = 1024;
    private static int id = 0;

    public static ArrayList<User> lobbyClients;
    public static HashMap<Integer, User> clientsAcess;
    public static HashSet<Integer> existingClients;
    public static ArrayList<Channel> channels;

    private Server() throws IOException {
        clientsAcess = new HashMap<>();
        existingClients = new HashSet<>();
        channels = new ArrayList<>();
        lobbyClients = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception{
        Server server = new Server();
        java.rmi.registry.LocateRegistry.createRegistry(1099);
        Naming.rebind("Server", new Channel());

        System.out.println("Server iniciado");
    }

    public static boolean channelExists(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return true;
        return false;
    }

    public static Channel getChannelByName(String channel){
        for (Channel c : channels)
            if (c.getName().equals(channel))
                return c;
        return null;
    }

    public static void partChannel(User user) throws RemoteException {
        for (Channel c: channels) {
            if (c == user.getChannel()) {
                c.message(user.getId(), user.getNickname() + " " + ServMessages.PART_CHANNEL);
            }
        }
    }

/*    private User getUserById(String ip, int port){
        for (User user : clients)
            if(user.getIPAddress().toString().equals(ip) && user.getPort() == port)
                return user;
        return null;
    }*/

/*    public String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(ServMessages.AVAILABLE_CHANNELS);
        channelList.append(*//*getName()*//*  " - " + clients.size() + " online users \n");
        if(!channels.isEmpty())
            for (Channel channel : channels){
                String channelName = "#" + channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }
        return channelList.toString();
    }*/

    public static String createChannel(User user, String command){
        try {
            Channel c = new Channel(user, command);
            Server.lobbyClients.remove(user);
            user.setChannel(c);
            channels.add(c);
            return ServMessages.CHANNEL_CREATED;
        }catch(Exception e){ return ServMessages.CHANNEL_NOT_FOUND; }
    }

    public static int getId(){
        id++;
        return id;
    }




}
