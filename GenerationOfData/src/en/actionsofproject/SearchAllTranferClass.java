package en.actionsofproject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import en.entitys.Entity;
import en.movemethod.MoveMethodNode;

public class SearchAllTranferClass {
	
	List<IMethod> allMethods = new ArrayList<IMethod>(); //the iJavaproject's all allMethods
	IProject iProject;
	public Map<IMethod, Integer> methodStatus = new HashMap();//whether method has moved  yes:1 no: 0
	public List<ITypeBinding> typeBindingCanMove = new ArrayList<>();//the classes can be moved
	public TypeDeclaration typeDeclarationOfCurrentClass;
	public ITypeBinding currentClass;
	public IMethod currentIMethod;
	IMethod methodIsMoving;
	Map<IMethod, IVariableBinding> methodAndTarget = new HashMap();
	Map<IMethod, MethodDeclaration> methodAndItsMethodDeclaration =new HashMap();
	List<IMethod> allMethodsCanBeMoved = new ArrayList<IMethod>();
	public List<MoveMethodNode> refactorNodes = new ArrayList<>();//有几个可以移动到的目的地类，就有几个节点
	public List<IVariableBinding> IVarbindsCanMove = new ArrayList<>();
	public static int refactorSteps = 1;
	public Map<IMethod, List<IVariableBinding>> methodAndTargets = new HashMap();
	public Map<IMethod, List<String>> methodAndItsdestinations = new HashMap();
	
	
	public SearchAllTranferClass(IMethod method){
		init();
		this.methodIsMoving = method;
	}
	
	@SuppressWarnings("restriction")
	public void getMoveMethodCandidates(IType type) throws NullPointerException{
		init();
		IMethodCollection iMethodCollection = new IMethodCollection();
		Entity entity = new Entity(type);
		typeDeclarationOfCurrentClass = entity.getTypeDeclaration();
		currentClass = typeDeclarationOfCurrentClass.resolveBinding();
		MethodDeclaration[] methodDeclarations = typeDeclarationOfCurrentClass.getMethods();
		JudgeMethodWhetherMove judgeMethodWhetherMove = new JudgeMethodWhetherMove(type);
		List<IMethod> getterOrSettermethods = iMethodCollection.collectAccessors(type); 
		List<IMethod> accessors = selectMethods(entity.getMethods(), methodDeclarations, judgeMethodWhetherMove, getterOrSettermethods);
		System.out.println("search clasaPath---------------------------------------------------------"+typeDeclarationOfCurrentClass.resolveBinding().getQualifiedName());
		for(IMethod method : accessors){
			if(!method.equals(methodIsMoving)){
				System.out.println("method name --------------------"+method.getElementName());
				Entity methodEntity = new Entity(method);
				MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
				if(methodDeclaration != null){
					methodAndItsMethodDeclaration.put(method, methodDeclaration);
					
					IVarbindsCanMove.clear();
					calVarbingdsCanMove(methodDeclaration, method);
					
					currentIMethod = method;
					VisitorForMethod vis = new VisitorForMethod();
					typeDeclarationOfCurrentClass.accept(vis);
					
					List<String> classNames = new ArrayList<String>();
					for(int j = 0; j < IVarbindsCanMove.size(); j++){
						String className = IVarbindsCanMove.get(j).getType().getQualifiedName();
						System.out.println("relations-----------------"+method.getElementName()+"--------------"+IVarbindsCanMove.get(j).getType().getQualifiedName());
						if(!classNames.contains(className))
							classNames.add(className);
					}
					methodAndItsdestinations.put(method, classNames);
				}
			}
		}
		for(int i=0; i < refactorNodes.size(); i++){
//			System.out.println("----------------------all destination-------------------------");
//			for(int j =0; j < refactorNodes.get(i).variableBindings.size(); j++){
//				System.out.println("destination-------------------------"+refactorNodes.get(i).variableBindings.get(j).getType().getQualifiedName());
//			}
			//System.out.println(refactorNodes.get(i).variableBindings.get(0).getName()+"----------------------destination-------------------------"+refactorNodes.get(i).targetTypeName);
			//System.out.println("method can be moved-------------"+refactorNodes.get(i).method.getElementName());
			IVariableBinding vb = refactorNodes.get(i).variableBindings.get(0);
			List<IVariableBinding> vbs = refactorNodes.get(i).variableBindings;
			IMethod method = refactorNodes.get(i).method;
			if(!methodAndTargets.containsKey(method)){
				
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
				if(vb == null){//method and it's one distance
					ITypeBinding targetTypeBinding = refactorNodes.get(i).typeBinding;
					IVariableBinding targetBinding = CreateTargetField.startCreateTarget(unit, typeDeclaration, methodDeclaration, targetTypeBinding);
					methodAndTarget.put(method, targetBinding);
				}
				else{
					methodAndTarget.put(method, vb);
				}
				if(vbs.isEmpty()){//method and it's all distances
					ITypeBinding targetTypeBinding = refactorNodes.get(i).typeBinding;
					IVariableBinding targetBinding = CreateTargetField.startCreateTarget(unit, typeDeclaration, methodDeclaration, targetTypeBinding);
					vbs.add(targetBinding);
					methodAndTargets.put(method, vbs);
				}
				else{
					methodAndTargets.put(method, vbs);
				}
					
			}	
		}
//		System.out.println("methodAndTarget's size------------------  "+methodAndTargets.size());
//		for(IMethod method : allMethodsCanBeMoved){
//			System.out.println("the method can move ================================================================"+method.getElementName());
//			 System.out.println("distance==================================================="+methodAndTarget.get(method).getName()+"\n\n\n");
//		}
		//refactoringsExcute(allMethodsCanBeMoved,methodAndTarget);
		IMethod[] methods = new IMethod[accessors.size()];
		accessors.toArray(methods);
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
						node.variableBindings.add(bind);
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
	
//	public void addRelations(MoveMethodNode refactorNode,List<IType> types) throws Exception{
//		System.out.println("inter addRelations--------------");
//		for(int i = 0; i< types.size(); i++){
//			IType type  = types.get(i);
//			Relations relations0 = new Relations(i, 2,4,1);
//			ActionsAboutDB actionsAboutDB0 = new ActionsAboutDB();
//			actionsAboutDB0.insertRelations(relations0);
//			SearchAllTranferClass relatedClass1 = new SearchAllTranferClass(iProject, allMethods);
//			relatedClass1.getMoveMethodCandidates(type);
//			
//			System.out.println("");
//			for(MoveMethodNode refactorNode1 : relatedClass1.refactorNodes){
//				List<String> classNames = refactorNode.relationsClasses;
//				IMethod method = refactorNode.method;
//				ActionsAboutDB actionsAboutDB = new ActionsAboutDB();
//				int methodId = actionsAboutDB.getRelationsMethodID(method.getElementName(), method.getDeclaringType().getFullyQualifiedName());
//				int classId = actionsAboutDB.getRelationsClassID(method.getDeclaringType().getFullyQualifiedName());
//				int maxTableRow = actionsAboutDB.getTableMaxRow(1);
//				System.out.println("methodId-------------"+ methodId);
//				System.out.println("classId--------------"+ classId);
//				
//				Relations relations1 = new Relations(maxTableRow+1, methodId,classId,1);
//				actionsAboutDB.insertRelations(relations1);
//				
//				for(String className : classNames){
//					ActionsAboutDB actionsAboutDB1 = new ActionsAboutDB();
//					maxTableRow = actionsAboutDB1.getTableMaxRow(1);
//					classId = actionsAboutDB1.getRelationsClassID(className);
//					System.out.println("classId*********************"+ classId);
//					Relations relations = new Relations(maxTableRow+1, methodId,classId,0);
//					actionsAboutDB1.insertRelations(relations);
//					
//				}
//			}
//		}
//
//	}
	
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
