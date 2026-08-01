const test = require('node:test');
const assert = require('node:assert/strict');
const DoublyLinkedList = require('../src/dataStructures/DoublyLinkedList');

test('append and toArray work correctly', () => {
  const list = new DoublyLinkedList();
  list.append(1);
  list.append(2);
  assert.deepEqual(list.toArray(), [1, 2]);
});

test('remove deletes the matching node', () => {
  const list = new DoublyLinkedList();
  list.append('a');
  list.append('b');
  assert.equal(list.remove('a'), true);
  assert.deepEqual(list.toArray(), ['b']);
});
