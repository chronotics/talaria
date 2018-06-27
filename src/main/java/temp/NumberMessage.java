package temp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NumberMessage {
	public NumberMessage(String _id, String _time, String _val) {
		setId(_id);
		setTime(_time);
		try {
			setValue(_val);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String id;
	
	public String getId() {
		return id;
	}
	
	public void setId(String _val) {
		this.id = _val;
	}
	
	private String time;

	public String getTime() {
		return time;
	}
	
	private void setTime(String _time) {
		this.time = _time;
	}
	
	private String value="";

	public String getValue() {
		return value;
	}

	public void setValue(String _val) throws Exception {
		boolean isNumeric = _val.matches("-?\\d+(\\.\\d+)?");
		if(!isNumeric) {
			throw new NumberFormatException();
		}
		value = _val;//String.valueOf(_val);
		//Using Date class
		Date date = new Date();
		//Pattern for showing milliseconds in the time "SSS"
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		time = sdf.format(date);
	}

}
