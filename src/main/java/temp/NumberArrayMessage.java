package temp;

import java.util.ArrayList;
import java.util.List;

public class NumberArrayMessage {
	private List<String> timeArray = new ArrayList<String>(); 
	private List<String> valueArray = new ArrayList<String>();
	
	public List<String> getValueArray() {
		return valueArray;
	}
	
	public void addDataToArray(String _time, String _val) throws Exception {
		boolean isNumeric = _val.matches("-?\\d+(\\.\\d+)?");
		if(!isNumeric) {
			throw new NumberFormatException();
		}
		timeArray.add(_time);
		valueArray.add(_val);
	}
}
