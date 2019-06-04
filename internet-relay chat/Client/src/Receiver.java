import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private byte[] buffer;

    Receiver(DatagramSocket socket) {
        this.socket = socket;
        buffer = new byte[Client.BUFFER_SIZE];
    }

    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);
                if(received.equals(Messages.LOGGED_IN_PM)){
                    Client.connected = true;
                }
                Client.PORT = packet.getPort();

            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}