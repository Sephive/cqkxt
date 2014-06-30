var APP = {
	alert: function(flag,title,msg,w){
		var len = $("div[name=wh_alert]").length + 1;
		var html = '<div class="alert" style="width:'+w+'px" id="wh_alert_'+len+'" name="wh_alert">';
		if(flag==1){
			html = html + '<div class="title"><a href="javascript:;" onclick="APP.close(this)"></a>'+title+'</div>'+msg+'</div>';
		}else{
			html = html + msg + '</div>';
		}
		$("body").prepend(html);
 		APP.mask();
		APP.center($('#wh_alert_'+len));
		if(flag == 1){APP.drag('wh_alert_'+len);}
	},
	alert2: function(flag,title,msg,w,index){
		var len = $("div[name=wh_alert]").length + 1;
		var html = '<div class="alert" style="width:'+w+'px" id="wh_alert_'+len+'" name="wh_alert">';
		if(flag==1 || flag == 2){
			html = html + '<div class="title"><a href="javascript:;" onclick="APP.close(this)"></a>'+title+'</div>'+msg+'</div>';
		}else{
			html = html + msg + '</div>';
		}
		$("body").prepend(html);
		APP.mask();
		APP.center2($('#wh_alert_'+len),index);
		if(flag == 1){
			APP.drag('wh_alert_'+len);
		} else if (flag == 2) {
			APP.drag2('wh_alert_'+len,index);
		}
	},
	show: function(obj){
		$(obj).show();center(obj);
		$(window).scroll(function(){center(obj);});
		$(window).resize(function(){center(obj);});
	},
	center: function(obj){
		var windowWidth = document.documentElement.clientWidth;
		var windowHeight = document.documentElement.clientHeight;   
		var popupHeight = $(obj).height();   
		var popupWidth = $(obj).width();
		var top = (windowHeight-popupHeight)/2+$(document).scrollTop() > 0 ? (windowHeight-popupHeight)/2+$(document).scrollTop() : 46;
		var index = parseInt( new Date().getTime()/1000 );
		$(obj).css({   
			"position": "absolute",
			"opacity": 0,
			"top": top-40,   
			"left": (windowWidth-popupWidth)/2,
			"z-index":index
		}); 
		$(obj).animate({"top":'+='+'40px',"opacity":"1"},400);
	},
	center2: function(obj,index){
		var windowWidth = document.documentElement.clientWidth;
		var windowHeight = document.documentElement.clientHeight;   
		var popupHeight = $(obj).height();   
		var popupWidth = $(obj).width();
		var top = (windowHeight-popupHeight)/2+$(document).scrollTop() > 0 ? (windowHeight-popupHeight)/2+$(document).scrollTop() : 46;
		//var index = parseInt( new Date().getTime()/1000 );
		$(obj).css({   
			"position": "absolute",
			"opacity": 0,
			"top": top-40,   
			"left": (windowWidth-popupWidth)/2,
			"z-index":index
		}); 
		$(obj).animate({"top":'+='+'40px',"opacity":"1"},400);
	},
	close: function(obj){
		var obj = obj || $("#wh_alert_1 div.title").find('a');
		$(obj).parents("div[name=wh_alert]").animate({"top":'-='+'60px',"opacity":"0"},400,function(){
			$(obj).parents("div[name=wh_alert]").remove();
			if($("div.alert").length<=0)
			$("#filter").animate({"opacity":"0"},400,function(){$("#filter").remove();});														
		})
	},
	loading: function(msg){
		var html = '<div id="msg_loading" class="msgbox"><span class="loading"><em>&nbsp;</em>'+msg+'</span><span class="msg_right"></span></div>';
		$("body").prepend(html);
		APP.center($("#msg_loading"));
		APP.mask();
	},
	removeLoad: function(){
		$("#msg_loading").remove();
		APP.delMask();
	},
	mask: function(){
		if($("#filter").length<=0){
			var mybg="<div id='filter'></div>";
			var h=$("body").height() > document.documentElement.clientHeight ? $("body").height():document.documentElement.clientHeight;
			$("body").append(mybg);
			$("div#filter").height(h);
			$("#filter").animate({"opacity":"0.75"},400);	
		}
	},
	delMask: function(){
		$("#filter").remove();
	},
	drag: function(dragObj){
 		var drag = typeof dragObj === 'string' ? document.getElementById(dragObj) : dragObj;
		var dTitle = drag.getElementsByTagName('div')[0];
		var disX = disY = 0;
		var isDrag = false;
		drag.style.position = 'absolute';
		dTitle.style.cursor = 'move';
		dTitle.onmousedown = function(evt){
			drag.style.zIndex = parseInt( new Date().getTime()/1000 );
			evt = evt || window.event;
			isDrag = true;

			disX = evt.clientX - drag.offsetLeft;
			disY = evt.clientY - drag.offsetTop;

			this.setCapture && this.setCapture();

			return false;
		}

		dTitle.onmousemove = function(evt){
			evt = evt || window.event;
			if(!isDrag) return false;

			drag.style.margin = 0;
			drag.style.left = evt.clientX - disX + 'px';
			drag.style.top = evt.clientY - disY + 'px';

			return false;
		}

		dTitle.onmouseup = dTitle.onblur = dTitle.onlosecapture = function(){
			isDrag = false;
			dTitle.releaseCapture && dTitle.releaseCapture();
		}
	},
	drag2: function(dragObj,index){
		var drag = typeof dragObj === 'string' ? document.getElementById(dragObj) : dragObj;
		var dTitle = drag.getElementsByTagName('div')[0];
		var disX = disY = 0;
		var isDrag = false;
		drag.style.position = 'absolute';
		dTitle.style.cursor = 'move';
		dTitle.onmousedown = function(evt){
			drag.style.zIndex = index;
			evt = evt || window.event;
			isDrag = true;
			
			disX = evt.clientX - drag.offsetLeft;
			disY = evt.clientY - drag.offsetTop;
			
			this.setCapture && this.setCapture();
			
			return false;
		}
		
		dTitle.onmousemove = function(evt){
			evt = evt || window.event;
			if(!isDrag) return false;
			
			drag.style.margin = 0;
			drag.style.left = evt.clientX - disX + 'px';
			drag.style.top = evt.clientY - disY + 'px';
			
			return false;
		}
		
		dTitle.onmouseup = dTitle.onblur = dTitle.onlosecapture = function(){
			isDrag = false;
			dTitle.releaseCapture && dTitle.releaseCapture();
		}
	},
	getbyteCount:function(str){
		var byteCount=0;
		var strLength=str.length;
		for(var i=0;i<strLength;i++){   
			byteCount=(str.charCodeAt(i)<=256)?byteCount+1:byteCount+2;   
		}  
		return byteCount;		
	},
	cutString: function(str,len,hasDot){
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
	},
	tips: function(status,msg,times,callback){
		var html='<div class="msgbox"><span class="msg_'+status+'">'+msg+'</span><span class="msg_right"></span></div>';
		var times = times || 1500;
		$("body").prepend(html);
 		APP.center($("div.msgbox"));
 		setTimeout(APP.delTips,times);
		if(callback){
			var long = parseInt(times) + 400;
			setTimeout(callback,long);
		}
	},
	delTips: function(){
 		$("div.msgbox").animate({"top":'-='+'30px',"opacity":"0"},400,function(){
			$("div.msgbox").remove();												
		})
	},
	conFirm: function(title,msg,fun){
		var html='<div class="tip_cont"><span class="warn"></span><div class="dialog">'+msg+'</div></div><div class="btn"><a href="javascript:void(0)" class="save_btn" onclick="'+fun+'"><em>确&nbsp;定</em></a><a href="javascript:void(0)" class="clear_btn" onclick="APP.close(this)"><em>取&nbsp;消</em></a></div>';
		APP.alert(1,title,html,440);
	},
	request: function(str){
		var href=window.location.href;
		var p=str+'=';
		var array=href.split(p);
		if(array.length>1){
			var b=array[1].split('&').length;
			if(b>1){
				return array[1].split('&')[0];
			}else{
				return array[1];
			}	
		}else{
			return "";	
		}
	},
	setCookie: function(name,value,days){
		var argv=APP.setCookie.arguments;
		var argc=APP.setCookie.arguments.length;
		var expires=(2<argc)?argv[2]:null;
		var path=(3<argc)?argv[3]:null;
		var domain=(4<argc)?argv[4]:null;
		var secure=(5<argc)?argv[5]:false;
		var expires = new Date(); 
		expires.setTime(expires.getTime() + days*24*60*60*1000);
		document.cookie=name+"="+escape(value)+((expires==null)?"":("; expires="+expires.toGMTString()))+((path==null)?"":("; path="+path))+((domain==null)?"":("; domain="+domain))+((secure==true)?"; secure":"");	
	},
	getCookie: function(name){
		var search = name + "=";
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
	},
	inArray: function(a,val){//判断一个元素是否存在数组中
		for(var i = 0; i < a.length; i++){
			if(a[i] == val){
				return true;
			}
		}
		return false;
	},
	checkTop: function(){
 		var html = '<div id="tooltop" class="hide" title="返回顶部"></div>';
		if($("#tooltop").length<=0){
			$("body").prepend(html);
		}
		if($(document).scrollTop()>600){
			$("#tooltop").fadeIn(800);
		}else{
			$("#tooltop").fadeOut(800);
		}
		$("#tooltop").unbind('click').bind('click',function(){
			$("html,body").animate({scrollTop:0},800);
			APP.checkTop();
		});
	},
	isMail: function(email){
		if( email=="" || ( email!="" && !/.+@.+\.[a-zA-Z]{2,4}$/.test(email) ) ){
 			return false;
		}else{
			return true;	
		}			
	},
	move: function(id,x,y,dir){
		if(dir == 'left'){
			var startX1 = x;
		}else{
			var startX1 = document.documentElement.clientWidth - x - $("#"+id).width();
		}
		var startY1 = y;
		$("#"+id).css({"left":startX1,"position":"absolute"});
		window.stayTopLeft=function(){
			var py = $(document).scrollTop();
			var y = $("#"+id).css("top").replace('px','');
			y = Math.ceil(y);
			y += (py + startY1 - y)/8;
			$("#"+id).css("top",y);
			setTimeout("stayTopLeft()",30);
		}
		stayTopLeft();	
	},
	now: function(){
		var now = new Date();
		var year = now.getFullYear();
		var month = now.getMonth()+1;
		var day = now.getDate();
		var hour = now.getHours();
		if(day<10){day = '0'+day;}
		if(month<10){month = '0'+month;}
		if(hour<10){hour = '0'+hour;}
		if(minute<10){minute = '0'+minute;}
		if(second<10){second = '0'+second;}
		var minute = now.getMinutes();
		var second = now.getSeconds();
		return (year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second);	
	},
	checkNum: function(f,obj,num){
		var str = f.value;
		var num = num || 140;
 		if(str == undefined){
			str=$(f).text();
		}
	 	var byte = Math.ceil(APP.getbyteCount(str)/2);
		var result = num-byte;
		if(result < 0){
			var cutStr = APP.cutString(str,num*2);
			f.value = cutStr;
			result = 0;
		}
		$("#"+obj).text(result);	
	},
	getFormData: function(id){
		var data = {};
 		$(id).find("input[type=text]").each(function(i,n){
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
		});
		$(id).find("input[type=hidden]").each(function(i,n){
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
		});
 		$(id).find("input[type=password]").each(function(i,n){
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
		});
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
 		$(id).find("textarea").each(function(i,n){
			data[$(this).attr("id")!=''?$(this).attr("id"):$(this).attr("name")] = $(this).val();
		});
		var key = new Array();
 		$(id).find("input[type=radio]").each(function(i,n){
			var id = $(this).attr('name');
 			if($(this).attr('checked')) data[id] = $(this).val();
		});
 		$(id).find("input[type=checkbox]").each(function(i,n){
			var id = $(this).attr('name');
 			if($(this).attr('checked')) {
				if(data[id] == '' || data[id] == undefined){
					data[id] = $(this).val();
				}else{
					data[id] += "," + $(this).val();
				}

			}
		});
		return data;		
	}
};
(function($) {//滚动加载
	$.fn.whScroll = function(settings) {
		settings = jQuery.extend({       //参数初始化，默认设置
 			url: "",	                 //请求url
			obj: "body",                 //内容复制到对象
			classname: "wh_loading",     //加载中样式
			txt: "加载中...",             //等待文字
			type: "html",				 //默认返回数据html
			action: 'get',               //异步请求方式
			isPaused: true,              //是否在等待中
			page: 0,                     //记录请求次数
 			data: {},					 //请求参数
			timeout: 10000,              //请求超时时间
			error: '网络异常',            //错误提示
			setb: true,
			onComplete: function(){},    //支持完回调函数
			handle: function(data){ return data;}           //数据处理
		},settings);
		var self = this;//当前对象 赋值
 		function _init(){
			var flag = isBottom();
			//console.log(flag);
			if(flag && settings.isPaused){
				settings.isPaused = false;
				onSuccess();	
			}
		}
		function onSuccess(){
			if(settings.url==''){
				APP.tips('error','请求页面地址设置不能为空');
				return false;	
			}
			if(settings.obj==''){
				APP.tips('error','对象设置不对');
				return false;	
			}
			settings.page++;
			settings.data['page'] = settings.page;
			if($("#wh_loading").length<=0)
			$(settings.obj).after('<div id="wh_loading" class="'+settings.classname+'"><span>'+settings.txt+'</span></div>')
			$.ajax({
				type:settings.action,
				url:settings.url,
				data:settings.data,
				dataType:settings.type,
				timeout:settings.timeout,
				success:function(html){
					$("#wh_loading").remove();
					if($.trim(html)==''){
						settings.isPaused = false;
					}else{
						var html=settings.handle(html);
						$(settings.obj).append(html);
						if(isBottom() && settings.setb==true){
							$("html,body").animate({scrollTop:$(settings.obj).offset().top},600);
						}
						settings.isPaused = true;
						settings.onComplete();
					}
				},
				error:function(request,status,error){
					if (status == 'timeout') {
						APP.tips('error','网络超时，刷新后再试');
						$("#wh_loading").remove();
					} else {
						APP.tips('error','操作失败');
					}
				}	   
			});
		}
		function isBottom(){
			if(self[0] == window){
				if(Math.abs(document.body.clientHeight - document.documentElement.clientHeight) <= (document.documentElement.scrollTop || document.body.scrollTop)){
					return true;
				}else{
					return false;	
				}
			}else{
				var nScrollHight = self[0].scrollHeight;
				var nScrollTop = self[0].scrollTop;
				var nDivHight = $(self[0]).innerHeight();
				if(nScrollTop + nDivHight >= nScrollHight){
					return true;
				}else{
					return false;
				}
			}
		}
 		return this.unbind('scroll').bind('scroll',_init);
	};//end	 	  
})(jQuery);