package com.example.myapplication;

import java.util.ArrayList;

public class RandomAccessQueue<T> {
    private ArrayList<T> list = new ArrayList<>();
    private int MAX_BUFFER = 121;
    private int PACKET_SIZE = 36;
    private int head = 0;

    public void enqueue(T item) {

        list.add(item);
        if (this.size() >= this.MAX_BUFFER)
            this.dequeue();
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        T item = list.get(head);
        head++;
        // Remove elements that have been dequeued to avoid memory leak
        if (head > list.size() / 2) {
            list.subList(0, head).clear();
            head = 0;
        }
        return item;
    }
    void dequeue(int index) {
        if (head + index < list.size())
            head += index;
        else if(head + index > list.size() && list.size() > 0)
            head = list.size() - 1;
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return list.get(head);
    }

    public boolean isEmpty() {
        return head == list.size();
    }

    public T get(int index) {
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("Index out of range");
        }
        return list.get(head + index);
    }

    public String getPacket() {
        String data = "";
        for (int i = head; i < head + PACKET_SIZE; i++) {
            data += String.valueOf(list.get(i));
        }
        return data;
    }
    public String getString() {
        String data = "";
        for (int i = head; i < list.size(); i++) {
            data += String.valueOf(list.get(i));
        }
        return data;
    }

    public String getHeader() {
        String data = "";
        for(int i = head; i < head + 6; i++)
            data += String.valueOf(list.get(i));
        return data;
    }

    public int size() {
        return list.size() - head;
    }
}
