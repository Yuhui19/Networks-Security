import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SessionKey {
    SecretKeySpec serverEncrypt_;
    SecretKeySpec clientEncrypt_;
    Mac serverMAC_;
    Mac clientMAC_;
    IvParameterSpec serverIV_;
    IvParameterSpec clientIV_;

    public SessionKey(SecretKeySpec serverEncrypt, SecretKeySpec clientEncrypt, Mac serverMAC, Mac clientMAC,
                      IvParameterSpec serverIV, IvParameterSpec clientIV) {
        serverEncrypt_ = serverEncrypt;
        clientEncrypt_ = clientEncrypt;
        serverMAC_ = serverMAC;
        clientMAC_ = clientMAC;
        serverIV_ = serverIV;
        clientIV_ = clientIV;
    }
}
