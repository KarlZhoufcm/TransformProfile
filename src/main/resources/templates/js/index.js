/**
 * 
 */
//init();
function init(str) {
	$("#lsFile").html("");
	$.post("getLsFileList", "lsPath="+str, function(data, statusText) {
		// $("#lsFilt").html(data);
		var dataObj = eval("(" + data + ")");
		console.log(dataObj);
		var sb = new StringBuffer();// 调用model
		for(var files in dataObj){
			if(dataObj[files]!="Completed" && dataObj[files]!="Manual"){
				sb.Append("<div><a href=\"mydownload/"+str+"/"+dataObj[files]+"\" target='_blank' >"+dataObj[files]+"</a></div>")
//				sb.Append("&nbsp; <a href=\"mydownload/"+dataObj[files]+"\" target='_blank' >download</a>");
//				sb.Append("&nbsp; <a href=\"javascript:void(0);\" onclick=\"mydelete('"+dataObj[files]+"');\">delete</a>");
//				sb.Append("</div>");
			}
		}
		$("#lsFile").html(sb.ToString());
	}, "html");
}

function mydelete(fileName){
	alert(fileName);
}

function StringBuffer() {
	this.__strings__ = [];
};
StringBuffer.prototype.Append = function(str) {
	this.__strings__.push(str);
	return this;
};
// 格式化字符串
StringBuffer.prototype.AppendFormat = function(str) {
	for (var i = 1; i < arguments.length; i++) {
		var parent = "\\{" + (i - 1) + "\\}";
		var reg = new RegExp(parent, "g")
		str = str.replace(reg, arguments[i]);
	}

	this.__strings__.push(str);
	return this;
}
StringBuffer.prototype.ToString = function() {
	return this.__strings__.join('');
};
StringBuffer.prototype.clear = function() {
	this.__strings__ = [];
}
StringBuffer.prototype.size = function() {
	return this.__strings__.length;
}