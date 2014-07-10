//jQuery.noConflict();
var lcf = {
	getCookie: function(name){
		var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
		if (arr = document.cookie.match(reg)) {
			return (arr[2]);
		} else {
			return null;
		}
	},
	getTimeStamp: function(){
		return new Date().getTime() / 1000;
	},
	mergeObject: function(a, b){
		var obj = [];
		for (var i in a) {
			obj[i] = a[i];
		}
		for (var i in b) {
			obj[i] = b[i];
		}
		return obj;
	}
};

lcf.sdop = {
	ssid: null,
	tokenId: null,
	myUserId: null,
	bp: 0,
	ep: 0,
	maxSp: 0,
	currentSp: 0,
	httpUrlPrefix: "http://sdop-g.bandainamco-ol.jp",
	members: [],
	greetingUsers: [],
	logPanel: null,
	init: function(callback){
		var _sdop = lcf.sdop;
		_sdop.ssid = lcf.getCookie("ssid");
		if (!_sdop.tokenId) {
			alert("请先点击【プロフィール】!");
		} else if (callback) {
			_sdop.log("获取 ssid 成功！");
			setTimeout(callback, 100);
		}
	},
	createGetParams: function(){
		var _sdop = lcf.sdop;
		return {
			ssid: _sdop.ssid,
			tokenId: _sdop.tokenId,
			timeStamp: lcf.getTimeStamp()
		};
	},
	createBasePayload: function(procedure, args){
		var _sdop = lcf.sdop;
		return {
			procedure: procedure,
			tokenId: _sdop.tokenId,
			ssid: _sdop.ssid,
			args: args
		};
	},
	log: function(msg){
		lcf.sdop.logPanel.prepend('<div>' + new Date().toLocaleString() + '　' + msg + '</div>');
	},
	clearLog: function(){
		lcf.sdop.logPanel.text('');
	},
	logCallback: function(data){
		console.info(data);
	},
	get: function(url, payload, callback){
		$.ajax({
			url: url,
			type: "GET",
			data: payload,
			success: callback,
			error: function(){
				lcf.sdop.log("<b class='c_red'>请求失败！</b>2秒后再次尝试！");
				setTimeout(lcf.sdop.get, 2000, url, payload, callback);
			}
		});
	},
	post: function(url, payload, callback){
		var _sdop = lcf.sdop;
		$.ajax({
			url: url,
			type: "POST",
			contentType: 'application/json; charset=UTF-8',
			data: JSON.stringify(payload),
			async: true,
			processData: false,
			success: callback,
			error: function(){
				lcf.sdop.log("<b class='c_red'>请求失败！</b>2秒后再次尝试！");
				setTimeout(lcf.sdop.post, 2000, url, payload, callback);
			}
		});
	},
	reloadMode: 0,
	checkReload: function(data){
		if (lcf.sdop.reloadMode == 0) {
			return;
		}
		if (data.args.message.indexOf("再度ログインお願いします。") > 0) {
			var opValue = 0;
			if (lcf.sdop.auto.setting.duel) {
				opValue = 1;
			} else if (lcf.sdop.auto.setting.boss) {
				switch (lcf.sdop.boss._currentType) {
				case 0:
					opValue = 3;//普通总力
					break;
				default://超总
					opValue = 2;
				}
			}
			if (opValue == 0) {
				return;
			}
			chrome.storage.local.set({
				op: opValue
			}, function(){
				location.href = "http://sdop.bandainamco-ol.jp/api/sdop-g/login.php";
			});
		}
	},
	checkBattleFinished: function(data){
		if (lcf.sdop.auto.setting.boss) {
			if (data.args.message.indexOf("戦闘は既に終了") > 0) {
				lcf.sdop.boss.AI.cancelAutoSuperRaidBoss();
				
				switch (lcf.sdop.boss._currentType) {
				case 0:
					lcf.sdop.boss.AI.startAutoNormalRaidBoss();
					break;
				default:
					lcf.sdop.boss.AI.startAutoSuperRaidBoss();
				}
			} else if (data.args.message.indexOf("しばらく時間を置いてからアクセスして頂き") > 0) {
				lcf.sdop.boss.AI.cancelAutoSuperRaidBoss();
				
				switch (lcf.sdop.boss._currentType) {
				case 0:
					setTimeout(lcf.sdop.boss.AI.startAutoNormalRaidBoss, 60000);
					break;
				default:
					setTimeout(lcf.sdop.boss.AI.startAutoSuperRaidBoss, 60000);
				}
			}
			return;
		}
		if (lcf.sdop.auto.setting.duel) {
			if (data.args.message.indexOf("しばらく時間を置いてからアクセスして頂き") > 0) {
				lcf.sdop.cancelAutoDuel();
				lcf.sdop.startAutoDuel();
			}
			return;
		}
	},
	checkError: function(data, msg){
		if (data.args.message) {
			console.warn(data);
			lcf.sdop.log(msg + "：" + data.args.message);
			lcf.sdop.checkReload(data);
			lcf.sdop.checkBattleFinished(data);
			return true;
		}
		return false;
	},
	checkCallback: function(callback){
		if (callback) {
			setTimeout(callback, 100);
		}
	}
};

/**
 * 挂机状态下使用
 * @param {Object} callback
 */
lcf.sdop.login = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForAuthentication/enter";
	var payload = {
		"args": {
			"loginParameterSet": {
				"timeStamp": "123456789",
				"userId": "idstring",
				"checkCode": "checkcode"
			}
		},
		"procedure": "enter"
	};
	lcf.sdop.post(url, payload, function(data){
		lcf.sdop.tokenId = data.args.tokenId;
		if (_sdop.checkError(data, "enter_Response")) {
			return;
		}
		lcf.sdop.checkCallback(callback);
	});
};

/**
 * 检查自动, 在挂机时使用
 */
lcf.sdop.checkAuto = function(){
	chrome.storage.local.get("op", function(items){
		var opValue = items.op;
		if (opValue == null) {
			return;
		}
		lcf.sdop.ui.btnStartAutoReload.click();
		//lcf.sdop.reloadMode = 1;
		$("#flash_container").text('');//移除, 怕重复登录请求, 导致依然失败
		lcf.sdop.login(function(){
			lcf.sdop.ui.btnInit.click();
			switch (opValue) {
			case 1:
				lcf.sdop.ui.btnStartDuel.click();
				break;
			case 2:
				lcf.sdop.ui.btnStartSuperRaidBoss.click();
				break;
			case 3:
				lcf.sdop.ui.btnStartNormalRaidBoss.click();
				break
			}
		});
		chrome.storage.local.clear();
	});
};

lcf.sdop.getOwnTeamData = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForTeam/getOwnTeamData";
	if (_sdop.members.length > 0) {
		lcf.sdop.checkCallback(callback);
		return;
	}
	lcf.sdop.get(url, _sdop.createGetParams(), function(data){
		if (_sdop.checkError(data, "getOwnTeamData")) {
			return;
		}
		var team = data.args;
		_sdop.myUserId = team.userId;
		
		var teamInfo = team.data;
		var memberList = teamInfo.memberList;
		
		for (var i in memberList) {
			var m = memberList[i];
			if (_sdop.myUserId == m.id) {
				continue;
			}
			_sdop.members[m.id] = m.name;
		}
		lcf.sdop.checkCallback(callback);
	});
};

lcf.sdop.getGreetingList = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForProfile/getGreetingList";
	if (_sdop.greetingUsers.length > 0) {
		lcf.sdop.checkCallback(callback);
		return;
	}
	lcf.sdop.get(url, _sdop.createGetParams(), function(data){
		if (_sdop.checkError(data, "getGreetingList")) {
			return;
		}
		var greetingList = data.args.greetingList;
		
		for (var i in greetingList) {
			var g = greetingList[i];
			_sdop.greetingUsers[g.userId] = g.name;
		}
		
		lcf.sdop.checkCallback(callback);
	});
};

lcf.sdop.getGreetingCondition = function(userId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForProfile/getGreetingCondition";
	var params = _sdop.createGetParams();
	params.userId = userId;
	
	lcf.sdop.get(url, params, function(data){
		if (_sdop.checkError(data, "getGreetingCondition")) {
			return;
		}
		
		var isHello = data.args.greetingCondition.greeted;
		if (callback) {
			setTimeout(callback, 100, isHello);
		}
	});
};

lcf.sdop.postGreeting = function(greetingUserId, comment, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForProfile/postGreeting?ssid=" + _sdop.ssid;
	var payload = _sdop.createBasePayload("postGreeting", {
		comment: comment,
		greetingUserId: greetingUserId
	});
	
	_sdop.post(url, payload, function(data){
		if (_sdop.checkError(data, "postGreeting")) {
			return;
		}
		lcf.sdop.checkCallback(callback);
	});
};

lcf.sdop.ms = {
	unitAttribute: ['FIGHT', 'SPECIAL', 'SHOOT'],
	current: null,
	max: null,
	logMsList: function(msList){
		var logMsg = "";
		for (var i in msList) {
			var msCard = msList[i].card;
			logMsg += lcf.sdop.ms.logMsCard(msCard, i);
		}
		return logMsg;
	},
	logMsCard: function(ms, i){
		var msLog = "<br>" + i + "） ";
		if (ms.userName) {
			if (ms.lcf_attack > 1) {
				msLog += "<b class='c_red'>";
			} else {
				msLog += "<b>";
			}
			msLog += ms.userName + "</b>";
		}
		msLog += ms.rarity + "c" + ms.cost + "【" + ms.type.name + "】，level：" + ms.level + "，属性：" + ms.attribute.value + "，attack：" + ms.attack + "，max HP：" + ms.maxHp + "，speed：" + ms.speed;
		for (var j in ms.characteristicList) {
			var c = ms.characteristicList[j];
			msLog += "<br>　　插件" + j + "：" + c.briefDescription;
		}
		var pilot = ms.pilot;
		msLog += "<br>　pilot：" + pilot.rarity + "c" + pilot.cost + "【" + pilot.type.name + "】，level：" + pilot.level;
		for (var j in pilot.activeSkillList) {
			msLog += "<br>　　主动技能：" + pilot.activeSkillList[j].description;
		}
		for (var j in pilot.passiveSkillList) {
			msLog += "<br>　　被动技能：" + pilot.passiveSkillList[j].description;
		}
		return msLog;
	}
};

lcf.sdop.auto = {
	setting: {
		duel: false,
		boss: false
	},
	ids: {
		duelId: null,
		playGachaId: null,
		raidBossResultId: null,
		autoRaidBoss: null
	}
};

lcf.sdop.card = {
	cardType: ["MS_CARD", "PILOT_CARD"],
	gachaType: ["NORMAL", "PLATINA"],
	currentCardType: "PILOT_CARD",
	currentGachaType: "NORMAL",
	count: 0
};

/**
 * 自动抽ms, 并卖出
 * @param {int} sellCardId 需要卖出的ms, not null
 * @param {int} count 剩余的次数
 */
lcf.sdop.card.msCardSellAndPlayGachaResult = function(sellCardId, count) {
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForCardGacha/msCardSellAndPlayGachaResult";
	var logMsg;
	if(null == sellCardId){
		logMsg = "无sellCardId";
		_sdop.log(logMsg);
		console.error(logMsg);
		return;
	}
	if(count <= 0){
		logMsg = "自动卖机已完成！";
		_sdop.log(logMsg);
		console.info(logMsg);
		return;
	}
	var payload = _sdop.createBasePayload("msCardSellAndPlayGachaResult", {
		"cardType":{"value":"MS_CARD"},"gachaType":{"value":"NORMAL"},"msCardId":sellCardId
	});
	_sdop.post(url, payload, function(data){
		if (_sdop.checkError(data, "msCardSellAndPlayGachaResult")) {
			return;
		}
		var cardId = data.args.playGachaResultDetail.msCard.id;
		logMsg = count + " get card id : " + cardId;
		_sdop.log(logMsg);
		console.info(logMsg);
		setTimeout(lcf.sdop.card.msCardSellAndPlayGachaResult, 200, cardId, --count);
	});
}

lcf.sdop.card.playGachaResult = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForCardGacha/playGachaResult";
	var payload = _sdop.createBasePayload("playGachaResult", {
		cardType: {
			"value": _sdop.card.currentCardType
		},
		gachaType: {
			"value": _sdop.card.currentGachaType
		}
	});
	
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "playGachaResult")) {
			return;
		}
		var pilot = data.args.playGachaResultDetail.pilotCard;
		var ms = data.args.playGachaResultDetail.msCard;
		
		var card = pilot;
		if (ms) {
			card = ms;
		}
		
		var playGachaResultDetail = data.args.playGachaResultDetail;
		var headerBriefDetail = playGachaResultDetail.headerBriefDetail;
		var logMsg = "抽取" + playGachaResultDetail.cardType.value + "：" + card.rarity + "c" + card.cost + "【" + card.name + "】，";
		
		var enough = true;
		if (pilot) {
			logMsg += headerBriefDetail.pilot + "/" + headerBriefDetail.pilotMax;
			if (playGachaResultDetail.gachaType.value == _sdop.card.gachaType[0]) {
				logMsg += "，剩余PP：" + headerBriefDetail.duelPoint;
				if (headerBriefDetail.duelPoint < 200) {
					logMsg += "，PP不足200，停止自动！";
					enough = false;
				}
			} else {
				logMsg += "，剩余platinaTicket：" + headerBriefDetail.platinaTicket;
				if (headerBriefDetail.platinaTicket < 5) {
					logMsg += "，platinaTicket不足5，停止自动！";
					enough = false;
				}
			}
			if (headerBriefDetail.pilot >= headerBriefDetail.pilotMax) {
				logMsg += "，pilot已满，停止自动！";
				enough = false;
			}
		} else if (ms) {
			logMsg += headerBriefDetail.ms + "/" + headerBriefDetail.msMax + "，剩余绊P：" + headerBriefDetail.servicePoint;
			if (headerBriefDetail.servicePoint < 200) {
				logMsg += "，绊P不足200，停止自动！";
				enough = false;
			}
			if (headerBriefDetail.ms >= headerBriefDetail.msMax) {
				logMsg += "，ms已满，停止自动！";
				enough = false;
			}
		}
		
		_sdop.log(logMsg);
		if (enough && callback) {
			_sdop.auto.ids.playGachaId = setTimeout(callback, 100);
		}
	});
	
};

lcf.sdop.card.autoPlayGachaResult = function(cardTypeIndex, gachaTypeIndex){
	var _sdop = lcf.sdop;
	if (_sdop.card.count <= 0) {
		return;
	}
	var cardType = _sdop.card.cardType[cardTypeIndex];
	if (cardType) {
		_sdop.card.currentCardType = cardType;
	}
	var gachaType = _sdop.card.gachaType[gachaTypeIndex];
	if (cardTypeIndex == 0) {
		gachaType = _sdop.card.gachaType[0];
	} else if (gachaType) {
		_sdop.card.currentGachaType = gachaType;
	}
	
	_sdop.card.count--;
	_sdop.auto.ids.playGachaId = setTimeout(lcf.sdop.card.playGachaResult, 234, lcf.sdop.card.autoPlayGachaResult);
};



lcf.sdop.duel = {
	targetUnitAttribute: 'FIGHT',
	_z: {
		"isEncount": true,
		"id": 9005
	}
};


lcf.sdop.duel.getEntryData = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForDuel/getEntryData";
	$.ajax({
		url: url,
		type: "GET",
		async: true,
		data: _sdop.createGetParams(),
		success: function(data){
			//console.log(data);
			if (_sdop.checkError(data, "getEntryData")) {
				_sdop.auto.setting.duel = false;
				return;
			}
			var entryList = data.args.list;
			if (callback) {
				setTimeout(callback, 300, entryList);
			}
		}
	});
};

lcf.sdop.duel.getHistoryData = function(){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForDuel/getHistoryData";
	var historyList;
	$.ajax({
		url: url,
		type: "GET",
		async: false,
		data: _sdop.createGetParams(),
		success: function(data){
			//console.log(data);
			if (_sdop.checkError(data, "getHistoryData")) {
				return;
			}
			historyList = data.args.list;
		}
	});
	return historyList;
};

lcf.sdop.duel.getDuelData = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForDuel/getDuelData";
	$.ajax({
		url: url,
		type: "GET",
		async: true,
		data: _sdop.createGetParams(),
		success: function(data){
			//console.log(data);
			if (_sdop.checkError(data, "getDuelData")) {
				_sdop.auto.setting.duel = false;
				_sdop.bp = 0;
				_sdop.ep = 0;
				return;
			}
			var headerDetail = data.args.headerDetail;
			_sdop.bp = headerDetail.bpDetail.currentValue;
			_sdop.ep = headerDetail.energyDetail.energy;
			//_sdop.log("getDuelData请求成功！当前bp:" + _sdop.bp + ", ep: " + _sdop.ep);
			lcf.sdop.checkCallback(callback);
		},
		error: function(){
			_sdop.bp = 0;
			_sdop.ep = 0;
		}
	});
};

lcf.sdop.duel.executeDuelBattle = function(targetId){
	if (!targetId) {
		console.error("executeDuelBattle targetId is null !");
		return;
	}
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForQuestBattle/executeDuelBattle?ssid=" + _sdop.ssid;
	var payload = _sdop.createBasePayload("executeDuelBattle", {
		isEncount: false,
		id: targetId
	});
	
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "executeDuelBattle")) {
			_sdop.auto.setting.duel = false;
			return;
		}
		
		var result = data.args.result.isWin ? "胜利" : "失败";
		var logMsg = "挑战【" + data.args.data.enemyData.name + "】" + result + "！对方MS阵容：";
		
		var enemyMsList = data.args.data.enemyMsList;
		logMsg += lcf.sdop.ms.logMsList(enemyMsList);
		
		_sdop.log(logMsg);
	});
};

lcf.sdop.duel.checkAndExecute = function(){
	lcf.sdop.duel.getDuelData(function(){
		if (lcf.sdop.bp < 5) {
			lcf.sdop.log("当前bp为" + lcf.sdop.bp + "，少于5, 等待下次检查！");
			return;
		}
		lcf.sdop.duel.getEntryData(function(entryList){
			var targetId;
			for (var i in entryList) {
				var entry = entryList[i];
				if (entry.unitAttribute.value == lcf.sdop.duel.targetUnitAttribute) {
					targetId = entry.playerId;
					lcf.sdop.log("准备对【" + entry.playerName + "】发起挑战，对方属性是【" + lcf.sdop.duel.targetUnitAttribute + "】" + entry.unitName + "！");
					break;
				}
			}
			entryList = null;
			lcf.sdop.duel.executeDuelBattle(targetId);
		});
	});
};

lcf.sdop.autoDuel = function(){
	var _sdop = lcf.sdop;
	var _duel = _sdop.duel;
	if (!_sdop.auto.setting.duel) {
		return;
	}
	var delayTime = 120000 + Math.round(Math.random() * 2000);
	_sdop.auto.ids.duelId = setTimeout(function(){
		_duel.checkAndExecute();
		_sdop.autoDuel();
	}, delayTime);
};

lcf.sdop.startAutoDuel = function(unitAttributeIndex){
	var _sdop = lcf.sdop;
	lcf.sdop.auto.setting.duel = true;
	_sdop.autoDuel();
	if (unitAttributeIndex != null) {
		var unitAttribute = _sdop.ms.unitAttribute[unitAttributeIndex];
		if (unitAttribute) {
			_sdop.duel.targetUnitAttribute = unitAttribute;
		}
	}
	_sdop.log("针对【" + _sdop.duel.targetUnitAttribute + "】的自动GB开始！");
	lcf.sdop.duel.checkAndExecute();
};

lcf.sdop.cancelAutoDuel = function(){
	var _sdop = lcf.sdop;
	_sdop.auto.setting.duel = false;
	clearTimeout(_sdop.auto.ids.duelId);
	_sdop.log("自动GB停止成功！");
};

lcf.sdop.autoSayHelloContent = "hello";
lcf.sdop.autoSayHello = function(validate){
	var _sdop = lcf.sdop;
	if (!_sdop.tokenId) {
		alert("请输入tokenId!");
		return;
	}
	
	lcf.sdop.getOwnTeamData(function(){
		lcf.sdop.getGreetingList(function(){
			var users = lcf.mergeObject(_sdop.members, _sdop.greetingUsers);
			var userIndex = 0;
			for (var uid in users) {
				setTimeout(function(uid){
					if (validate) {
						lcf.sdop.getGreetingCondition(uid, function(isHello){
							if (!isHello) {
								_sdop.log("对【" + users[uid] + "】说：" + lcf.sdop.autoSayHelloContent);
								_sdop.postGreeting(uid, lcf.sdop.autoSayHelloContent);
							}
							return;
						});
					} else {
						lcf.sdop.log("对【" + users[uid] + "】说：" + lcf.sdop.autoSayHelloContent);
						lcf.sdop.postGreeting(uid, lcf.sdop.autoSayHelloContent);
					}
				}, ++userIndex * 1234, uid);
			}
		});
	});
};

lcf.sdop.equipItem4Sp = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForCardPlatoon/equipItem?ssid=" + _sdop.ssid;
	var payload = _sdop.createBasePayload("equipItem", {
		itemIdList: [20006, 20011, 20013]
	});
	
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "equipItem4Sp")) {
			return;
		}
		var itemList = data.args.itemList;
		var logMsg = "自动带SP药成功！";
		for (var i in itemList) {
			var item = itemList[i];
			if (item.isEquiped) {
				logMsg += "<br>　　" + item.name + "剩余：" + item.currentStock;
			}
		}
		_sdop.log(logMsg);
		if (callback) {
			setTimeout(callback, 100);
		}
	});
	
};

lcf.sdop.boss = {
	_normal: {
		search: "",
		maxLevel: 31,
		minLevel: 30,
		length: 10,
		key: {
			value: "ALL"
		},
		kind: {
			value: "NORMAL"
		},
		start: 0
	},
	_super: {
		search: "",
		maxLevel: 0,
		minLevel: 0,
		length: 30,
		key: {
			value: "ALL"
		},
		kind: {
			value: "SUPER"
		},
		start: 0
	},
	_currentType: 1,//0表示普通, 1表示超总
	getCurrentType: function(){
		switch (lcf.sdop.boss._currentType) {
		case 0:
			return lcf.sdop.boss._normal;
			break;
		default:
			return lcf.sdop.boss._super;
		}
	},
	mode: ["NORMAL", "RAID_BOSS"],
	kind: ["NORMAL", "SUPER"],
	currentMode: 'RAID_BOSS',
	currentKind: 'SUPER',
	x3: [250070],
	x6: 250071,
	getTopLevel: function(list){
		var target, _currentBoss;
		var Least_Hp = 5000000;
		for (var i in list) {
			_currentBoss = list[i];
			if (target) {
				//if (_currentBoss.isForRecommend) {
				//if (!target.isForRecommend) {//目标是不推荐
				//target = _currentBoss;
				//continue;
				//}
				//} else {//不推荐
				//continue;
				//}
				if(_currentBoss.state.value == "JOIN"){//已参战
					continue;
				}
				if (_currentBoss.currentHp < Least_Hp) {//判断血量
					continue;
				}
				if (target.level > _currentBoss.level) {//判断等级
					continue;
				} else if (target.level < _currentBoss.level) {
					target = _currentBoss;
					continue;
				}
				if (target.currentHp < _currentBoss.currentHp) {//判断剩余血量
					target = _currentBoss;
				}
			} else {
				if (_currentBoss.currentHp < Least_Hp) {
					continue;
				}
				target = _currentBoss;
			}
		}
		return target;
	},
	battleId: 0,
	playerMsList: [],
	getPlayerByOwnerId: function(ownerId){
		for (var k in lcf.sdop.boss.playerMsList) {
			if (ownerId == lcf.sdop.boss.playerMsList[k].id) {
				return lcf.sdop.boss.playerMsList[k];
			}
		}
		return null;
	},
	itemList: [],
	ownerId: null,
	itemTurn: false
};

/**
 * 初始化
 * @param {function} callback
 */
lcf.sdop.boss.initRaidBossOutlineList = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossList/initRaidBossOutlineList?ssid=" + _sdop.ssid;
	var payload = _sdop.createBasePayload("initRaidBossOutlineList", {
		"length": 5,
		"key": {
			"value": "ALL"
		}
	});
	_sdop.post(url, payload, function(data){
		if (lcf.sdop.checkError(data, "initRaidBossOutlineList")) {
			return;
		}
		var headerDetail = data.args.headerDetail;
		lcf.sdop.ms.max = headerDetail.msMax;
		lcf.sdop.ms.current = headerDetail.ms;
		lcf.sdop.pilot.max = headerDetail.pilotMax;
		lcf.sdop.pilot.current = headerDetail.pilot;
		
		_sdop.log("获取总力战初始化信息成功！当前MS：" + lcf.sdop.ms.current + "/" + lcf.sdop.ms.max);
		if (callback) {
			setTimeout(callback, 100, data);
		}
	});
};

/**
 * 获取完成列表
 * @param {function} callback
 */
lcf.sdop.boss.getRaidBossOutlineList4Finish = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossList/getRaidBossOutlineList";
	var payload = _sdop.createBasePayload("getRaidBossOutlineList", {
		"start": 0,
		"length": 5,
		"key": {
			"value": "FINISH"
		},
		"kind": {
			"value": "NORMAL"
		},
		"search": "",
		"minLevel": 0,
		"maxLevel": 0
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "getRaidBossOutlineList4Finish")) {
			return;
		}
		var finish = data.args.listNum.finish;
		_sdop.log("总力战剩余<b class='c_blue'>" + finish + "</b>个结果待处理！");
		if (callback) {
			setTimeout(callback, 100, data.args.list);
		}
	});
};

/**
 * 获取单个boss的结果
 * @param {int} raidBossId
 * @param {function} callback
 */
lcf.sdop.boss.getRaidBossResultData = function(raidBossId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForRaidBossResult/getRaidBossResultData";
	var params = _sdop.createGetParams();
	params.raidBossId = raidBossId;
	lcf.sdop.get(url, params, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "getRaidBossResultData")) {
			return;
		}
		var award = data.args.award;
		if (award) {
			var logMsg = "获得" + award.awardGp + "GP，当前GP：" + award.currentGp;
			logMsg += "。获得【" + award.kind.value + "】抽取机会【" + award.num + "】次！";
			_sdop.log(logMsg);
		} else {
			_sdop.log("击破失败！");
		}
		if (callback) {
			setTimeout(callback, 100, award);
		}
	});
};

/**
 * boss抽奖
 * @param {int} raidBossId
 * @param {function} callback
 */
lcf.sdop.boss.raidBossGacha = function(raidBossId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossResult/raidBossGacha";
	var payload = _sdop.createBasePayload("raidBossGacha", {
		"raidBossId": raidBossId
	});
	if (lcf.sdop.ms.current >= lcf.sdop.ms.max) {
		_sdop.log("MS数量已满，停止自动！");
	}
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "raidBossGacha")) {
			return;
		}
		var restGacha = data.args.restGacha;
		var card = data.args.result;
		var logMsg = "获得：";
		if (card.rarity > 2) {
			logMsg += "<b class='c_red'>" + card.rarity + "c" + card.cost + "</b>【" + card.name + "】，";
		} else {
			logMsg += card.rarity + "c" + card.cost + "【" + card.name + "】，";
		}
		lcf.sdop.ms.current++;
		logMsg += lcf.sdop.ms.current + "/" + lcf.sdop.ms.max + "，";
		if (lcf.sdop.ms.current >= lcf.sdop.ms.max) {
			logMsg += "MS数量已满，停止自动！";
			_sdop.log(logMsg);
			return;
		}
		if (restGacha > 0) {
			logMsg += "还有" + restGacha + "次！";
			_sdop.log(logMsg);
			setTimeout(lcf.sdop.boss.raidBossGacha, 100, raidBossId, callback);
			return;
		} else {
			logMsg += "该次已经抽完！";
		}
		_sdop.log(logMsg);
		if (callback) {
			setTimeout(callback, 100);
		}
	});
};

/**
 * 删除boss结果
 * @param {int} raidBossId
 * @param {function} callback
 */
lcf.sdop.boss.deleteRaidBossList = function(raidBossId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossList/deleteRaidBossList";
	var payload = _sdop.createBasePayload("deleteRaidBossList", {
		"id": raidBossId
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "deleteRaidBossList")) {
			return;
		}
		_sdop.log("Boss结果删除成功！");
		if (callback) {
			setTimeout(callback, 100);
		}
	});
};

/**
 * 自动处理总力战结果
 */
lcf.sdop.boss.autoRaidBossResult = function(){
	var _sdop = lcf.sdop;
	lcf.sdop.boss.getRaidBossOutlineList4Finish(function(results){
		if (results == null || results.length == 0) {//无结果
			_sdop.log("总力战结果已全部处理完毕！");
			return;
		}
		for (var i in results) {
			var r = results[i];
			var logMsg = "正在处理【" + r.comment + "】，state：" + r.state.value;
			logMsg += "，可抽" + r.restGacha + "回。";
			_sdop.log(logMsg);
			lcf.sdop.boss.getRaidBossResultData(r.id, function(award){
				if (award && award.num > 0) {
					lcf.sdop.boss.raidBossGacha(r.id, function(){
						lcf.sdop.boss.deleteRaidBossList(r.id, function(){
							lcf.sdop.auto.ids.raidBossResultId = setTimeout(lcf.sdop.boss.autoRaidBossResult, 100);
						});
					});
				} else {
					lcf.sdop.boss.deleteRaidBossList(r.id, function(){
						lcf.sdop.auto.ids.raidBossResultId = setTimeout(lcf.sdop.boss.autoRaidBossResult, 100);
					});
				}
			});
			break;//只执行一次
		}
	});
};

/**
 * 获取总力boss列表
 * @param {function} callback 成功, 则会带入目标boss的对象, 可通过target.id获取对应id
 * @param {function} noListCallback
 */
lcf.sdop.boss.getRaidBossOutlineList = function(callback, noListCallback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossList/getRaidBossOutlineList";
	var payload = _sdop.createBasePayload("getRaidBossOutlineList", lcf.sdop.boss.getCurrentType());
	
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "getRaidBossOutlineList")) {
			return;
		}
		
		var list = data.args.list;
		if (!list || list.length == 0) {
			_sdop.log("没有对应等级的boss！");
			if (!lcf.sdop.auto.setting.boss) {
				return;
			}
			if (noListCallback) {
				setTimeout(noListCallback, 100);
			}
			return;
		}
		
		var target = lcf.sdop.boss.getTopLevel(list);
		if(null == target){
			if (noListCallback) {
				setTimeout(noListCallback, 100);
			}
			return;
		}
		_sdop.log(lcf.sdop.boss.currentKind + "目标boss等级：<b class='c_red'>" + target.level + "</b>，残余血量：" + target.currentHp + "，【" + target.comment + "】");
		if (callback) {
			setTimeout(callback, 100, target);
		}
	});
};

/**
 * 尝试进入boss
 * @param {int} bossId
 * @param {function} callback
 * @param {function} failCallback
 */
lcf.sdop.boss.postRaidBossBattleEntry = function(bossId, callback, failCallback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForRaidBossList/postRaidBossBattleEntry";
	var payload = _sdop.createBasePayload("postRaidBossBattleEntry", {
		"id": bossId,
		"isChargeBp": false
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "postRaidBossBattleEntry")) {
			if (data.args.message.indexOf("BPが不足しています") != -1) {
				return;
			}
			if (failCallback) {
				setTimeout(failCallback, 100);
			}
			return;
		}
		_sdop.log("进入boss成功！");
		if (callback) {
			setTimeout(callback, 100);
		}
	});
};

lcf.sdop.pilot = {
	activeSkill: [],
	passiveSkill: [],
	current: null,
	max: null
};
lcf.sdop.pilot.activeSkill[21001] = {
	id: 21001,
	prefix: "味方1人の攻撃力を4ターンの間、",
	cost: 24
};
lcf.sdop.pilot.activeSkill[21002] = {
	id: 21002,
	prefix: "味方1人の機動力を4ターンの間、",
	cost: 25
};
lcf.sdop.pilot.passiveSkill[51001] = {
	id: 51001,
	prefix: "常に攻撃力+",
	suffix: "%アップするスキル"
};
lcf.sdop.pilot.passiveSkill[51002] = {
	id: 51002,
	prefix: "常に機動力+",
	suffix: "%アップするスキル"
};
lcf.sdop.pilot.getPassiveSkillUpValue = function(skillId, skillDescription){
	return parseInt(skillDescription.replace(lcf.sdop.pilot.passiveSkill[skillId].prefix, ''));
};

/**
 * 检查是否x6机体
 * @param {Object} m members中的member, playerList中的player.card
 */
lcf.sdop.boss.checkX6 = function(m){
	for (var j in m.characteristicList) {
		if (m.characteristicList[j].id == lcf.sdop.boss.x6) {
			m.lcf_attack = 6;
			return true;
		}
	}
	return false;
};

/**
 * 检查是否x3机体
 * @param {Object} m members中的member, playerList中的player.card
 */
lcf.sdop.boss.checkX3 = function(m){
	for (var j in m.characteristicList) {
		for(var i in lcf.sdop.boss.x3){
			if (m.characteristicList[j].id == lcf.sdop.boss.x3[i]) {
				m.lcf_attack = 3;
				return true;
			}
		}
	}
	return false;
};

/**
 * 选择非倍机成员
 * @param {Array} members
 * @param {Member} other 已选成员
 */
lcf.sdop.boss.getNotAttackMember = function(members, other){
	var nextMember;
	for (var i in members) {
		var m = members[i];
		if (m.id == lcf.sdop.myUserId) {//不能是自己
			continue;
		}
		if (other && (m.id == other.id)) {//不能是已选成员
			continue;
		}
		if (m.coolTime != 0) {//没有冷却
			continue;
		}
		//校验是否倍机
		if (lcf.sdop.boss.checkX6(m)) {
			m.lcf_attack = 6;
		} else if (lcf.sdop.boss.checkX3(m)) {
			m.lcf_attack = 3;
		} else {
			m.lcf_attack = 1;
		}
		if (null == nextMember) {//默认没有选择时
			nextMember = m;
			continue;
		}
		if (nextMember.lcf_attack > m.lcf_attack) {//倍数级别高
			nextMember = m;
			continue;
		} else if (nextMember.lcf_attack < m.lcf_attack) {
			continue;
		}
		//倍数级别一样
		if (nextMember.attack < m.attack) {//攻击低
			continue;
		}
		
		nextMember = m;
	}
	return nextMember;
};

/**
 * 选择合适的倍机
 * @param {Array} members
 */
lcf.sdop.boss.getFixAttackMember = function(members){
	var attackMember;
	//选择倍机
	for (var i in members) {
		var m = members[i];
		if (m.id == lcf.sdop.myUserId) {//不能是自己
			continue;
		}
		if (m.coolTime != 0) {//没有冷却
			continue;
		}
		//校验是否倍机
		if (lcf.sdop.boss.checkX6(m)) {
			m.lcf_attack = 6;
		} else if (lcf.sdop.boss.checkX3(m)) {
			if (lcf.sdop.boss.AI.checkAttackSkill4Card(m)) {
				m.lcf_attack = 3;
			} else {
				m.lcf_attack = 2;
			}
		} else {
			m.lcf_attack = 1;
		}
		if (attackMember == null) {//默认没有选择时
			attackMember = m;
			continue;
		}
		if (attackMember.lcf_attack > m.lcf_attack) {//倍数级别高
			continue;
		} else if (attackMember.lcf_attack < m.lcf_attack) {
			attackMember = m;
			continue;
		}
		//倍数级别一样
		if (attackMember.attack > m.attack) {//攻击高
			continue;
		}
		attackMember = m;
	}
	
	return attackMember;
};

lcf.sdop.boss.getTrueSpeed = function(m){
	var speed = m.speed;
	if (m.pilot.passiveSkillList == null) {
		return speed;
	}
	for (var j in m.pilot.passiveSkillList) {
		var skill = m.pilot.passiveSkillList[j];
		if (skill.id == 51002) {//被动加速
			speed *= 1 + lcf.sdop.pilot.getPassiveSkillUpValue(51002, skill.description) / 100;
		}
	}
	return speed;
};

/**
 * 必须在调用完lcf.sdop.boss.getFixAttackMember后才能调用
 * @param {Array} members
 * @param {Object} attackMember
 */
lcf.sdop.boss.getFixHelpMember = function(members, attackMember){
	if (attackMember == null) {
		return null;
	}
	attackMember.lcf_speed = lcf.sdop.boss.getTrueSpeed(attackMember);
	var helpMember;
	var skill;
	for (var i in members) {
		var m = members[i];
		if (m.id == lcf.sdop.myUserId) {//不能是自己
			continue;
		}
		if (m.id == attackMember.id || m.lcf_attack > 1) {//不能是倍机
			continue;
		}
		if (m.coolTime != 0) {//没有冷却
			continue;
		}
		if (m.pilot.activeSkillList == null) {//没有主动技能
			m.lcf_isHelp = false;
		} else {
			for (var j in m.pilot.activeSkillList) {
				skill = m.pilot.activeSkillList[j];
				if (skill.id == 21001) {
					m.lcf_isHelp = true;
					break;
				} else {
					m.lcf_isHelp = false;
				}
			}
			
		}
		m.lcf_speed = lcf.sdop.boss.getTrueSpeed(m);
		
		if (helpMember == null) {//默认没有选择时
			helpMember = m;
			continue;
		}
		if (helpMember.lcf_isHelp) {//选定的是辅助机
			if (helpMember.lcf_speed >= attackMember.lcf_speed) {//辅助机已经快过倍机
				break;
			}
			if (m.lcf_isHelp) {//当前是辅助机
				if (helpMember.lcf_speed > m.lcf_speed) {//选定机比当前机快
					continue;
				} else {
					helpMember = m;
					continue;
				}
			}
		}
		if (m.lcf_isHelp) {//当前是辅助机, 选定机不是
			helpMember = m;
			continue;
		}
		//剩余的规则没想好
	}
	
	return helpMember;
};

/**
 * 专门处理超总时的情况, 进入前, 请先确保myUserId是有值
 * @param {Object} members
 */
lcf.sdop.pilot.getFixMember = function(members){
	var _sdop = lcf.sdop;
	var attackMember = lcf.sdop.boss.getFixAttackMember(members);
	var helpMember = lcf.sdop.boss.getFixHelpMember(members, attackMember);
	
};

/**
 * 获取和boss的战斗数据
 * @param {int} raidBossId
 * @param {function} callback
 */
lcf.sdop.boss.getRaidBossBattleData = function(raidBossId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForQuestBattle/getRaidBossBattleData";
	var params = _sdop.createGetParams();
	params.raidBossId = raidBossId;
	lcf.sdop.get(url, params, function(data){
		//console.log(data);
		if (_sdop.checkError(data, "getRaidBossBattleData")) {
			return;
		}
		_sdop.myUserId = data.args.leaderCardId;
		var battleId = data.args.battleId;
		var members = data.args.memberCardList;
		
		if (callback) {
			setTimeout(callback, 100, data.args);
		}
	});
};

/**
 * 断网后，重新获取boss战斗数据的方法
 * @param {function} callback
 */
lcf.sdop.boss.getBattleData = function(callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForQuestBattle/getBattleData";
	lcf.sdop.get(url, _sdop.createGetParams(), function(data){
		//console.log(data);
		if (_sdop.checkError(data, "getBattleData")) {
			return;
		}
		if (callback) {
			setTimeout(callback, 100, data.args);
		}
	});
};

/**
 * true战斗自动, 不进行AI判定
 */
lcf.sdop.boss.isAutoBattle = false;

/**
 * 选好人, 并开始boss战斗
 * @param {int} battleId
 * @param {int} firstId
 * @param {int} secondId
 * @param {function} callback
 */
lcf.sdop.boss.executeBattleStart = function(battleId, firstId, secondId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForQuestBattle/executeBattleStart";
	var payload = _sdop.createBasePayload("executeBattleStart", {
		"battleId": battleId,
		"unitList": [{
			"id": _sdop.myUserId,
			"arrangement": {
				"value": "MIDDLE_FRONT"
			},
			"isUseChargePoint": false,
			"isNpc": false,
			"isLeader": true,
			"isUseGoldPoint": false
		}, {
			"id": firstId,
			"arrangement": {
				"value": "BOTTOM_FRONT"
			},
			"isUseChargePoint": false,
			"isNpc": false,
			"isLeader": false,
			"isUseGoldPoint": false
		}, {
			"id": secondId,
			"arrangement": {
				"value": "TOP_FRONT"
			},
			"isUseChargePoint": false,
			"isNpc": false,
			"isLeader": false,
			"isUseGoldPoint": false
		}],
		"isAutoBattle": lcf.sdop.boss.isAutoBattle,
		"mode": {
			"value": _sdop.boss.currentMode
		}
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "executeBattleStart")) {
			return;
		}
		_sdop.log("boss战开始！是否自动：" + lcf.sdop.boss.isAutoBattle);
		if (callback) {
			setTimeout(callback, 100, data.args);
		}
	});
};

lcf.sdop.boss.actionType = ["ITEM", "SKILL", "ATTACK"];

/**
 * 执行技能
 * @param {int} battleId
 * @param {String} actionTypeValue 对应lcf.sdop.boss.actionType
 * @param {int} targetId 1为boss, 2-4为参战队友
 * @param {int} playerId 同targetId
 * @param {int} actionId 技能代号, 30sp药为20006
 * @param {function} callback
 */
lcf.sdop.boss.executeActionCommand = function(battleId, actionTypeValue, targetId, playerId, actionId, callback){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/PostForQuestBattle/executeActionCommand";
	var payload = _sdop.createBasePayload("executeActionCommand", {
		"battleId": battleId,
		"actionType": {
			"value": actionTypeValue
		},
		"mode": {
			"value": _sdop.boss.currentMode
		},
		"targetId": targetId,
		"playerId": playerId,
		"isAutoBattle": false,
		"actionId": actionId,
		"isRaidBossChargeAttack": false
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "executeActionCommand")) {
			return;
		}
		//_sdop.log("boss战开始！");
		if (callback) {
			setTimeout(callback, 100, data.args);
		}
	});
};
lcf.sdop.item = [];
lcf.sdop.item[20006] = {
	id: 20006,
	name: "SP回復ドリンク30"
};
lcf.sdop.item[20013] = {
	id: 20013,
	name: "SP回復ドリンク60"
};

/**
 * 检查当前sp, 根据条件执行
 * @param {function} callback
 */
lcf.sdop.boss.checkSp = function(callback){
	lcf.sdop.log("开始校验SP！当前SP：" + lcf.sdop.currentSp + "，最大SP：" + lcf.sdop.maxSp + "，上一个动作是否使用物品：" + lcf.sdop.boss.itemTurn);
	if (lcf.sdop.maxSp - lcf.sdop.currentSp <= 30 || lcf.sdop.boss.itemTurn) {
		setTimeout(callback, 100);
		return;
	}
	var _item;
	var _itemId = 20006;
	var itemList = lcf.sdop.boss.itemList;
	if (lcf.sdop.maxSp - lcf.sdop.currentSp > 30) {
		for (var j in itemList) {
			_item = itemList[j];
			if (_item.id == _itemId) {
				if (_item.num > 0) {
					_item.num--;
					lcf.sdop.currentSp += 30;
					lcf.sdop.boss.itemTurn = true;
					lcf.sdop.log("使用【" + _item.name + "】，当前SP：" + lcf.sdop.currentSp);
					setTimeout(lcf.sdop.boss.executeActionCommand, 100, lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[0], lcf.sdop.boss.ownerId, lcf.sdop.boss.ownerId, _itemId, callback);
					return;
				}
			}
		}
	}
	
	_itemId = 20013;
	if (lcf.sdop.currentSp < 30) {
		for (var j in itemList) {
			_item = itemList[j];
			if (_item.id == _itemId) {
				if (_item.num > 0) {
					_item.num--;
					lcf.sdop.currentSp += 60;
					lcf.sdop.boss.itemTurn = true;
					lcf.sdop.log("使用【" + _item.name + "】，当前SP：" + lcf.sdop.currentSp);
					setTimeout(lcf.sdop.boss.executeActionCommand, 100, lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[0], lcf.sdop.boss.ownerId, lcf.sdop.boss.ownerId, _itemId, callback);
					return;
				}
			}
		}
	}
	
	setTimeout(callback, 100);
	return;
};

lcf.sdop.boss.AI = {
	actionCode: [{
		code: 0,
		name: "普通攻击"
	}, {
		code: 1,
		name: "给队友加攻击状态"
	}, {
		code: 2,
		name: "给队友加速度状态"
	}, {
		code: 3,
		name: "单体技能攻击",
		prefix: "敵単体に"
	}],
	simple: [0, 0, 0, 0, 0, 0],
	me: [1, 2, 2, 0, 0, 0],
	help: [1, 0, 0, 0, 0, 0],
	attack: [3, 3, 3, 3, 3, 3],
	attackPlayers: []
};

lcf.sdop.boss.AI.checkMySkill = function(player){
	var activeSkillList = player.card.pilot.activeSkillList;
	for (var k in activeSkillList) {
		if (activeSkillList[k].id == 21001) {
			continue;
		}
		if (activeSkillList[k].id == 21002) {
			continue;
		}
		return false;
	}
	return true;
};

/**
 * 返回true表示有攻击性技能
 * @param {Object} player
 */
lcf.sdop.boss.AI.checkAttackSkill = function(player){
	return lcf.sdop.boss.AI.checkAttackSkill4Card(player.card);
};

/**
 * 返回true表示有攻击性技能
 * @param {Object} card
 */
lcf.sdop.boss.AI.checkAttackSkill4Card = function(card){
	var activeSkillList = card.pilot.activeSkillList;
	for (var k in activeSkillList) {
		if (activeSkillList[k].description.indexOf(lcf.sdop.boss.AI.actionCode[3].prefix) === 0) {
			return true;
		}
	}
	return false;
};

/**
 * 获得攻击技能对象, 有属性skill.id和skill.cost
 * 若返回null, 则没有对应的攻击技能
 * @param {Object} player
 */
lcf.sdop.boss.AI.getAttackSkill = function(player){
	var activeSkillList = player.card.pilot.activeSkillList;
	for (var k in activeSkillList) {
		if (activeSkillList[k].description.indexOf(lcf.sdop.boss.AI.actionCode[3].prefix) === 0) {
			return activeSkillList[k];
		}
	}
	return null;
};

lcf.sdop.boss.AI.checkHelpSkill = function(player){
	var activeSkillList = player.card.pilot.activeSkillList;
	for (var k in activeSkillList) {
		if (activeSkillList[k].id == 21001) {
			return true;
		}
	}
	return false;
};

/**
 * 设置适应的行动AI
 */
lcf.sdop.boss.AI.fixPlayerMsListAI = function(){
	var playerMsList = lcf.sdop.boss.playerMsList;
	var hasAttack = false;
	for (var j in playerMsList) {
		var player = playerMsList[j];
		player.AIType = lcf.sdop.boss.AI.simple;
		player.AITurn = 0;
		if (player.card.id == lcf.sdop.myUserId) {
			if (lcf.sdop.boss.AI.checkMySkill(player)) {
				player.AIType = lcf.sdop.boss.AI.me;
			}
			continue;
		}
		if (lcf.sdop.boss.checkX6(player.card) || lcf.sdop.boss.checkX3(player.card)) {
			if (lcf.sdop.boss.AI.checkAttackSkill(player)) {
				player.AIType = lcf.sdop.boss.AI.attack;
				player.lcf_attack = player.card.lcf_attack;//设置倍数信息
				lcf.sdop.boss.AI.attackPlayers[player.id] = player;
				hasAttack = true;
			}
			continue;
		}
		if (lcf.sdop.boss.AI.checkHelpSkill(player)) {
			player.AIType = lcf.sdop.boss.AI.help;
		}
	}
	if (!hasAttack) {
		for (var j in playerMsList) {
			var player = playerMsList[j];
			player.AIType = lcf.sdop.boss.AI.simple;
		}
	}
};

/**
 * 获取对应的行动代号
 * @param {Object} player
 */
lcf.sdop.boss.AI.getActionCode = function(player){
	return player.AIType[player.AITurn];
};

/**
 * 获取合适的倍机
 * @param {String} fieldName
 */
lcf.sdop.boss.AI.getFixAttackPlayerInBattle = function(fieldName){
	var _fixPlayer, _player;
	for (var k in lcf.sdop.boss.AI.attackPlayers) {
		_player = lcf.sdop.boss.AI.attackPlayers[k];
		if (_player[fieldName]) {
			_player[fieldName] = 0;
		}
		if (_fixPlayer == null) {
			_fixPlayer = _player;
			continue;
		}
		if (_fixPlayer[fieldName] > _player[fieldName]) {//状态的次数多
			_fixPlayer = _player;
			continue;
		}
		if (_fixPlayer.lcf_attack < _player.lcf_attack) {//相同时比较倍数
			_fixPlayer = _player;
			continue;
		}
	}
	return _fixPlayer;
};

/**
 * 自动战斗, 用于循环执行
 */
lcf.sdop.boss.AI.autoBattle = function(data){
	var actionOrder = data.actionOrderList[data.actionOrderList.length - 1];
	if (data.resultData) {//仅针对超总的判断, 或者可根据data.resultDate是否为空来判断
		console.info(data);
		lcf.sdop.log("Boss战结束！");
		return;
	}
	//获取对应的player
	var player = lcf.sdop.boss.getPlayerByOwnerId(actionOrder.ownerId);
	if (player == null) {
		lcf.sdop.log("没有对应的ownerId：" + actionOrder.ownerId);
		return;
	}
	lcf.sdop.boss.ownerId = actionOrder.ownerId;
	
	lcf.sdop.boss.checkSp(function(){
		// 获取对应的行动
		var actionCode = lcf.sdop.boss.AI.getActionCode(player);
		lcf.sdop.boss.itemTurn = false;
		var targetPlayer;
		var targetId, actionId;
		var skill;
		var logMsg;
		if (player.lcf_attack > 1) {
			logMsg = "【<b class='c_red'>" + player.card.userName + "</b>】";
		} else {
			logMsg = "【" + player.card.userName + "】";
		}
		switch (actionCode) {
		case 0://普通攻击
			targetId = 1;
			actionId = player.card.weaponList[0].id;
			lcf.sdop.boss.executeActionCommand(lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[2], targetId, lcf.sdop.boss.ownerId, actionId, lcf.sdop.boss.AI.autoBattle);
			logMsg += "对Boss使用武器" + player.card.weaponList[0].name + "进行攻击！";
			break;
		case 1://给队友加攻击状态
			//targetId = lcf.sdop.boss.AI.attackPlayers[lcf.sdop.boss.AI.attackPlayers.length - 1];
			targetPlayer = lcf.sdop.boss.AI.getFixAttackPlayerInBattle('lcf_attack_buff');
			targetId = targetPlayer.id;
			actionId = 21001;
			skill = lcf.sdop.pilot.activeSkill[actionId];
			lcf.sdop.currentSp -= skill.cost;
			lcf.sdop.boss.executeActionCommand(lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[1], targetId, lcf.sdop.boss.ownerId, actionId, lcf.sdop.boss.AI.autoBattle);
			logMsg += "对队友【" + targetPlayer.card.userName + "】使用技能【" + skill.prefix + "】，消耗SP：" + skill.cost + "，剩余SP：" + lcf.sdop.currentSp + "！";
			break;
		case 2://给队友加速度状态
			//targetId = lcf.sdop.boss.AI.attackPlayers[lcf.sdop.boss.AI.attackPlayers.length - 1];
			targetPlayer = lcf.sdop.boss.AI.getFixAttackPlayerInBattle('lcf_attack_buff');
			targetId = targetPlayer.id;
			actionId = 21002;
			skill = lcf.sdop.pilot.activeSkill[actionId];
			lcf.sdop.currentSp -= skill.cost;
			lcf.sdop.boss.executeActionCommand(lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[1], targetId, lcf.sdop.boss.ownerId, actionId, lcf.sdop.boss.AI.autoBattle);
			logMsg += "对队友【" + targetPlayer.card.userName + "】使用技能【" + skill.prefix + "】，消耗SP：" + skill.cost + "，剩余SP：" + lcf.sdop.currentSp + "！";
			break;
		case 3://单体技能攻击
			targetId = 1;
			actionId = player.card.weaponList[0].id;
			actionTypeIndex = 2;
			var skill = lcf.sdop.boss.AI.getAttackSkill(player);
			if (skill) {
				actionId = skill.id;
				actionTypeIndex = 1;
				lcf.sdop.currentSp -= skill.cost;
				logMsg += "对Boss使用技能【" + skill.description + "】，消耗SP：" + skill.cost + "，剩余SP：" + lcf.sdop.currentSp + "！";
			} else {
				logMsg += "对Boss使用武器" + player.card.weaponList[0].name + "进行攻击！";
			}
			lcf.sdop.boss.executeActionCommand(lcf.sdop.boss.battleId, lcf.sdop.boss.actionType[actionTypeIndex], targetId, lcf.sdop.boss.ownerId, actionId, lcf.sdop.boss.AI.autoBattle);
			break;
		}
		player.AITurn++;
		lcf.sdop.log(logMsg);
	});
	
};

/**
 * 自动总力
 */
lcf.sdop.boss.autoNormalRaidBoss = function(){
	if (!lcf.sdop.auto.setting.boss) {
		return;
	}
	lcf.sdop.boss.getRaidBossOutlineList(function(boss){
		lcf.sdop.boss.postRaidBossBattleEntry(boss.id, function(){
			lcf.sdop.boss.getRaidBossBattleData(boss.id, function(battleData){
				lcf.sdop.myUserId = battleData.leaderCardId;
				var my = battleData.playerMsList[0];
				
				lcf.sdop.currentSp = my.card.currentSp;
				lcf.sdop.maxSp = my.card.maxSp;
				
				lcf.sdop.boss.battleId = battleData.battleId;
				var members = battleData.memberCardList;
				var first = lcf.sdop.boss.getNotAttackMember(members);
				var second = lcf.sdop.boss.getNotAttackMember(members, first);
				
				lcf.sdop.boss.isAutoBattle = true;
				//开始战斗
				setTimeout(lcf.sdop.boss.executeBattleStart, 3000, lcf.sdop.boss.battleId, first.id, second.id);
			});
		}, function(){
			setTimeout(lcf.sdop.boss.autoNormalRaidBoss, 1000);
		});
	}, function(){
		setTimeout(lcf.sdop.boss.autoNormalRaidBoss, 1000);
	});
};

/**
 * 自动超总
 */
lcf.sdop.boss.autoSuperRaidBoss = function(){
	if (!lcf.sdop.auto.setting.boss) {
		return;
	}
	lcf.sdop.boss.getRaidBossOutlineList(function(boss){
		lcf.sdop.boss.postRaidBossBattleEntry(boss.id, function(){
			lcf.sdop.boss.getRaidBossBattleData(boss.id, function(battleData){
				lcf.sdop.myUserId = battleData.leaderCardId;
				lcf.sdop.boss.AI.attackPlayers = [];
				
				var my = battleData.playerMsList[0];
				lcf.sdop.currentSp = my.card.currentSp;
				lcf.sdop.maxSp = my.card.maxSp;
				
				lcf.sdop.boss.battleId = battleData.battleId;
				var members = battleData.memberCardList;
				var attackMember = lcf.sdop.boss.getFixAttackMember(members);
				var helpMember = lcf.sdop.boss.getFixHelpMember(members, attackMember);
				
				lcf.sdop.boss.isAutoBattle = false;
				//带药
				setTimeout(lcf.sdop.equipItem4Sp, 100, function(){
					//开始战斗
					setTimeout(lcf.sdop.boss.executeBattleStart, 3000, lcf.sdop.boss.battleId, attackMember.id, helpMember.id, function(dataArgs){
						lcf.sdop.boss.playerMsList = dataArgs.playerMsList;
						lcf.sdop.boss.AI.fixPlayerMsListAI();
						lcf.sdop.log("对Boss选择阵容：" + lcf.sdop.ms.logMsList(lcf.sdop.boss.playerMsList));
						
						lcf.sdop.boss.itemList = dataArgs.itemList;
						
						setTimeout(lcf.sdop.boss.AI.autoBattle, 100, dataArgs);
					});
				});
				
			});
		}, function(){
			setTimeout(lcf.sdop.boss.autoSuperRaidBoss, 100);
		});
	}, function(){
		setTimeout(lcf.sdop.boss.autoSuperRaidBoss, 1000);
	});
};

/**
 * 开始自动总力, UI调用
 */
lcf.sdop.boss.AI.startAutoNormalRaidBoss = function(){
	lcf.sdop.auto.setting.boss = true;
	lcf.sdop.boss._currentType = 0;
	clearTimeout(lcf.sdop.auto.ids.autoRaidBoss);
	//立刻检查BP
	lcf.sdop.boss.initRaidBossOutlineList(function(data){
		var bpDetail = data.args.headerDetail.bpDetail;
		lcf.sdop.bp = bpDetail.currentValue;
		var logMsg = "当前BP：" + lcf.sdop.bp;
		
		var delayTime;
		if (lcf.sdop.bp >= 10) {
			setTimeout(lcf.sdop.boss.autoNormalRaidBoss, 100);
			logMsg += "，满足总力要求！";
			delayTime = 180;
		} else {
			//delayTime = 60000 + Math.round(Math.random() * 2000)
			var recoveryTime = (bpDetail.maxValue - bpDetail.currentValue - 1) * bpDetail.recoveryInterval + bpDetail.recoveryTime;
			delayTime = (recoveryTime - 60);
			if (delayTime < 0) {
				delayTime = recoveryTime / 2;
			}
			logMsg += "，不满足总力要求！" + delayTime + "秒后再尝试！";
		}
		lcf.sdop.log(logMsg);
		lcf.sdop.auto.ids.autoRaidBoss = setTimeout(lcf.sdop.boss.AI.startAutoNormalRaidBoss, delayTime * 1000);
	});
};

/**
 * 开始自动超总, UI调用
 */
lcf.sdop.boss.AI.startAutoSuperRaidBoss = function(){
	lcf.sdop.auto.setting.boss = true;
	lcf.sdop.boss._currentType = 1;
	clearTimeout(lcf.sdop.auto.ids.autoRaidBoss);
	//立刻检查BP
	lcf.sdop.boss.initRaidBossOutlineList(function(data){
		var bpDetail = data.args.headerDetail.bpDetail;
		lcf.sdop.bp = bpDetail.currentValue;
		var logMsg = "当前BP：" + lcf.sdop.bp;
		
		var delayTime;
		if (lcf.sdop.bp >= 10) {
			setTimeout(lcf.sdop.boss.autoSuperRaidBoss, 100);
			logMsg += "，满足超总要求！";
			delayTime = 180;
		} else {
			//delayTime = 60000 + Math.round(Math.random() * 2000)
			var recoveryTime = (bpDetail.maxValue - bpDetail.currentValue - 1) * bpDetail.recoveryInterval + bpDetail.recoveryTime;
			delayTime = (recoveryTime - 60);
			if (delayTime < 0) {
				delayTime = recoveryTime / 2;
			}
			logMsg += "，不满足超总要求！" + delayTime + "秒后再尝试！";
		}
		lcf.sdop.log(logMsg);
		lcf.sdop.auto.ids.autoRaidBoss = setTimeout(lcf.sdop.boss.AI.startAutoSuperRaidBoss, delayTime * 1000);
	});
};

/**
 * 取消自动超总, UI调用
 */
lcf.sdop.boss.AI.cancelAutoSuperRaidBoss = function(){
	lcf.sdop.auto.setting.boss = false;
	clearTimeout(lcf.sdop.auto.ids.autoRaidBoss);
	lcf.sdop.log("自动超总停止成功！");
};

lcf.sdop.map = {};

/**
 * 查询当前地图
 * @param {Object} callback
 */
lcf.sdop.map.getQuestData = function(callback){
	var url = lcf.sdop.httpUrlPrefix + "/GetForQuestMap/getQuestData";
	var payload = lcf.sdop.createGetParams();
	payload.isEventMap = false;
	payload.nodeId = 0;
	lcf.sdop.get(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "GetForQuestMap")) {
			return;
		}
		var playerExist = data.args.playerExist;
		if (callback) {
			setTimeout(callback, 100, playerExist);
		}
	});
};

/**
 * 执行当前地图的探索
 * @param {int} nodeId 地图node id
 * @param {Object} callback
 */
lcf.sdop.map.executeQuest = function(nodeId, callback){
	var url = lcf.sdop.httpUrlPrefix + "/PostForQuestMap/executeQuest";
	var payload = lcf.sdop.createBasePayload("executeQuest", {
		"isEventMap": false,
		"nodeId": nodeId,
		"renderingIdList": []
	});
	lcf.sdop.post(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "executeQuest")) {
			return;
		}
		lcf.sdop.checkCallback(callback);
	});
};

lcf.sdop.sneaking = {};

lcf.sdop.sneaking.getSneakingMissionTopData = function(callback){
	var url = lcf.sdop.httpUrlPrefix + "/GetForSneakingMission/getSneakingMissionTopData";
	var payload = lcf.sdop.createGetParams();
	lcf.sdop.get(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "getSneakingMissionTopData")) {
			return;
		}
	});
};

lcf.sdop.sneaking.getResultData = function(callback){
	var url = lcf.sdop.httpUrlPrefix + "/PostForSneakingMission/getResultData";
	var payload = {
		"tokenId": "972ae783c0d006c85e1a7b6456772579",
		"procedure": "getResultData",
		"ssid": "75bvjdup4usmdl3bj0q02k63o7het70k",
		"args": {
			"platoonId": 3
		}
	};
	lcf.sdop.post(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "PostForSneakingMission")) {
			return;
		}
	});
};

lcf.sdop.sneaking.getHighRiskPossibility = function(callback){
	var url = lcf.sdop.httpUrlPrefix + "/PostForSneakingMission/getHighRiskPossibility";
	var payload = {
		"tokenId": "972ae783c0d006c85e1a7b6456772579",
		"procedure": "getHighRiskPossibility",
		"ssid": "75bvjdup4usmdl3bj0q02k63o7het70k",
		"args": {
			"itemIdList": [20006, 20013, 20016],
			"platoonBattleForce": 80909,
			"destinationId": 302
		}
	};
	lcf.sdop.post(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "getHighRiskPossibility")) {
			return;
		}
	});
};

lcf.sdop.sneaking.sortieTroops = function(callback){
	var url = lcf.sdop.httpUrlPrefix + "/PostForSneakingMission/sortieTroops";
	var payload = {
		"tokenId": "63c5a73d0970ec5d440397a75afaebf8",
		"args": {
			"platoonId": 1,
			"itemIdList": [20006, 20013, 20016],
			"msCardIdList": [205786367, 226142975, 218468769],
			"destinationId": 310
		},
		"procedure": "sortieTroops"
	};
	lcf.sdop.post(url, payload, function(data){
		console.info(data);
		if (lcf.sdop.checkError(data, "sortieTroops")) {
			return;
		}
	});
};

lcf.sdop.ui = {
	initAfterPanel: [],
	btnInit: null,
	btnStartDuel: null,
	btnStartSuperRaidBoss: null,
	btnStartNormalRaidBoss: null,
	btnStartAutoReload: null,
	addInAfterPanel: function(jqueryObj){
		var _ui = lcf.sdop.ui;
		for (var i in _ui.initAfterPanel) {
			if (jqueryObj === _ui.initAfterPanel[i]) {
				return;
			}
		}
		jqueryObj.hide();
		_ui.initAfterPanel[_ui.initAfterPanel.length] = jqueryObj;
	},
	init: function(callback){
		var _ui = lcf.sdop.ui;
		var _sdop = lcf.sdop;
		var footer = $("#footer");
		footer.text('');
		
		var pInit = $("<p>");
		var pPanel = $("<p>");
		_ui.addInAfterPanel(pPanel);
		
		var btnInit = $('<input type="button">').val("初始化脚本, 初始化前, 请先点击【プロフィール】!");
		_ui.btnInit = btnInit;
		
		pInit.append(btnInit);
		
		///////////////////////////////////////////////////////////
		
		var btnEquipItem4Sp = $('<input type="button">').val("自动携带双SP药!");
		//btnEquipItem4Sp.hide();
		_ui.addInAfterPanel(btnEquipItem4Sp);
		btnEquipItem4Sp.click(function(){
			btnEquipItem4Sp.attr('disabled', 'disabled');
			_sdop.equipItem4Sp(function(){
				btnEquipItem4Sp.removeAttr('disabled');
			});
		});
		
		pInit.append(btnEquipItem4Sp);
		
		///////////////////////////////////////////////////////////
		
		var logPanel = $("<p>");
		logPanel.attr("id", "lucifer_sdop_log_panel");
		//logPanel.hide();
		_ui.addInAfterPanel(logPanel);
		_sdop.logPanel = logPanel;
		
		var btnClearLog = $('<input type="button">').val("清除日志!");
		//btnClearLog.hide();
		_ui.addInAfterPanel(btnClearLog);
		pInit.append(btnClearLog);
		btnClearLog.click(_sdop.clearLog);
		
		var contents = $("#contents");
		contents.append(logPanel);
		
		btnInit.click(function(){
			_sdop.init(function(){
				btnInit.val("初始化成功！");
				btnInit.unbind();
				for (var i in _ui.initAfterPanel) {
					_ui.initAfterPanel[i].show();
				}
				//pPanel.show();
				//logPanel.show();
				//btnClearLog.show();
				$("#headerlogo").hide();
			});
		});
		
		footer.append(pInit).append(pPanel);
		_ui.initFunPanel(pPanel);
		
		lcf.sdop.checkCallback(callback);
	},
	initFunPanel: function(pPanel){
		var _ui = lcf.sdop.ui;
		var _sdop = lcf.sdop;
		pPanel.hide();
		
		var pAutoDuel = $("<p>").text("自动GB");
		var sltDuelTargetUnitAttribute = $("<select>");
		for (var i in _sdop.ms.unitAttribute) {
			sltDuelTargetUnitAttribute.append('<option value="' + i + '">' + _sdop.ms.unitAttribute[i] + '</option>');
		}
		pAutoDuel.append(sltDuelTargetUnitAttribute);
		
		var btnStartDuel = $('<input type="button">').val("开始");
		pAutoDuel.append(btnStartDuel);
		_ui.btnStartDuel = btnStartDuel;
		
		var btnCancelDuel = $('<input type="button">').val("停止");
		btnCancelDuel.hide();
		pAutoDuel.append(btnCancelDuel);
		
		btnStartDuel.click(function(){
			_sdop.startAutoDuel(sltDuelTargetUnitAttribute.val());
			btnStartDuel.hide();
			btnCancelDuel.show();
		});
		
		btnCancelDuel.click(function(){
			_sdop.cancelAutoDuel();
			btnStartDuel.show();
			btnCancelDuel.hide();
		});
		
		pPanel.append(pAutoDuel);
		
		///////////////////////////////////////////////////////////
		
		
		var btnStartSuperRaidBoss = $('<input type="button">').val("开始自动超总");
		var btnStartNormalRaidBoss = $('<input type="button">').val("开始自动总力");
		var btnCancelSuperRaidBoss = $('<input type="button">').val("停止自动超总/总力");
		btnCancelSuperRaidBoss.hide();
		
		btnStartSuperRaidBoss.click(function(){
			lcf.sdop.boss.AI.startAutoSuperRaidBoss();
			btnStartSuperRaidBoss.hide();
			btnStartNormalRaidBoss.hide();
			btnCancelSuperRaidBoss.show();
		});
		
		btnCancelSuperRaidBoss.click(function(){
			lcf.sdop.boss.AI.cancelAutoSuperRaidBoss();
			btnStartSuperRaidBoss.show();
			btnStartNormalRaidBoss.show();
			btnCancelSuperRaidBoss.hide();
		});
		
		pAutoDuel.append(btnStartSuperRaidBoss);
		pAutoDuel.append(btnCancelSuperRaidBoss);
		pAutoDuel.append(btnStartNormalRaidBoss);
		
		_ui.btnStartSuperRaidBoss = btnStartSuperRaidBoss;
		
		btnStartNormalRaidBoss.click(function(){
			lcf.sdop.boss.AI.startAutoNormalRaidBoss();
			btnStartSuperRaidBoss.hide();
			btnStartNormalRaidBoss.hide();
			btnCancelSuperRaidBoss.show();
		});
		
		_ui.btnStartNormalRaidBoss = btnStartNormalRaidBoss;
		
		///////////////////////////////////////////////////////////
		
		var btnStartAutoReload = $('<input type="button">').val("开启挂机模式");
		var btnCancelAutoReload = $('<input type="button">').val("取消挂机模式");
		btnCancelAutoReload.hide();
		
		btnStartAutoReload.click(function(){
			lcf.sdop.reloadMode = 1;
			btnStartAutoReload.hide();
			btnCancelAutoReload.show();
		});
		
		btnCancelAutoReload.click(function(){
			lcf.sdop.reloadMode = 0;
			btnStartAutoReload.show();
			btnCancelAutoReload.hide();
		});
		
		pAutoDuel.append(btnStartAutoReload);
		pAutoDuel.append(btnCancelAutoReload);
		
		_ui.btnStartAutoReload = btnStartAutoReload;
		
		///////////////////////////////////////////////////////////
		
		var pCard = $("<p>").text("自动抽卡片次数：");
		var txtGetCardCount = $("<input>");
		pCard.append(txtGetCardCount);
		
		var sltCardType = $("<select>").append('<option value="0">ms</option>').append('<option value="1">pilot</option>');
		pCard.append(sltCardType);
		
		var sltGachaType = $("<select>").append('<option value="0">普通</option>').append('<option value="1">pilot白金</option>');
		pCard.append(sltGachaType);
		
		var btnAutoGetCard = $('<input type="button">').val("开始");
		pCard.append(btnAutoGetCard);
		
		btnAutoGetCard.click(function(){
			clearTimeout(_sdop.auto.ids.playGachaId);
			var count = parseInt(txtGetCardCount.val());
			_sdop.card.count = count;
			_sdop.auto.ids.playGachaId = setTimeout(_sdop.card.autoPlayGachaResult, 100, sltCardType.val(), sltGachaType.val());
		});
		
		var btnAutoGetRaidBossCard = $('<input type="button">').val("一键总力战抽奖！");
		btnAutoGetRaidBossCard.click(function(){
			clearTimeout(lcf.sdop.auto.ids.raidBossResultId);
			lcf.sdop.auto.ids.raidBossResultId = setTimeout(lcf.sdop.boss.initRaidBossOutlineList, 100, function(){
				lcf.sdop.auto.ids.raidBossResultId = setTimeout(lcf.sdop.boss.autoRaidBossResult, 100);
			});
		});
		
		pCard.append(btnAutoGetRaidBossCard);
		
		pPanel.append(pCard);
		
		///////////////////////////////////////////////////////////
		
		var pAutoSayHello = $("<p>").text("自动say hello! 内容：");
		var txtHelloContent = $("<input>");
		var sltValidate = $("<select>").append('<option value="false">不校验</option>').append('<option value="true">校验</option>');
		var btnAutoSayHello = $('<input type="button">').val("开始");
		
		pAutoSayHello.append(txtHelloContent).append(sltValidate).append(btnAutoSayHello);
		
		btnAutoSayHello.click(function(){
			var validate = eval(sltValidate.val());
			var helloContent = txtHelloContent.val();
			if (helloContent.length > 1) {
				lcf.sdop.autoSayHelloContent = helloContent;
			}
			_sdop.autoSayHello(validate);
			btnAutoSayHello.hide();
		});
		
		pPanel.append(pAutoSayHello);
		
	}
};

chrome.runtime.onMessage.addListener(function(request, sender, sendResponse){
	//console.log(sender.tab ? "from a content script:" + sender.tab.url : "from the extension");
	console.log("get tokenId is " + request.tokenId);
	lcf.sdop.log("获取 tokenId 成功！ ");
	lcf.sdop.tokenId = request.tokenId;
	sendResponse({
		farewell: "goodbye"
	});
});



$(function(){
	lcf.sdop.ui.init(function(){
		lcf.sdop.checkAuto();
	});
});

window.$lcf = lcf;
