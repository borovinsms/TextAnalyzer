package org.example;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {

    public static ArrayBlockingQueue<String> aBlockingQueue = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> bBlockingQueue = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> cBlockingQueue = new ArrayBlockingQueue<>(100);

    public static ConcurrentMap<String, Integer> maxSequenceMap = new ConcurrentHashMap<>();

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static void iterateBlockingQueue(ArrayBlockingQueue<String> queue, char letter) {
        String text;
        while (true) {
            try {
                if ((text = queue.poll(100, MILLISECONDS)) == null) break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            findMaxSequence(text, letter);
        }
    }

    public static void findMaxSequence(String str, char letter) {
        char[] chars = str.toCharArray();
        int maxSequenceLength = 0;
        int currentSequenceLength = 0;
        for (char ch : chars) {
            if (ch == letter) {
                currentSequenceLength++;
            } else {
                if (currentSequenceLength > maxSequenceLength) {
                    maxSequenceLength = currentSequenceLength;
                }
                currentSequenceLength = 0;
            }
        }
        Integer currentMaxSequenceLength = maxSequenceMap.getOrDefault(String.valueOf(letter), 0);
        if (maxSequenceLength > currentMaxSequenceLength) {
            maxSequenceMap.put(String.valueOf(letter), maxSequenceLength);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread textGenerator = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    aBlockingQueue.put(text);
                    bBlockingQueue.put(text);
                    cBlockingQueue.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread aSequenceThread = new Thread(() -> {
            iterateBlockingQueue(aBlockingQueue, 'a');
        });
        Thread bSequenceThread = new Thread(() -> {
            iterateBlockingQueue(bBlockingQueue, 'b');

        });
        Thread cSequenceThread = new Thread(() -> {
            iterateBlockingQueue(cBlockingQueue, 'c');

        });

        textGenerator.start();
        aSequenceThread.start();
        bSequenceThread.start();
        cSequenceThread.start();

        textGenerator.join();
        aSequenceThread.join();
        bSequenceThread.join();
        cSequenceThread.join();

        for (Map.Entry<String, Integer> entry : maxSequenceMap.entrySet()) {
            System.out.format("Максимальное количество символов '%s': %d \n", entry.getKey(), entry.getValue());
        }
    }
}