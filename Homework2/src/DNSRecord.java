import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class DNSRecord {

    private String[] answers;
    private byte[] Type = new byte[2];
    private byte[] Class = new byte[2];
    private byte[] TTL = new byte[4];
    private byte[] RDLENGTH = new byte[2];
    private byte[] RDDATA;
    Calendar cal;

    static DNSRecord decodeRecord(InputStream in, DNSMessage message) throws IOException {
        DNSRecord output = new DNSRecord();
        output.cal = Calendar.getInstance();
        output.answers = message.readDomainName(in);
        in.read(output.Type);
        in.read(output.Class);
        in.read(output.TTL);
        long TTLInt = 0;
        for (int i = 0; i < output.TTL.length; i++) {
            TTLInt = (TTLInt << 8) + (output.TTL[i] & 0xff);
        }
        output.cal.add(Calendar.SECOND, (int) TTLInt);
        in.read(output.RDLENGTH);
        int len = getInt(output.RDLENGTH);
        output.RDDATA = new byte[len];
        in.read(output.RDDATA);

        return output;
    }


    void writeBytes(ByteArrayOutputStream out, HashMap<String, Integer> map) throws IOException {
        DNSMessage.writeDomainName(out, map, answers);
        out.write(Type);
        out.write(Class);
        out.write(TTL);
        out.write(RDLENGTH);
        out.write(RDDATA);
    }


    public String toString(){
        StringBuilder output=new StringBuilder();
        output.append("DNSRecord [answers=");
        output.append(Arrays.toString(answers));
        output.append(", Type=");
        output.append(Arrays.toString(Type));
        output.append(", Class=");
        output.append(Arrays.toString(Class));
        output.append(", TTL=");
        output.append(Arrays.toString(TTL));
        output.append(", RDLENGTH=");
        output.append(Arrays.toString(RDLENGTH));
        output.append(", RDDATA=");
        output.append(Arrays.toString(RDDATA));
        output.append(", creation=");
        output.append(cal);
        output.append("]");
        return output.toString();
    }

    /**
     * return whether the creation date + the time to live is after the current time.
     * The Date and Calendar classes will be useful for this
     * @return
     */
    boolean timestampValid(){
        return cal.after(Calendar.getInstance());
    }


    private static int getInt(byte[] arr) {
        int ret = 0;
        ret |= arr[0] << 8;
        ret |= arr[1];
        return ret;
    }
}