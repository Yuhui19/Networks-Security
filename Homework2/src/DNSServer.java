import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class DNSServer {

    private DatagramSocket serverSocket;

    public DNSServer() {
        try {
            serverSocket = new DatagramSocket(8053);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        DatagramPacket googlePacket = new DatagramPacket(receiveData, receiveData.length);

        while (true) {
            try {
                serverSocket.receive(receivePacket);
                ByteArrayInputStream client = new ByteArrayInputStream(receivePacket.getData());
                byte[] message = new byte[receivePacket.getLength()];
                client.read(message);
                DNSMessage request = DNSMessage.decodeMessage(message);
                System.out.println("original question " + request.getQuestion().get(0));

                DNSQuestion requestQues = request.getQuestion().get(0);
                DNSMessage response = null;

                if (DNSCache.containsDomain(requestQues)) {
                    ArrayList<DNSRecord> answers = DNSCache.query(requestQues);
                    response = DNSMessage.buildResponse(request, answers);

                } else {
                    DatagramPacket queryPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                            InetAddress.getByName("8.8.8.8"), 53);
                    DatagramSocket querySocket = new DatagramSocket();
                    querySocket.send(queryPacket);
                    querySocket.receive(googlePacket);
                    querySocket.close();

                    ByteArrayInputStream toGoogleStream = new ByteArrayInputStream(googlePacket.getData());
                    byte[] gMsgArr = new byte[googlePacket.getLength()];
                    toGoogleStream.read(gMsgArr);
                    DNSMessage googleMessage = DNSMessage.decodeMessage(gMsgArr);
                    DNSCache.insertCache(requestQues, googleMessage.getAnswer().get(0));
                    response = DNSMessage.buildResponse(request, googleMessage.getAnswer());
                }

                sendData = response.toBytes();
                DatagramPacket responsePacket = new DatagramPacket(sendData, sendData.length,
                        receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(responsePacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String args[]) throws IOException {
        DNSServer DNSServer = new DNSServer();
        DNSServer.run();
    }



}
