
//单个数字配
function getDouble(number){
	var numbers=["0","1","2","3","4","5","6","7","8","9"];
	for(var i=0;i<numbers.length;i++){
		if(numbers[i]==number){
			return "0"+numbers[i];
		}else if(i==9){
			return number;
		}
		
	}
}
//得到当天时
function getTodayTime(){
	var days=["星期日","星期一","星期二","星期三","星期四","星期五","星期六"];
	var today=new Date();
	var str= getDouble([today.getMonth()+1])+"/" +getDouble(today.getDate()) +"&nbsp;&nbsp;" +days[today.getDay()];
	if(document.getElementById('date'))
	document.getElementById('date').innerHTML=str;
	
}

//每隔一秒刷新一
setInterval("getTodayTime()",1000);