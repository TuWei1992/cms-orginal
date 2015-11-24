<#import "/webcommon/ftlib/spring.ftl" as spring />

<#import "/webcommon/ftlib/jscss.ftl" as jc />

<@jc.cssurlComm url = ["css/reset",  "css/header/mainsite-header"]/>

<script>
	
	window.base="${base}";
	
	window.accountBase="${accountBase}";
	
	window.mainBase="${mainBase}";
	
	window.imBase="${imBase}";
	
	window.memberBase="${memberBase}";
	
</script>

<link href="<@jc.appImgUrlComm url = "images/favicon.ico"/>" type="image/x-icon" rel="shortcut icon">

<@jc.jsCommonUrlComm url = ["js/jquery_1_8_3"]/>

<script>$.ajaxSetup({ cache: false });</script>

<@jc.jsurlComm url = ["js/index/header"]/>