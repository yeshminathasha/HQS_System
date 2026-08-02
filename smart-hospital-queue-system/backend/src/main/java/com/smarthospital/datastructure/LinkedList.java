package com.smarthospital.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class LinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public Node<T> head() {
        return head;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public LinkedList<T> addLast(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        return this;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        T value = head.value;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return value;
    }

    public T first() {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return head.value;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    public List<T> toList() {
        List<T> values = new ArrayList<>(size);
        for (Node<T> current = head; current != null; current = current.next) {
            values.add(current.value);
        }
        return values;
    }
}