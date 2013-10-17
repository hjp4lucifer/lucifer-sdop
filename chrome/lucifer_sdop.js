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
	post: function(url, payload, callback){
		var _sdop = lcf.sdop;
		$.ajax({
			url: url,
			type: "POST",
			contentType: 'application/json; charset=UTF-8',
			data: JSON.stringify(payload),
			async: true,
			processData: false,
			success: callback
		})
	},
	checkError: function(data, msg){
		if (data.args.message) {
			console.warn(data);
			lcf.sdop.log(msg + "：" + data.args.message);
			return true;
		}
		return false;
	}
};

lcf.sdop.getOwnTeamData = function(){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForTeam/getOwnTeamData";
	if (_sdop.members.length > 0) {
		return;
	}
	$.ajax({
		url: url,
		type: "GET",
		async: false,
		data: _sdop.createGetParams(),
		success: function(data){
			//console.log(data);
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
		}
	});
};

lcf.sdop.getGreetingList = function(){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForProfile/getGreetingList";
	if (_sdop.greetingUsers.length > 0) {
		return;
	}
	$.ajax({
		url: url,
		type: "GET",
		async: false,
		data: _sdop.createGetParams(),
		success: function(data){
			//console.log(data);
			if (_sdop.checkError(data, "getGreetingList")) {
				return;
			}
			var greetingList = data.args.greetingList;
			
			for (var i in greetingList) {
				var g = greetingList[i];
				_sdop.greetingUsers[g.userId] = g.name;
			}
		}
	});
};

lcf.sdop.getGreetingCondition = function(userId){
	var _sdop = lcf.sdop;
	var url = _sdop.httpUrlPrefix + "/GetForProfile/getGreetingCondition";
	var params = _sdop.createGetParams();
	params.userId = userId;
	
	var isHello = true;
	$.ajax({
		url: url,
		type: "GET",
		async: false,
		data: params,
		success: function(data){
			//console.log(data);
			if (_sdop.checkError(data, "getGreetingCondition")) {
				return;
			}
			isHello = data.args.greetingCondition.greeted;
		}
	});
	return isHello;
};

lcf.sdop.postGreeting = function(greetingUserId, comment){
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
	logMsCard: function(msCard, i){
		var msLog = "<br>" + i + "） " + ms.rarity + "c" + ms.cost + "【" + ms.type.name + "】，level：" + ms.level + "，属性：" + ms.attribute.value + "，attack：" + ms.attack + "，max HP：" + ms.maxHp + "，speed：" + ms.speed;
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
		duel: false
	},
	ids: {
		duelId: null,
		playGachaId: null,
		raidBossResultId: null
	}
};

lcf.sdop.card = {
	cardType: ["MS_CARD", "PILOT_CARD"],
	gachaType: ["NORMAL", "PLATINA"],
	currentCardType: "PILOT_CARD",
	currentGachaType: "NORMAL",
	count: 0
};

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
	getEntryData: function(callback){
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
	},
	getHistoryData: function(){
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
	},
	getDuelData: function(callback){
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
				if (callback) {
					setTimeout(callback, 100);
				}
			},
			error: function(){
				_sdop.bp = 0;
				_sdop.ep = 0;
			}
		});
	},
	executeDuelBattle: function(targetId){
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
	},
	checkAndExecute: function(){
		var _sdop = lcf.sdop;
		var _duel = _sdop.duel;
		_duel.getDuelData(_duel.checkAndExecute2);
	},
	checkAndExecute2: function(){
		var _sdop = lcf.sdop;
		var _duel = _sdop.duel;
		if (_sdop.bp < 5) {
			_sdop.log("当前bp为" + _sdop.bp + "，少于5, 等待下次检查！");
			return;
		}
		_duel.getEntryData(_duel.checkAndExecute3);
	},
	checkAndExecute3: function(entryList){
		var _sdop = lcf.sdop;
		var _duel = _sdop.duel;
		var targetId;
		for (var i in entryList) {
			var entry = entryList[i];
			if (entry.unitAttribute.value == _duel.targetUnitAttribute) {
				targetId = entry.playerId;
				_sdop.log("准备对【" + entry.playerName + "】发起挑战，对方属性是【" + _duel.targetUnitAttribute + "】" + entry.unitName + "！");
				break;
			}
		}
		entryList = null;
		_duel.executeDuelBattle(targetId);
	}
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
	_sdop.auto.setting.duel = true;
	_sdop.autoDuel();
	if (unitAttributeIndex != null) {
		var unitAttribute = _sdop.ms.unitAttribute[unitAttributeIndex];
		if (unitAttribute) {
			_sdop.duel.targetUnitAttribute = unitAttribute;
		}
	}
	_sdop.log("针对【" + _sdop.duel.targetUnitAttribute + "】的自动GB开始！");
	_sdop.duel.checkAndExecute();
};

lcf.sdop.cancelAutoDuel = function(){
	var _sdop = lcf.sdop;
	_sdop.auto.setting.duel = false;
	clearTimeout(_sdop.auto.ids.duelId);
	_sdop.log("自动GB停止成功！");
};

lcf.sdop.autoSayHello = function(validate, comment){
	var _sdop = lcf.sdop;
	if (!_sdop.tokenId) {
		alert("请输入tokenId!");
		return;
	}
	if (!comment) {
		comment = "hello";
	}
	
	_sdop.getOwnTeamData();
	_sdop.getGreetingList();
	var users = lcf.mergeObject(_sdop.members, _sdop.greetingUsers);
	var userIndex = 0;
	for (var uid in users) {
		if (validate) {
			if (_sdop.getGreetingCondition(uid)) {
				continue;
			}
		}
		setTimeout(function(uid){
			//console.log(uid + " : " + users[uid]);
			//_sdop.postGreeting(uid, "hello " + users[uid]);
			_sdop.log("对【" + users[uid] + "】说：" + comment);
			_sdop.postGreeting(uid, comment);
		}, ++userIndex * 1234, uid);
	}
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
		length: 10,
		key: {
			value: "ALL"
		},
		kind: {
			value: "SUPER"
		},
		start: 0
	},
	getCurrentType: function(){
		return lcf.sdop.boss._super;
	},
	mode: ["NORMAL", "RAID_BOSS"],
	kind: ["NORMAL", "SUPER"],
	currentMode: 'RAID_BOSS',
	currentKind: 'SUPER',
	x3: 250033,
	x6: 250034,
	getTopLevel: function(list){
		var target;
		for (var i in list) {
			if (target) {
				if (target.level < list[i].level) {//判断等级
					target = list[i];
				} else if (target.currentHp < list[i].currentHp) {//判断剩余血量
					target = list[i];
				}
			} else {
				target = list[i];
			}
		}
		return target;
	}
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
		if (_sdop.checkError(data, "initRaidBossOutlineList")) {
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
		_sdop.log("总力战剩余" + finish + "结果待处理！");
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
	$.get(url, params, function(data){
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
		var logMsg = "获得：" + card.rarity + "c" + card.cost + "【" + card.name + "】，";
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
			if (noListCallback) {
				setTimeout(noListCallback, 100);
			}
			return;
		}
		
		var target = lcf.sdop.boss.getTopLevel(list);
		_sdop.log(lcf.sdop.boss.currentKind + "目标boss等级：" + target.level + "，残余血量：" + target.currentHp);
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

lcf.sdop.boss.checkX6 = function(m){
	for (var j in m.characteristicList) {
		if (j.id == lcf.sdop.boss.x6) {
			return true;
		}
	}
	return false;
};

lcf.sdop.boss.checkX3 = function(m){
	for (var j in m.characteristicList) {
		if (j.id == lcf.sdop.boss.x3) {
			return true;
		}
	}
	return false;
};

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
			m.lcf_attack = 3;
		} else {
			m.lcf_attack = 1;
		}
		if (attackMember == null) {//默认没有选择时
			attackMember = m;
			continue;
		}
		if (attackMember.lcf_attack > m.lcf_attack) {//倍数级别高
			continue;
		}
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

lcf.sdop.boss.getFixHelpMember = function(members, attackMember){
	if (attackMember == null) {
		return null;
	}
	attackMember.lcf_speed = lcf.sdop.boos.getTrueSpeed(attackMember);
	var helpMember;
	var skill;
	for (var i in members) {
		var m = members[i];
		if (m.id == lcf.sdop.myUserId) {//不能是自己
			continue;
		}
		if (m.id == attackMember.id) {//不能是倍机
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
		m.lcf_speed = lcf.sdop.boos.getTrueSpeed(m);
		
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
	$.get(url, params, function(data){
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
	$.get(url, _sdop.createGetParams(), function(data){
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
		"isAutoBattle": false,
		"mode": {
			"value": _sdop.boss.currentMode
		}
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "executeBattleStart")) {
			return;
		}
		_sdop.log("boss战开始！");
		if (callback) {
			setTimeout(callback, 100, data);
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
		"actionId": actionId
	});
	_sdop.post(url, payload, function(data){
		//console.info(data);
		if (_sdop.checkError(data, "executeActionCommand")) {
			return;
		}
		//_sdop.log("boss战开始！");
		if (callback) {
			setTimeout(callback, 100, data);
		}
	});
};

lcf.sdop.item = [];
lcf.sdop.item[20006] = {};

/**
 * 自动超总
 */
lcf.sdop.boss.autoSuperRaidBoss = function(){
	lcf.sdop.boss.getRaidBossOutlineList(function(boss){
		lcf.sdop.boss.postRaidBossBattleEntry(boss.id, function(){
			lcf.sdop.boss.getRaidBossBattleData(boss.id, function(battleData){
				var myUserId = battleData.leaderCardId;
				var my = battleData.playerMsList[0];
				var currentSp = my.currentSp;
				var maxSp = my.maxSp;
				
				var battleId = battleData.battleId;
				var members = battleData.memberCardList;
				var attackMember = lcf.sdop.boss.getFixAttackMember(members);
				var helpMember = lcf.sdop.boss.getFixHelpMember(members, attackMember);
				setTimeout(lcf.sdop.boss.executeBattleStart, 3000, battleId, myUserId, helpMember.id, attackMember.id, function(data){
					var playerMsList = data.playerMsList;
					lcf.sdop.log("对Boss选择阵容：" + lcf.sdop.ms.logMsList(playerMsList));
					for (var i in playerMsList) {
						var player = playerMsList[i];
						if (player.card.id == myUserId) {
							my = player;
						} else if (player.card.id == attackMember.id) {
							attackMember = player;
						} else if (player.card.id == helpMember.id) {
							helpMember = player;
						}
					}
					//理论上会填充满my、attackMember、helpMember
					
					var actionOrder = data.actionOrderList[data.actionOrderList.length - 1];
					if (0 == actionOrder.ownerId) {
						lcf.sdop.log("Boss战结束！");
						return;
					}
					var trun = 0;//自己和辅助对攻击机加状态
					if (my.id == actionOrder.ownerId) {
//						lcf.sdop.boss
					}
				});
			});
		}, function(){
			setTimeout(lcf.sdop.boss.autoSuperRaidBoss, 100);
		});
	}, function(){
		setTimeout(lcf.sdop.boss.autoSuperRaidBoss, 1000);
	});
};

lcf.sdop.ui = {
	initAfterPanel: [],
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
	init: function(){
		var _ui = lcf.sdop.ui;
		var _sdop = lcf.sdop;
		var footer = $("#footer");
		footer.text('');
		
		var pInit = $("<p>");
		var pPanel = $("<p>");
		_ui.addInAfterPanel(pPanel);
		
		var btnInit = $('<input type="button">').val("初始化脚本, 初始化前, 请先点击【プロフィール】!");
		
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
			});
		});
		
		footer.append(pInit).append(pPanel);
		_ui.initFunPanel(pPanel);
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
		
		var btnCancelDuel = $('<input type="button">').val("停止");
		btnCancelDuel.hide();
		pAutoDuel.append(btnCancelDuel);
		
		btnStartDuel.click(function(){
			_sdop.startAutoDuel(sltDuelTargetUnitAttribute.val());
			btnStartDuel.toggle();
			btnCancelDuel.toggle();
		});
		
		btnCancelDuel.click(function(){
			_sdop.cancelAutoDuel();
			btnStartDuel.toggle();
			btnCancelDuel.toggle();
		});
		
		pPanel.append(pAutoDuel);
		
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
			_sdop.autoSayHello(validate, txtHelloContent.text());
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
	lcf.sdop.ui.init();
});

window.$lcf = lcf;
