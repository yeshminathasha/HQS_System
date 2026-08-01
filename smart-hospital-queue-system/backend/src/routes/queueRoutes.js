const express = require('express');
const { getQueue, enqueuePatient, dequeuePatient } = require('../controllers/queueController');

const router = express.Router();

router.get('/', getQueue);
router.post('/', enqueuePatient);
router.delete('/', dequeuePatient);

module.exports = router;
