package en.actionsofproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

public class JudgeMethodWhetherMove {
	
	private static  boolean isRecursive = false;
	private static  boolean methodHasSuper = false;
	private IType type;
	
	public JudgeMethodWhetherMove(IType type){
		this.type = type;
		
	}
	
	public boolean methodCanMove(IMethod method, MethodDeclaration methodDeclaration){
		
		if(!methodDeclaration.resolveBinding().getDeclaringClass().isClass())
			return false;
		
		try {
			if(method.isConstructor() || Flags.isStatic(method.getFlags()) || method.isMainMethod() || Flags.isAbstract(method.getFlags())){
				return false;
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if(methodIsRecursive(methodDeclaration)){
//			return false;
//		}
//		if(methodIsOverride(methodDeclaration.resolveBinding())){
//			return false;
//		}
		
//		if(sameWithTypeMethods(method))
//			return false;
		methodDeclaration.accept(new Visitor());
		if(methodHasSuper){
			methodHasSuper = false;
			return false;
		}
		
		return true;
		}
		public boolean methodIsImplemented(IMethodBinding methodBinding){
			
			ITypeBinding typeBinding = methodBinding.getDeclaringClass(); 
			List<IMethodBinding> allIMethodBindings = new ArrayList<IMethodBinding>();
			for(ITypeBinding impledITypeBinding : typeBinding.getInterfaces()){
				for(IMethodBinding binding : impledITypeBinding.getDeclaredMethods()){
					allIMethodBindings.add(binding);
				}
			}
			
			if(typeBinding.getSuperclass() != null){
				ITypeBinding superClassBinding = typeBinding.getSuperclass();
				IType superType = (IType)superClassBinding.getJavaElement();
				try {
					if(Flags.isAbstract(superType.getFlags())){//如果是抽象类
						for(ITypeBinding binding : superClassBinding.getInterfaces()){
							for(IMethodBinding binding2 : binding.getDeclaredMethods()){
								allIMethodBindings.add(binding2);
							}
						}
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for(IMethodBinding mBinding : allIMethodBindings){
				
				if(methodBinding.overrides(mBinding)){
					return true;
				}
			}
			
			return false;
		}
		
		private boolean sameWithTypeMethods(IMethod method1){
			try {
				IMethod[] methodsExists = type.getMethods();
				
				for(IMethod method:methodsExists){
					//DetectSchizophrenicClass.methodEquals(method1, method)
					if(methodEquals(method, method1)){
						return true;
					}
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		public static boolean methodEquals(IMethod method1, IMethod method2){
			try {
				if(!method1.getElementName().equals(method2.getElementName())){
					return false;
				}

				String[] pars1 =method1.getParameterTypes();
				String[] pars2 = method2.getParameterTypes();
				if((pars1.length==0)&&(pars2.length ==0))
					return true;
				
				if(pars1.length == pars2.length){
					for(int k=0; k<pars1.length; k++){		
						String par1 = JavaModelUtil.getResolvedTypeName(pars1[k], method1.getDeclaringType());
						String par2 = JavaModelUtil.getResolvedTypeName(pars2[k], method2.getDeclaringType());
						if((!par1.equals("T"))&&(!par1.equals(par2)))
							return false;
					}
					return true;
				}

			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public  boolean methodIsOverride(IMethodBinding methodBinding){
			ITypeBinding typeBinding2 = methodBinding.getDeclaringClass();
			
			List<IMethodBinding> allIMethodBindings = new ArrayList<IMethodBinding>();
			
			while(typeBinding2.getSuperclass() != null){
				ITypeBinding superITypeBinding = typeBinding2.getSuperclass();
				for(IMethodBinding binding : superITypeBinding.getDeclaredMethods()){
					allIMethodBindings.add(binding);
				}
				typeBinding2 = superITypeBinding;
			}
			
			
			for(IMethodBinding mBinding : allIMethodBindings){
				if(methodBinding.overrides(mBinding)){
					return true;
				}   
			}
			
			return false;
		}
		public  boolean methodIsRecursive(MethodDeclaration methodDeclaration){
			final IMethod method = (IMethod)methodDeclaration.resolveBinding().getJavaElement();
			//判断是否是递归
			 methodDeclaration.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation node){
					if(node.getName().toString().equals(method.getElementName())){
						if(((IMethod)node.resolveMethodBinding().getJavaElement()).equals(method)){
							isRecursive = true;
						}
					}
					return true;
				}
			});
			return isRecursive;
		}
		 class Visitor extends ASTVisitor{
				@Override
				public boolean visit(SuperFieldAccess node) {
					// TODO Auto-generated method stub
					methodHasSuper = true;
					return true;
				}

				@Override
				public boolean visit(SuperMethodInvocation node) {
					// TODO Auto-generated method stub
					methodHasSuper = true;
					return true;
				}


			}

}
