package com.zving.contentcore.privilege;

import com.zving.platform.privilege.AbstractMenuPriv;

public class FilePriv extends AbstractMenuPriv {
  public static final String MenuID = "ContentCore.FileMenu";
  public static final String Edit = "ContentCore.FileMenu.Edit";
  public static final String Delete = "ContentCore.FileMenu.Delete";
  public static final String Add = "ContentCore.FileMenu.Add";
  public static final String Rename = "ContentCore.FileMenu.Rename";
  public static final String Upload = "ContentCore.FileMenu.Upload";
  public static final String Export = "ContentCore.FileMenu.Export";
  public static final String COMPRESS_CSS_SCRIPT = "ContentCore.FileMenu.compressCSSScript";
  public static final String DELETE_SERVER = "ContentCore.FileMenu.DeleteServer";
  
  public FilePriv() {
    super("ContentCore.FileMenu", null);
    addItem("ContentCore.FileMenu.Add", "@{Contentcore.AddNewFileOrDir}");
    addItem("ContentCore.FileMenu.Edit", "@{Contentcore.EditFile}");
    addItem("ContentCore.FileMenu.Delete", "@{Common.Delete}");
    
    addItem("ContentCore.FileMenu.Rename", "@{Contentcore.Rename}");
    addItem("ContentCore.FileMenu.Upload", "@{Contentcore.Upload}");
    addItem("ContentCore.FileMenu.Export", "@{Contentcore.Export}");
    
    addItem(COMPRESS_CSS_SCRIPT, "压缩脚本样式");
    addItem(DELETE_SERVER, "删除分发服务器文件");
  }
}
