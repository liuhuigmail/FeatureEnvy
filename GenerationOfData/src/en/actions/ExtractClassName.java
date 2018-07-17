package en.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import en.actionsofproject.ProjectEvolution;
import en.actionsofproject.database.InsertDataIntoDistanceValue;
import en.actionsofproject.ep.AddEntityPlacementIntoDB;
import gr.uom.java.ast.Standalone;


public class ExtractClassName implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		final IJavaProject selectedProject = JavaCore.create(getProject());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject selectedIProject = null;
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if(selectedProject == null){
			Display.getDefault().asyncExec(new Runnable() {
	            @Override
	            public void run() {
	            	 MessageBox dialog=new MessageBox(shell,SWT.OK|SWT.ICON_INFORMATION);
	 		        dialog.setText("Warning");
	 		        dialog.setMessage("Please select a JavaProject to ExtractName!");
	 		        dialog.open();
	 		        return;
	            }
		 });
		}
		else{
			//System.out.println("Project   "+selectedProject.getElementName());
			for (IProject iProject : root.getProjects()) {
				if(iProject.getName().equals((selectedProject.getElementName()))){
					selectedIProject = iProject;
					break;
				}	
			 }		
			System.out.println("IProject's  name----"+selectedIProject.getName());
			ProjectEvolution projectEvolution = new ProjectEvolution(selectedProject, selectedIProject);
			try {
				projectEvolution.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InsertDataIntoDistanceValue insertDataIntoDistanceValue = new InsertDataIntoDistanceValue();
			try {
				insertDataIntoDistanceValue.AddDistanceMatric(selectedProject);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			AddEntityPlacementIntoDB addEntityPlacementIntoDB = new AddEntityPlacementIntoDB();
//			try {
//				addEntityPlacementIntoDB.AddEntityPlacement(selectedProject);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		

	}
	
	public IProject getProject(){  
		IProject project = null;  
	//	IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();  
		
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();    
		ISelection selection = selectionService.getSelection();    
		if(selection instanceof IStructuredSelection) {    
			Object element = ((IStructuredSelection)selection).getFirstElement();    
			
			if (element instanceof IResource) {    
				project= ((IResource)element).getProject();    
			} else if (element instanceof PackageFragmentRootContainer) {    
				IJavaProject jProject =     
						((PackageFragmentRootContainer)element).getJavaProject();    
				project = jProject.getProject();    
			} else if (element instanceof IJavaElement) {    
				IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
				project = jProject.getProject();    
			}  
		}     
		
		return project;  
	} 

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

}
