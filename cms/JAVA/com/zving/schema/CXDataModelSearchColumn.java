package com.zving.schema;

import com.zving.framework.data.*;
import com.zving.framework.orm.*;
import com.zving.framework.utility.*;
import com.zving.framework.annotation.dao.*;
import java.util.*;

/**
 * <b>CXDataModelSearchColumn(车享数据字段表)</b><br>
 * PrimaryKeys: <b>ID</b><br>
 */
@Table("CXDataModelSearchColumn")
@Indexes("")
public class CXDataModelSearchColumn extends DAO<CXDataModelSearchColumn> {
	
	
	@Column(type=DataTypes.LONG,mandatory=true,pk=true)
	protected Long ID;
	
	@Column(type=DataTypes.LONG,mandatory=true)
	protected Long ModelID;
	
	@Column(type=DataTypes.STRING,length=50,mandatory=true)
	protected String Code;
	
	@Column(type=DataTypes.STRING,length=2000,mandatory=true)
	protected String Name;
	
	@Column(type=DataTypes.STRING,length=20,mandatory=true)
	protected String ControlType;
	
	@Column(type=DataTypes.STRING,length=2,mandatory=true)
	protected String MandatoryFlag;
	
	@Column(type=DataTypes.STRING,length=2000)
	protected String ListOptions;
	
	@Column(type=DataTypes.STRING,length=1000)
	protected String DefaultValue;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String VerifyRule;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String VerifyCondition;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String StyleClass;
	
	@Column(type=DataTypes.STRING,length=400)
	protected String StyleText;
	
	@Column(type=DataTypes.STRING,length=2)
	protected String ArrayFlag;
	
	@Column(type=DataTypes.STRING,length=1000)
	protected String Memo;
	
	@Column(type=DataTypes.LONG)
	protected Long OrderFlag;
	
	@Column(type=DataTypes.STRING,length=50,mandatory=true)
	protected String AddUser;
	
	@Column(type=DataTypes.DATETIME,mandatory=true)
	protected Date AddTime;
	
	@Column(type=DataTypes.STRING,length=50)
	protected String ModifyUser;
	
	@Column(type=DataTypes.DATETIME)
	protected Date ModifyTime;
	
		
	/**
	* <b>ID(ID)</b>,<b></b>,<b>primary key</b>,<b>mandatory</b><br>
	*/
	public long getID() {
		if(ID==null){return 0;}
		return ID;		
	}

	/**
	* <b>ID(ID)</b>, <b></b>, , part of <b>Primary Keys</b>, <b>mandatory</b><br>
	*/
	public void setID(long iD) {
		this.ID = new Long(iD);
  }
  
	/**
	* <b>ID(ID)</b>, <b></b>, , part of <b>Primary Keys</b>, <b>mandatory</b><br>
	*/
	public void setID(String iD) {
		if (StringUtil.isNull(iD)){
			this.ID = null;
			return;
		}
		this.ID = new Long(iD);;
 	}	
	/**
	* <b>ModelID(数据模型ID)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public long getModelID() {
		if(ModelID==null){return 0;}
		return ModelID;		
	}

	/**
	* <b>ModelID(数据模型ID)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setModelID(long modelID) {
		this.ModelID = new Long(modelID);
  }
  
	/**
	* <b>ModelID(数据模型ID)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setModelID(String modelID) {
		if (StringUtil.isNull(modelID)){
			this.ModelID = null;
			return;
		}
		this.ModelID = new Long(modelID);;
 	}	
	/**
	* <b>Code(字段代码)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getCode() {
		return Code;		
	}

	/**
	* <b>Code(字段代码)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setCode(String code) {
		this.Code = code;
  }
  	
	/**
	* <b>Name(字段名称)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getName() {
		return Name;		
	}

	/**
	* <b>Name(字段名称)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setName(String name) {
		this.Name = name;
  }
  	
	/**
	* <b>ControlType(控件类型)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getControlType() {
		return ControlType;		
	}

	/**
	* <b>ControlType(控件类型)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setControlType(String controlType) {
		this.ControlType = controlType;
  }
  	
	/**
	* <b>MandatoryFlag(是否必填)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getMandatoryFlag() {
		return MandatoryFlag;		
	}

	/**
	* <b>MandatoryFlag(是否必填)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setMandatoryFlag(String mandatoryFlag) {
		this.MandatoryFlag = mandatoryFlag;
  }
  	
	/**
	* <b>ListOptions(数据选项)</b>,<b></b><br>
	*/
	public String getListOptions() {
		return ListOptions;		
	}

	/**
	* <b>ListOptions(数据选项)</b>, <b></b>, <br>
	*/
	public void setListOptions(String listOptions) {
		this.ListOptions = listOptions;
  }
  	
	/**
	* <b>DefaultValue(默认值)</b>,<b></b><br>
	*/
	public String getDefaultValue() {
		return DefaultValue;		
	}

	/**
	* <b>DefaultValue(默认值)</b>, <b></b>, <br>
	*/
	public void setDefaultValue(String defaultValue) {
		this.DefaultValue = defaultValue;
  }
  	
	/**
	* <b>VerifyRule(校验规则)</b>,<b></b><br>
	*/
	public String getVerifyRule() {
		return VerifyRule;		
	}

	/**
	* <b>VerifyRule(校验规则)</b>, <b></b>, <br>
	*/
	public void setVerifyRule(String verifyRule) {
		this.VerifyRule = verifyRule;
  }
  	
	/**
	* <b>VerifyCondition(校验条件)</b>,<b></b><br>
	*/
	public String getVerifyCondition() {
		return VerifyCondition;		
	}

	/**
	* <b>VerifyCondition(校验条件)</b>, <b></b>, <br>
	*/
	public void setVerifyCondition(String verifyCondition) {
		this.VerifyCondition = verifyCondition;
  }
  	
	/**
	* <b>StyleClass(样式类)</b>,<b></b><br>
	*/
	public String getStyleClass() {
		return StyleClass;		
	}

	/**
	* <b>StyleClass(样式类)</b>, <b></b>, <br>
	*/
	public void setStyleClass(String styleClass) {
		this.StyleClass = styleClass;
  }
  	
	/**
	* <b>StyleText(样式文本)</b>,<b></b><br>
	*/
	public String getStyleText() {
		return StyleText;		
	}

	/**
	* <b>StyleText(样式文本)</b>, <b></b>, <br>
	*/
	public void setStyleText(String styleText) {
		this.StyleText = styleText;
  }
  	
	/**
	* <b>ArrayFlag(是否是集合)</b>,<b></b><br>
	*/
	public String getArrayFlag() {
		return ArrayFlag;		
	}

	/**
	* <b>ArrayFlag(是否是集合)</b>, <b></b>, <br>
	*/
	public void setArrayFlag(String arrayFlag) {
		this.ArrayFlag = arrayFlag;
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
	* <b>AddUser(添加人)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public String getAddUser() {
		return AddUser;		
	}

	/**
	* <b>AddUser(添加人)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddUser(String addUser) {
		this.AddUser = addUser;
  }
  	
	/**
	* <b>AddTime(添加时间)</b>,<b></b>,<b>mandatory</b><br>
	*/
	public Date getAddTime() {
		return AddTime;		
	}

	/**
	* <b>AddTime(添加时间)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddTime(Date addTime) {
		this.AddTime = addTime;
  }
  
	/**
	* <b>AddTime(添加时间)</b>, <b></b>, , <b>mandatory</b><br>
	*/
	public void setAddTime(String addTime) {
		if (StringUtil.isNull(addTime)){
			this.AddTime = null;
			return;
		}
		this.AddTime = DateUtil.parseDateTime(addTime);;
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
}