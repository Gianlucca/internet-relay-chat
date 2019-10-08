import java.net.InetAddress;

public class User {
    private String nickname;
    private InetAddress IPAddress;
    private int port;
    private Channel channel;
    private int userId;
    private static int id = 0;

    public User(String nickname, InetAddress IPAddress, int port){
        this.IPAddress = IPAddress;
        this.nickname = nickname;
        this.port = port;
        this.userId= id;
        id++;
    }

    public int getId() { return id; }

    public void setId(int id) {this.id = id;
    }

    public InetAddress getIPAddress() {
        return IPAddress;
    }

    public int getPort() {
        return port;
    }

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
