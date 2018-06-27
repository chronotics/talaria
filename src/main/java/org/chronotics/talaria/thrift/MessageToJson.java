package org.chronotics.talaria.thrift;

import org.chronotics.talaria.thrift.gen.Message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//public java.lang.String _sender_id; // required
//public java.lang.String _receiver_id; // required
//public java.lang.String _subject; // required
//public java.lang.String _sequence_no; // required
//public java.lang.String _timestamp; // required
//public java.util.List<java.lang.Boolean> _list_bool; // required
//public java.util.List<java.lang.Short> _list_i16; // required
//public java.util.List<java.lang.Integer> _list_i32; // required
//public java.util.List<java.lang.Long> _list_i64; // required
//public java.util.List<java.lang.Double> _list_double; // required
//public java.util.List<java.lang.String> _list_string; // required
//public java.nio.ByteBuffer _binary; // required
//public java.lang.String _payload; // optional

public class MessageToJson {
	public static String convert(Message _message) {
		JsonObject root = new JsonObject();
		root.addProperty("senderId", _message._sender_id);
		root.addProperty("receiverId", _message._receiver_id);
		root.addProperty("subject", _message._subject);
		root.addProperty("sequenceNo", _message._sequence_no);
		root.addProperty("timestamp", _message._timestamp);
	
		JsonArray listBool = new JsonArray();
		for(Boolean e:_message._list_bool) {
			listBool.add(e);
		}
		root.add("listBool", listBool);
		
		JsonArray listShort = new JsonArray();
		for(Short e:_message._list_i16) {
			listShort.add(e);
		}
		root.add("listShort", listShort);
		
		JsonArray listInteger = new JsonArray();
		for(Integer e:_message._list_i32) {
			listInteger.add(e);
		}
		root.add("listInteger", listInteger);
		
		JsonArray listLong = new JsonArray();
		for(Long e:_message._list_i64) {
			listLong.add(e);
		}
		root.add("listLong", listLong);
		
		JsonArray listDouble = new JsonArray();
		for(Double e:_message._list_double) {
			listDouble.add(e);
		}
		root.add("listDouble", listDouble);
		
		JsonArray listString = new JsonArray();
		for(String e:_message._list_string) {
			listString.add(e);
		}
		root.add("listString", listString);
		
		// have to check
		if(_message._binary != null) {
			root.addProperty("binary", _message._binary.toString());
		}
	
		if(_message._payload != null) {
			root.addProperty("payload", _message._payload);
		}
		
		Gson gson = new Gson();
		return gson.toJson(root);
	}
}
