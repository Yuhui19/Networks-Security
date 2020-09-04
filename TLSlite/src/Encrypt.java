import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
    static byte[] encryption(byte[] input, IvParameterSpec ivParameterSpec, Mac mac, SecretKeySpec key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        byte[] keyBytes = mac.doFinal(input);
        byte[] concatenated = new byte[input.length + keyBytes.length];
        System.arraycopy(input, 0, concatenated, 0, input.length);
        System.arraycopy(keyBytes, 0, concatenated, input.length, keyBytes.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(concatenated);
        return encrypted;
    }
}
