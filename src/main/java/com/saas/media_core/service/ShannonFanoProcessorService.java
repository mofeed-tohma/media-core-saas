package com.saas.media_core.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ShannonFanoProcessorService {

    public byte[] compress(String input) {
        if (input == null || input.isEmpty()) return new byte[0];
        
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        
        List<Node> nodes = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }
        
        nodes.sort((a, b) -> Integer.compare(b.freq, a.freq));
        
        Map<Character, String> codes = new HashMap<>();
        assignCodes(nodes, 0, nodes.size() - 1, "", codes);
        
        StringBuilder bitStream = new StringBuilder();
        for (char c : input.toCharArray()) {
            bitStream.append(codes.get(c));
        }
        
        return packBitsToBytes(bitStream.toString());
    }

    private static class Node {
        char ch;
        int freq;
        Node(char ch, int freq) { 
            this.ch = ch; 
            this.freq = freq; 
        }
    }

    private void assignCodes(List<Node> nodes, int start, int end, String currentCode, Map<Character, String> codes) {
        if (start == end) {
            codes.put(nodes.get(start).ch, currentCode.isEmpty() ? "0" : currentCode);
            return;
        }
        
        int total = 0;
        for (int i = start; i <= end; i++) total += nodes.get(i).freq;
        
        int sumLeft = 0;
        int split = start;
        for (int i = start; i <= end; i++) {
            sumLeft += nodes.get(i).freq;
            if (sumLeft >= total / 2) {
                split = i;
                break;
            }
        }

        
        if (split == end) {
            split = end - 1;
        }

        for (int i = start; i <= split; i++) {
            codes.put(nodes.get(i).ch, currentCode + "0");
        }
        for (int i = split + 1; i <= end; i++) {
            codes.put(nodes.get(i).ch, currentCode + "1");
        }

        if (start < split) assignCodes(nodes, start, split, currentCode + "0", codes);
        if (split + 1 < end) assignCodes(nodes, split + 1, end, currentCode + "1", codes);
    }

    private byte[] packBitsToBytes(String bitStr) {
        int len = (bitStr.length() + 7) / 8;
        byte[] bytes = new byte[len + 4];
        for (int i = 0; i < bitStr.length(); i++) {
            if (bitStr.charAt(i) == '1') {
                int byteIdx = i / 8 + 4;
                int bitIdx = 7 - (i % 8);
                bytes[byteIdx] |= (1 << bitIdx);
            }
        }
        return bytes;
    }
}