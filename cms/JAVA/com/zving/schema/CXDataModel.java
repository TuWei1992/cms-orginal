package com.zving.schema;

import com.zving.framework.data.*;
import com.zving.framework.orm.*;
import com.zving.framework.utility.*;
import com.zving.framework.annotation.dao.*;
import java.util.*;

/**
 * <b>CXDataModel(车享数据模型表)</b><br>
 * PrimaryKeys: <b>ID</b><br>
 */
@Table("CXDataModel")
@Indexes("")
public class CXDataModel extends DAO<CXDataModel> {
	
	
	@Column(type=DataTypes.LONG,mandatory=true,pk=true)
	protected Long ID;
	
	@Column(type=DataTypes.STRING,length=200,mandatory=true)
	protected String APIName;
	
	@Column(type=DataTypes.STRING,length=40,mandatory=true)
	protected String Code;
	
	@Column(type=DataTypes.STRING,length=200,mandatory=true)
	protected String Name;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String Memo;
	
	@Column(type=DataTypes.STRING,length=2)
	protected String PageFlag;
	
	@Column(type=DataTypes.LONG)
	protected Long OrderFlag;
	
	@Column(type=DataTypes.DATETIME,mandatory=true)
	protected Date AddTime;
	
	@Column(type=DataTypes.STRING,length=200,mandatory=true)
	protected String AddUser;
	
	@Column(type=DataTypes.DATETIME)
	protected Date ModifyTime;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String ModifyUser;
	
		
	/**
	* <b>ID(数据配置ID)</b>,<b></b>,<b>primary key</b>,<b>mandatory</b><br>
	*/
	public long getID() {
		if(ID==null){return 0;}
		return ID;		
	}

	/**
	* <b>ID(数据配置ID)</b>, <b></b>, , part of <b>Primary Keys</b>, <b>mandatory</b><br>
	*/
	public void setID(long iD) {
		this.ID = new Long(iD);
  }
  
	/**
	* <b>ID(数据配置ID)</b>, <b></b>, , part of <b>Primary Keys</b>, <b>mandatory</b><br>
	*/
	public void setID(String iD) {
		if (StringUtil.isNull(iD)){
			this.ID = null;
			return;
		}
		this.ID = new Long(iD);;
 	}	
	/**
	* <b>APIName(接口名称)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getAPIName() {
		return APIName;		
	}

	/**
	* <b>APIName(接口名称)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAPIName(String aPIName) {
		this.APIName = aPIName;
  }
  	
	/**
	* <b>Code(数据代码)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getCode() {
		return Code;		
	}

	/**
	* <b>Code(数据代码)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setCode(String code) {
		this.Code = code;
  }
  	
	/**
	* <b>Name(数据名称)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getName() {
		return Name;		
	}

	/**
	* <b>Name(数据名称)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setName(String name) {
		this.Name = name;
  }
  	
	/**
	* <b>Memo(备注)</b>,<b></b><br>
	*/
	public String getMemo() {
		return Memo;		
	}

	/**
	* <b>Memo(备注)</b>, <b></b>, <br>
	*/
	public void setMemo(String memo) {
		this.Memo = memo;
  }
  	
	/**
	* <b>PageFlag(是否分页)</b>,<b></b><br>
	*/
	public String getPageFlag() {
		return PageFlag;		
	}

	/**
	* <b>PageFlag(是否分页)</b>, <b></b>, <br>
	*/
	public void setPageFlag(String pageFlag) {
		this.PageFlag = pageFlag;
  }
  	
	/**
	* <b>OrderFlag(排序字段)</b>,<b></b><br>
	*/
	public long getOrderFlag() {
		if(OrderFlag==null){return 0;}
		return OrderFlag;		
	}

	/**
	* <b>OrderFlag(排序字段)</b>, <b></b>, <br>
	*/
	public void setOrderFlag(long orderFlag) {
		this.OrderFlag = new Long(orderFlag);
  }
  
	/**
	* <b>OrderFlag(排序字段)</b>, <b></b>, <br>
	*/
	public void setOrderFlag(String orderFlag) {
		if (StringUtil.isNull(orderFlag)){
			this.OrderFlag = null;
			return;
		}
		this.OrderFlag = new Long(orderFlag);;
 	}	
	/**
	* <b>AddTime(增加时间)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public Date getAddTime() {
		return AddTime;		
	}

	/**
	* <b>AddTime(增加时间)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddTime(Date addTime) {
		this.AddTime = addTime;
  }
  
	/**
	* <b>AddTime(增加时间)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddTime(String addTime) {
		if (StringUtil.isNull(addTime)){
			this.AddTime = null;
			return;
		}
		this.AddTime = DateUtil.parseDateTime(addTime);;
 	}	
	/**
	* <b>AddUser(增加人)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getAddUser() {
		return AddUser;		
	}

	/**
	* <b>AddUser(增加人)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddUser(String addUser) {
		this.AddUser = addUser;
  }
  	
	/**
	* <b>ModifyTime(修改时间)</b>,<b></b><br>
	*/
	public Date getModifyTime() {
		return ModifyTime;		
	}

	/**
	* <b>ModifyTime(修改时间)</b>, <b></b>, <br>
	*/
	public void setModifyTime(Date modifyTime) {
		this.ModifyTime = modifyTime;
  }
  
	/**
	* <b>ModifyTime(修改时间)</b>, <b></b>, <br>
	*/
	public void setModifyTime(String modifyTime) {
		if (StringUtil.isNull(modifyTime)){
			this.ModifyTime = null;
			return;
		}
		this.ModifyTime = DateUtil.parseDateTime(modifyTime);;
 	}	
	/**
	* <b>ModifyUser(修改人)</b>,<b></b><br>
	*/
	public String getModifyUser() {
		return ModifyUser;		
	}

	/**
	* <b>ModifyUser(修改人)</b>, <b></b>, <br>
	*/
	public void setModifyUser(String modifyUser) {
		this.ModifyUser = modifyUser;
  }
  
}