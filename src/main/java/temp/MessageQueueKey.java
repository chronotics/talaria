package temp;

public class MessageQueueKey<T> {
	private String id;
	private Class<T> classType;
	
	public MessageQueueKey(String _id,Class<T> _classType) {
		id = _id;
		classType = _classType;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public Class<T> getClassType() {
		return classType;
	}
	public void setClassType(Class<T> classType) {
		this.classType = classType;
	}
	
	public boolean isEqual(MessageQueueKey<T> _obj) {
		return (this.id == _obj.id && this.classType == _obj.classType)? true : false;
	}
}
