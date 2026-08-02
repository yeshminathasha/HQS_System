package datastructure;

import model.Node;
import model.Patient;

/**
 * Manual Doubly Linked List for the live patient queue.
 * Handles insertion (normal and emergency priority), deletion, and searching.
 */
public class PatientQueueDLL {
    private Node head;
    private Node tail;
    private int size;

    public PatientQueueDLL() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void insertPatient(Patient p) {
        Node newNode = new Node(p);
        if (p.isEmergency()) {
            insertEmergency(newNode);
        } else {
            // Append to tail for normal patients
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.setNext(newNode);
                newNode.setPrev(tail);
                tail = newNode;
            }
        }
        size++;
    }

    private void insertEmergency(Node newNode) {
        if (head == null) {
            head = newNode;
            tail = newNode;
            return;
        }

        Node current = head;
        // Traverse to find the correct insertion point.
        // We skip past existing emergency patients with HIGHER or EQUAL priority
        // Priority 1 is highest. So we skip nodes where node.priority <= new.priority (since lower number = higher priority).
        // Wait, priority 1 comes BEFORE priority 2.
        // So we skip as long as current is emergency AND current.priorityLevel <= newNode.priorityLevel.
        while (current != null && current.getPatient().isEmergency() && 
               current.getPatient().getPriorityLevel() <= newNode.getPatient().getPriorityLevel()) {
            current = current.getNext();
        }

        if (current == head) {
            // Insert at head
            newNode.setNext(head);
            head.setPrev(newNode);
            head = newNode;
        } else if (current == null) {
            // Insert at tail
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        } else {
            // Insert before current
            Node prevNode = current.getPrev();
            prevNode.setNext(newNode);
            newNode.setPrev(prevNode);
            newNode.setNext(current);
            current.setPrev(newNode);
        }
    }

    public Patient deletePatient(String patientId) {
        Node current = head;
        while (current != null) {
            if (current.getPatient().getPatientId().equalsIgnoreCase(patientId)) {
                if (current == head && current == tail) {
                    head = null;
                    tail = null;
                } else if (current == head) {
                    head = head.getNext();
                    head.setPrev(null);
                } else if (current == tail) {
                    tail = tail.getPrev();
                    tail.setNext(null);
                } else {
                    current.getPrev().setNext(current.getNext());
                    current.getNext().setPrev(current.getPrev());
                }
                size--;
                return current.getPatient();
            }
            current = current.getNext();
        }
        return null;
    }

    public Patient findById(String patientId) {
        Node current = head;
        while (current != null) {
            if (current.getPatient().getPatientId().equalsIgnoreCase(patientId)) {
                return current.getPatient();
            }
            current = current.getNext();
        }
        return null;
    }

    public Patient[] findByName(String name) {
        // First pass: count matches
        int matchCount = 0;
        Node current = head;
        while (current != null) {
            if (current.getPatient().getName().toLowerCase().contains(name.toLowerCase())) {
                matchCount++;
            }
            current = current.getNext();
        }

        // Second pass: fill array
        Patient[] matches = new Patient[matchCount];
        int index = 0;
        current = head;
        while (current != null) {
            if (current.getPatient().getName().toLowerCase().contains(name.toLowerCase())) {
                matches[index++] = current.getPatient();
            }
            current = current.getNext();
        }
        return matches;
    }

    public int getPosition(String patientId) {
        int pos = 0;
        Node current = head;
        while (current != null) {
            if (current.getPatient().getPatientId().equalsIgnoreCase(patientId)) {
                return pos;
            }
            pos++;
            current = current.getNext();
        }
        return -1;
    }

    public Patient[] getAllPatients() {
        Patient[] arr = new Patient[size];
        Node current = head;
        int i = 0;
        while (current != null) {
            arr[i++] = current.getPatient();
            current = current.getNext();
        }
        return arr;
    }

    public Patient[] getPatientsByDoctor(String doctorName) {
        int count = 0;
        Node current = head;
        while (current != null) {
            if (current.getPatient().getDoctorName().equalsIgnoreCase(doctorName)) {
                count++;
            }
            current = current.getNext();
        }

        Patient[] arr = new Patient[count];
        int i = 0;
        current = head;
        while (current != null) {
            if (current.getPatient().getDoctorName().equalsIgnoreCase(doctorName)) {
                arr[i++] = current.getPatient();
            }
            current = current.getNext();
        }
        return arr;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
