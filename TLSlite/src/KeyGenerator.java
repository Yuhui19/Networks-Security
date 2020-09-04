import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyGenerator {
    static byte[] hkdfExpand(byte[] input, String tag)
            throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        byte[] temp = Arrays.copyOf(tag.getBytes(), tag.length() + 1);
        temp[temp.length - 1] = 1;
        byte[] ret = Util.HMAC(input, temp);
        return Arrays.copyOf(ret, 16);
    }

    static SessionKey makeSecretKey(byte[] clientNonce, BigInteger DHSharedSecret)
            throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {

        byte[] prk = Util.HMAC(clientNonce, DHSharedSecret.toByteArray());
        byte[] serverEncrypt = hkdfExpand(prk, "server encrypt");
        byte[] clientEncrypt = hkdfExpand(serverEncrypt, "client encrypt");
        byte[] serverMACKey = hkdfExpand(clientEncrypt, "server MAC");
        byte[] clientMACKey = hkdfExpand(serverMACKey, "client MAC");
        byte[] serverIV = hkdfExpand(clientMACKey, "server IV");
        byte[] clientIV = hkdfExpand(serverIV, "client IV");

        SecretKeySpec serverEncrypt_ = new SecretKeySpec(serverEncrypt, "AES");
        SecretKeySpec clientEncrypt_ = new SecretKeySpec(clientEncrypt, "AES");
        Mac serverMAC = Mac.getInstance("HmacSHA256");
        serverMAC.init(new SecretKeySpec(serverMACKey, "HmacSHA256"));
        Mac clientMAC = Mac.getInstance("HmacSHA256");
        clientMAC.init(new SecretKeySpec(clientMACKey, "HmacSHA256"));
        IvParameterSpec serverIV_ = new IvParameterSpec(serverIV);
        IvParameterSpec clientIV_ = new IvParameterSpec(clientIV);

        return new SessionKey(serverEncrypt_, clientEncrypt_, serverMAC, clientMAC, serverIV_, clientIV_);
    }

}
