import java.net.InetAddress;

public class User {
    private String nickname;
    private Channel channel;
    private int userId;



    private ChatClientInterface remoteAcessForServer;

    public User(int id, String nickname, ChatClientInterface remoteAcessForServer){
        this.nickname = nickname;
        this.userId= id;
        this.remoteAcessForServer = remoteAcessForServer;

    }

    public ChatClientInterface getRemoteAcessForServer() {
        return remoteAcessForServer;
    }

    public int getId() { return userId; }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
