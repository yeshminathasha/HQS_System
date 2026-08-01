const DoublyLinkedList = require('../dataStructures/DoublyLinkedList');

class QueueService {
  constructor() {
    this.queue = new DoublyLinkedList();
  }

  enqueue(patient) {
    this.queue.append(patient);
  }

  dequeue() {
    if (!this.queue.head) return null;
    const removed = this.queue.head.value;
    this.queue.remove(removed);
    return removed;
  }

  list() {
    return this.queue.toArray();
  }
}

module.exports = QueueService;
