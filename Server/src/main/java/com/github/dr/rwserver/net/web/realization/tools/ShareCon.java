package com.github.dr.rwserver.net.web.realization.tools;

import java.util.HashMap;
import java.util.Map;

import com.github.dr.rwserver.net.web.realization.agreement.ShareMessage;

public class ShareCon {
	public ShareMessage getShareMessage(String uri, String bodyMessage) {
		Map<Object, Object> map = new HashMap<>();
		String params;// get 参数
		if (uri.indexOf("?") > 0) {// 存在GET参数
			params = uri.split("\\?")[1];
			uri = uri.split("\\?")[0];
			String[] names = params.split("&");
			for (String name : names) {
				String[] nameAndValue = name.split("=");
				map.put(nameAndValue[0], nameAndValue[1]);
			}
		}
		// 将每一个/后面的第一个字母大写
		// String[] urs = uri.split("/");
		// System.out.println("end uri==" + uri);
		ShareMessage share = new ShareMessage();
		share.setBody(bodyMessage);
		share.setUri(uri);
		share.setParams(map);
		return share;
	}
}
