$(function(){
	if($("#start_time").length>=1){
		$("#start_time").calendar({ format:'yyyy-MM-dd'});
	}
	if($("#end_time").length>=1){
		$("#end_time").calendar({ format:'yyyy-MM-dd'});
	}		   
})
function my_alertWidth(flag,title,msg,w){
	$("select").hide();
	var a_w=w-12;
	if(flag==1)//是否要显示标题
	var msg='<div id="alert" style="width:'+a_w+'px"><div class="title"><span onclick="aclose()"></span>'+title+'</div>'+msg+'</div>';
 	$("body").prepend('<div id="stylebox" style="width:'+w+'px"><table cellpadding="0" cellspacing="0"><tr><td class="corner_upleft"></td><td class="boder_cross"></td><td class="corner_upright"></td></tr><tr><td class="border_vertical"></td><td class="mid">'+msg+'</td><td class="border_vertical"></td></tr><tr><td class="corner_bottomleft"></td><td class="boder_cross"></td><td class="corner_bottomright"></td></tr></table></div>');
	var mybg="<div id='filter'></div>";
	var h=$("body").height();
	var windowHeight = document.documentElement.clientHeight;
	h=h>windowHeight?h:windowHeight;
	$("body").append(mybg);
	showDiv($("#stylebox"));
	$("div#filter").height(h);
}
function showDiv(obj){
	$("#stylebox select").show();
	$(obj).show();center(obj);
	$(window).scroll(function(){center(obj);});
	$(window).resize(function(){center(obj);}); 
}
function center(obj){
	var windowWidth = document.documentElement.clientWidth;   
	var windowHeight = document.documentElement.clientHeight;   
	var popupHeight = $(obj).height();   
	var popupWidth = $(obj).width();    
	$(obj).css({   
		"position": "absolute",   
		"top": (windowHeight-popupHeight)/2+$(document).scrollTop(),   
		"left": (windowWidth-popupWidth)/2   
	});  
}
function aclose(){
	$("#stylebox,#alert,#filter,#load_bg,#msg_loading").remove();
	$(".loading").remove();$("select").show();
}
function closeAuto(){
	$("#poplayer").remove();	
}
function getbyteCount(str){//获取字符串字节
	var byteCount=0;
	var strLength=str.length;
	for(var i=0;i<strLength;i++){   
		byteCount=(str.charCodeAt(i)<=256)?byteCount+1:byteCount+2;   
	}  
	return byteCount;
}
function cutString(str,len,hasDot) { //截取字符
	var newLength = 0; 
	var newStr = ""; 
	var chineseRegex = /[^\x00-\xff]/g; 
	var singleChar = ""; 
	var strLength = str.replace(chineseRegex,"**").length; 
	for(var i = 0;i < strLength;i++) { 
	  singleChar = str.charAt(i).toString(); 
	  if(singleChar.match(chineseRegex) != null) { 
		  newLength += 2; 
	  }  else  { 
		  newLength++; 
	  } 
	  if(newLength > len){ 
		  break; 
	  } 
	  newStr += singleChar; 
	} 
	if(hasDot && strLength > len) { 
	  newStr += "..."; 
	} 
	return newStr; 
}
function msgTips(status,msg){//信息提示
  	var html='<div class="msgbox"><span class="msg_'+status+'">'+msg+'</span><span class="msg_right"></span></div>';
 	$("body").prepend(html);
	showDiv($(".msgbox"));
	center($(".msgbox"));
	setTimeout(clearMsg,1500)
}
function clearMsg(){
	$("div.msgbox").remove();
}
function loading(msg){//loading
 	$("body").append('<div id="load_bg"></div>');
 	var html='<div id="msg_loading" class="msgbox"><span class="msg_left"></span><span class="loading"><em>&nbsp;</em>'+msg+'</span><span class="msg_right"></span></div>';
  	$("body").prepend(html);
 	showDiv($(".msgbox"));
	center($(".msgbox"));
	$("#load_bg").height($("body").height());
}
function msgconFirm(title,msg,fun){
	var html='<div class="msg"><span class="warn">&nbsp;</span>'+'&nbsp;'+msg+'</div><div class="op_btns" id="dialog_buttons"><a href="javascript:void(0)" onclick="'+fun+'" class="save_btn"><em>确&nbsp;定</em></a><a href="javascript:void(0)" onclick="aclose()" class="clear_btn"><em>取&nbsp;消</em></a></div>';
	my_alertWidth(1,title,html,442);
}
function checkNum(f,obj,num){//控制输入字数
	var str=f.value;
	if(num==undefined)
		var num=140;
	if(str==undefined){
		str=$(f).text();
	}
 	var byte=Math.ceil(getbyteCount(str)/2);
	var result=num-byte;
	if(result<=0){
		var cutStr=cutString(str,num*2);
		f.value=cutStr;
		result=0;
	}
	$("#"+obj).text(result);
}
function getCookie(Name) {
     var search = Name + "=";
     var returnvalue = "";
     if (document.cookie.length > 0) {
           offset = document.cookie.indexOf(search);
           if (offset != -1) {      
                 offset += search.length;
                 end = document.cookie.indexOf(";", offset);                        
                 if (end == -1)
                       end = document.cookie.length;
                 returnvalue=unescape(document.cookie.substring(offset,end));
           }
     }
     return returnvalue;
}
function setCookie(name,value,Days){
     var argv=SetCookie.arguments;
     var argc=SetCookie.arguments.length;
     var expires=(2<argc)?argv[2]:null;
     var path=(3<argc)?argv[3]:null;
     var domain=(4<argc)?argv[4]:null;
     var secure=(5<argc)?argv[5]:false;
	 var expires = new Date(); 
	 expires.setTime(expires.getTime() + Days*24*60*60*1000);
     document.cookie=name+"="+escape(value)+((expires==null)?"":("; expires="+expires.toGMTString()))+((path==null)?"":("; path="+path))+((domain==null)?"":("; domain="+domain))+((secure==true)?"; secure":"");
}
function removeLoad(){
	$("#msg_loading,#load_bg").remove();	
}
function getFormData(id){//聚合表单内容
	var data = {};
	//单行文本
	$(id).find("input[type=text]").each(function(i,n){
		data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
	});
	$(id).find("input[type=hidden]").each(function(i,n){
		data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
	});
	//密码区域
	$(id).find("input[type=password]").each(function(i,n){
		data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
	});
	//单选框
	$(id).find("select").each(function(i,n){
		if($(this).attr("multiple") == true){
			var opt = $(this).find("option");
			var len = $(opt).length;
			var str = "";
			for(i=0;i<len;i++)
				str += "," + $(opt).eq(i).val();
			if(str != '') str = str.substr(1,str.length);
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = str;
		}
		else
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val()+'';
		
	});
	//多行文本
	$(id).find("textarea").each(function(i,n){
		data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
	});
	var key = new Array();
	//单选按钮
	$(id).find("input[type=radio]").each(function(i,n){
		var id = $(this).attr('name');
		/*if(key.search(id)==-1) {
			key.push(id);
			data[id] = '';
		}*/
		if($(this).attr('checked') == true) data[id] = $(this).val();
	});
	//多选框
	$(id).find("input[type=checkbox]").each(function(i,n){
		var id = $(this).attr('name');
		/*if(key.search(id)==-1) {
			key.push(id);
			data[id] = '';
		}*/
		if($(this).attr('checked') == true) {
			if(data[id] == '')
				data[id] = $(this).val();
			else
				data[id] += "," + $(this).val();
		}
	});
	return data;
}
//url:内容路径 title:标题 width:宽度 height:高度 status：状态栏 toolbar:工具栏 location:地址栏 scrollbars:滚动条 resize:是否可调整大小 menuber:菜单栏 
//status等取值：'yes'、'no'
function openwindow(url,title,width,height,toolbar,menubar,scrollbars,resizable,location,status)
{
	var iTop = (window.screen.availHeight-height)/2;
	var iLeft = (window.screen.availWidth-width)/2;
	window.open(url,title,'width='+width+',height='+height+',top='+iTop+',left='+iLeft+',toolbar='+toolbar+',menubar='+menubar+',scrollbars='+scrollbars+',resizable='+resizable+',location='+location+',status='+status);
}
function winClose(){
	window.top.opener = null; window.close();
}