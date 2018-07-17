package en.actionsofproject.database.ui;

public class EPValue {
	int ClassID;
	String ClassName;
	double EntityPlacement;
	public EPValue(int ClassID, String ClassName, double EntityPlacement){
		this.ClassID = ClassID;
		this.ClassName = ClassName;
		this.EntityPlacement = EntityPlacement;
		
	}
	public int getClassID() {
		return ClassID;
	}
	public void setClassID(int classID) {
		ClassID = classID;
	}
	public String getClassName() {
		return ClassName;
	}
	public void setClassName(String className) {
		ClassName = className;
	}
	public double getEntityPlacement() {
		return EntityPlacement;
	}
	public void setEntityPlacement(double entityPlacement) {
		EntityPlacement = entityPlacement;
	}
	

}
