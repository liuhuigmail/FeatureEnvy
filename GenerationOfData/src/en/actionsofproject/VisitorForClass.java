package en.actionsofproject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class VisitorForClass extends ASTVisitor {
	
	private IMethod caller = null;
	private String currentClass = null;
	private ITypeBinding dataClass;
	public Set<IVariableBinding> targets = new HashSet<>();
	public MethodDeclaration methodDeclaration = null;
	public TypeDeclaration typeDeclaration = null;
	
	public VisitorForClass(IMethod caller){
		this.caller = caller;
		
	}
	
	public boolean visit(TypeDeclaration node){
		this.dataClass = node.resolveBinding();
		if(!node.getName().toString().equals(currentClass))
			return false;
		for(IVariableBinding field : node.resolveBinding().getDeclaredFields())
			if(field.getType().getName().equals(dataClass.getName())){
				targets.add(field);
			}
		typeDeclaration = node;
		return true;
	}
	
	public boolean visit(ImportDeclaration node){
		return false;
	}
	
	public boolean visit(PackageDeclaration node){
		return false;
	}
	
	public boolean visit(FieldDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return false;
	}
	@SuppressWarnings({ "rawtypes" })
	public boolean visit(MethodDeclaration node){
		if(node.isConstructor() || (node.getModifiers() & Modifier.ABSTRACT) != 0)
			return false;
		IMethod me = (IMethod)node.resolveBinding().getJavaElement();
		if(!me.equals(caller)){
			return false;
		}
		try {
			String returnType = caller.getReturnType();
			ITypeBinding returnType2 = node.getReturnType2().resolveBinding();
			if(returnType != null && returnType2 != null && !Signature.createTypeSignature(returnType2.getName(), false).equals(returnType))
				return false;
			else if(returnType != null && returnType2 == null)
				return false;
			else if(returnType == null && returnType2 != null)
				return false;
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] parameters = caller.getParameterTypes();
		List list = node.parameters();
		if(list.size() != parameters.length)
			return false;
		else{
			for(int i=0; i<list.size(); i++){
				if(list.get(i) instanceof SingleVariableDeclaration){
					ITypeBinding parameter = ((SingleVariableDeclaration)list.get(i)).resolveBinding().getType();
					if(parameter == null)
						return false;  
					if(!Signature.createTypeSignature(parameter.getName(), false).equals(parameters[i]))
							return false;
					if(parameter.getName().equals(dataClass.getName())){
						targets.add(((SingleVariableDeclaration) list.get(i)).resolveBinding());
						break;	
					}
				}
			}
		}
		methodDeclaration = node;
		return true;
	}

}
