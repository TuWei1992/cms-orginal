package com.zving.schema;

import com.zving.framework.data.*;
import com.zving.framework.orm.*;
import com.zving.framework.utility.*;
import com.zving.framework.annotation.dao.*;
import java.util.*;

/**
 * <b>Book(Book)</b><br>
 * PrimaryKeys: <b>ID</b><br>
 */
@Table("Book")
@Indexes("")
public class Book extends DAO<Book> {
	
	
	@Column(type=DataTypes.LONG,mandatory=true,pk=true)
	protected Long ID;
	
	@Column(type=DataTypes.STRING,length=100)
	protected String ISBN;
	
	@Column(type=DataTypes.STRING,length=200)
	protected String Category;
	
	@Column(type=DataTypes.DATETIME)
	protected Date PublishTime;
	
		
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
	* <b>ISBN(ISBN编码)</b>,<b></b><br>
	*/
	public String getISBN() {
		return ISBN;		
	}

	/**
	* <b>ISBN(ISBN编码)</b>, <b></b>, <br>
	*/
	public void setISBN(String iSBN) {
		this.ISBN = iSBN;
  }
  	
	/**
	* <b>Category(图书分类)</b>,<b></b><br>
	*/
	public String getCategory() {
		return Category;		
	}

	/**
	* <b>Category(图书分类)</b>, <b></b>, <br>
	*/
	public void setCategory(String category) {
		this.Category = category;
  }
  	
	/**
	* <b>PublishTime(出版时间)</b>,<b></b><br>
	*/
	public Date getPublishTime() {
		return PublishTime;		
	}

	/**
	* <b>PublishTime(出版时间)</b>, <b></b>, <br>
	*/
	public void setPublishTime(Date publishTime) {
		this.PublishTime = publishTime;
  }
  
	/**
	* <b>PublishTime(出版时间)</b>, <b></b>, <br>
	*/
	public void setPublishTime(String publishTime) {
		if (StringUtil.isNull(publishTime)){
			this.PublishTime = null;
			return;
		}
		this.PublishTime = DateUtil.parseDateTime(publishTime);;
 	}
}