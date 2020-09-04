import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Server {

    private static ByteArrayOutputStream historyBytes = new ByteArrayOutputStream();
    public static byte[] clientNonce;
    private static SecretKeySpec serverEncrypt;
    private static SecretKeySpec clientEncrypt;
    private static SecretKeySpec serverMAC;
    private static SecretKeySpec clientMAC;
    private static IvParameterSpec serverIV;
    private static IvParameterSpec clientIV;
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static BigInteger Ks;
    private static BigInteger N;
    private static byte[] server_DHPublicKey;
    private static byte[] client_DHPublicKey;
    private static byte[] DHSharedSecret;
    private static BigInteger g = new BigInteger("2");

    public static void sendSignedDHPublicKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        PrivateKey RSA_privateKey = Sup.readPrivateKey("server");
        byte[] signedDHPublicKey = Sup.signDHPublicKey(server_DHPublicKey, RSA_privateKey);
        Sup.sendBytes(socket, signedDHPublicKey);
        historyBytes.writeBytes(signedDHPublicKey);
    }

    public static void handshake() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        receiveNonce();
        sendServerCertificate();
        sendDHPublicKey();
        sendSignedDHPublicKey();

        client_DHPublicKey = Sup.verifySignedDHPublicKey(socket, historyBytes);
        DHSharedSecret = Sup.computeSharedDHKey(client_DHPublicKey, Ks.toByteArray(), N.toByteArray());
        makeSecretKeys();

        Sup.sendMAC(socket, serverMAC, historyBytes);
        Sup.receiveMAC(socket, clientMAC, historyBytes);
        System.out.println("handshake in server side finished");
    }

    public static void makeSecretKeys() throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] prk = Sup.HMAC(clientNonce, DHSharedSecret);

        serverEncrypt = new SecretKeySpec(Sup.hkdfExpand(prk, "server encrypt"), "AES");
        clientEncrypt = new SecretKeySpec(Sup.hkdfExpand(serverEncrypt.getEncoded(), "client encrypt"), "AES");
        serverMAC = new SecretKeySpec(Sup.hkdfExpand(clientEncrypt.getEncoded(), "server MAC"), "SHA256");
        clientMAC = new SecretKeySpec(Sup.hkdfExpand(serverMAC.getEncoded(), "client MAC"), "SHA256");
        serverIV = new IvParameterSpec(Sup.hkdfExpand(clientMAC.getEncoded(), "server IV"));
        clientIV = new IvParameterSpec(Sup.hkdfExpand(serverIV.getIV(), "client IV"));
    }

    public static void sendFile(String filename) throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        InputStream inputStream = new FileInputStream(filename);
        byte[] allBytes = inputStream.readAllBytes();
        Sup.sendEncrypted(socket, allBytes, serverEncrypt, serverIV);
    }

    public static void receiveNonce() throws IOException {
        clientNonce = Sup.receiveBytes(socket);
        historyBytes.writeBytes(clientNonce);
    }

    public static void sendServerCertificate() throws IOException, CertificateException {
        byte[] certificateBytes = Sup.read_certificate("server");
        Sup.sendBytes(socket, certificateBytes);
        historyBytes.writeBytes(certificateBytes);
    }

    public static void sendDHPublicKey() throws IOException {
        Ks = Sup.generateDHPrivateKey();
        N = new BigInteger(Sup.read_N(), 16);
        server_DHPublicKey = Sup.computeDHPubKey(g, Ks, N).toByteArray();
        Sup.sendBytes(socket, server_DHPublicKey);
        historyBytes.writeBytes(server_DHPublicKey);
    }

    public static void receiveACK() throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        byte[] ACKBytes = Sup.receiveEncrypted(socket, clientEncrypt, clientIV);
        String ACKMessage = new String(ACKBytes);

        if (ACKMessage.equals("Filed Received")) {
            System.out.println("Successfully received an ACK");
        }
    }

    public static void main(String[] args) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, ClassNotFoundException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException {

        serverSocket = new ServerSocket(8080);
        System.out.println("Server is waiting for port 8080");

        socket = serverSocket.accept();
        System.out.println("Successfully connect");

        handshake();
        System.out.println("Successfully handshake");

        sendFile("hint.txt");
        System.out.println("Successfully send file");

        receiveACK();
    }
}
