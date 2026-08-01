module.exports = (io) => {
  io.on('connection', (socket) => {
    console.log('Client connected to queue socket');

    socket.on('disconnect', () => {
      console.log('Client disconnected from queue socket');
    });
  });
};
