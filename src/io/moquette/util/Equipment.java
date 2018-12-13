/**
 * 
 */
package io.moquette.util;

import java.util.List;

/**   
 * @ClassName:  Equipment   
 * @Description:设备实体  
 * @author: huchengye 
 * @date:   2018年12月6日 上午8:59:46   
 *     
 * @Copyright: 2018 LM All rights reserved. 
 * 注意：本内容仅限于黄山罗米测控技术有限公司内部传阅，禁止外泄以及用于其他的商业目的 
 */
public class Equipment {
	private String equipmentCode;
	private List<DataPoint> dataPoints;
	public String getEquipmentCode() {
		return equipmentCode;
	}
	public void setEquipmentCode(String equipmentCode) {
		this.equipmentCode = equipmentCode;
	}
	public List<DataPoint> getDataPoints() {
		return dataPoints;
	}
	public void setDataPoints(List<DataPoint> dataPoints) {
		this.dataPoints = dataPoints;
	}
	@Override
	public String toString() {
		return "Equipment [equipmentCode=" + equipmentCode + ", dataPoints=" + dataPoints + "]";
	}
}
