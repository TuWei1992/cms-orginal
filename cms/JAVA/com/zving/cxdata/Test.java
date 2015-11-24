package com.zving.cxdata;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.zving.adapter.VenusSocketConnect;

public class Test {
	public static void main(String[] args) {
		//Config.setPluginContext(true);
		//ExtendManager.getInstance().start();
		/*
		Map m = new HashMap();
		m.put("name", "是我");
		String result = VenusSocketConnect.getInstance().execute("z.q.test", m);
		System.out.println(result);
		*/
		int[] a = {1,2};
		int[] b = {3,4};
		int[] all = new int[a.length + b.length];
		System.arraycopy(a, 0, all, 0, a.length);
		System.arraycopy(b, 0, all, a.length, b.length);
		
		System.out.println(Arrays.toString(all));
	}
	
	 public static Object[] addAll(Object[] array1, Object[] array2) {
	        Object[] joinedArray = (Object[]) Array.newInstance(array1.getClass().getComponentType(),
	                                                            array1.length + array2.length);
	        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
	        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
	        return joinedArray;
	 }
}
