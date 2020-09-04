import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class Client {

    private static byte[] client_DHPublicKey;
    private static byte[] DHSharedSecret;
    private static SecretKeySpec serverEncrypt;
    private static SecretKeySpec clientEncrypt;
    private static SecretKeySpec serverMAC;
    private static SecretKeySpec clientMAC;
    private static IvParameterSpec serverIV;
    private static IvParameterSpec clientIV;
    private static Socket socket;
    private static ByteArrayOutputStream historyBytes = new ByteArrayOutputStream();
    public static byte[] clientNonce;
    public static byte[] server_DHPublicKey;
    private static BigInteger Kc;
    private static BigInteger N;
    private static BigInteger g = new BigInteger("2");

    public static void handshake() throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        sendClientNonce();
        server_DHPublicKey = Sup.verifySignedDHPublicKey(socket, historyBytes);

        sendClientCertificate();
        sendDHPublicKey();
        sendSignedDHPublicKey();

        DHSharedSecret = Sup.computeSharedDHKey(server_DHPublicKey, Kc.toByteArray(), N.toByteArray());
        makeSecretKeys();

        Sup.receiveMAC(socket, serverMAC, historyBytes);
        Sup.sendMAC(socket, clientMAC, historyBytes);

        System.out.println("handshake in client side finish");
    }

    private static byte[] generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return bytes;
    }

    private static void sendClientNonce() throws IOException {
        clientNonce = generateNonce();
        Sup.sendBytes(socket, clientNonce);
        historyBytes.writeBytes(clientNonce);
    }

    public static void sendClientCertificate() throws IOException, CertificateException {
        byte[] certificateBytes = Sup.read_certificate("client");

        Sup.sendBytes(socket, certificateBytes);
        historyBytes.writeBytes(certificateBytes);
    }

    public static void sendDHPublicKey() throws IOException {
        Kc = Sup.generateDHPrivateKey();
        N = new BigInteger(Sup.read_N(), 16);

        client_DHPublicKey = Sup.computeDHPubKey(g, Kc, N).toByteArray();

        Sup.sendBytes(socket, client_DHPublicKey);
        historyBytes.writeBytes(client_DHPublicKey);
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

    public static void sendSignedDHPublicKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        PrivateKey RSA_privateKey = Sup.readPrivateKey("client");

        byte[] signedDHPublicKey = Sup.signDHPublicKey(client_DHPublicKey, RSA_privateKey);

        Sup.sendBytes(socket, signedDHPublicKey);
        historyBytes.writeBytes(signedDHPublicKey);
    }

    public static void recv_file(String filename) throws IOException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        FileOutputStream fos = new FileOutputStream(filename);

        byte[] bytes = Sup.receiveEncrypted(socket, serverEncrypt, serverIV);

        fos.write(bytes);
    }

    public static void sendACK() throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {
        byte[] ACKBytes = "Filed Received".getBytes();

        Sup.sendEncrypted(socket, ACKBytes, clientEncrypt, clientIV);
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, InvalidKeyException, SignatureException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {

        socket = new Socket("127.0.0.1", 8080);
        System.out.println("Successfully connect");

        handshake();
        System.out.println("Successfully handshake");

        recv_file("hint_output.txt");
        System.out.println("Successfully received file");

        sendACK();
    }
}
