import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {

    private byte[] message;
    private DNSHeader header;
    ArrayList<DNSQuestion> question = new ArrayList<>();
    ArrayList<DNSRecord> answer = new ArrayList<>();
    ArrayList<DNSRecord> authority = new ArrayList<>();
    ArrayList<DNSRecord> addition = new ArrayList<>();


    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage output=new DNSMessage();
        output.message=bytes;
        ByteArrayInputStream in=new ByteArrayInputStream(bytes);
        output.header=DNSHeader.decodeHeader(in);

        for(int i=0;i<output.header.getQDCOUNT();i++){
            output.question.add(DNSQuestion.decodeQuestion(in,output));
        }
        for(int i=0;i<output.header.getANCOUNT();i++){
            output.answer.add((DNSRecord.decodeRecord(in,output)));
        }
        for(int i=0;i<output.header.getNSCOUNT();i++){
            output.authority.add((DNSRecord.decodeRecord(in,output)));
        }
        for(int i=0;i<output.header.getARCOUNT();i++){
            output.addition.add((DNSRecord.decodeRecord(in,output)));
        }
        return output;
    }

    /**
     * read the pieces of a domain name starting from the current position of the input stream
     * @return
     */
    String[] readDomainName(InputStream in) throws IOException {
        byte[] length=new byte[1];

        if((length[0] & (1 << 7)) == 128 && (length[0] & (1 << 6)) == 64){
            int offset=0 | (length[0]&0x3f);
            offset<<=8;
            in.read(length);
            offset |= length[0];
            String[] output=readDomainName(offset);
            return output;
        }else {
            ArrayList<String> domainName=new ArrayList<>();
            while(length[0] != 0){
                byte[] label=new byte[(int) length[0]];
                in.read(label);
                String oneLable = new String(label);
                domainName.add(oneLable);
                in.read(length);
            }
            String[] output = new String[domainName.size()];
            for (int i = 0; i < domainName.size(); i++) {
                output[i] = domainName.get(i);
            }
            return output;
        }
    }

    /**
     * same, but used when there's compression and we need to find the domain from earlier in the message.
     * This method should make a ByteArrayInputStream
     * that starts at the specified byte and call the other version of this method
     * @param firstByte
     * @return
     */
    String[] readDomainName(int firstByte) throws IOException {
        return readDomainName(new ByteArrayInputStream(message, firstByte, message.length-firstByte));
    }

    /**
     * build a response based on the request and the answers you intend to send back
     * @param request
     * @param answers
     * @return
     */
    static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers){
        DNSMessage output=new DNSMessage();
        output.header=DNSHeader.buildResponseHeader(request,output);
        output.question=request.getQuestion();
        output.answer= answers;
        output.authority=request.getAuthority();
        output.addition=request.getAddition();
        return output;
    }

    /**
     * get the bytes to put in a packet and send back
     * @return
     */
    byte[] toBytes() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        header.writeBytes(output);
        HashMap<String, Integer> domainLocation = new HashMap<>();

        for (int i = 0; i < question.size(); i++) {
            question.get(i).writeBytes(output, domainLocation);
        }
        for (int i = 0; i < answer.size(); i++) {
            answer.get(i).writeBytes(output, domainLocation);
        }
        for (int i = 0; i < authority.size(); i++) {
            authority.get(i).writeBytes(output, domainLocation);
        }
        for (int i = 0; i < addition.size(); i++) {
            addition.get(i).writeBytes(output, domainLocation);
        }

        return output.toByteArray();
    }

    /**
     * If this is the first time we've seen this domain name in the packet,
     * write it using the DNS encoding (each segment of the domain prefixed with its length, 0 at the end),
     * and add it to the hash map. Otherwise,
     * write a back pointer to where the domain has been seen previously
     * @param domainLocations
     * @param domainPieces
     */
    static void writeDomainName(ByteArrayOutputStream out, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {

        String domain = octetsToString(domainPieces);

        if (!domainLocations.containsKey(domain)) {
            domainLocations.put(domain, out.size());
            for (String s : domainPieces) {
                out.write((byte) s.length());
                out.write(s.getBytes());
            }
        } else {
            int offset = domainLocations.get(domain);
            offset |= (3 << 12);
            byte[] temp = new byte[2];
            temp[0] = (byte) (0xc0 | (offset >> 8 & 0x3f00));
            temp[1] = (byte) (offset & 0xff);
            out.write(temp);
        }
    }

    /**
     * join the pieces of a domain name with dots ([ "utah", "edu"] -> "utah.edu" )
     * @param octets
     * @return
     */
    static String octetsToString(String[] octets){
        if(octets.length == 0) {
            return null;
        }
        String domainName = "";
        int i;
        for (i = 0; i < octets.length - 1; i++) {
            domainName = domainName + octets[i] + ".";
        }
        domainName += octets[i];
        System.out.println("octets " + domainName);
        return domainName;
    }


    public String toString(){
        StringBuilder output=new StringBuilder();
        output.append("DNSMessage [completeMsg=");
        output.append(Arrays.toString(message));
        output.append(", header=");
        output.append(header);
        output.append(", questions=");
        output.append(question);
        output.append(", answers=");
        output.append(answer);
        output.append(", authority=");
        output.append(authority);
        output.append(", additional=");
        output.append(addition);
        output.append("]");
        return output.toString();
    }

    public ArrayList<DNSQuestion> getQuestion() {
        return question;
    }

    public ArrayList<DNSRecord> getAddition() {
        return addition;
    }

    public ArrayList<DNSRecord> getAnswer() {
        return answer;
    }

    public ArrayList<DNSRecord> getAuthority() {
        return authority;
    }

    public byte[] getMessage() {
        return message;
    }

    public DNSHeader getHeader() {
        return header;
    }
}
