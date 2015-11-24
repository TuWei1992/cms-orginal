package com.zving.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.mozilla.javascript.tools.ToolErrorReporter;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.zving.cxdata.UCMConfig;
import com.zving.framework.utility.Errorx;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;


public class YuiCompressUtil {
    
    public static void main(String[] args){
        Collection<File> files = getFiles("D:/test/", null, new String[]{"a", "jquery.js","jquery.vsdoc.js","wow.js"}, null,  new String[]{".js", ".css"});
        for(File file : files){            
            System.out.println(file.getAbsolutePath());
             if (file.getName().endsWith(".css")) {
            	 compressCss(file);
             } else {
            	 compressJs(file);
             }
             
        }
    }
    
    
    /**
     * 压缩指定的css文件
     * @param file      待压缩的文件
     */
    public static void compressCss(File file) {
           compressFile(file, ".css");
       }
    
    
    /**
     * 压缩指定的js文件.
     * @param file      待压缩的文件
     */
    public static void compressJs(File file){
        compressFile(file, ".js");
    }
    
    
    /**
     * 压缩指定的文件.
     * @param file      待压缩的文件
     * @param suffix    后缀名
     */
    private static void compressFile(File file, String suffix){
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        try {
            in = new InputStreamReader(new FileInputStream(file), "utf-8");
            String compressedFileName = file.getParent()+"/"+file.getName().substring(0, file.getName().lastIndexOf("."))+".min"+suffix;
            out = new OutputStreamWriter(new FileOutputStream(compressedFileName), "utf-8");
            if(".js".equalsIgnoreCase(suffix)){
                //将error转化为异常抛出.
                ToolErrorReporter t = new ToolErrorReporter(false);
                JavaScriptCompressor jsc = new JavaScriptCompressor(in, t);
                jsc.compress(out, -1, true, false, false, false);
            }else if(".css".equalsIgnoreCase(suffix)){
                CssCompressor csscompressor = new CssCompressor(in);  
                csscompressor.compress(out, -1);  
            }
            out.flush();
        }catch(Exception e){
            LogUtil.error(e);
        	Errorx.addError(file.getAbsolutePath() + " " + e.getMessage());
        }finally{
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.error(e);
            }
            try {
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
    }
    
    
    /**
     * 获得指定目录下，指定的文件。包含、不包含、前缀、后缀之间，为且的关系
     * @param rootDirStr    指定根目录
     * @param includes      文件名包含哪些内容，多个之间为或的关系
     * @param excludes      文件名不包含哪些内容，多个之间为且的关系
     * @param prefix        文件名包含哪些前缀
     * @param suffix        文件名包含哪些后缀
     * @return
     */
    public static Collection<File> getFiles(String rootDirStr, String[] includes, String[] excludes, String[] prefix, String[] suffix){
        // 多个大条件为且的关系
        AndFileFilter andFileter = new AndFileFilter();
        
        
        //后缀名有要求，且添加进过滤器
        if(suffix != null && suffix.length > 0){
            SuffixFileFilter sufFileter = new SuffixFileFilter(suffix);
            andFileter.addFileFilter(sufFileter);
        }
        
        //前缀名有要求，且添加进过滤器
        if(prefix != null && prefix.length > 0){
            PrefixFileFilter preFileter = new PrefixFileFilter(prefix);
            andFileter.addFileFilter(preFileter);
        }
        
        //文件名的包含有要求，各要求之前为或的关系，并添加进过“且”滤器
        if(includes != null && includes.length > 0){
            OrFileFilter orFileter = new OrFileFilter();
            for(String str : includes){
                RegexFileFilter regexFileter = new RegexFileFilter("[\\d\\D]*"+str+"[\\d\\D]*");
                orFileter.addFileFilter(regexFileter);
            }
            andFileter.addFileFilter(orFileter);
        }
        
        //文件名的不包含有要求，各要求之前为且的关系，并添加进过“且”滤器
        if(excludes != null && excludes.length > 0){
            for(String str : excludes){
                RegexFileFilter regexFileter = new RegexFileFilter("^(?!.*("+ str +")).*$");
                andFileter.addFileFilter(regexFileter);
            }
        }
        File rootDir = new File(rootDirStr);
        RegexFileFilter dirFilter = new RegexFileFilter("[\\d\\D]*");
        Collection<File> files = FileUtils.listFiles(rootDir, andFileter, dirFilter);
        
        //过滤已压缩文件
        Iterator<File> ite = files.iterator();
        while(ite.hasNext()){
            File delFile = ite.next();
            for(int i=0; i< suffix.length; i++){
                if(delFile.getName().endsWith(".min"+suffix[i])){
                    ite.remove();
                    break;
                }
            }
        }
        return files;
    }
    public static void compress(String prefix, String... fileNames) {
    	List<File> files = new ArrayList<File>();
    	String excludeConfig = UCMConfig.getValue("ebiz.zcms.web.compressExclude");
 	    String[] excludeNames = null;
 	    if (StringUtil.isNotEmpty(excludeConfig)) {
 	    	excludeNames = excludeConfig.split(",");
 	    }
 	    for (String name : fileNames) {
 	      String fileName = FileUtil.normalizePath(prefix + name);
 	      File f = new File(fileName);
 	      if ((f.exists())) {
 	    	if (f.isFile()) {
 	    		if ((name.endsWith(".css") && !name.endsWith(".min.css"))||(name.endsWith(".js") && !name.endsWith(".min.js"))) {
 	    			files.add(f);
 	    		}
 	    	}
 	    	if (f.isDirectory()){
 	    		files.addAll(YuiCompressUtil.getFiles(f.getPath(), null, excludeNames, null, new String[]{".css", ".js"}));
 	    	}
 	       }
 	     }
 	    
 	    if (files.size()  > 0) {
 	    	for (File f : files) {
 	    		if (f.getName().endsWith(".css")) {
 	    			YuiCompressUtil.compressCss(f);
 	    		} else {
 	    			YuiCompressUtil.compressJs(f);
 	    		}
 	    		
 	    	}
 	    }
    }
    public static boolean filterFile(File f) {
    	String name = f.getName();
    	if ((name.endsWith(".css") && !name.endsWith(".min.css"))||(name.endsWith(".js") && !name.endsWith(".min.js"))) {
    		String excludeNames = UCMConfig.getValue("ebiz.zcms.web.compressExclude");
    		if (StringUtil.isNotEmpty(excludeNames)) {
    			for (String exName : excludeNames.split(",")) {
    				
    			}
    		}
    	}
    	return false;
    }
}
