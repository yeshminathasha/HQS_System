const DoublyLinkedList = require('../dataStructures/DoublyLinkedList');

const queue = new DoublyLinkedList();

exports.getQueue = (req, res) => {
  res.json(queue.toArray());
};

exports.enqueuePatient = (req, res) => {
  const patient = req.body;
  queue.append(patient);
  res.status(201).json(patient);
};

exports.dequeuePatient = (req, res) => {
  const removed = queue.remove(queue.head?.value);
  if (!removed) {
    return res.status(404).json({ message: 'Queue is empty' });
  }
  res.json({ message: 'Patient removed from queue' });
};
