import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel extends Thread implements ChatServerInterface{
    private final static int BUFFER = 1024;

    volatile boolean stop = false;
    private User admin;
    private ArrayList<User> users;
    private DatagramSocket socket;

    public Channel(User admin, String name){
        try {
            socket = new DatagramSocket();
        }catch(Exception e){}
        admin.setNickname("*"+admin.getNickname());
        this.admin = admin;
        setName(name);
        users = new ArrayList<>();
        users.add(admin);
        messageUser(admin, ServMessages.CHANNEL_CREATE_MESSAGE + name);
    }

    public void run(){
        byte[] receiveData = new byte[BUFFER];
        try{
            while(!stop) {
                Arrays.fill(receiveData, (byte) 0);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);

                String content = new String(packet.getData()).trim();
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();

                User sender = getUserById(IPAddress.toString(), port);
                if (!content.equals("")) {
                    if (content.startsWith("NICK ") || content.startsWith("JOIN")) {
                        String command = content.split(" ")[1].trim();
                        if (content.startsWith("NICK ")) {
                            nick(sender, command);
                        } else if (content.startsWith("JOIN")) {
                            join(sender, command);
                        }
                    } else if (content.startsWith("LIST"))
                        messageUser(sender, list(sender).toString());
                    else if (content.startsWith("PART")) {
                        part(sender);
                    } else if (content.startsWith("MSG")) {
                        msg(sender, content);
                    } else if (content.startsWith("HELP"))
                        messageUser(sender, ServMessages.CHANNEL_HELP);
                    else if (content.startsWith("QUIT")) {
                        quit(sender);
                    } else if (content.startsWith("NAMES"))
                        messageUser(sender, names(sender).toString());
                    else if (admin == sender) {
                        if (!sender.getNickname().substring(0, 1).equals("*"))
                            sender.setNickname("*" + admin.getNickname());
                        if (content.startsWith("REMOVE"))
                            remove(sender);
                        else if (content.startsWith("KICK ")) {
                            sender.setNickname(sender.getNickname().substring(1));
                            if (sender.getNickname().equals(content.split(" ")[1].trim()))
                                messageUser(sender, ServMessages.CANT_KICK_YOURSELF);
                            else
                                kickUser(content.split(" ")[1].trim());
                            sender.setNickname("*" + sender.getNickname());
                        } else echoMessage("<" + sender.getNickname() + ">: " + content);
                    } else echoMessage("<" + sender.getNickname() + ">: " + content);
                }
            }
        }catch(Exception e){ System.err.println(e); }
    }

    @Override
    public int register(String ipaddress, String nickname) throws RemoteException {
        return 0;
    }

    @Override
    public void nick(User sender, String nickname) throws RemoteException {
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

    private void echoMessage(String message){
        System.out.println(message);
        for (User user : users)
            messageUser(user, message);

    }

    private User getUserById(String ip, int port) {
        for (User user : users)
            if (user.getIPAddress().toString().equals(ip) && user.getPort() == port)
                return user;
        return null;
    }

    private User getUserById(int id) {
        for (User user : users)
            if (id == user.getId())
                return user;
        return null;
    }

    private User getUserByNick(String nick) {
        for (User user : users)
            if (user.getNickname().toString().equals(nick))
                return user;
        return null;
    }

    public int getUsersOnline(){
        return users.size();
    }

    public static void inviteUser(User user, Channel c){
        user.setChannel(c);
        c.users.add(user);
        if(c.admin == user){
            user.setNickname("*" + user.getNickname());
            c.admin = user;
        }
        System.out.println(user.getNickname() + ServMessages.USER_JOINING_CHANNEL + c.getName());
        c.messageUser(user, ServMessages.CHANNEL_WELCOME_MESSAGE);
    }

    private void kickUser(String user){
        User kick = getUserByNick(user);
        if (kick == null)
            messageUser(admin, ServMessages.USER_NOT_FOUND);
        else {
            users.remove(kick);
            kick.setChannel(null);
            echoMessage(kick.getNickname() + ServMessages.USER_KICKED);
            messageUser(kick, ServMessages.YOU_GOT_KICKED);
            Server.partChannel(kick);
        }
    }


    private void messageUser(User user, String message){
        byte[] data = message.getBytes();
        try{ socket.send(new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));}
        catch (Exception e){ }
    }


}
