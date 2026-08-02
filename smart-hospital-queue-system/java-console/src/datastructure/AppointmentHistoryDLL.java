package datastructure;

import model.Node;
import model.Patient;

/**
 * Append-only Doubly Linked List to record appointment history.
 * Nodes are never deleted, just appended to the tail.
 */
public class AppointmentHistoryDLL {
    private Node head;
    private Node tail;
    private int size;

    public AppointmentHistoryDLL() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addHistory(Patient p) {
        Node newNode = new Node(p);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size++;
    }

    public Patient searchById(String patientId) {
        Node current = head;
        while (current != null) {
            if (current.getPatient().getPatientId().equalsIgnoreCase(patientId)) {
                return current.getPatient();
            }
            current = current.getNext();
        }
        return null;
    }

    public Patient[] getAllHistoryForward() {
        Patient[] arr = new Patient[size];
        Node current = head;
        int i = 0;
        while (current != null) {
            arr[i++] = current.getPatient();
            current = current.getNext();
        }
        return arr;
    }

    public Patient[] getAllHistoryReverse() {
        Patient[] arr = new Patient[size];
        Node current = tail;
        int i = 0;
        while (current != null) {
            arr[i++] = current.getPatient();
            current = current.getPrev();
        }
        return arr;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
