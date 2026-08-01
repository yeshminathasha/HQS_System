class Patient {
  constructor(id, name, priority = 'normal') {
    this.id = id;
    this.name = name;
    this.priority = priority;
  }
}

module.exports = Patient;
