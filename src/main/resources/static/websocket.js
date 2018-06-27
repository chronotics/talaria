var ws;

function connect(event) {
	ws = new WebSocket("ws://localhost:8080/sockJS");
	
	ws.onerror = function(event) {
		onError(event);
	};

	ws.onmessage = function(event) {
		onMessage(event);
	}
	
	ws.onclose = function(event) {
		onClose(event);
	}
	send("hi");
}

function disconnect(event) {
	ws.close();
}

function reconnect() {
    setTimeout(function () {
        connect();
    }, 10000);
}

function onError(event) {
	console.log("error");	
}

function onMessage(event) {
	var message = event.data;
	$("#outputMessage").html($("#outputMessage").html() + 
	message);
	send("client");
}

function send(msg) {
	ws.send(msg);
}