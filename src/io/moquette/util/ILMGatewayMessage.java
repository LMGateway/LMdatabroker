/**
 * 
 */
package io.moquette.util;

/**   
 * @ClassName:  ILMGatewayMessage   
 * @Description:网关消息接口   
 * @author: huchengye 
 * @date:   2018年12月6日 上午9:36:02   
 *     
 * @Copyright: 2018 LM All rights reserved. 
 * 注意：本内容仅限于黄山罗米测控技术有限公司内部传阅，禁止外泄以及用于其他的商业目的 
 */
public interface ILMGatewayMessage {
	void getRealTimeMessage(String realTimeMessage,String topic);
	void processConnect(String clientId,String ipAddress);
	void processDisconnect(String clientId,String ipAddress);
}
