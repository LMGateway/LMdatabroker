/**
 * 
 */
package io.moquette.util;

import java.sql.Timestamp;

/**   
 * @ClassName:  DataPoint   
 * @Description:设备数据点实体   
 * @author: huchengye 
 * @date:   2018年12月6日 上午8:58:51   
 *     
 * @Copyright: 2018 LM All rights reserved. 
 * 注意：本内容仅限于黄山罗米测控技术有限公司内部传阅，禁止外泄以及用于其他的商业目的 
 */
public class DataPoint {
	private String tagCode;
	private String tagStatus;
	private String tagDescription;
	private Timestamp gatherTime;
	private String gatherValue;
	private String objectType;
	public String getTagCode() {
		return tagCode;
	}
	public void setTagCode(String tagCode) {
		this.tagCode = tagCode;
	}
	public String getTagStatus() {
		return tagStatus;
	}
	public void setTagStatus(String tagStatus) {
		this.tagStatus = tagStatus;
	}
	public String getTagDescription() {
		return tagDescription;
	}
	public void setTagDescription(String tagDescription) {
		this.tagDescription = tagDescription;
	}
	public Timestamp getGatherTime() {
		return gatherTime;
	}
	public void setGatherTime(Timestamp gatherTime) {
		this.gatherTime = gatherTime;
	}
	public String getGatherValue() {
		return gatherValue;
	}
	public void setGatherValue(String gatherValue) {
		this.gatherValue = gatherValue;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	@Override
	public String toString() {
		return "DataPoint [tagCode=" + tagCode + ", tagStatus=" + tagStatus + ", tagDescription=" + tagDescription
				+ ", gatherTime=" + gatherTime + ", gatherValue=" + gatherValue + ", objectType=" + objectType + "]";
	}
}
