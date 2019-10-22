import java.net.DatagramPacket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

public class Channel extends UnicastRemoteObject implements ChatServerInterface{
    private final static int BUFFER = 1024;
    private static final long serialVersionUID = -4164277960532266268L;
    
    volatile boolean stop = false;

    public User getAdmin() {
        return admin;
    }

    private User admin;
    private ArrayList<User> users;
    private String name;
    public Channel() throws RemoteException{

    }

    public Channel(User admin, String name) throws RemoteException {
        admin.setNickname("*"+admin.getNickname());
        this.admin = admin;
        this.name = name;
        users = new ArrayList<>();
        users.add(admin);
        //messageUser(admin, ServMessages.CHANNEL_CREATE_MESSAGE + name);
    }

/*    public void run(){
        byte[] receiveData = new byte[BUFFER];
        try{
            while(!stop) {
                *//*Arrays.fill(receiveData, (byte) 0);

                String content = new String(packet.getData()).trim();

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
                }*//*
            }
        }catch(Exception e){ System.err.println(e); }
    }*/


    @Override
    public String register(int user) throws RemoteException {
        Server.existingClients.add(user);

        return ServMessages.LOGGED_IN_PM + " " + user;
      // Registry registry = LocateRegistry.createRegistry(Integer.parseInt(part(null)));
		

		//registry.rebind("ChatClient", new Client());
		
    }

    @Override
    public String nick(User sender, String nickname) throws RemoteException {
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

        return ServMessages.CHANGED_NAME;
    }

    @Override
    public String[] list(User user) throws RemoteException {
        String[] channelList = new String[100];
        channelList[0] = (ServMessages.AVAILABLE_CHANNELS);
        int i = 1;
        if(!Server.channels.isEmpty())
            for (Channel channel : Server.channels){
                String channelName = "#"+ i +  channel.getUsersOnline() + " online users \n";
                channelList[i] = (channelName);
                i++;
            }

        return channelList;
    }

    @Override
    public String create(User user, String command) throws RemoteException {
        return Server.createChannel(user, command);
    }

    @Override
    public String remove(User user) throws RemoteException {
        Server.channels.remove(user.getChannel());
        for (User u : users) {
            if (u == user)
                user.setNickname(user.getNickname().substring(1));
            echoMessage(u.getNickname() + ServMessages.USER_DISCONNECTED);
            Server.partChannel(u);
            return ServMessages.CHANNEL_CLOSING;

        }
        stop = true;
        return ServMessages.CHANNEL_HELP;
    }

    @Override
    public String join(User sender, String channel) throws RemoteException {
        if (Server.channelExists(channel)) {
            if(admin == sender){
                sender.setNickname(sender.getNickname().substring(1));
                admin = sender;
            }
            users.remove(sender);
            return ServMessages.USER_JOINING_CHANNEL;

        } else return ServMessages.CHANNEL_NOT_FOUND;
    }

    @Override
    public String part(User sender) throws RemoteException {
        users.remove(sender);
        if (admin == sender){
            sender.setNickname(sender.getNickname().substring(1));
            admin = sender;
        }
        sender.setChannel(null);
        echoMessage(sender.getNickname() + ServMessages.USER_DISCONNECTED);
        Server.partChannel(sender);

        return ServMessages.USER_DISCONNECTED;


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
    public String kick(User user, String nickname) throws RemoteException {
        User kick = getUserByNick(user.getNickname());
        if (kick == null)
            return ServMessages.USER_NOT_FOUND;
        else {
            users.remove(kick);
            kick.setChannel(null);
            //echoMessage(kick.getNickname() + ServMessages.USER_KICKED);

            Server.partChannel(kick);
            return ServMessages.YOU_GOT_KICKED;
        }
    }

    @Override
    public void msg(User user, String message) throws RemoteException {
        String[] command = message.split(" ",3);
        User u = getUserByNick(command[1]);
        if(u == null)
            message(user, ServMessages.USER_NOT_FOUND);
        else if (u == user)
            message(user, ServMessages.CANNOT_MESSAGE_YOURSELF);
        else
            message(u, "<"+user.getNickname()+">:" + command[2]);
    }

    @Override
    public String message(User user, String message) throws RemoteException {
        return message;
    }

    @Override
    public void quit(User user) throws RemoteException {
        users.remove(user);
        Server.existingClients.remove(user.getId());
        // echoMessage(user.getNickname() + ServMessages.USER_DISCONNECTED);

    }

    private void echoMessage(String message){
        System.out.println(message);
        for (User user : users)
            ;

    }

/*    private User getUserById(String ip, int port) {
        for (User user : users)
            if (user.getIPAddress().toString().equals(ip) && user.getPort() == port)
                return user;
        return null;
    }*/

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


    public String getName() {
        return name;
    }
    
    private void enviaMensagemParaCliente(String mensagem, Integer porta) throws RemoteException, NotBoundException {
		Registry enviarmensagem = LocateRegistry.getRegistry(porta);
		ChatClientInterface chatClient = (ChatClientInterface) enviarmensagem.lookup("Client");
		chatClient.message(mensagem);
	}
}
