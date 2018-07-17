package en.actionsofproject.database;

import java.sql.*;
import java.util.List;

import en.actionsofproject.database.ui.ClassInfo;
import en.actionsofproject.database.ui.DistanceValue;
import en.actionsofproject.database.ui.EPValue;
import en.actionsofproject.database.ui.MethodInfo;
import en.actionsofproject.database.ui.Relations;

public class ActionsAboutDB {
	
	Connection conn;
	public ActionsAboutDB(){
		try {
			this.conn = getConn();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Connection getConn() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/testdata",
                "root","12345");
        
        //Statement stmt =  conn.createStatement();
        return conn;
	}
	public int getTableMaxRow(int i) throws Exception{
		String sql = null;
		Connection conn = getConn();
		if(i == 1){
			sql = "select max(KeyNum) from relations;";
		}else{
			if(i == 2){
				sql = "select max(MethodID) from methodinfo;";
			}else{
				if(i == 3){
					sql = "select max(ClassID) from classinfo;";
				}
			}		
		}
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    //System.out.println("getTableMaxRow"+rs.getInt(0));
	    int maxRow = 0;
	    if(rs.next()){
	    	if(i == 1)
	    		maxRow = rs.getInt("max(KeyNum)");
	    	else
	    		if(i == 2)
	    			maxRow = rs.getInt("max(MethodID)");
	    		else{
	    			maxRow = rs.getInt("max(ClassID)");
	    		}
	    			
	    }
	    pstmt.close();
		conn.close();
	    return maxRow;
	}
	public int getMaxTimes() throws Exception{
		String sql =  "select max(NumOfTimes) from classinfo;";
		Connection conn = getConn();
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    int maxTimes = 0;
	    if(rs.next()){
	    	maxTimes = rs.getInt("max(NumOfTimes)");
	    }
	    pstmt.close();
		conn.close();
	    return maxTimes;
		
	}
	public void delete(int i) throws Exception{
		String sql = null;
		if(i == 1){
			sql = "delete from relations;";
		}else 
			if(i == 2){
				sql = "delete from methodinfo;";
			}else
				sql = "delete from classinfo;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    pstmt.close();
		conn.close();
	}
	public int getRelationsClassID(String className) throws Exception {
		int classId = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select ClassID from ClassInfo where ClassQualifiedName = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, className);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	classId = rs.getInt("ClassID");
	    }
		pstmt.close();
		conn.close();
		return classId;
	}
	public int getRelationsMethodID(String methodName, String methodparameters, String className) throws Exception {
		int methodId = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select methodID from MethodInfo where methodName = ? and methodParameters = ? and methodOfClass = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodparameters);
		pstmt.setString(3, className);
	    ResultSet rs = pstmt.executeQuery();
	    while(rs.next()){
	    	methodId = rs.getInt("MethodID");
	    }
		pstmt.close();
		conn.close();
		return methodId;
	}
	
	public int insertClassInfo(ClassInfo classInfo) throws Exception{
		int i = 0;
		if(whetherClassIsExistOrNot(classInfo.getClassQualifiedName())==0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into ClassInfo (ClassID,ClassQualifiedName,ClassName) values(?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, classInfo.getClassID());
			pstmt.setString(2, classInfo.getClassQualifiedName());
			pstmt.setString(3, classInfo.getClassName());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
		
		return i; 
	}
	public int whetherClassIsExistOrNot(String classQualifiedName) throws Exception{
		int i=0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from ClassInfo where classQualifiedName = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, classQualifiedName);
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		System.out.println("whetherClassIsExistOrNot-----------"+i);
		return i;
	}
//	public int insertEPValue(EPValue epvalue) throws Exception{
//		int i = 0;
//		String sql = "insert into EPValue (ClassID,ClassName,EntityPlacement) values(?,?,?);";
//		PreparedStatement pstmt;
//		pstmt = (PreparedStatement) conn.prepareStatement(sql);
//		pstmt.setInt(1, epvalue.getClassID());
//		pstmt.setString(2, epvalue.getClassName());
//		pstmt.setDouble(3, epvalue.getEntityPlacement());
//		i = pstmt.executeUpdate();
//		pstmt.close();
//		conn.close();
//		return i;	 
//	}
	public int insertMethodInfo(MethodInfo methodInfo) throws Exception{
		int i = 0;
		if(whetherMethodIsExistOrNot(methodInfo.getMethodName(),methodInfo.getMethodKey(),methodInfo.getMethodOfClass())==0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into methodinfo (MethodID, MethodName, MethodParameters, MethodOfClass) values(?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, methodInfo.getMethodID());
			pstmt.setString(2, methodInfo.getMethodName());
			pstmt.setString(3, methodInfo.getMethodKey());
			pstmt.setString(4, methodInfo.getMethodOfClass());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
//		System.out.println("methodinfo the num of insert----" + i);
		return i;
	}
	public int whetherMethodIsExistOrNot(String methodName, String methodKey, String methodOfClass) throws Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from MethodInfo where methodName = ? and methodParameters = ? and methodOfClass = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodKey);
		pstmt.setString(3, methodOfClass);
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		System.out.println("whether Method Is Exist Or Not----------"+i);
		return i;
	}

	public int insertRelations(Relations relations) throws Exception{
		int i = 0;
		if(whetherRelationsIsExistOrNot(relations) == 0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into relations (KeyNum, ClassID, MethodID,MethodInThisClassOrNot) values(?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, relations.getKey());
			pstmt.setInt(2, relations.getClassID());
			pstmt.setInt(3, relations.getMethodID());
			pstmt.setInt(4, relations.getMethodInThisClassOrNot());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
		return i;
	}
	public int whetherRelationsIsExistOrNot(Relations relations) throws Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql ="select * from relations where MethodID = ? and ClassID = ?;";
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) conn.prepareStatement(sql);
		pstmt.setInt(1, relations.getMethodID());
		pstmt.setInt(2, relations.getClassID());
		ResultSet rs = pstmt.executeQuery();
		if(rs.next())
			i = 1;
		pstmt.close();
		conn.close();
		
		return i;
	}
	public int insertDistanceValue(DistanceValue distanceValue) throws Exception{
		
		int i = 0;
		if(whetherClassExistOrNot(distanceValue.getMethodName(),distanceValue.getMethodKey(), distanceValue.getMethodOfClass(),distanceValue.getClassName()) == 0){
			if(conn.isClosed()){
				conn = getConn();
			}
			String sql = "insert into distanceValue1 (methodId,methodName,methodParameters, methodOfClass,className,distance) values(?,?,?,?,?,?);";
			PreparedStatement pstmt;
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, distanceValue.getMethodId());
			pstmt.setString(2, distanceValue.getMethodName());
			pstmt.setString(3, distanceValue.getMethodKey());
			pstmt.setString(4, distanceValue.getMethodOfClass());
			pstmt.setString(5, distanceValue.getClassName());
			pstmt.setDouble(6, distanceValue.getDistance());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		}
//		System.out.println("----------"+i);
	
		return i;	 
	}
	public int getTableMaxRowofDistance() throws Exception{
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select max(methodid) from distanceValue1;";
			
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
	    ResultSet rs = pstmt.executeQuery();
	    //System.out.println("getTableMaxRow"+rs.getInt(0));
	    int maxRow = 0;
	    if(rs.next()){
			maxRow = rs.getInt("max(methodid)");		
	    }
	    else
	    	return 0;
	    pstmt.close();
		conn.close();
	    return maxRow;
	}
	public int whetherClassExistOrNot(String methodName,String methodParameters, String methodOfClass, String className) throws SQLException, Exception{
		int i = 0;
		if(conn.isClosed()){
			conn = getConn();
		}
		String sql = "select * from DistanceValue1 where methodName = ? and methodParameters = ? and methodofclass = ? and className = ?;";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		pstmt.setString(1, methodName);
		pstmt.setString(2, methodParameters);
		pstmt.setString(3, methodOfClass);
		pstmt.setString(4, className);
	    ResultSet rs = pstmt.executeQuery();
	    if(rs.next()){
	    	i = 1;
	    }
	    System.out.println("whetherClassExistOrNot-----"+i);
	    pstmt.close();
		conn.close();
		return i;
	}
	public void commitMySQL(){
		try {
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
