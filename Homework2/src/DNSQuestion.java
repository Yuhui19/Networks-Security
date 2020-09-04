import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public class DNSQuestion {

    private String[] QNAME;
    private byte[] QTYPE=new byte[2];
    private byte[] QCLASS=new byte[2];

    /**
     * read a question from the input stream. Due to compression,
     * you may have to ask the DNSMessage containing this questino
     * to read some of the fields
     * @return
     */
    static DNSQuestion decodeQuestion(InputStream in, DNSMessage message) throws IOException {
        DNSQuestion output=new DNSQuestion();
        output.QNAME=message.readDomainName(in);
        in.read(output.QTYPE);
        in.read(output.QCLASS);
        return output;
    }

    /**
     * Write the question bytes which will be sent to the client.
     * The hash map is used for us to compess the message,
     * see the DNSMessage class below
     * @param domainNameLocations
     */
    void writeBytes(ByteArrayOutputStream out, HashMap<String,Integer> domainNameLocations) throws IOException {
        DNSMessage.writeDomainName(out, domainNameLocations,QNAME);
        byte[] last={(byte)0};
        out.write(last);
        out.write(QTYPE);
        out.write(QCLASS);
    }

    public String toString() {
        return "DNSQuestion [QNAME=" + Arrays.toString(QNAME) + ", QTYPE=" + Arrays.toString(QTYPE) + ", QCLASS="
                + Arrays.toString(QCLASS) + "]";
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(QCLASS);
        result = prime * result + Arrays.hashCode(QNAME);
        result = prime * result + Arrays.hashCode(QTYPE);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DNSQuestion other = (DNSQuestion) obj;
        if (!Arrays.equals(QCLASS, other.QCLASS))
            return false;
        if (!Arrays.equals(QNAME, other.QNAME))
            return false;
        if (!Arrays.equals(QTYPE, other.QTYPE))
            return false;
        return true;
    }

}
