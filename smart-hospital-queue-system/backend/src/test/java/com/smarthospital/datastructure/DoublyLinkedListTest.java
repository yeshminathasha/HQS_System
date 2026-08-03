package com.smarthospital.datastructure;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DoublyLinkedListTest {

    @Test
    void addAndRemoveFromBothEnds() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        assertTrue(list.isEmpty());

        list.addLast("A").addLast("B");
        list.addFirst("Z");

        assertEquals(3, list.size());
        assertEquals("Z", list.first());
        assertEquals("B", list.last());
        assertEquals("Z", list.removeFirst());
        assertEquals("B", list.removeLast());
        assertEquals("A", list.first());
        assertEquals("A", list.last());
        assertEquals(List.of("A"), list.toList());
    }

    @Test
    void traversalWorksInBothDirections() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("A").addLast("B").addLast("C");

        assertEquals("A", list.head().value());
        assertEquals("B", list.head().next().value());
        assertEquals("C", list.head().next().next().value());
        assertEquals("C", list.tail().value());
        assertEquals("B", list.tail().prev().value());

        assertEquals(List.of("A", "B", "C"), list.toList());
        assertEquals(List.of("C", "B", "A"), list.toReversedList());
    }

    @Test
    void getReturnsValueAtIndex() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        for (int i = 1; i <= 5; i++) {
            list.addLast(i);
        }
        assertEquals(3, list.get(2));
        assertEquals(5, list.get(4));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(9));
    }

    @Test
    void removingFromEmptyListThrows() {
        DoublyLinkedList<Object> list = new DoublyLinkedList<>();
        assertThrows(NoSuchElementException.class, list::removeFirst);
        assertThrows(NoSuchElementException.class, list::removeLast);
    }

    @Test
    void removingAllEmptiesTheList() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("A").addLast("B");
        list.removeFirst();
        list.removeLast();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertEquals(List.of(), list.toList());
    }
}