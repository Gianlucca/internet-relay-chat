import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel extends Thread{
    private final static int BUFFER = 1024;

    private static User admin;
    private static ArrayList<User> users;
    private DatagramSocket socket;

    public Channel(User admin, String name){
        this.admin = admin;
        setName(name);
        this.users = new ArrayList<>();
        users.add(this.admin);
        try {
            socket = new DatagramSocket();
            socket.send(new DatagramPacket((Messages.CHANNEL_CREATE_MESSAGE + name).getBytes(),
                    (Messages.CHANNEL_CREATE_MESSAGE + name).getBytes().length,
                    admin.getIPAddress(),
                    admin.getPort())
            );
        }catch(Exception e){
            System.out.println(Messages.CHANNEL_SOCKET_ERROR);
        }
    }

    public void run(){
        byte[] receiveData = new byte[BUFFER];
        try{
            while(true) {
                Arrays.fill(receiveData, (byte)0);
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);

                String content = new String(packet.getData()).trim();
                InetAddress IPAddress = packet.getAddress();
                int port = packet.getPort();

                User sender = getUserById(IPAddress.toString(), port);

                if(admin == sender){
                    this.admin.setNickname("*" + this.admin.getNickname());
                    if(content.startsWith("REMOVE ")){

                    }
                    else if(content.startsWith("KICK ")){

                    }
                }

                if(content.startsWith("NICK ") || content.startsWith("JOIN")) {
                    String command = content.split(" ")[1].trim();
                    if (content.startsWith("NICK ")) {
                        String oldNick = sender.getNickname();
                        sender.setNickname(command);
                        System.out.println(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                        echoMessage(oldNick + Messages.CHANGED_NAME + sender.getNickname());
                    } else if (content.startsWith("JOIN")) {
                        if (Server.channelExists(command)) {
                            Channel.inviteUser(sender, Server.getChannelByName(command));
                            System.out.println(sender.getNickname() + Messages.USER_JOINING_CHANNEL + command);
                        } else{
                            byte[] denied = Messages.CHANNEL_NOT_FOUND.getBytes();
                            socket.send(new DatagramPacket(denied, denied.length, sender.getIPAddress(), sender.getPort()));
                        }
                    }
                }
                else if(content.startsWith("LIST")){
                    // /list mostra canais criados no servidor
                    byte[] data = listServers().getBytes();
                    socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                }
                else if(content.startsWith("PART")){
                    users.remove(sender);
                    if(admin == sender){
                        sender.setNickname(sender.getNickname().replaceFirst("\\*", ""));
                    }
                    sender.setChannel(null);
                    echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                    System.out.println(sender.getNickname() + Messages.USER_DISCONNECTED);
                    Server.partChannel(sender);
                }
                else if(content.startsWith("MSG")){

                }
                else if(content.startsWith("HELP")){
                    byte[] data = Messages.CHANNEL_HELP.getBytes();
                    socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));

                }
                else if(content.startsWith("QUIT")){
                    users.remove(sender);
                    String id = IPAddress.toString() + ":" + port;
                    Server.existingClients.remove(id);
                    echoMessage(sender.getNickname() + Messages.USER_DISCONNECTED);
                    System.out.println(sender.getNickname() + Messages.USER_DISCONNECTED);

                }
                else if(content.startsWith("NAMES")){
                    byte[] data = listsUsers().getBytes();
                    socket.send( new DatagramPacket(data, data.length, sender.getIPAddress(), sender.getPort()));
                }
                else{
                    echoMessage("<"+sender.getNickname()+">: "+content);
                    System.out.println("<"+sender.getNickname()+">: "+ content);
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
    }

    private String listsUsers(){
        StringBuilder userList = new StringBuilder();
        userList.append(Messages.ONLINE_USERS);
        for (User user : users){
            String nick = user.getNickname() + "\n";
            userList.append(nick);
        }
        return userList.toString();
    }

    private String listServers(){
        StringBuilder channelList = new StringBuilder();
        channelList.append(Messages.AVAILABLE_CHANNELS);
        if(!Server.channels.isEmpty()){
            for (Channel channel : Server.channels){
                String channelName = channel.getName() + " - " + channel.getUsersOnline() + " online users \n";
                channelList.append(channelName);
            }
        }
        return channelList.toString();
    }

    public int getUsersOnline(){
        return users.size();
    }

    public static void inviteUser(User user, Channel c){
        user.setChannel(c);
        users.add(user);
        if(admin == user){
            admin.setNickname("*" + admin.getNickname());
        }
        byte[] data = Messages.CHANNEL_WELCOME_MESSAGE.getBytes();
        try{
            c.socket.send( new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort()));
        }catch (Exception e){ }
    }

    private void echoMessage(String message) throws Exception{
        byte[] data = message.getBytes();
        for (User user : users) {
            DatagramPacket echoPacket = new DatagramPacket(data, data.length, user.getIPAddress(), user.getPort());
            socket.send(echoPacket);
        }
    }

    private User getUserById(String ip, int port){
        for (User user : users) {
            if(user.getIPAddress().toString().equals(ip) && user.getPort() == port){
                return user;
            }
        }
        return null;
    }


}
