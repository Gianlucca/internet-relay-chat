import java.io.*;
import java.net.*;

class Client {
    public static void main(String args[]) throws Exception{
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");

        System.out.println("Digite seu nick: ");

        String sentence = inFromUser.readLine();
        byte[] sendData = sentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        do {
            String newSentence = inFromUser.readLine();
            if(newSentence.startsWith("/")){
                String[] command = newSentence.split(" ", 2);
                newSentence = command[0].replaceFirst("/", "").toUpperCase() + " " + command[1];
            }
            sendData = newSentence.getBytes();

            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);
            if (newSentence.toLowerCase().startsWith("/quit")){
                break;
            }
        }while (true);
        clientSocket.close();
    }
}
 