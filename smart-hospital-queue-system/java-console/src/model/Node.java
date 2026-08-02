package model;

/**
 * Node structure for the Doubly Linked List.
 * Simple wrapper class for Patient data and memory pointers.
 */
public class Node {
    private Patient patient;
    private Node prev;
    private Node next;

    public Node(Patient patient) {
        this.patient = patient;
        this.prev = null;
        this.next = null;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
