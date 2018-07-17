package en.actionsofproject.database;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.ui.DistanceValue;
import gr.uom.java.ast.ASTReader;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.CompilationErrorDetectedException;
import gr.uom.java.ast.CompilationUnitCache;
import gr.uom.java.ast.SystemObject;
import gr.uom.java.distance.DistanceMatrix;
import gr.uom.java.distance.Entity;
import gr.uom.java.distance.MyClass;
import gr.uom.java.distance.MyMethod;
import gr.uom.java.distance.MySystem;
import gr.uom.java.distance.SystemEntityPlacement;

public class InsertDataIntoDistanceValue {    
		
//	public List<Entity> entityList = new ArrayList();
	public List<MyClass> classList = new ArrayList();
	public Double[][] distanceMatrix;
	public List<Entity> entityList = new ArrayList();;
	
	private SystemEntityPlacement systemEntityPlacement; 
	
	public InsertDataIntoDistanceValue(){
//		entityList.clear();
		classList.clear();
	}
	
	public void AddDistanceMatric(IJavaProject project) throws Exception{
		System.out.println("----start insert into distanceValue---");
		getDistanceMetrics(project);
	}
	


	public void getDistanceMetrics(IJavaProject project) {
		//TODO:  DistanceMatrix COMPUTATION. 
		Map<String, Double> entityPlacementValue = new HashMap();
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		
		MySystem system = getMySystem(project);
	
	    final DistanceMatrix distanceMatrix = new DistanceMatrix(system);
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					distanceMatrix.generateDistances(monitor);
					Double[][] distanceMatrix1 = distanceMatrix.getDistanceMatrix();
					String[] classNames = distanceMatrix.getClassNames();
					String[] methodNames = distanceMatrix.getEntityNames();
					classList = distanceMatrix.getClassList();
					entityList = distanceMatrix.getEntityList();
					ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
					int maxTableRow =0;
					try {
						maxTableRow = actionsAboutDB.getTableMaxRowofDistance()+1;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for(int i=0; i<entityList.size();i++)
						for(int j=0; j<classNames.length; j++){
							if(distanceMatrix1[i][j] != 1){
								MyMethod myMethod = (MyMethod) entityList.get(i);
								List<String> parameterList = myMethod.getParameterList();
								StringBuilder sb = new StringBuilder();
								if(!parameterList.isEmpty()){
									for(String parameter : parameterList)
										sb.append(parameter).append(",");
								}
								else
									sb.append("0");
								System.out.println("parameters--------------------"+sb);
								DistanceValue distanceValue = new DistanceValue(maxTableRow,myMethod.getMethodName(),sb.toString(),myMethod.getClassOrigin(),classNames[j],distanceMatrix1[i][j]);
								System.out.println("-----maxTableRow------"+maxTableRow);
								System.out.println("methodName---------"+myMethod.getMethodName()+"----"+myMethod.getClassOrigin()+"---");
								System.out.println("className---------"+classNames[j]+"-------");
								System.out.println("distance------"+distanceMatrix1[i][j]);
								try {
									actionsAboutDB.insertDistanceValue(distanceValue);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								maxTableRow++;
							}	
						}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
//		systemEntityPlacement = distanceMatrix.getSystemEntityPlacement();
//		entityList= distanceMatrix.getEntityList();
		
		this.distanceMatrix=distanceMatrix.getDistanceMatrix();
//		double systemEntityPlacementValue = distanceMatrix.getSystemEntityPlacementValue();
//		System.out.println("systemEntityPlacementValue----------"+systemEntityPlacementValue);
		
		 for(MyClass myClass : classList) {
//			 ClassEntityPlacement entityPlacement = systemEntityPlacement.getClassEntityPlacement(myClass.getName());
//			 System.out.println(myClass.getName()+"--entityPlacement--"+entityPlacement.getClassEntityPlacementValue());
//			 if(entityPlacement.getClassEntityPlacementValue() != null)
//				 entityPlacementValue.put(myClass.getName(), entityPlacement.getClassEntityPlacementValue());
			 
		 }
		//print();
	}
	
	protected void print(){
		for(int i = 0; i < 100; i++){
			for(int j = 0; j < 100; j++){
				System.out.print("---"+distanceMatrix[i][j]);
			}
			System.out.println(" ");
		}	
	}

	public static MySystem getMySystem(IJavaProject project) {
		CompilationUnitCache.getInstance().clearCache();
		
//			if(ASTReader.getSystemObject() != null && project.equals(ASTReader.getExaminedProject())) {
//				new ASTReader(project, ASTReader.getSystemObject(), null);
//			}
			//else {
				try {
					new ASTReader(project, null);
				} catch (CompilationErrorDetectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//}
		
		
		SystemObject systemObject = ASTReader.getSystemObject();
		if(systemObject != null) {
			Set<ClassObject> classObjectsToBeExamined = new LinkedHashSet<ClassObject>();
			classObjectsToBeExamined.addAll(systemObject.getClassObjects());
			
			MySystem system = new MySystem(systemObject, true);
	
			return system;	
		}
		else{
			return new MySystem(null,false);
		}
	}
}
