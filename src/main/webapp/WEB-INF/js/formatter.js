var nextLine = "<br />";
var collapsed = "/img/collapsed.gif";
var expanded = "/img/expanded.gif";
var lines = 0;
var color_key = "red";
var color_value = "green";
var color_null = "#993300";
var color_quote = "#CC0000";

var collapsed_img = "<img src=\"/img/collapsed.gif\" onclick=\"collapse_by_img(this);\" />";
var expanded_img = "<img src=\"/img/expanded.gif\" onclick=\"collapse_by_img(this);\" />";

var max_level = 0;

//染色
function dye(color,value){
	return "<span style=\"color:"+color+";\">"+value+"</span>";
}

function grading(level,html){
	return "<span level=\"" + level + "\">"+html+"</span>";
}

function format_json() {
	var tab = $("#tab").val();
	var valueTab = $("#valueTab").val();
	var highLight = $("#highLight").prop("checked");
	var startNewline = $("#startNewline").prop("checked");
	$("#validate_result").html("正在解析。。。");
	$("#dest").html("");
	tab = tab == "0" ? "<pre>&#09;</pre>" : multiplyString("&nbsp;",
			Number(tab));
	valueTab = valueTab == "0" ? "<pre>&#09;</pre>" : multiplyString("&nbsp;",
			Number(valueTab));
	var level = 0;
	var source = $("#source").val();
	var isObject = false;
	if (source == "") {
		source = "\"\"";
	}
	var capital = source.trim().charAt(0);
	source = "(" + source + ")";//防止注入并声明为对象解析
	
	try {
		var sourceObj = eval(source);
		var formatHtml = parseObject(sourceObj, tab, level, highLight, startNewline, valueTab);
		$("#validate_result").html("解析成功").css("color", "green");
		$("#dest").html(formatHtml);
		$("#copy_result").attr("data-clipboard-text",formatHtml);
		lines = countLines(formatHtml);
	} catch (e) {
		general_ajax("/function/format", "POST", {
			source : source
		}, null, function(result) {
			if (get_type(result) == "[object String]") {
				result = json_parse(result);
			}
			$("#dest").html(result.data);
			$("#copy_result").attr("data-clipboard-text",result.data);
			$("#validate_result").html("Next is the result from the server!")
					.css("color", "red");
		}, null, null);
		$("#validate_result").html("json校验失败，正从服务器格式化！").css("color", "red");
		$("#line-number").html("");
	}
	refreshLineNumbers();
	generateLevelOptions();
}

function parseObject(obj, tab, level, highLight, startNewline, valueTab) {
	var formatJson = "";
	var tabs = multiplyString(tab, level);
	var quote = highLight ? dye(color_quote, "\""):"\"";
	var nullV = highLight ? dye(color_quote, "null"):"null";
	var type = typeof obj;
	max_level = max_level < level - 1 ? level - 1 : max_level;
	if (isArray(obj)) {
		var arrayStart = (startNewline && level > 0 ? nextLine + tabs : "") + "[" + expanded_img;
		var arrayEnd = "]";
		
		var array = [];
		for (var i = 0; i < obj.length; i++) {
			var objJson = tabs + tab + parseObject(obj[i], tab, level+1, highLight, startNewline, valueTab);
			array.push(objJson);
		}
		var html = (obj.length > 0 ? nextLine : "")+array.join("," + nextLine)+(obj.length > 0 ? nextLine+ tabs : "");
		var arrayContent = grading(level, html);

		formatJson += arrayStart+arrayContent+arrayEnd;
	} else if (type == "object") {
		if (obj == null) {
			formatJson += highLight ? dye(color_null, "null") : "null";
		} else if (obj.constructor == Date.constructor) {
			formatJson += obj.toLocaleString();
		} else if (obj.constructor == RegExp.constructor) {
			formatJson += obj;
		} else {
			var objStart = "{" + expanded_img;
			var objEnd = (Object.getOwnPropertyNames(obj).length>0 ? nextLine+tabs : "") + "}";
			
			var array = [];
			for ( var key in obj) {
				var value = obj[key];
				key = quote + (highLight ? dye(color_key, key) : key )+ quote;
				value = parseObject(value, tab, level + 1, highLight, startNewline, valueTab);
				array.push(tabs + tab + key + ":" + valueTab + value);
			} 
			var html = (Object.getOwnPropertyNames(obj).length>0 ? nextLine : "")
				+array.join("," + nextLine);
			var objContent = grading(level, html);
			
			formatJson += objStart+objContent+objEnd;
		}
		// 直接类型
	} else {
		var value = highLight ? dye(color_value, obj): obj;
		if (type == "number") {
			formatJson += value;
		} else if (type == "boolean") {
			formatJson += value;
		} else if (type == "function") {
			if (obj.constructor == window.RegExp.constructor) {
				formatJson += value;
			} else {
				formatJson += value;
			}
		} else {
			formatJson += quote + value + quote;
		}
	}
	return formatJson;
}

function isArray(obj) {
	return obj && typeof obj === "object" && typeof obj.length === "number"
			&& !(obj.propertyIsEnumerable("length"));
}

function multiplyString(str, count) {
	var array = [];
	for (var i = 0; i < count; i++) {
		array.push(str);
	}
	return array.join("");
}

function collapse_by_img(img) {
	var span = $($(img).next());
	if (span.is(":visible")) {
		span.hide();
		$(img).attr("src", collapsed);
	} else {
		span.show();
		$(img).attr("src", expanded);
	}
	refreshLineNumbers();
}

function collapse_by_action(action) {
	var level = Number($("#level").val());
	if (level <= 0) {
		level = 0;
	}
	for (var i = 0; i < level; i++) {
		collapse_by_action_level("expend", i);
	}
	collapse_by_action_level(action, level);
	refreshLineNumbers();
}

function collapse_by_action_level(action, level) {
	var spans = $("span[level='" + level + "']");
	for (var i = 0; i < spans.length; i++) {
		var span = $(spans.get(i));
		var img = span.prev();
		if (action == "collapse" && span.is(":visible")) {
			span.hide();
			$(img).attr("src", collapsed);
		} else if (action == "expend" && span.is(":hidden")) {
			span.show();
			$(img).attr("src", expanded);
		}
	}
}

function generateLevelOptions() {
	var options = "";
	for (var i = 0; i <= max_level; i++) {
		options += "<option value=\"" + i + "\">" + i + "</option>";
	}
	$("#level").html(options);
}

function refreshLineNumbers() {
	var hiddenLines = countAllHiddenLines();
	var indexArray = [];
	for (var i = 0; i < lines - hiddenLines; i++) {
		indexArray.push(i);
	}
	$("#line-number").html(indexArray.join(nextLine));
}

function countAllHiddenLines() {
	var hiddenLines = 0;
	var children = $("#dest").find("span[level='" + 0 + "']");
	for (var j = 0; j < children.length; j++) {
		var child = $(children.get(j));
		hiddenLines += countHiddenLines(child, 0);
	}
	return hiddenLines;
}

function countHiddenLines(span, level) {
	var hiddenLines = 0;
	if (level > max_level) {
		return hiddenLines;
	}
	if (span.is(":hidden")) {
		hiddenLines += countLines(span.html()) - 1;
	} else {
		var children = span.find("span[level='" + (level + 1) + "']");
		for (var j = 0; j < children.length; j++) {
			var child = $(children.get(j));
			hiddenLines += countHiddenLines(child, level + 1);
		}
	}
	return hiddenLines;
}

function countLines(content) {
	var reg = /<br\s{0,1}\/{0,1}>/g;
	var i = 0;
	while (reg.exec(content) != null) {// exec使arr返回匹配的第一个，while循环一次将使re在g作用寻找下一个匹配。
		i++;
	}
	return i + 1;
}