document.onkeydown = function(evt) {
	var isie = (document.all) ? true : false; 
	var key; 
	var srcobj;
	if (isie) {
		key = event.keyCode; 
		srcobj=event.srcElement;
	} else {
		key = evt.which; 
		srcobj=evt.target;
	}
    if(key==13 && srcobj.type!='button' && srcobj.type!='submit' &&srcobj.type!='reset' 
       && srcobj.type!='textarea' && srcobj.type!='' && srcobj.type != undefined)
	{
		var el = getNextElement(srcobj);
		while (el.type == 'hidden' || el.getAttribute('ignore-tab') == 'true') {
			el = getNextElement(el);
		}
		if (el.type == 'select-one') {
			el.click();
			el.focus();
		} else {
			el.focus();
		}
		return false;
     }
}                
function getNextElement(field) {
   var form = field.form;
   for (var e = 0; e < form.elements.length; e++) { 
     if (field == form.elements[e])
         break;
   }
   return form.elements[++e % form.elements.length];
}