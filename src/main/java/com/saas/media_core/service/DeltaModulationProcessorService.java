package com.saas.media_core.service;

import org.springframework.stereotype.Service;

@Service
public class DeltaModulationProcessorService {

    public byte[] compress(byte[] inputBytes) {
        if (inputBytes == null || inputBytes.length == 0) return new byte[0];
        
        int numBits = inputBytes.length;
        int numBytes = (numBits + 7) / 8;
        byte[] output = new byte[numBytes];
        
        int prevVal = 0;
        for (int i = 0; i < numBits; i++) {
            int currVal = inputBytes[i] & 0xFF;
            int bit = (currVal >= prevVal) ? 1 : 0;
            prevVal = currVal;
            
            int byteIdx = i / 8;
            int bitIdx = 7 - (i % 8);
            if (bit == 1) {
                output[byteIdx] |= (1 << bitIdx);
            }
        }
        return output;
    }
}