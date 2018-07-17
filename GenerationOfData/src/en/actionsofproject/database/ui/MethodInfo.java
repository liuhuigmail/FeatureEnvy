package en.actionsofproject.database.ui;

public class MethodInfo {
	int MethodID;
	String MethodName;
	String MethodKey;
	String MethodOfClass;
	
	public MethodInfo(int MethodID, String MethodName, String MethodKey, String MethodOfClass){
		this.MethodID = MethodID;
		this.MethodName = MethodName;
		this.MethodKey = MethodKey;
		this.MethodOfClass = MethodOfClass;
	}
	
	public String getMethodKey() {
		return MethodKey;
	}
	public void setMethodKey(String methodKey) {
		MethodKey = methodKey;
	}
	public int getMethodID() {
		return MethodID;
	}
	public void setMethodID(int methodID) {
		MethodID = methodID;
	}
	public String getMethodName() {
		return MethodName;
	}
	public void setMethodName(String methodName) {
		MethodName = methodName;
	}
	public String getMethodOfClass() {
		return MethodOfClass;
	}
	public void setMethodOfClass(String methodOfClass) {
		MethodOfClass = methodOfClass;
	}
	
}
