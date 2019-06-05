import java.net.*;

class Client {

    public static int BUFFER_SIZE = 1024;
    public static int PORT = 9876;
    public static boolean connected = false;


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
 