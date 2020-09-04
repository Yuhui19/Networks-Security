//
//  main.cpp
//  ProgrammingPart1
//
//  Created by Yuhui on 3/10/20.
//  Copyright © 2020 Yuhui. All rights reserved.
//

#include <iostream>
#include <vector>

std::vector<uint8_t> generateKey(std::string password){
    std::vector<uint8_t> key(8, 0);
    
    for(int i=0;i<password.size(); i++){
        key[i%8] = key[i%8]^password[i];
    }
    return key;
}

std::vector<uint8_t> fisherYatesShuffle(std::vector<uint8_t> prev){
    for(long i=prev.size()-1; i>0; i--){
        int j=rand() % (i+1);
        std::swap(prev[i], prev[j]);
    }
    return prev;
}

std::vector<std::vector<uint8_t>> getSubstitutionTable(){
    std::vector<std::vector<uint8_t>> substitutionTable;
    
    std::vector<uint8_t> first;
    for(int i=0; i<256;i++){
        first.push_back(i);
    }
    
    substitutionTable.push_back(first);
    
    for(int i=0;i<7; i++){
        std::vector<uint8_t> shuffledTable = fisherYatesShuffle(substitutionTable[i]);
        substitutionTable.push_back(shuffledTable);
    }
    return substitutionTable;
}

uint64_t arrayToInt(std::vector<uint8_t>& message){
    uint64_t output=0;
    
    for(int i=0;i<8;i++){
        output+=(uint64_t)message[i]<< (7-i)*8;
    }
    return output;
}

std::vector<uint8_t> intToArray(uint64_t input){
    std::vector<uint8_t> output;
    
    for(int i=0;i<8;i++){
        uint8_t byte= input<<(i*8)>>56;
        output.push_back(byte);
    }
    return output;
}

std::vector<uint8_t> leftShift1Bit(std::vector<uint8_t>& message) {
    uint64_t number = arrayToInt(message);
    
    uint64_t firstBit = number >> 63;
    
    uint64_t shiftedNumber = (number << 1) + firstBit;
    
    return intToArray(shiftedNumber);
}

std::vector<uint8_t> rightShift1Bit(std::vector<uint8_t>& message) {
    uint64_t number = arrayToInt(message);
    
    uint64_t lastBit = number & 0x1;
    
    uint64_t shiftedNumber = (number >> 1) + (lastBit << 63);
    
    return intToArray(shiftedNumber);
}

std::vector<uint8_t> encrypt(std::vector<uint8_t> message,
                             std::vector<uint8_t>& key,
                             std::vector<std::vector<uint8_t>>& substitutionTables) {
    for (int round = 0; round < 16; round++) {
        
        for (int i = 0; i < message.size(); i++) {
            message[i] = message[i] ^ key[i];
        }
        
        
        for (int i = 0; i < message.size(); i++) {
            message[i] = substitutionTables[i][message[i]];
        }
        
        
        message = leftShift1Bit(message);
    }
    
    return message;
}

std::vector<uint8_t> decrypt(std::vector<uint8_t> message,
                             std::vector<uint8_t>& key,
                             std::vector<std::vector<uint8_t>>& substitutionTables) {
    for (int round = 0; round < 16; round++) {
        message = rightShift1Bit(message);
        
        for (int i = 0; i < message.size(); i++) {
            message[i] = std::distance(substitutionTables[i].begin(),
                                       std::find(substitutionTables[i].begin(), substitutionTables[i].end(), message[i]));
        }
        
        for (int i = 0; i < message.size(); i++) {
            message[i] = message[i] ^ key[i];
        }
    }
    
    return message;
}

int main(int argc, const char * argv[]) {
    
    srand((unsigned)time(nullptr));
    int ITERATION = 300;
    
    std::string right_password = "justForTest";
    std::vector<uint8_t> right_key = generateKey(right_password);
    
    std::string wrong_password = "justfortest";
    std::vector<uint8_t> wrong_key = generateKey(wrong_password);
    
    for (int round = 0; round < ITERATION; round++) {
        std::vector<std::vector<uint8_t>> substitutionTables = getSubstitutionTable();
        
        std::vector<uint8_t> message;
        for (int i = 0; i < 8; i++) {
            message.push_back(rand() % 255);
        }
        
        std::vector<uint8_t> encrypted_message = encrypt(message, right_key, substitutionTables);
        
        std::vector<uint8_t> right_decrypted_message = decrypt(encrypted_message, right_key, substitutionTables);
        for (int i = 0; i < 8; i++) {
            assert(message[i] == right_decrypted_message[i]);
        }
        
        std::vector<uint8_t> wrong_decrypted_message = decrypt(encrypted_message, wrong_key, substitutionTables);
        
        assert(arrayToInt(message) != arrayToInt(wrong_decrypted_message));
    }
    
    // insert code here...
    std::cout << "Hello, World!\n";
    return 0;
}
