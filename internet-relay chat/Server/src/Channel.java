import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Channel extends UnicastRemoteObject implements ChatServerInterface{
    private final static int BUFFER = 1024;

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


    @Override
    public int register(String nickname) throws RemoteException {
        try {
            int uniqueIdClient = Server.getId();
            ChatClientInterface nextClient = (ChatClientInterface) Naming.lookup("//localhost:1099/Client");
            Server.clientsAcess.put(uniqueIdClient, new User(uniqueIdClient, nickname, nextClient));

            return uniqueIdClient;

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return 0;

    }

    @Override
    public String nick(int user, String nickname) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);
        if (admin.getId() == user) {
            String oldNick = userWhoRequested.getNickname();
            userWhoRequested.setNickname("*" + nickname);
            admin = userWhoRequested;
            echoMessage(userWhoRequested.getChannel(), oldNick + ServMessages.CHANGED_NAME + userWhoRequested.getNickname());
        } else {
            String oldNick = userWhoRequested.getNickname();
            userWhoRequested.setNickname(nickname);
            echoMessage(userWhoRequested.getChannel(),oldNick + ServMessages.CHANGED_NAME + userWhoRequested.getNickname());
        }

        return ServMessages.CHANGED_NAME;
    }

    @Override
    public String list() throws RemoteException {
        String channelList = (ServMessages.AVAILABLE_CHANNELS);
        int i = 1;
        if(!Server.channels.isEmpty())
            for (Channel channel : Server.channels){
                String channelName = "#"+ i + " - " +  channel.getName() + "\n";
                channelList += (channelName);
                i++;
            }

        return channelList;
    }

    @Override
    public String create(int user, String command) throws RemoteException {
        return Server.createChannel(Server.clientsAcess.get(user), command);
    }

    @Override
    public String remove(int user) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);
        Server.channels.remove(userWhoRequested);
        for (User u : users) {
            if (u == userWhoRequested)
                userWhoRequested.setNickname(userWhoRequested.getNickname().substring(1));
            echoMessage(userWhoRequested.getChannel(),u.getNickname() + ServMessages.USER_DISCONNECTED);
            Server.partChannel(u);
            return ServMessages.CHANNEL_CLOSING;

        }
        stop = true;
        return ServMessages.CHANNEL_HELP;
    }

    @Override
    public String join(int user, String channel) throws RemoteException {
        if (Server.channelExists(channel)) {
            User userWhoRequested = Server.clientsAcess.get(user);
            if(admin == userWhoRequested){
                userWhoRequested.setNickname(userWhoRequested.getNickname().substring(1));
                admin = userWhoRequested;
            }
            users.remove(userWhoRequested);
            return ServMessages.USER_JOINING_CHANNEL;

        } else return ServMessages.CHANNEL_NOT_FOUND;
    }

    @Override
    public String part(int user, String channel) throws RemoteException {
        if (Server.getChannelByName(channel)!= null) {
            Channel ch = Server.getChannelByName(channel);
            User userWhoRequested = Server.clientsAcess.get(user);
            ch.users.remove(userWhoRequested);
            if (ch.admin == userWhoRequested) {
                userWhoRequested.setNickname(userWhoRequested.getNickname().substring(1));
                ch.admin = userWhoRequested;
            }
            userWhoRequested.setChannel(null);
            echoMessage(userWhoRequested.getChannel(), userWhoRequested.getNickname() + ServMessages.USER_DISCONNECTED);
            Server.partChannel(userWhoRequested);

            return ServMessages.USER_DISCONNECTED;
        }

        return ServMessages.CHANNEL_NOT_FOUND;
    }

    @Override
    public String names(int user) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);

        String userList = (ServMessages.ONLINE_USERS);
        int i = 1;
        for (User u : userWhoRequested.getChannel().users){
            String nick = u.getNickname() + "\n";
            userList+= (nick);
            i++;
        }
        return userList;
    }

    @Override
    public String kick(int user, String nickname) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);
        User kick = getUserByNick(userWhoRequested.getNickname());
        if (kick == null)
            return ServMessages.USER_NOT_FOUND;
        else {
            Channel ch = userWhoRequested.getChannel();
            ch.users.remove(kick);
            kick.setChannel(null);
            echoMessage(ch, kick.getNickname() + ServMessages.USER_KICKED);

            Server.partChannel(kick);
            return ServMessages.YOU_GOT_KICKED;
        }
    }

    @Override
    public String msg(int user, String nickname, String message) throws RemoteException {
        String[] command = message.split(" ",3);
        User u = getUserByNick(command[1]);
        User userWhoRequested = Server.clientsAcess.get(user);
        if(u == null)
            return ServMessages.USER_NOT_FOUND;
        else if (u == userWhoRequested)
            return ServMessages.CANNOT_MESSAGE_YOURSELF;
        else {
            String msgem = "<" + userWhoRequested.getNickname() + ">:" + command[2];
            u.getRemoteAcessForServer().message(msgem);
            return msgem;
        }
    }

    @Override
    public void message(int user, String message) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);

        echoMessage(userWhoRequested.getChannel(), message);
    }

    @Override
    public void quit(int user) throws RemoteException {
        User userWhoRequested = Server.clientsAcess.get(user);
        userWhoRequested.getChannel().users.remove(userWhoRequested);
        Server.clientsAcess.remove(user);
        echoMessage(userWhoRequested.getChannel(), userWhoRequested.getNickname() + ServMessages.USER_DISCONNECTED);

    }

    private void echoMessage(Channel channel, String message) throws RemoteException {
        for (User user : channel.users)
            user.getRemoteAcessForServer().message(message);

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


    public String getName() {
        return name;
    }
}
