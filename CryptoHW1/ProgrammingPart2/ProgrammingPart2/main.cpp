//
//  main.cpp
//  ProgrammingPart2
//
//  Created by Yuhui on 3/11/20.
//  Copyright © 2020 Yuhui. All rights reserved.
//

#include <iostream>
#include <vector>

class RC4Cypher{
    int i;
    int j;
    uint8_t arr[256];
    
public:
    RC4Cypher(std::vector<uint8_t> key){
        for(int p=0;p<256;p++){
            arr[p]=p;
        }
        j=0;
        for( i=0;i<256;i++){
            j=(j+arr[i]+key[i%key.size()]) % 256;
            std::swap(arr[i], arr[j]);
        }
        i=0;
        j=0;
    }
    
    uint8_t nextByte(){
        i=(i+1)%256;
        j=(j+arr[i])%256;
        std::swap(arr[i], arr[j]);
        return arr[(arr[i]+arr[j]) % 256];
    }
};

int main(int argc, const char * argv[]) {
    
    std::string message = "justfortest";
    
    std::vector<uint8_t> right_key, wrong_key;
    int keyLength = 10;
    for (int i = 0; i < keyLength; i++) {
        right_key.push_back(rand() % 256);
        wrong_key.push_back(rand() % 256);
    }
    
    RC4Cypher encrypt_cypher(right_key);
    
    std::string encrypted_message = "";
    for (int i = 0; i < message.size(); i++) {
        encrypted_message += message[i] ^ encrypt_cypher.nextByte();
    }

    RC4Cypher right_decrypt_cypher(right_key);
    std::string right_decrypted_message = "";
    for (int i = 0; i < message.size(); i++) {
        right_decrypted_message += encrypted_message[i] ^ right_decrypt_cypher.nextByte();
    }
    
    assert(message == right_decrypted_message);

    RC4Cypher wrong_decrypt_cypher(wrong_key);
    std::string wrong_decrypted_message = "";
    for (int i = 0; i < message.size(); i++) {
        wrong_decrypted_message += encrypted_message[i] ^ wrong_decrypt_cypher.nextByte();
    }
    
    assert(message != wrong_decrypted_message);

    std::string message1 = "hello";
    std::string message2 = "world";
    RC4Cypher insecure_cypher(right_key);
    
    for (int i = 0; i < message1.size(); i++) {
        uint8_t nextByte = insecure_cypher.nextByte();
        assert(((message1[i] ^ nextByte) ^ (message2[i] ^ nextByte)) == (message1[i] ^ message2[i]));
    }
    
    std::string original_message = "Your age is 20";
    std::string target_message = "Your age is 30";
    std::string encrypt_message = "";
    std::string modified_encrypted_message;
    std::string modified_message = "";
    RC4Cypher modify_cypher(right_key);
   
    for (int i = 0; i < original_message.size(); i++) {
        encrypt_message += original_message[i] ^ modify_cypher.nextByte();
    }

    for (int i = 0; i < original_message.size(); i++) {
        modified_encrypted_message[i] = encrypt_message[i] ^ (original_message[i] ^ target_message[i]);
    }
    
    RC4Cypher decrypt_cypher(right_key);
    for (int i = 0; i < original_message.size(); i++) {
        modified_message += modified_encrypted_message[i] ^ decrypt_cypher.nextByte();
    }
    
    assert(modified_message == target_message);

    // insert code here...
    std::cout << "Hello, World!\n";
    return 0;
}
