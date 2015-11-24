<#-- 截字符串：endIndex小于字符长度则正常截取，大于字符长度则截至字符尾部 -->
<#function subString str startIndex endIndex>
	<#if str?length gte endIndex>
		<#return str?substring(startIndex, endIndex)>
	<#elseif str?length lt endIndex>
		<#return str?substring(startIndex, str?length)>
	</#if>
</#function>
