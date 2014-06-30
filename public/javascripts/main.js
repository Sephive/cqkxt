$(function(){
	var cheight = document.documentElement.clientHeight - 231;
	$("div.content").css("minHeight",cheight+'px');
	$("#logout").click(function(){
		var url=$(this).attr('href');
		APP.conFirm('退出系统','确认退出系统吗','admin.submitUrl(\''+url+'\')');							
		return false;
	});
	$("div.inner h1").prepend('<span id="his_back"><a href="javascript:;" onclick="history.back();"><img src="images/back.png" title="返回上一页" /></a></span>');
	$("#main_menu li").click(function(){
		if($(this).find('ul').is(":hidden")){
			$("#main_menu ul").slideUp('normal');	
			$(this).find('ul').slideDown('normal');
		}else{
			$(this).find('ul').slideUp('normal');	
		}
	});		   
})
admin={
	init: function(){
		$("#search_btn").keyup(function(e){
			var url=location.href;
			var a=url.split('/');
			var loc=a[a.length-1].split('?');
 			if(e.which==13){
 				var key=encodeURI($(this).val());
				window.location=loc[0]+'?key='+key;								
			}
		});
	},
	submitUrl: function(url){
		window.location = url;	
	},
	del: function(id,url){
		$.post(url,{"action":"del",id:id},function(msg){
			if(msg.error==1){
				APP.tips('success','删除成功');
				var str=id.toString();
				var list=str.split(',');
				var len=list.length;
				for(i=0;i<len;i++)
				$("#list_data_"+list[i]).remove();
				APP.close();
			}else{
				APP.tips('error',msg.error);
			}										   
		},'json')
	}
}