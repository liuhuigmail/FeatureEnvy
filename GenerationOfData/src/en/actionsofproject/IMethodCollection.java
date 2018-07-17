package en.actionsofproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import en.entitys.Entity;


public class IMethodCollection {
	
	private static final String EMPTY[] = new String[0]; 
	
	public List<IMethod> collectAccessors(IType type) {
		List<IMethod> accessors = new ArrayList<IMethod>();
		try {
			IField[] fields = type.getFields();
			IMethod method = null;
			for(IField field : fields){
				method = getGetter(field);
				if(method != null){
					Entity methodEntity = new Entity(method);
					MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
					if(isGetterOrSetter(methodDeclaration))
						accessors.add(method); 
				}
				method = null;
				method = getSetter(field);
				if(method != null){
					Entity methodEntity = new Entity(method);
					MethodDeclaration methodDeclaration = (MethodDeclaration) methodEntity.getAssociatedNode();
					if(isGetterOrSetter(methodDeclaration))
						accessors.add(method); 
				}
				method = null;
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return accessors;
	}
	public IMethod getGetter(IField field) throws JavaModelException {  
        String getterName = getGetterName(field, EMPTY, true);  
        IMethod primaryCandidate = JavaModelUtil.findMethod(getterName,  
                new String[0], false, field.getDeclaringType());  
        if (!JavaModelUtil.isBoolean(field) || primaryCandidate != null  
                && primaryCandidate.exists()) {  
            return primaryCandidate;  
        } else {  
            String secondCandidateName = getGetterName(field, EMPTY, false);  
            return JavaModelUtil.findMethod(secondCandidateName, new String[0],  
                    false, field.getDeclaringType());  
        }  
    }  
  
    public IMethod getSetter(IField field) throws JavaModelException {  
        String args[] = { field.getTypeSignature() };  
        return JavaModelUtil.findMethod(getSetterName(field, EMPTY), args,  
                false, field.getDeclaringType());  
    } 
    
    private String getGetterName(IField field, String excludedNames[],  
            boolean useIsForBoolGetters) throws JavaModelException {  
        if (excludedNames == null)  
            excludedNames = EMPTY;  
        return getGetterName(field.getJavaProject(), field.getElementName(),  
                field.getFlags(), useIsForBoolGetters  
                        && JavaModelUtil.isBoolean(field), excludedNames);  
    }  
  
    public String getGetterName(IJavaProject project, String fieldName,  
            int flags, boolean isBoolean, String excludedNames[]) {  
        return NamingConventions.suggestGetterName(project, fieldName, flags,  
                isBoolean, excludedNames);  
    }  
    public String getSetterName(IField field, String excludedNames[])  
            throws JavaModelException {  
        if (excludedNames == null)  
            excludedNames = EMPTY;  
        return NamingConventions.suggestSetterName(field.getJavaProject(),  
                field.getElementName(), field.getFlags(), JavaModelUtil  
                        .isBoolean(field), excludedNames);  
    } 
    public static  boolean isGetterOrSetter(MethodDeclaration node){
		IMethodBinding binding = node.resolveBinding();
		String methodName = binding.getName();
	
		if(methodName.length() <= 3)
			return false;
		if(methodName.startsWith("get")){
			String targetField = methodName.substring(3);
			IVariableBinding[] fields = node.resolveBinding().getDeclaringClass().getDeclaredFields();
			for(IVariableBinding field : fields){
				if(field.getName().equalsIgnoreCase(targetField) && field.getType().equals(node.resolveBinding().getReturnType()))
				{
					return true;
				}
			}
		}
		if(methodName.startsWith("set")){
			String targetField = methodName.substring(3);
			IVariableBinding[] fields = node.resolveBinding().getDeclaringClass().getDeclaredFields();
			for(IVariableBinding field : fields)
				if(field.getName().equalsIgnoreCase(targetField)){
					ITypeBinding[] parameterTypes = node.resolveBinding().getParameterTypes();
					if(parameterTypes.length == 1 && field.getType().equals(parameterTypes[0]))
					{
						return true;
					}
				}
		}
		return false;
	}

}
