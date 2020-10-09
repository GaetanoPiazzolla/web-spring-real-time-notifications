
// IE compatible JS

var wsController = {};

var headers = {
  'authorization': 'jwt-token',
};

wsController._onConnected = function(frame) {
  this.setConnected(true);
  console.log('Connected: ' + frame);
  this.stompClient.subscribe( '/topic/pzzgtn' , this.showMessage, headers);
};

wsController.setConnected = function(connected) {
  document.getElementById('connect').disabled = connected;
  document.getElementById('disconnect').disabled = !connected;
  document.getElementById('mural').style.visibility = connected ? 'visible' : 'hidden';
  document.getElementById('response').innerHTML = '';
};

wsController.connect = function() {
  var socket = new SockJS('https://localhost:9999/ws-socket-alert',null);
  this.stompClient = Stomp.over(socket);

  // TODO in authorization we will put JWT token, user and password will be set by backend
  this.stompClient.connect(headers, this._onConnected);

  this._onConnected = this._onConnected.bind(this);

};

wsController.disconnect = function() {
  if(this.stompClient != null) {
    this.stompClient.disconnect();
  }
  this.setConnected(false);
  console.log("Disconnected");
};

wsController.sendMessage = function() {
  var message = document.getElementById('text').value;
  this.stompClient.send("/topic/pzzgtn", {}, message);
};

wsController.showMessage = function(message) {
  var response = document.getElementById('response');
  var p = document.createElement('p');
  p.style.wordWrap = 'break-word';
  p.appendChild(document.createTextNode(message.body));
  response.appendChild(p);
};

wsController._onConnected = wsController._onConnected.bind(wsController);
