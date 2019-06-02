import java.net.*;

class Client {
    public static void main(String args[]) throws Exception{
        String host = "localhost";
        DatagramSocket socket = new DatagramSocket();
        Receiver r = new Receiver(socket);
        Sender s = new Sender(socket, host);
        Thread rt = new Thread(r);
        Thread st = new Thread(s);
        rt.start(); st.start();
    }
}
 