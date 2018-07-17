package en.actionsofproject.database.ui;

public class ClassInfo {
	int ClassID;
	String ClassQualifiedName;
	String ClassName;
	
	public ClassInfo(int ClassID, String ClassQualifiedName, String ClassName){
		this.ClassID = ClassID;
		this.ClassQualifiedName = ClassQualifiedName;
		this.ClassName = ClassName;
	}
	public int getClassID() {
		return ClassID;
	}
	
	public String getClassName() {
		return ClassName;
	}
	public void setClassName(String className) {
		ClassName = className;
	}
	public void setClassID(int classID) {
		ClassID = classID;
	}
	public String getClassQualifiedName() {
		return ClassQualifiedName;
	}

	public void setClassQualifiedName(String classQualifiedName) {
		ClassQualifiedName = classQualifiedName;
	}

}
