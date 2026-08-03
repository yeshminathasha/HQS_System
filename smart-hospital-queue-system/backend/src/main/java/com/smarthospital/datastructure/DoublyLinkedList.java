package com.smarthospital.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DoublyLinkedList<T> {

    private DoublyLinkedNode<T> head;
    private DoublyLinkedNode<T> tail;
    private int size;

    public DoublyLinkedNode<T> head() {
        return head;
    }

    public DoublyLinkedNode<T> tail() {
        return tail;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public DoublyLinkedList<T> addLast(T value) {
        DoublyLinkedNode<T> node = new DoublyLinkedNode<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
        return this;
    }

    public DoublyLinkedList<T> addFirst(T value) {
        DoublyLinkedNode<T> node = new DoublyLinkedNode<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            head.prev = node;
            node.next = head;
            head = node;
        }
        size++;
        return this;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        T value = head.value;
        head = head.next;
        if (head == null) {
            tail = null;
        } else {
            head.prev = null;
        }
        size--;
        return value;
    }

    public T removeLast() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        T value = tail.value;
        tail = tail.prev;
        if (tail == null) {
            head = null;
        } else {
            tail.next = null;
        }
        size--;
        return value;
    }

    public T first() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
        return head.value;
    }

    public T last() {
        if (tail == null) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.value;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        DoublyLinkedNode<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    public List<T> toList() {
        List<T> values = new ArrayList<>(size);
        for (DoublyLinkedNode<T> current = head; current != null; current = current.next) {
            values.add(current.value);
        }
        return values;
    }

    public List<T> toReversedList() {
        List<T> values = new ArrayList<>(size);
        for (DoublyLinkedNode<T> current = tail; current != null; current = current.prev) {
            values.add(current.value);
        }
        return values;
    }
}