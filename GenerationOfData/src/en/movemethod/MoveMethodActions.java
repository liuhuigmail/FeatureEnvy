package en.movemethod;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveStaticMembersProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

@SuppressWarnings("restriction")
public class MoveMethodActions {
	
	public void moveMethod(IMethod method, IVariableBinding target) throws NullPointerException, CoreException{
		

		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.setTarget(target);
			processor.setDelegateUpdating(false);
		
			MoveRefactoring refactoring = new MoveRefactoring(processor);
			IProgressMonitor pm = new NullProgressMonitor();
			try {
				refactoring.checkInitialConditions(pm);
				refactoring.checkFinalConditions(pm);
				final PerformRefactoringOperation op = new PerformRefactoringOperation(refactoring, 2);
				op.run(new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	public void moveSingleStaticMethod(IJavaElement element, String destination){

		IMember[] members = new IMember[1];
		IMember member = (IMember)element;
		members[0] = member;
		MoveStaticMembersProcessor sProcessor = new MoveStaticMembersProcessor(members, JavaPreferencesSettings.getCodeGenerationSettings(member.getJavaProject()));
		sProcessor.setDelegateUpdating(false);
		try {
			sProcessor.setDestinationTypeFullyQualifiedName(destination);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MoveRefactoring sRefactoring = new MoveRefactoring(sProcessor);
		IProgressMonitor sPm = new NullProgressMonitor();
		try {
			sRefactoring.checkInitialConditions(sPm);
			sRefactoring.checkFinalConditions(sPm);
			final PerformRefactoringOperation op = new PerformRefactoringOperation(sRefactoring, 2);
			op.run(new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	private CompilationUnit setCompilationUnit(IMethod element){
		IMethod method = (IMethod)element;
		ICompilationUnit iUnit = method.getCompilationUnit();
		//IType belongType = member.getDeclaringType();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(iUnit);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit unit1 = (CompilationUnit) parser.createAST(null);
		return unit1;
	}

}
