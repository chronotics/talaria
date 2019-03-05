package org.chronotics.talaria.thrift;

import org.chronotics.talaria.thrift.gen.ThriftMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

//struct ThriftMessage {
//        1:  required string 			_sender_id;
//        2:  optional string 			_receiver_id;
//        3:  required string			_timestamp;
//        4:  optional string 			_subject;
//        5:  optional i64 			_sequence_no;
//        6:  optional i64			_total_count;
//        7:  optional binary 			_binary;
//        8:  optional string 			_payload;
//        9:  optional list<ThriftMessage> 	_list_message;
//        10: optional set<ThriftMessage>		_set_message;
//        11: optional map<string,ThriftMessage> 	_map_message;
//        12: optional list<bool> 		_list_bool;
//        13: optional list<i16> 			_list_i16;
//        14: optional list<i32> 			_list_i32;
//        15: optional list<i64> 			_list_i64;
//        16: required list<double> 		_list_double;
//        17: optional list<string>		_list_string;
//        18: optional set<bool> 			_set_bool;
//        19: optional set<i16> 			_set_i16;
//        20: optional set<i32> 			_set_i32;
//        21: optional set<i64> 			_set_i64;
//        22: optional set<double> 		_set_double;
//        23: optional set<string>		_set_string;
//        24: optional map<string,bool> 		_map_bool;
//        25: optional map<string,i16> 		_map_i16;
//        26: optional map<string,i32> 		_map_i32;
//        27: optional map<string,i64> 		_map_i64;
//        28: optional map<string,double> 	_map_double;
//        29: optional map<string,string>		_map_string;
//        }

public class ThriftMessageToJson {
    public static JSONObject convert(ThriftMessage message) {
        JSONObject root = new JSONObject();

        if(message.isSet_sender_id()) {
            root.put("_sender_id",message._sender_id);
        }

        if(message.isSet_receiver_id()) {
            root.put("receiver_id",message._receiver_id);
        }

        if(message.isSet_timestamp()) {
            root.put("_timestamp",message._timestamp);
        }

        if(message.isSet_subject()) {
            root.put("_subject",message._subject);
        }

        if(message.isSet_sequence_no()) {
            root.put("_sequence_no",message._sequence_no);
        }

        if(message.isSet_total_count()) {
            root.put("_total_count",message._total_count);
        }

        if(message.isSet_binary()) {
            // convert nio.ByteBuffer to byte[]
            byte[] bytes = new byte[message._binary.capacity()];
            message._binary.get(bytes,0,bytes.length);

            // convert byte[] to String with Base64
            String str = Base64.getEncoder().encodeToString(bytes);
            // you have to check whether conversion is right or not
            assert(false);

            root.put("_binary",str);

            //////////////////////////////////////////////
        }

        if(message.isSet_payload()) {
            root.put("_payload",message._payload);
        }

//        if(message.isSet_list_message()) {
//            JSONArray array = new JSONArray();
//            message._list_message.forEach(msg -> {
//                array.put(convert(msg));
//            });
//            root.put("_list_message",array);
//        }
//
//        if(message.isSet_set_message()) {
//            JSONArray array = new JSONArray();
//            message._set_message.forEach(msg -> {
//                array.put(convert(msg));
//            });
//            root.put("_set_message",array);
//        }
//
//        if(message.isSet_map_message()) {
//            JSONObject obj = new JSONObject();
//            message._map_message.forEach((k,v) -> {
//               obj.put(k,convert(v));
//            });
//            root.put("_map_message",obj);
//        }

        if(message.isSet_list_bool()) {
            JSONArray array = new JSONArray();
            message._list_bool.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_bool",array);
        }

        if(message.isSet_list_i16()) {
            JSONArray array = new JSONArray();
            message._list_i16.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_i16",array);
        }

        if(message.isSet_list_i32()) {
            JSONArray array = new JSONArray();
            message._list_i32.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_i32",array);
        }

        if(message.isSet_list_i64()) {
            JSONArray array = new JSONArray();
            message._list_i64.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_i64",array);
        }

        if(message.isSet_list_double()) {
            JSONArray array = new JSONArray();
            message._list_double.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_double",array);
        }

        if(message.isSet_list_string()) {
            JSONArray array = new JSONArray();
            message._list_string.forEach(msg -> {
                array.put(msg);
            });
            root.put("_list_string",array);
        }

        if(message.isSet_set_bool()) {
            JSONArray array = new JSONArray();
            message._set_bool.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_bool",array);
        }

        if(message.isSet_set_i16()) {
            JSONArray array = new JSONArray();
            message._set_i16.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_i16",array);
        }

        if(message.isSet_set_i32()) {
            JSONArray array = new JSONArray();
            message._set_i32.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_i32",array);
        }

        if(message.isSet_set_i64()) {
            JSONArray array = new JSONArray();
            message._set_i64.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_i64",array);
        }

        if(message.isSet_set_double()) {
            JSONArray array = new JSONArray();
            message._set_double.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_double",array);
        }

        if(message.isSet_set_string()) {
            JSONArray array = new JSONArray();
            message._set_string.forEach(msg -> {
                array.put(msg);
            });
            root.put("_set_string",array);
        }

        if(message.isSet_map_bool()) {
            JSONObject obj = new JSONObject();
            message._map_bool.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_bool",obj);
        }

        if(message.isSet_map_i16()) {
            JSONObject obj = new JSONObject();
            message._map_i16.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_i16",obj);
        }

        if(message.isSet_map_i32()) {
            JSONObject obj = new JSONObject();
            message._map_i32.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_i32",obj);
        }

        if(message.isSet_map_i64()) {
            JSONObject obj = new JSONObject();
            message._map_i64.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_i64",obj);
        }

        if(message.isSet_map_double()) {
            JSONObject obj = new JSONObject();
            message._map_double.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_double",obj);
        }

        if(message.isSet_map_string()) {
            JSONObject obj = new JSONObject();
            message._map_string.forEach((k,v) -> {
                obj.put(k,v);
            });
            root.put("_map_string",obj);
        }

        return root;
    }
}
