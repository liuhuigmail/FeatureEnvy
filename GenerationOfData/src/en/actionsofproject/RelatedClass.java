package en.actionsofproject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.text.edits.MalformedTreeException;

import en.actions.undo.ActionsAboutUndo;
import en.actionsofproject.database.ActionsAboutDB;
import en.actionsofproject.database.ui.MethodInfo;
import en.entitys.Entity;
import en.movemethod.MoveMethodActions;
import en.movemethod.MoveMethodNode;


public class RelatedClass {
	
	List<IMethod> allMethods = new ArrayList<IMethod>(); //the iJavaproject's all allMethods
	IProject iProject;
	public Map<IMethod, Integer> methodStatus = new HashMap();//whether method has moved  yes:1 no: 0
	public List<ITypeBinding> typeBindingCanMove = new ArrayList<>();//the classes can be moved
	public TypeDeclaration typeDeclarationOfCurrentClass;
	public ITypeBinding currentClass;
	public IMethod currentIMethod;
	public List<IType> types = new ArrayList<IType>();
	Map<IMethod, IVariableBinding> methodAndTarget = new HashMap();//method and it's one target
	Map<IMethod, MethodDeclaration> methodAndItsMethodDeclaration =new HashMap();//
	List<IMethod> allMethodsCanBeMoved = new ArrayList<IMethod>();
	public List<MoveMethodNode> refactorNodes = new ArrayList<>();//有几个可以移动到的目的地类，就有几个节点
	public List<IVariableBinding> IVarbindsCanMove = new ArrayList<>();
	public static int refactorSteps = 1;
	
	
	public RelatedClass(IProject iProject, List<IMethod> allMethods, List<IType> types){
		init();
		this.allMethods = allMethods;
		this.iProject = iProject;
		this.types = types;
	}
	
	@SuppressWarnings("restriction")
	public void getMoveMethodCandidates(IType type){
		init();
		IMethodCollection iMethodCollection = new IMethodCollection();
		Entity entity = new Entity(type);
		typeDeclarationOfCurrentClass = entity.getTypeDeclaration();
		currentClass = typeDeclarationOfCurrentClass.resolveBinding();
		MethodDeclaration[] methodDeclarations = typeDeclarationOfCurrentClass.getMethods();
		JudgeMethodWhetherMove judgeMethodWhetherMove = new JudgeMethodWhetherMove(type);
		List<IMethod> getterOrSettermethods = iMethodCollection.collectAccessors(type); 
		List<IMethod> accessors = selectMethods(entity.getMethods(), methodDeclarations, judgeMethodWhetherMove, getterOrSettermethods);
		System.out.println("clasaPath---------------------------------------------------------"
				+ "------------------------------------------"+typeDeclarationOfCurrentClass.resolveBinding().getQualifiedName());
		for(IMethod method : accessors){
			//System.out.println("method of name---------//////////////////////////////////"+method.getElementName());
			Entity methodEntity = new Entity(method);
			MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
			methodAndItsMethodDeclaration.put(method, methodDeclaration);
			
			IVarbindsCanMove.clear();
			calVarbingdsCanMove(methodDeclaration, method);
						
			//System.out.println("method name ------------------------------------------------------------------------"+method.getElementName());
			//System.out.println("is class or not---------------------------------------------------------------"+methodDeclaration.resolveBinding().getDeclaringClass().isClass());
			currentIMethod = method;
			VisitorForMethod vis = new VisitorForMethod();
			typeDeclarationOfCurrentClass.accept(vis);
//			for(ITypeBinding tBCanMove : typeBindingCanMove){
//				System.out.println("typeBindingCanMove-----------------"+tBCanMove.getName());
//			}
		}
		for(int i=0; i < refactorNodes.size(); i++){
//			System.out.println("method can be moved-------------"+refactorNodes.get(i).method.getElementName());
//			System.out.println("----------------------all destination-------------------------");
//			for(int j =0; j < refactorNodes.get(i).variableBindings.size(); j++){
//				System.out.println("destination-------------------------"+refactorNodes.get(i).variableBindings.get(j).getType().getQualifiedName());
//			}
			
//			System.out.println(refactorNodes.get(i).variableBindings.get(0).getName()+"----------------------destination-------------------------"+refactorNodes.get(i).targetTypeName);
			IVariableBinding vb = refactorNodes.get(i).variableBindings.get(0);
			IMethod method = refactorNodes.get(i).method;
			if(!methodAndTarget.containsKey(method)){
				
				Entity entity1 = new Entity(method);
				CompilationUnit unit = entity1.getUnit();
				TypeDeclaration typeDeclaration = entity1.getTypeDeclaration();
				MethodDeclaration[] allMethodDeclaration = typeDeclaration.getMethods();
				MethodDeclaration methodDeclaration = methodAndItsMethodDeclaration.get(method);
				for(MethodDeclaration methodDeclarationx : allMethodDeclaration){
					if(methodDeclarationx.equals(methodDeclaration)){
						methodDeclaration = methodDeclarationx;
					}
						
				}
				if((methodDeclaration.getModifiers()& Modifier.STATIC) != 0){
				}
				else{
					allMethodsCanBeMoved.add(method);
				}
				if(vb == null){
					ITypeBinding targetTypeBinding = refactorNodes.get(i).typeBinding;
					IVariableBinding targetBinding = CreateTargetField.startCreateTarget(unit, typeDeclaration, methodDeclaration, targetTypeBinding);
					methodAndTarget.put(method, targetBinding);
				}
				else
					methodAndTarget.put(method, vb);
			}	
		}
//		System.out.println("methodAndTarget's size------------------  "+methodAndTarget.size());
		try {
			addClassInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		refactoringsExcute(allMethodsCanBeMoved,methodAndTarget);
		
		IMethod[] allMethods = new IMethod[accessors.size()];
		accessors.toArray(allMethods);
	}
	public void init(){
		IVarbindsCanMove.clear();
		refactorNodes.clear();
		typeBindingCanMove.clear();
		methodAndTarget.clear();
		allMethodsCanBeMoved.clear();
	}
	
	public List<IMethod> selectMethods(List<IMethod> allMethods, MethodDeclaration[] methodDeclarations, JudgeMethodWhetherMove judgeMethodWhetherMove, List<IMethod> getterOrSettermethods){
		List<IMethod> methodCanBeMoved = new ArrayList<IMethod>();
		int length = (int) (allMethods.size()*0.1);
		int flag = 0;
		
		for(IMethod method : allMethods){
			for(MethodDeclaration methodDeclaration : methodDeclarations){
				if((methodDeclaration.getName().toString().equals(method.getElementName())) && (method.getDeclaringType().getFullyQualifiedName().toString().equals(methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName().toString()))){
					if(judgeMethodWhetherMove.methodCanMove(method, methodDeclaration)){
						if(!getterOrSettermethods.contains(method) && !methodCanBeMoved.contains(method)){
							methodCanBeMoved.add(method);
							//methodAndItsMethodDeclaration.put(method, methodDeclaration);
							//methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
							//System.out.println(methodDeclaration.resolveBinding().getName()+"---methodCanBeMoved$$$$$$$$$-----------");
							break;
							
						}
					}	
				}
			}
		}
		return methodCanBeMoved;
	}
	public void calVarbingdsCanMove(MethodDeclaration method, IMethod currentIMethod){//find all destinations
		
		List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>)method.parameters();
		if(parameters == null || parameters.size() == 0){
			return;
		}
		List<IVariableBinding> targets = new ArrayList<>();
		boolean canMove = false;
		for(Iterator<SingleVariableDeclaration> it = parameters.iterator(); it.hasNext(); ){
			IVariableBinding bind = it.next().resolveBinding();
			
			if(bind.getType().isFromSource() && !bind.getType().equals(currentClass)){
				targets.add(bind);
				if(!typeBindingCanMove.contains(bind.getType()))
					typeBindingCanMove.add(bind.getType());
				canMove = true;
				boolean visited = false;
				for(MoveMethodNode node : refactorNodes){
					if(node.typeBinding.equals(bind.getType())){
						visited = true;
						node.variableBindings.add(0,bind);
						break;
					}
				}
				if(visited == false){ 
					MoveMethodNode node2 = new MoveMethodNode();
					node2.typeBinding = bind.getType();
					List<IVariableBinding> vars = new ArrayList<>();
					vars.add(bind);
					node2.variableBindings = vars;
					node2.method = currentIMethod;
					node2.targetTypeName = bind.getName();
					refactorNodes.add(node2);
				}
			}
		}
		if(canMove == false)
			return;
		IVarbindsCanMove.addAll(targets);
		
	}
	protected void refactoringsExcute(List<IMethod> allMethods, Map<IMethod, IVariableBinding> methodAndTarget) {
		 ActionsAboutUndo undoActions = new ActionsAboutUndo();
		 ActionsAboutDB actionsAboutDB1 = new ActionsAboutDB();
		 int num = 0;
		
			try {
				num = actionsAboutDB1.getMaxTimes() + 1;
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	
		 for(int i = 0; i < allMethods.size(); i++){
			 Random r =new Random(); 
			 int status = r.nextInt(3);
			 if(status == 1){
				 System.out.println("the method is moving ================================================================"+allMethods.get(i).getElementName());
				 System.out.println("targetClass==================================================="+methodAndTarget.get(allMethods.get(i)).getName()+"\n\n\n");
				 
				 MoveMethodActions moveMethodActions = new MoveMethodActions();
				 try {
					 moveMethodActions.moveMethod(allMethods.get(i), methodAndTarget.get(allMethods.get(i)));
				} catch (NullPointerException | CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				 if(!undoActions.calculateCompilationErrorMarkers(iProject)){//编译不出现错误
					 undoActions.undo();
					 MethodDeclaration methodDeclaration = methodAndItsMethodDeclaration.get(allMethods.get(i));
					 String methodItselfClass = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
//					 addClassInfo(num);
					 String methodOfClass = methodAndTarget.get(allMethods.get(i)).getType().getQualifiedName();
					 String methodName = allMethods.get(i).getElementName();
					 ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
					 
					try {
//						 System.out.println("start insert into methodinfo-----");
//						 int maxTableRow = actionsAboutDB.getTableMaxRow(2);
//						 MethodInfo methodInfo = new MethodInfo(maxTableRow+1, methodName, methodOfClass);
//						 actionsAboutDB.insertMethodInfo(methodInfo);
//						 addOtherMethod(allMethods.get(i),num);
//						 
//						 int methodId = actionsAboutDB.getRelationsMethodID(methodName, methodOfClass, num);
//						 int classId = actionsAboutDB.getRelationsClassID(methodOfClass,num);
//						 int classItselfId = actionsAboutDB.getRelationsClassID(methodItselfClass,num);
//						 int maxTableRow1 = actionsAboutDB.getTableMaxRow(1);
//						 System.out.println("methodId-------------"+ methodId);
//						 //System.out.println("classItselfId--------------"+ classItselfId);
//						 Relations relations = new Relations(maxTableRow1+1, methodId,classItselfId,0,num);
//						 Relations relations1 = new Relations(maxTableRow1+2, methodId,classId,1,num);
//						 actionsAboutDB.insertRelations(relations);
//						 actionsAboutDB.insertRelations(relations1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 //System.out.println("methodName------" + methodName);
					 //System.out.println("methodOfClass------" + methodOfClass);
					addAllRelations(allMethods.get(i), num);
					actionsAboutDB.commitMySQL();
				 }
				 
				 num++;
				
			 }
		 }
	}
	public void addClassInfo() throws Exception{
		System.out.println("start insert into classinfo-------------");
		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		int maxTableRow = actionsAboutDB.getTableMaxRow(3)+1;
		for(int i = 0; i<types.size(); i++){
			IType type= types.get(i);

			if(type.isClass()){
				String className = type.getElementName();
				String classQualifiedName = type.getFullyQualifiedName();
//				ClassInfo classinfo = new ClassInfo(maxTableRow, classQualifiedName,className);
//				actionsAboutDB.insertClassInfo(classinfo);
			}
			maxTableRow++;
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addAllRelations(IMethod methodIsMoving, int num){
		System.out.println("start into relations-----------");
		 for(int j = 0 ;j < types.size();j ++){
			 IType type = types.get(j);
			 SearchAllTranferClass searchAllTranferClass = new SearchAllTranferClass(methodIsMoving);
			 searchAllTranferClass.getMoveMethodCandidates(type);
			 Map<IMethod, List<String>> methodAndTargets = new HashMap();
			 methodAndTargets = searchAllTranferClass.methodAndItsdestinations;
			 if(!methodAndTargets.isEmpty())
				 for(IMethod method : allMethods){
					 
					 if(methodAndTargets.containsKey(method)){
						 //System.out.println("method %%%%%%%%%%%%%%%%%%%%%%%---"+method.getElementName());
						 //System.out.println("method of class---------"+method.getDeclaringType().getFullyQualifiedName());
						 List<String> classNames = methodAndTargets.get(method);
	//					 for(String className : classNames){
	//						 System.out.println("classname--------*********---------"+className);
	//					 }
						 try {
							addRelations(method, classNames, num);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 } 
				 }
		 }
	}
	public void addRelations(IMethod method, List<String> classNames,int num) throws Exception{
		//System.out.println("inter addRelations--------------");

		ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
		System.out.println("Method name -----------------"+method.getElementName());
		//System.out.println("method of class---------"+method.getDeclaringType().getFullyQualifiedName());
//		int methodId = actionsAboutDB.getRelationsMethodID(method.getElementName(), method.getDeclaringType().getFullyQualifiedName(),num);
//		int classItselfId = actionsAboutDB.getRelationsClassID(method.getDeclaringType().getFullyQualifiedName(),num);
		int maxTableRow = actionsAboutDB.getTableMaxRow(1);
//		System.out.println("methodId-------------"+ methodId);
		//System.out.println("classItselfId--------------"+ classItselfId);
//		Relations relations1 = new Relations(maxTableRow+1, methodId,classItselfId,1,num);
//		actionsAboutDB.insertRelations(relations1);
		
		
		for(String className :classNames){
			//System.out.println("ClassQualifiedName &&&&&&&&&&------" + className);
			ActionsAboutDB actionsAboutDB1 = new ActionsAboutDB();
			maxTableRow = actionsAboutDB1.getTableMaxRow(1);
//			int classId = actionsAboutDB1.getRelationsClassID(className,num);
			//System.out.println("classId*********************"+ classId);
//			if(classId != 0){
//				Relations relations = new Relations(maxTableRow+1, methodId,classId,0,num);
//				actionsAboutDB1.insertRelations(relations);
//			}
		}
	}
	
	public void addOtherMethod(IMethod method0 ,int num){
		for(int i= 0; i<allMethods.size(); i++){
			IMethod method = allMethods.get(i);
			if(!method0.equals(method)){
				String methodOfClass = method.getDeclaringType().getFullyQualifiedName();
				String methodName = method.getElementName();
				ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
				try {
					 int maxTableRow = actionsAboutDB.getTableMaxRow(2);
//					 MethodInfo methodInfo = new MethodInfo(maxTableRow+1, methodName, methodOfClass, 0, num);
//					 actionsAboutDB.insertMethodInfo(methodInfo);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
	
	class VisitorForMethod extends ASTVisitor{
		
		public boolean visit(TypeDeclaration node){
			if(!node.resolveBinding().equals(currentClass)){
				return false;
			}
			return true;
		}
		public boolean visit(EnumDeclaration node){
			return false;
		}
		
		public boolean visit(EnumConstantDeclaration node){
			return false;
		}
		
		public boolean visit(FieldDeclaration node){
				for (Object obj: node.fragments()) {  
		            VariableDeclarationFragment v = (VariableDeclarationFragment)obj;  
		            if(v.resolveBinding().getType().isFromSource() && !v.resolveBinding().getType().equals(currentClass)){
		            	IVarbindsCanMove.add(v.resolveBinding());
		            	MoveMethodNode node2 = new MoveMethodNode();
		            	
		            	if(v.resolveBinding().getType().isEnum())
		            		return true;
		            	if(!typeBindingCanMove.contains(v.resolveBinding().getType())){
		            		typeBindingCanMove.add(v.resolveBinding().getType());
		            	}
		            	
		            	
		            	boolean visited = false;
		            	for(MoveMethodNode nod: refactorNodes){
		            		if(nod.typeBinding.equals(v.resolveBinding().getType())){
		            			visited = true;
		            			nod.variableBindings.add(v.resolveBinding());
		            			break;
		            		}
		            	}
		            	
		            	if(visited == false){
		            		node2.typeBinding = v.resolveBinding().getType();
							List<IVariableBinding> vars = new ArrayList<>();
							vars.add(v.resolveBinding());
							node2.variableBindings = vars;
							node2.method = currentIMethod;
							node2.targetTypeName = v.resolveBinding().getType().getName();
							refactorNodes.add(node2);
		            	}
		            }
		        }
			return true;
		}
	}

}
