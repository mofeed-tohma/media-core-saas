package com.saas.media_core.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LzwProcessorService {

    
    public byte[] compress(String inputData) {
        
        int dictSize = 256;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();
        
        
        for (char c : inputData.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                
                dictionary.put(wc, dictSize++);
                w = String.valueOf(c);
            }
        }
        
        
        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }

        
        return convertListToByteArray(result);
    }

    
    private byte[] convertListToByteArray(List<Integer> compressedData) {
        StringBuilder sb = new StringBuilder();
        for (Integer code : compressedData) {
            sb.append(code).append(",");
        }
        return sb.toString().getBytes();
    }
}