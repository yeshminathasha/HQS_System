package com.smarthospital.datastructure;

public class Node<T> {

    final T value;
    Node<T> next;

    public Node(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public Node<T> next() {
        return next;
    }
}