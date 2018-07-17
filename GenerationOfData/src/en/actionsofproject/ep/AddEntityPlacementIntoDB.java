package en.actionsofproject.ep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.ui.EPValue;
//import gr.uom.java.jdeodorant.refactoring.actions.EntityPlacementCalculator;

public class AddEntityPlacementIntoDB {
	
	private Map<String, Double> entityPlacementValue = new HashMap();
	
	public void AddEntityPlacement(IJavaProject project) throws Exception{
		System.out.println("----start insert into EPValue---");
//		EntityPlacementCalculator entityPlacementCalculator = new EntityPlacementCalculator();
//		entityPlacementValue = entityPlacementCalculator.getEntityPlaceMentMetrics(project);
		
		insertIntoEntityPlacement();
	}
	public void insertIntoEntityPlacement() throws Exception{
		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		int maxTableRow = actionsAboutDB.getTableMaxRow(4);
		Set<String> classNames = entityPlacementValue.keySet();
		for(String className : classNames){
			EPValue ePValue = new EPValue(maxTableRow, className, entityPlacementValue.get(className));
			//actionsAboutDB.insertEPValue(ePValue);
		}
	}
}
