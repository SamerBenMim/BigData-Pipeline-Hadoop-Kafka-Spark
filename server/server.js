const express = require('express');
const app = express();
const mongoose = require('mongoose');
const WebSocket = require('ws');


// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Connect to MongoDB using Mongoose
const url = 'mongodb+srv://sborcheni:XHJJVDb8SrAOfmig@cluster0.ymh6fip.mongodb.net/BigData?retryWrites=true&w=majority';
mongoose.connect(url)
  .then(() => console.log('Connected to MongoDB'))
  .catch((err) => console.error('Error connecting to MongoDB:', err));

// Define a schema and a model
const mySchema = new mongoose.Schema({
  time: Number,
  id: Number,
  vwap: Number,
});

const MyModel = mongoose.model('samer', mySchema, 'samer');

// Routes
app.get('/', (req, res) => {
  res.send('server is up');
});

// Start server
const port = 3000;
const server = app.listen(port, () => {
  console.log(`Server started on port ${port}`);
});

// Set up a change stream using Mongoose
const changeStream = MyModel.watch();
const ws = new WebSocket.Server({server });

changeStream.on('change', (change) => {
  console.log('Change stream event:', change);
wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(change));
    }
  }
);
});

