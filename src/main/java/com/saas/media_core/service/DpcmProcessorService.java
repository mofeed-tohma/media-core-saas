package com.saas.media_core.service;

import org.springframework.stereotype.Service;

@Service
public class DpcmProcessorService {

    
    public byte[] compress(byte[] inputBytes) {
        if (inputBytes == null || inputBytes.length == 0) {
            return new byte[0];
        }

        byte[] compressed = new byte[inputBytes.length];
        
        
        compressed[0] = inputBytes[0];
        
        
        for (int i = 1; i < inputBytes.length; i++) {
            
            int diff = inputBytes[i] - inputBytes[i - 1];
            
            
            compressed[i] = (byte) diff;
        }
        
        return compressed;
    }
}