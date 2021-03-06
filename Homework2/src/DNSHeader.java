import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DNSHeader {

    private byte[] header=new byte[2];
    private byte[] flag=new byte[2];
    private byte[] QDCOUNT=new byte[2];
    private byte[] ANCOUNT=new byte[2];
    private byte[] NSCOUNT=new byte[2];
    private byte[] ARCOUNT=new byte[2];

    /**
     * read the header from an input stream (we'll use a ByteArrayInputStream but
     * we will only use the basic read methods of input stream to read 1 byte,
     * or to fill in a byte array, so we'll be generic)
     * @return
     */
    static DNSHeader decodeHeader(InputStream in) throws IOException {
        DNSHeader output=new DNSHeader();
        in.read(output.header);
        in.read(output.flag);
        in.read(output.QDCOUNT);
        in.read(output.ANCOUNT);
        in.read(output.NSCOUNT);
        in.read(output.ARCOUNT);
        return output;
    }

    /**
     *This will create the header for the response. It will copy some fields from the request
     */
    static DNSHeader buildResponseHeader(DNSMessage request, DNSMessage response){
        DNSHeader server=new DNSHeader();

        server.header=request.getHeader().header;
        server.flag[1] = request.getHeader().flag[1];
        server.QDCOUNT = request.getHeader().QDCOUNT;
        server.ANCOUNT = toByte(request.getHeader().getQDCOUNT());
        server.NSCOUNT = toByte(request.getHeader().getNSCOUNT());
        server.ARCOUNT = toByte(request.getHeader().getARCOUNT());
        return server;
    }

    /**
     * encode the header to bytes to be sent back to the client.
     * The OutputStream interface has methods to write a single byte
     * or an array of bytes
     */
    void writeBytes(OutputStream out) throws IOException {
        out.write(header);
        out.write(flag);
        out.write(QDCOUNT);
        out.write(ANCOUNT);
        out.write(NSCOUNT);
        out.write(ARCOUNT);
    }


    /**
     * Return a human readable string version of a header object.
     * A reasonable implementation can be autogenerated by your IDE
     * @return
     */
    public String toString(){
        StringBuilder output=new StringBuilder();
        output.append("DNSHeader [headerID=");
        output.append(Arrays.toString(header));
        output.append(", flag=");
        output.append(Arrays.toString(flag));
        output.append(", QDCOUNT=");
        output.append(Arrays.toString(QDCOUNT));
        output.append(", ANCOUNT=");
        output.append(Arrays.toString(ANCOUNT));
        output.append(", NSCOUNT=");
        output.append(Arrays.toString(NSCOUNT));
        output.append(", ARCOUNT=");
        output.append(Arrays.toString(ARCOUNT));
        output.append("]");
        return output.toString();
    }


    private int getInteger(byte[] b){
        int output=0;
        output |= b[0]<<8;
        output |=b[1];
        return output;
    }

    private static byte[] toByte(int i){
        byte[] output=new byte[2];
        output[0]=(byte) (i>>8);
        output[1]=(byte) (i);
        return output;
    }

    public int getANCOUNT() {
        return getInteger(ANCOUNT);
    }

    public int getQDCOUNT() {
        return getInteger(QDCOUNT);
    }

    public int getNSCOUNT() {
        return getInteger(NSCOUNT);
    }

    public int getARCOUNT() {
        return getInteger(ARCOUNT);
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getFlag() {
        return flag;
    }
}
