chrome.webRequest.onCompleted.addListener(function(info){
	var arr, reg = new RegExp("(^|)tokenId=([^&]*)(&|$)");
	var tokenId;
	if (arr = info.url.match(reg)) {
		tokenId = arr[2];
	}
	//console.info(info.url);
	//console.info(chrome.runtime.id);
	
	chrome.tabs.query({
		active: true,
		currentWindow: true
	}, function(tabs){
		console.info(tabs);
		chrome.tabs.sendMessage(tabs[0].id, {
			tokenId: tokenId
		}, function(response){
			//console.log(response.farewell);
		});
	});
	
	return;
}, // filters
{
	urls: ["http://sdop-g.bandainamco-ol.jp/GetForProfile/getOwnProfileDetail*"],
	types: ["object"]
});

