/**
 * 
 */
package io.moquette.util;

import java.sql.Timestamp;
import java.util.List;

/**   
 * @ClassName:  RealTimeMessage   
 * @Description:实时消息实体  
 * @author: huchengye 
 * @date:   2018年12月6日 上午9:18:06   
 *     
 * @Copyright: 2018 LM All rights reserved. 
 * 注意：本内容仅限于黄山罗米测控技术有限公司内部传阅，禁止外泄以及用于其他的商业目的 
 */
public class RealTimeMessage {
	private String clientId;
	private Timestamp photoTime;
	private List<Equipment> equipments;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public Timestamp getPhotoTime() {
		return photoTime;
	}
	public void setPhotoTime(Timestamp photoTime) {
		this.photoTime = photoTime;
	}
	public List<Equipment> getEquipments() {
		return equipments;
	}
	public void setEquipments(List<Equipment> equipments) {
		this.equipments = equipments;
	}
	@Override
	public String toString() {
		return "RealTimeMessage [clientId=" + clientId + ", photoTime=" + photoTime + ", equipments=" + equipments
				+ "]";
	}
}
