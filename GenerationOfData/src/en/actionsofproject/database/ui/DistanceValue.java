package en.actionsofproject.database.ui;

public class DistanceValue {
	int methodId;
	String methodName;
	String methodKey;
	String methodOfClass;
	String className;
	Double distance;
	public DistanceValue(int methodId, String methodName, String methodKey, String methodOfClass, String className, Double distance){
		this.methodId = methodId;
		this.methodName = methodName;
		this.methodKey = methodKey;
		this.methodOfClass = methodOfClass;
		this.className = className;
		this.distance = distance;	
	}
	public String getMethodKey() {
		return methodKey;
	}
	public void setMethodKey(String methodKey) {
		this.methodKey = methodKey;
	}
	public int getMethodId() {
		return methodId;
	}
	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodOfClass() {
		return methodOfClass;
	}
	public void setMethodOfClass(String methodOfClass) {
		this.methodOfClass = methodOfClass;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	
}
