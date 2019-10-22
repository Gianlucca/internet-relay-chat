import java.net.InetAddress;

public class User {
    private String nickname;
    private Channel channel;
    private int userId;
    private static int id = 0;

    public User(String nickname){
        this.nickname = nickname;
        this.userId= id;
        id++;
    }

    public int getId() { return id; }

    public void setId(int id) {this.id = id;
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
