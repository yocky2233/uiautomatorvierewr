/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.uiautomator.tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiNode extends BasicTreeNode {
	private static final Pattern BOUNDS_PATTERN = Pattern
			.compile("\\[-?(\\d+),-?(\\d+)\\]\\[-?(\\d+),-?(\\d+)\\]");
	// use LinkedHashMap to preserve the order of the attributes
	private final Map<String, String> mAttributes = new LinkedHashMap<String, String>();
	private String mDisplayName = "ShouldNotSeeMe";
	private Object[] mCachedAttributesArray;

	public void addAtrribute(String key, String value) {
		mAttributes.put(key, value);
		updateDisplayName();
		if ("bounds".equals(key)) {
			updateBounds(value);
		}
	}

	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(mAttributes);
	}

	/**
	 * Builds the display name based on attributes of the node
	 */
	private void updateDisplayName() {
		String className = mAttributes.get("class");
		if (className == null)
			return;
		String text = mAttributes.get("text");
		if (text == null)
			return;
		String contentDescription = mAttributes.get("content-desc");
		if (contentDescription == null)
			return;
		String index = mAttributes.get("index");
		if (index == null)
			return;
		String bounds = mAttributes.get("bounds");
		if (bounds == null) {
			return;
		}
		// shorten the standard class names, otherwise it takes up too much
		// space on UI
		className = className.replace("android.widget.", "");
		className = className.replace("android.view.", "");
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(index);
		builder.append(") ");
		builder.append(className);
		if (!text.isEmpty()) {
			builder.append(':');
			builder.append(text);
		}
		if (!contentDescription.isEmpty()) {
			builder.append(" {");
			builder.append(contentDescription);
			builder.append('}');
		}
		builder.append(' ');
		builder.append(bounds);
		mDisplayName = builder.toString();
	}

	private void updateBounds(String bounds) {
		Matcher m = BOUNDS_PATTERN.matcher(bounds);
		if (m.matches()) {
			x = Integer.parseInt(m.group(1));
			y = Integer.parseInt(m.group(2));
			width = Integer.parseInt(m.group(3)) - x;
			height = Integer.parseInt(m.group(4)) - y;
			mHasBounds = true;
		} else {
			throw new RuntimeException("Invalid bounds: " + bounds);
		}
	}

	@Override
	public String toString() {
		return mDisplayName;
	}

	public String getAttribute(String key) {
		return mAttributes.get(key);
	}

	@Override
	public Object[] getAttributesArray() {
		// this approach means we do not handle the situation where an attribute
		// is added
		// after this function is first called. This is currently not a concern
		// because the
		// tree is supposed to be readonly
		if (mCachedAttributesArray == null) {
			mCachedAttributesArray = new Object[mAttributes.size()];
			int i = 0;
			for (String attr : mAttributes.keySet()) {
				mCachedAttributesArray[i++] = new AttributePair(attr,
						mAttributes.get(attr));
			}
		}
		return mCachedAttributesArray;
	}

	public String getXpath() {
		String className = getNodeClassAttribute();
		String xpath = "/" + className;
		boolean flag = false;
		String text = getAttribute("text");
		if (text != null && !text.equals("")) {
			text = text.replaceAll("\"", "\\\\\"");
			xpath += "[@text=\\\"" + text + "\\\"";
			flag = true;
		}
		String content_desc = getAttribute("content-desc");
		if(!content_desc.equals("")){
			content_desc = content_desc.replaceAll("'", "\\\\'");
			if(flag){
				xpath += " and @content-desc=\\\"" + content_desc + "\\\"";
			}else{
				xpath += "[@content-desc=\\\"" + content_desc + "\\\"";
				flag = true;
			}
		}
		if(flag){
			xpath = xpath + "]";
		}
		return xpath;
	}
	
	public String getXpath2() {
		String className = getNodeClassAttribute();
		String xpath = "/" + className;
		boolean flag = false;
		String text = getAttribute("text");
		if (text != null && !text.equals("")) {
			text = text.replaceAll("\"", "\\\\\"");
			xpath += "[@text=\\\"" + text + "\\\"";
			flag = true;
		}
		String content_desc = getAttribute("content-desc");
		if(!content_desc.equals("")){
			content_desc = content_desc.replaceAll("'", "\\\\'");
			if(flag){
				xpath += " and @content-desc=\\\"" + content_desc + "\\\"";
			}else{
				xpath += "[@content-desc=\\\"" + content_desc + "\\\"";
				flag = true;
			}
		}
		String index = getAttribute("index");
		if(!index.equals("")){
			if(flag){
				xpath += " and @index=\\\"" + index + "\\\"";
			}else{
				xpath += "[@index=\\\"" + index + "\\\"";
				flag = true;
			}
		}
		if(flag){
			xpath = xpath + "]";
		}
		return xpath;
	}
	
	private String getNodeClassAttribute() {
		return this.mAttributes.get("class");
	}
	
	public String getBoundsCenter()
	{
		UiNode u = new UiNode();
		String bounds = (String)this.mAttributes.get("bounds");
		String[] b = bounds.split("]");
		String[] c = b[0].split(",");
		String[] d = b[1].split(",");
		int x = Integer.parseInt(c[0].substring(1));
		int y = Integer.parseInt(c[1]);
		int width = Integer.parseInt(d[0].substring(1));
		int height = Integer.parseInt(d[1]);
		int X = x+(width-x)/2;
		int Y = y+(height-y)/2;
		String Resolution = u.getPhoneResolution();
		String[] XY = Resolution.split("x");
		int aa = Integer.parseInt(XY[0].trim());
		int bb = Integer.parseInt(XY[1].trim());
//		float xPCT = (float)X/aa;
//		float yPCT = (float)Y/bb;
		String xPCT = new DecimalFormat("0.00").format((float)X/aa);
		String yPCT = new DecimalFormat("0.00").format((float)Y/bb);
		String zx = "("+X+","+Y+")"+" "+"("+xPCT+","+yPCT+")";
		return zx;
	 
		  }

	public String getPhoneResolution() {
		String str3 = null;
		try {
			Process proc = Runtime.getRuntime().exec("adb shell dumpsys display | grep PhysicalDisplayInfo");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			StringBuffer stringBuffer = new StringBuffer();
			String line = null;
			while ((line = in.readLine()) != null) {
				stringBuffer.append(line + " ");
			}

			String[] str1 = stringBuffer.toString().split("PhysicalDisplayInfo");
			String[] str2 = str1[1].split(",");
			str3 = str2[0].substring(1);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return str3;
		}
}
