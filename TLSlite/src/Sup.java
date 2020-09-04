import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Sup {

    public static String read_N() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("DH.txt"));
        String str = "";

        while (scanner.hasNext()) {
            str += scanner.next();
        }
        return str;
    }

    public static void sendBytes(Socket socket, byte[] toBeSent) throws IOException {
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        dOut.writeInt(toBeSent.length);
        dOut.write(toBeSent);
    }

    public static byte[] receiveBytes(Socket socket) throws IOException {
        DataInputStream dIn = new DataInputStream(socket.getInputStream());
        int length = dIn.readInt();

        if(length>0) {
            byte[] message = new byte[length];
            dIn.readFully(message, 0, message.length);
            return message;
        }
        return null;
    }

    public static void sendInt(Socket socket, int num) throws IOException {
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        dOut.writeInt(num);
    }

    public static int receiveInt(Socket socket) throws IOException {
        DataInputStream dIn = new DataInputStream(socket.getInputStream());
        return dIn.readInt();
    }

    public static byte[] read_certificate(String input) throws IOException, CertificateException {
        String filename;

        if (input.equals("client"))
            filename = "CASignedClientCertificate.pem";
        else
            filename = "CASignedServerCertificate.pem";

        InputStream inputStream = new FileInputStream(filename);
        return inputStream.readAllBytes();
    }

    public static byte[] signDHPublicKey(byte[] DHPublicKey, PrivateKey RSA_privateKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException, InvalidKeySpecException {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        SecureRandom secureRandom = new SecureRandom();
        signature.initSign(RSA_privateKey, secureRandom);
        signature.update(DHPublicKey);
        return signature.sign();
    }

    public static PublicKey getRSAPubKey(byte[] certificateBytes) throws  CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream certificateInputStream = new ByteArrayInputStream(certificateBytes);
        Certificate certificate = certificateFactory.generateCertificate(certificateInputStream);
        return certificate.getPublicKey();
    }

    public static boolean verify(byte[] signature_toBeVerified, byte[] DHPubKey_toBeVerified, PublicKey RSAPubKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(RSAPubKey);
        signature.update(DHPubKey_toBeVerified);
        return signature.verify(signature_toBeVerified);
    }

    public static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i]) {
                System.out.println("bytes");
                return false;
            }
        return true;
    }

    public static PrivateKey readPrivateKey(String input) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String filename = input + "PrivateKey.der";
        InputStream inputStream = new FileInputStream(filename);
        byte[] keyBytes = inputStream.readAllBytes();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static byte[] verifySignedDHPublicKey(Socket socket, ByteArrayOutputStream historyBytes) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateException {
        byte[] certificate = Sup.receiveBytes(socket);
        historyBytes.writeBytes(certificate);
        PublicKey RSAPublicKey = Sup.getRSAPubKey(certificate);
        byte[] DHPublicKey = Sup.receiveBytes(socket);
        historyBytes.writeBytes(DHPublicKey);
        byte[] signedDHPublicKey = Sup.receiveBytes(socket);
        historyBytes.writeBytes(signedDHPublicKey);
        boolean verified = Sup.verify(signedDHPublicKey, DHPublicKey, RSAPublicKey);

        if (!verified) {
            socket.close();
            System.exit(1);
        }
        return DHPublicKey;
    }

    public static byte[] computeSharedDHKey(byte[] _T, byte[] _K, byte[] _N) {
        BigInteger T = new BigInteger(_T);
        BigInteger K = new BigInteger(_K);
        BigInteger N = new BigInteger(_N);
        return T.modPow(K, N).toByteArray();
    }

    public static byte[] HMAC(byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(keySpec);
        byte [] mac_data = sha256_HMAC.doFinal(data);
        return mac_data;
    }

    public static byte[] addOneByteToTag(String tag) {
        byte[] result = new byte[tag.length() + 1];
        byte[] originalBytes = tag.getBytes();
        System.arraycopy(originalBytes, 0, result, 0, originalBytes.length);
        result[tag.length()] = (byte)1;

        return result;
    }

    public static byte[] hkdfExpand(byte[] key, String tag) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] okm = HMAC(key, addOneByteToTag(tag));
        return Arrays.copyOfRange(okm, 0, 16);
    }

    public static void sendMAC(Socket socket, SecretKeySpec MACKey, ByteArrayOutputStream os) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        byte[] HMAC = Sup.HMAC(MACKey.getEncoded(), os.toByteArray());
        Sup.sendBytes(socket, HMAC);
        os.writeBytes(HMAC);
    }

    public static void receiveMAC(Socket socket, SecretKeySpec MACKey, ByteArrayOutputStream os) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] HMAC_received = Sup.receiveBytes(socket);
        byte[] HMAC = Sup.HMAC(MACKey.getEncoded(), os.toByteArray());

        if (!Sup.equals(HMAC_received, HMAC)) {
            socket.close();
            System.exit(1);
        }
        os.writeBytes(HMAC_received);
    }

    public static byte[] encrypt(byte[] message, SecretKeySpec key, IvParameterSpec IV) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, key, IV);

        return cipher.doFinal(message);
    }

    public static byte[] decrypt(byte[] encrypted, SecretKeySpec key, IvParameterSpec IV) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, IV);
        return cipher.doFinal(encrypted);
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.writeBytes(a);
        bos.writeBytes(b);
        return bos.toByteArray();
    }

    public static void sendEncrypted(Socket socket, byte[] toBeSent, SecretKeySpec key, IvParameterSpec IV) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int chunkSize = 1000;
        int numOfChunks = (int) Math.ceil(toBeSent.length / (double)chunkSize);
        Sup.sendInt(socket, numOfChunks);

        for (int i = 0; i < numOfChunks; i++) {
            byte[] messageBytes = Arrays.copyOfRange(toBeSent, i * chunkSize, (i + 1) * chunkSize);
            byte[] HMAC = Sup.HMAC(key.getEncoded(), messageBytes);
            byte[] concatenatedBytes = Sup.concatenate(messageBytes, HMAC);
            byte[] encryptedBytes = Sup.encrypt(concatenatedBytes, key, IV);
            Sup.sendBytes(socket, encryptedBytes);
        }
    }

    public static byte[] receiveEncrypted(Socket socket, SecretKeySpec key, IvParameterSpec IV) throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int numOfChunks = Sup.receiveInt(socket);

        for (int i = 0; i < numOfChunks; i++) {
            byte[] encrypted = Sup.receiveBytes(socket);
            byte[] original = Sup.decrypt(encrypted, key, IV);
            byte[] message = new byte[original.length - 32];
            byte[] HMAC_received = new byte[32];
            System.arraycopy(original, 0, message, 0, original.length - 32);
            System.arraycopy(original, original.length-32, HMAC_received, 0, 32);
            byte[] HMAC = Sup.HMAC(key.getEncoded(), message);

            if (!Sup.equals(HMAC, HMAC_received)) {
                socket.close();
                System.exit(1);
            }
            bos.write(message);
        }
        return bos.toByteArray();
    }

    public static BigInteger computeDHPubKey(BigInteger g, BigInteger K, BigInteger N) {
        return g.modPow(K, N);
    }

    public static BigInteger generateDHPrivateKey() {
        Random rnd = new Random();
        BigInteger K = new BigInteger(2048, rnd);
        return K;
    }
}
