/**
 * 
 */
 
// 앱 커넥트 팩토리: cnApp
var gnx_app_factory = function(){
	
	// 콜백함수 선언(배열로 각 함수의 호출값을 키로 가지고 있음)
	var _callback_fn = new Object();
	
	// Call Android
	var _call_and = function(fn, arr){
		
		try {
			
			var wordFn = 'javascript:window.GA.'+fn+'(';
			
			// 데이터 세팅			
			if(typeof arr == "undefined") { // 연락처 관련 undefined가 들어올 때
				console.log('arr undefined');
				if (fn == 'reqSendDeviceInfo') {
					arr = '';
					wordFn += '';
				} else {
					wordFn += '"' + arr + '"';
				}
			}
			
			if(typeof arr == "object") {
				console.log('arr object');
				if (fn == 'reqSendDeviceInfo') {
					arr = '';
					wordFn += ''
				} else {
					var jsonString = JSON.stringify(arr);
					console.log(jsonString);
					wordFn += '\'' + jsonString + '\'';
				}
			}
			
			wordFn += ")";
			console.log(wordFn);
			
			// 안드로이드 함수 호출(동적)
			//console.log(wordFn);
			//alert(wordFn);
			
			eval(wordFn);
			
		} catch (e) {
			//alert('[ERROR-607]에러가 발생했습니다');
			//wrap_mask(0);
			console.log(e);
			//alert(e);
			
			//_call_local(fn, map);
		}
		
	}
	
	// Call IOS
	var _call_ios = function(fn, map){
		try {
			var jsonData = map == null ? '' : {data:map};
			var message = {
				name: fn,
				parameters: jsonData
			};
			var v2 = JSON.stringify(message);
			window.webkit.messageHandlers.GA.postMessage(v2); 
//			window.webkit.messageHandlers.callApp.postMessage(v2);
			
		} catch (e) {
			//alert('[ERROR-606]에러가 발생했습니다');
			//wrap_mask(0);
			console.log(e);
			//alert(e);
		}
	}
	
	// Call Local For TEST
	var _call_local = function(fn, map){
		
		// 키 세팅
		var _keys = Object.keys(map || {});
		var arr = new Array();
		for (var i in _keys){
			arr.push(map[_keys[i]]);
		}
		
		// 로컬 테스트 데이터 콜백 영역
		switch (fn) {
		case "reqSendDeviceInfo": gnx_app_master.result(fn, _t_reqSendDeviceInfo); break;	// 기기정보 전달
		case "reqLoginKakao": gnx_app_master.result(fn, _t_reqLoginKakao); break;	// 소셜정보 전달
		case "reqLoginNaver": gnx_app_master.result(fn, _t_reqLoginNaver); break;	// 소셜정보 전달
		case "reqLoginGoogle": gnx_app_master.result(fn, _t_reqLoginGoogle); break;	// 소셜정보 전달
		default:
			console.log('None fn here... for >> ' + fn);
			//alert('[ERROR-601]에러가 발생했습니다');
			//wrap_mask(0);
			console.log(e);
			break;
		}
	}
	
	return {
		// 앱 함수 호출하기 
		call : function(fn, cb, map){
			
			// 콜백함수 보관
			_callback_fn[fn] = cb;
			
			try {
				var device = swichDevice();
				if('and' == device){
					_call_and(fn, map);		// 안드로이드
				}else if('ios' == device){
					_call_ios(fn, map);		// 아이폰
				}else{
					_call_local(fn, map);	// 로컬
				}
			} catch (e) {
				console.log(e);
				//alert(e);
				//alert('[ERROR-604]에러가 발생했습니다');
				//wrap_mask(0);
			}
		}
		// 앱 함수 결과값 실행
		, result : function(fn, data){
			console.log(_callback_fn);
			if(_callback_fn[fn]){
				_callback_fn[fn](data);
			}else{
				var _keys = Object.keys(_callback_fn);
				console.log('해당 함수가 존재하지 않습니다 -> ' + fn + '\n ------------- \n ' + _keys);
				//alert('[ERROR-602]에러가 발생했습니다');
				//wrap_mask(0);
				//alert(fn + "))) 함수가 존재하지 않습니다");
			}
		}
	}
}

/**
	★☆★☆[사용예시]★☆★☆
	
gnx_app_master.call(
	// 호출되는 API 함수명
	'reqSendDeviceInfo'
	
	// 앱 호출에 대한 콜백함수
	, function(data){
		// data 값에는 앱에서 반환된 결과값이 담겨 있음
	}
	
	// 전송해야되는 파라미터 값, null 가능
	, {data: "a", code: "b"}	
);

 */
var gnx_app_master = gnx_app_factory();	// 디폴트 팩토리 클로저



// 앱 연동 데이터 콜백 함수(함수명, 결과데이터)
var gnx_app_callback = function(fn, data){
	
	console.log('gnx_app_callback --*');
	
	// resultCode = '0000'; 성공
	// resultCode = '1000'; 취소
	var resultJSON;
	
	if("string" == typeof data){
		
		resultJSON = data.replace(/[\n|\r\n|\t\n]/g, '');
		
		try {
			resultJSON = JSON.parse(resultJSON);
		} catch (e) {
			
			// 엔진구동 결과 저장
			$.ajax({
				url: "/engine/errorJson.ajax"
				, data: {
					ERRORJSON : data
					, FUNTION_NM : fn
					, _csrf : $("meta[name='_csrf']").attr("content")
				}
				, type: "post"
				, success: function(data){
					gnx_ift_status = 'ready';
					alert('[ERROR-603]조회 결과를 저장하는데 실패했습니다\n(reason: '+e+')');
				}
			});
			
		}
		
	} else {
		resultJSON = data;
	}
	
	gnx_app_master.result(fn, resultJSON);
	
}

// 안드로이드, IOS, 로컬 구분용
var swichDevice = function(){
	
    var varUA = navigator.userAgent.toLowerCase();
    if ( varUA.indexOf('android') > -1) {
    // userAgent에 앱에서 appaos 삽입해주기로.. 없으면 모바일 웹.
  		if ( varUA.indexOf('appaos') > -1) {
        //안드로이드
        	return "and";
        } else {
        	return "other";
        }
    } else if ( varUA.indexOf("iphone") > -1||varUA.indexOf("ipad") > -1||varUA.indexOf("ipod") > -1 ) {
   		if ( varUA.indexOf('appios') > -1) {
        //IOS
        	return "ios";
        } else {
        	return "other";
        }
    } else {
        //아이폰, 안드로이드 외
        return "other";
    }
    return "ios";
}

// ============================================

//var checkMobile = function(){
//    var varUA = navigator.userAgent.toLowerCase();
//    if ( varUA.indexOf('android') > -1) {
//    // userAgent에 앱에서 appaos 삽입해주기로.. 없으면 모바일 웹.
//  		if ( varUA.indexOf('appaos') > -1) {
//        //안드로이드
//        	return "and";
//        } else {
//        	return "other";
//        }
//    } else if ( varUA.indexOf("iphone") > -1||varUA.indexOf("ipad") > -1||varUA.indexOf("ipod") > -1 ) {
//   		if ( varUA.indexOf('appios') > -1) {
//        //IOS
//        	return "ios";
//        } else {
//        	return "other";
//        }
//    } else {
//        //아이폰, 안드로이드 외
//        return "other";
//    }
//    return "ios";
//}

var testF = function(str){
	console.log(str);
}

function BackKeyPressed(action) {
    var newp = 1234;
    //return newp;
}

function KeyboardHide(action) {
    console.log("KeyboardHide");
}

function KeyboardShow(action) {
    console.log("KeyboardShow");
}

function redirectUrl(str) {
    console.log(str);
}