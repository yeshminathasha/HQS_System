package com.smarthospital.datastructure;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTest {

    @Test
    void maintainsFifoOrderOnAddAndRemove() {
        LinkedList<String> queue = new LinkedList<>();
        assertTrue(queue.isEmpty());

        queue.addLast("P001").addLast("P002").addLast("P003");

        assertEquals(3, queue.size());
        assertEquals("P001", queue.first());
        assertEquals("P001", queue.removeFirst());
        assertEquals("P002", queue.removeFirst());
        assertEquals(1, queue.size());
        assertEquals("P003", queue.first());
        assertEquals(List.of("P003"), queue.toList());
    }

    @Test
    void getReturnsValueAtIndex() {
        LinkedList<Integer> queue = new LinkedList<>();
        for (int i = 1; i <= 5; i++) {
            queue.addLast(i);
        }
        assertEquals(3, queue.get(2));
        assertEquals(5, queue.get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> queue.get(9));
        assertThrows(IndexOutOfBoundsException.class, () -> queue.get(-1));
    }

    @Test
    void removeFirstFromEmptyQueueThrows() {
        assertThrows(NoSuchElementException.class, () -> new LinkedList<Object>().removeFirst());
    }

    @Test
    void canTraverseFromHeadUsingNodes() {
        LinkedList<String> queue = new LinkedList<>();
        queue.addLast("A").addLast("B");
        Node<String> node = queue.head();
        assertEquals("A", node.value());
        assertEquals("B", node.next().value());
    }
}