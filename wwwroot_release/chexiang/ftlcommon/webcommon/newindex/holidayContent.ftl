<#if holidayAd?exists>
	   <#if holidayAd.picType == 'swf'>
	   <a href="${holidayAd.picTargetUrl}" target="_blank" title="${holidayAd.picDescribe}" style="position:absolute;display:block;width:990px;height:50px;z-index:10;overflow:hidden;font-size:2000px;line-height:2000px;color:transparent" <#if holidayAd.exposureUrl??>exposureUrl="${holidayAd.exposureUrl}"</#if>>$</a>
	       <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0" width="990" height="50" id="holidayAdbanner">
	              <param name="quality" value="high">
	              <param name="movie" value="${imgUrl("${holidayAd.picUrl}")}">
	              <param name="wmode" value="opaque">
	              <embed src="${imgUrl("${holidayAd.picUrl}")}" wmode="opaque" width="990" height="400" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash"></embed>
	       </object>
	   <#else>
	       <a href="${holidayAd.picTargetUrl}" target="_blank" title="${holidayAd.picDescribe}" <#if holidayAd.exposureUrl??>exposureUrl="${holidayAd.exposureUrl}"</#if>><img src="${imgUrl("/990x50${holidayAd.picUrl}")}" width="990" height="50" id="holidayAdbanner"/></a>
	   </#if>
</#if>