namespace cpp thrift_gen_messenger
namespace java thrift_gen_messenger
namespace php thrift_gen_messenger

struct Message {
	1:  required string 		_sender_id;
	2:  required string 		_receiver_id;
	3:  required string 		_subject;
	4:  required string 		_sequence_no;
	5:  required string 		_timestamp;
	6:  required list<bool> 	_list_bool;
	7:  required list<i16> 		_list_i16;
	8:  required list<i32> 		_list_i32;
	9:  required list<i64> 		_list_i64;
	10: required list<double> 	_list_double;
	11: required list<string>	_list_string;
	12: required binary 		_binary;
	13: optional string 		_payload;
}

service TransferService {
	void writeMessageBegin(1:Message _v),
	void writeMessage(1:Message _v),
	void writeMessageEnd(1:Message _v),
	void writeBool(1:bool _v),
	void writeI16(1:i16 _v),
	void writeI32(1:i32 _v),
	void writeI64(1:i64 _v),
	void writeDouble(1:double _v),
	void writeString(1:string _v),
	Message readMessageBegin(1:string _receiver_id),
	Message readMessage(1:string _receiver_id),
	bool readMessageEnd(1:string _receiver_id),
	bool readBool(),
	i16 readI16(),
	i32 readI32(),
	i64 readI64(),
	double readDouble(),
	string readString()
}
