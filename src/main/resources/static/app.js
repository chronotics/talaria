var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect(event) {
	console.log('prepare');
    var socket = new SockJS('/sockJS');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError); 
    
//    event.preventDefault();
}

function onConnected() {
	setConnected(true);
	stompClient.subscribe('/topic/vibration', onMessageReceived);
}

function onError(error) {
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    stompClient.send("/ap/execute", {}, $("#name").val());//JSON.stringify({'name': $("#name").val()}));
}

//var queue = [];
//
//var maxQueueSize = 2;
//function pushDataToQueue(_v, callback) {
//	queue.push(_v);
//	document.getElementById("senderId").innerHTML += ", " + _v.senderId;
//	
//	if(queue.length > maxQueueSize) {
//		sortByKey(queue, timestamp);
//		for(let i = 0; i < maxQueueSize; i++) {
//			var data = queue.shift();
//			callback(data);
//		}
//	}
//}
//
//function sortByKey(array, key) {
//    return array.sort(function(a, b) {
//        var x = a.timestamp; var y = b.timestamp;
//        console.log(x);
//        console.log(y);
//        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
//    });
//}
//
//function pushDataToDyGraph(_v) {
//	
//    var rawDataSize = 10000;
//    var oneSecond = 1000; // msec
//    var N = 1000;
//    var samplingTime = (N*oneSecond) / rawDataSize;
//    
//	var senderId = _v.senderId;
//	var listDouble = _v.listDouble;
//	var timestamp = parseInt(_v.timestamp);
//	
//	document.getElementById("deviceTime").innerHTML += ", " + timestamp;
//
//	var count = 0;
//	for(var i = 0; i < listDouble.length; i++) {
//	  if(i % N == 0) { // per 10ms, 1 sec / (10,000K data / #N ) = #N sec / 10,000 
//			var x = timestamp + count*samplingTime;//new Date((timestamp + count*samplingTime));
//	//	    	console.log(count);
//	//	    	console.log(samplingTime);
//			var y = parseFloat(listDouble[i]);
//			if(x=="" || y=="") {
//				return;
//			}
//		  data.push([x,y]);//y[0]]);
//	//  		document.getElementById("d0Time").innerHTML = (timestamp + count*samplingTime);
//	//  		document.getElementById("convertedtime").innerHTML = x;
//		  count++;
//	  }
//	}
//}
var N = 100;

function onMessageReceived(payload) {
	// JSON.parse(payload.body).content
//    $("#greetings").append("<tr><td>" + payload.body + "</td></tr>");
    
//    if(Number.isInteger(payload.body)) {
//    	console.log("payload is NaN " + payload.body);
//    	return;
//    }
    if(payload == null || payload.body == null || payload.body == "") {
    	return;
    }
//    var x = new Date();  // current time
//    var y = parseFloat(payload.body);
    var jsonObject = JSON.parse(payload.body);
    var senderId = jsonObject.senderId;
    var listDouble = jsonObject.listDouble;
    var timestamp = parseInt(jsonObject.timestamp);
    
    var rawDataSize = 10000;
    var oneSecond = 1000; // msec
    var samplingTime = (N*oneSecond) / rawDataSize;
    if(senderId === "d0") {
//    	var data = { "senderId":senderId, "timstamp":timestamp, "listDouble":listDouble }
////    	pushDataToQueue(jsonObject, pushDataToDyGraph);
//    	var convTime = new Date(timestamp);
//    	document.getElementById("d0Time").innerHTML += "<br>" + jsonObject.timestamp + ", " + convTime;
        
    document.getElementById("senderId").innerHTML = senderId;
    document.getElementById("timestamp").innerHTML = timestamp;

    var count = 0;
	    for(var i = 0; i < listDouble.length; i++) {
	    	if(i % N == 0) { // per 10ms, 1 sec / (10,000K data / #N ) = #N sec / 10,000 
		    	var x = new Date((timestamp + count*samplingTime));
//		    	console.log(count);
//		    	console.log(samplingTime);
		    	var y = parseFloat(listDouble[i]);
				if(x=="" || y=="") {
					return;
				}
	    		data.push([x,y]);//y[0]]);
//	    		document.getElementById("d0Time").innerHTML = (timestamp + count*samplingTime);
//	    		document.getElementById("convertedtime").innerHTML = x;
	    		count++;
	    	}
	    }
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendMessage(); });
});

var buffer = [];
var dataSize = 1000;
var samplingTimeToUpdateChart = 50; // ms
var data = [];

$(document).ready(function () {
	
	var t = new Date();
	//document.getElementById("d0Time").innerHTML += "<br>" + t.getTime();
    for (var i = dataSize; i > 0; i--) {
    	var x = new Date(t.getTime() - i * dataSize);
    	data.push([x, 0.0]);
    }

    var g = new Dygraph(document.getElementById("div_g"), data,
                        {
                          drawPoints: true,
                          showRoller: true,
                          valueRange: [0.0, 0.2],
                          labels: ['time with ms', 'Value']
                        });
    // It sucks that these things aren't objects, and we need to store state in window.
    window.intervalId = setInterval(function() {
    	
    	g.updateOptions( { 'file': data } );
		var i = 0;
		while(data.length >= dataSize) {
			data.shift();
		}
    }, samplingTimeToUpdateChart);
  }
);
