package com.smarthospital.datastructure;

public class DoublyLinkedNode<T> {

    final T value;
    DoublyLinkedNode<T> prev;
    DoublyLinkedNode<T> next;

    public DoublyLinkedNode(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public DoublyLinkedNode<T> prev() {
        return prev;
    }

    public DoublyLinkedNode<T> next() {
        return next;
    }
}