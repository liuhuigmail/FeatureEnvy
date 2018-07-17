package en.actionsofproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import en.actionsofproject.SearchAllTranferClass.VisitorForMethod;
import en.entitys.Entity;
import en.movemethod.MoveMethodNode;

public class OneMethodAboutRelationedClass {
	
	public List<ITypeBinding> typeBindingCanMove = new ArrayList<>();//the classes can be moved
	public List<MoveMethodNode> refactorNodes = new ArrayList<>();//有几个可以移动到的目的地类，就有几个节点
	public List<IVariableBinding> IVarbindsCanMove = new ArrayList<>();
	public Map<IMethod, List<IVariableBinding>> methodAndTargets = new HashMap();
	public Map<IMethod, List<String>> methodAndItsdestinations = new HashMap();
	public TypeDeclaration typeDeclarationOfCurrentClass;
	Map<IMethod, IVariableBinding> methodAndTarget = new HashMap();
	List<String> classNames = new ArrayList<String>();
	public ITypeBinding currentClass;
	public IMethod currentIMethod;
	
	public void getRelationsClass(IType type, IMethod method){
		Entity entity = new Entity(type);
		typeDeclarationOfCurrentClass = entity.getTypeDeclaration();
		currentClass = typeDeclarationOfCurrentClass.resolveBinding();
		
		//System.out.println("method name --------------------"+method.getElementName());
		Entity methodEntity = new Entity(method);
		MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
		if(methodDeclaration != null){
			IVarbindsCanMove.clear();
			calVarbingdsCanMove(methodDeclaration, method);
			currentIMethod = method;
			VisitorForMethod vis = new VisitorForMethod();
			typeDeclarationOfCurrentClass.accept(vis);
			
			classNames.clear();
			for(int j = 0; j < IVarbindsCanMove.size(); j++){
				String className = IVarbindsCanMove.get(j).getType().getQualifiedName();
				//System.out.println("relations-----------------"+method.getElementName()+"--------------"+IVarbindsCanMove.get(j).getType().getQualifiedName());
				if(!classNames.contains(className))
					classNames.add(className);
			}
			methodAndItsdestinations.put(method, classNames);
		}
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
