import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel extends Thread{
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
        messageUser(admin, Messages.CHANNEL_CREATE_MESSAGE + name);
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
                            if (admin == sender) {
                                String oldNick = sender.getNickname();
                                sender.setNickname("*" + command);
                                admin = sender;
                                echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                            } else {
                                String oldNick = sender.getNickname();
                                sender.setNickname(command);
                                echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                            }
                        } else if (content.startsWith("JOIN")) {
                            if (Server.channelExists(command)) {
                                if(admin == sender){
                                    sender.setNickname(sender.getNickname().substring(1));
                                    admin = sender;
                                }
                                users.remove(sender);
                                Channel.inviteUser(sender, Server.getChannelByName(command));
                            } else
                                messageUser(sender, Messages.CHANNEL_NOT_FOUND);
                        }
                    } else if (content.startsWith("LIST"))
                        messageUser(sender, listServers());
                    else if (content.startsWith("PART")) {
                        users.remove(sender);
                        if (admin == sender){
                            sender.setNickname(sender.getNickname().substring(1));
                            admin = sender;
                        }
                        sender.setChannel(null);
                        echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                        Server.partChannel(sender);
                    } else if (content.startsWith("MSG")) {
                        String[] command = content.split(" ",3);
                        User u = getUserByNick(command[1]);
                        if(u == null)
                            messageUser(sender, Messages.USER_NOT_FOUND);
                        else if (u == sender)
                            messageUser(sender, Messages.CANNOT_MESSAGE_YOURSELF);
                        else
                            messageUser(u, "<"+sender.getNickname()+">:" + command[2]);
                    } else if (content.startsWith("HELP"))
                        messageUser(sender, Messages.CHANNEL_HELP);
                    else if (content.startsWith("QUIT")) {
                        users.remove(sender);
                        Server.existingClients.remove(IPAddress.toString() + ":" + port);
                        echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                    } else if (content.startsWith("NAMES"))
                        messageUser(sender, listUsers());
                    else if (admin == sender) {
                        if (!sender.getNickname().substring(0, 1).equals("*"))
                            sender.setNickname("*" + admin.getNickname());
                        if (content.startsWith("REMOVE"))
                            removeChannel(sender);
                        else if (content.startsWith("KICK ")) {
                            sender.setNickname(sender.getNickname().substring(1));
                            if (sender.getNickname().equals(content.split(" ")[1].trim()))
                                messageUser(sender, Messages.CANT_KICK_YOURSELF);
                            else
                                kickUser(content.split(" ")[1].trim());
                            sender.setNickname("*" + sender.getNickname());
                        } else echoMessage("<" + sender.getNickname() + ">: " + content);
                    } else echoMessage("<" + sender.getNickname() + ">: " + content);
                }
            }
        }catch(Exception e){ System.err.println(e); }
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

    private User getUserByNick(String nick) {
        for (User user : users)
            if (user.getIPAddress().toString().equals(nick))
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
        System.out.println(user.getNickname() + Messages.USER_JOINING_CHANNEL + c.getName());
        c.messageUser(user, Messages.CHANNEL_WELCOME_MESSAGE);
    }

    private void kickUser(String user){
        User kick = getUserByNick(user);
        if (kick == null)
            messageUser(admin, Messages.USER_NOT_FOUND);
        else {
            users.remove(kick);
            kick.setChannel(null);
            echoMessage(kick.getNickname() + Messages.USER_KICKED);
            messageUser(kick, Messages.YOU_GOT_KICKED);
            Server.partChannel(kick);
        }
    }

    private String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(Messages.AVAILABLE_CHANNELS);
        if(!Server.channels.isEmpty())
            for (Channel channel : Server.channels){
                String channelName = "#"+ channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }

        return channelList.toString();
    }

    private String listUsers(){
        StringBuilder userList = new StringBuilder();
        userList.append(Messages.ONLINE_USERS);
        for (User user : users){
            String nick = user.getNickname() + "\n";
            userList.append(nick);
        }
        return userList.toString();
    }

    private void messageUser(User user, String message){
        byte[] data = message.getBytes();
        try{ socket.send(new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));}
        catch (Exception e){ }
    }

    public void removeChannel(User user){
        Server.channels.remove(user.getChannel());
        for (User u : users) {
            if (u == user)
                user.setNickname(user.getNickname().substring(1));
            echoMessage(u.getNickname() + Messages.USER_DISCONNECTED);
            messageUser(u, Messages.CHANNEL_CLOSING);
            Server.partChannel(u);
        }
        stop = true;
    }
}