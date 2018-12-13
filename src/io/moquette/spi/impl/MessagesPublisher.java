/*
 * Copyright (c) 2012-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.moquette.spi.impl;

import io.moquette.server.ConnectionDescriptorStore;
import io.moquette.spi.ClientSession;
import io.moquette.spi.IMessagesStore;
import io.moquette.spi.ISessionsStore;
import io.moquette.spi.impl.subscriptions.Subscription;
import io.moquette.spi.impl.subscriptions.SubscriptionsDirectory;
import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.util.DataPoint;
import io.moquette.util.Equipment;
import io.moquette.util.ILMGatewayMessage;
import io.moquette.util.RealTimeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import static io.moquette.spi.impl.ProtocolProcessor.lowerQosToTheSubscriptionDesired;

class MessagesPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(MessagesPublisher.class);
    private final ConnectionDescriptorStore connectionDescriptors;
    private final ISessionsStore m_sessionsStore;
    private final PersistentQueueMessageSender messageSender;
    private final SubscriptionsDirectory subscriptions;

    public MessagesPublisher(ConnectionDescriptorStore connectionDescriptors, ISessionsStore sessionsStore,
                             PersistentQueueMessageSender messageSender, SubscriptionsDirectory subscriptions) {
        this.connectionDescriptors = connectionDescriptors;
        this.m_sessionsStore = sessionsStore;
        this.messageSender = messageSender;
        this.subscriptions = subscriptions;
    }

    static MqttPublishMessage notRetainedPublish(String topic, MqttQoS qos, ByteBuf message) {
        return notRetainedPublishWithMessageId(topic, qos, message, 0);
    }

    private static MqttPublishMessage notRetainedPublishWithMessageId(String topic, MqttQoS qos, ByteBuf message,
            int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
        MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, messageId);
        return new MqttPublishMessage(fixedHeader, varHeader, message);
    }

    void publish2Subscribers(IMessagesStore.StoredMessage pubMsg, Topic topic, int messageID) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending publish message to subscribers. ClientId={}, topic={}, messageId={}, payload={}, " +
                    "subscriptionTree={}", pubMsg.getClientID(), topic, messageID, DebugUtils.payload2Str(pubMsg.getPayload()),
                subscriptions.dumpTree());
        } else if(LOG.isInfoEnabled()){
            LOG.info("Sending publish message to subscribers. ClientId={}, topic={}, messageId={}", pubMsg.getClientID(), topic,
                messageID);
        }
        publish2Subscribers(pubMsg, topic);
    }
    
    //chengye
    void publish2Subscribers(IMessagesStore.StoredMessage pubMsg, Topic topic) {
        List<Subscription> topicMatchingSubscriptions = subscriptions.matches(topic);
        final String topic1 = pubMsg.getTopic();
        final MqttQoS publishingQos = pubMsg.getQos();
        final ByteBuf origPayload = pubMsg.getPayload();
        for (final Subscription sub : topicMatchingSubscriptions) {
            MqttQoS qos = lowerQosToTheSubscriptionDesired(sub, publishingQos);
            ClientSession targetSession = m_sessionsStore.sessionForClient(sub.getClientId());
            boolean targetIsActive = this.connectionDescriptors.isConnected(sub.getClientId());
//TODO move all this logic into messageSender, which puts into the flightZone only the messages that pull out of the queue.
            if (targetIsActive) {
                if(LOG.isDebugEnabled()){
                    LOG.debug("Sending PUBLISH message to active subscriber. CId={}, topicFilter={}, qos={}",
                        sub.getClientId(), sub.getTopicFilter(), qos);
                }
                // we need to retain because duplicate only copy r/w indexes and don't retain() causing
                // refCnt = 0
                ByteBuf payload = origPayload.retainedDuplicate();
                MqttPublishMessage publishMsg;
                if (qos != MqttQoS.AT_MOST_ONCE) {
                    // QoS 1 or 2
                    int messageId = targetSession.inFlightAckWaiting(pubMsg);
                    // set the PacketIdentifier only for QoS > 0
                    publishMsg = notRetainedPublishWithMessageId(topic1, qos, payload, messageId);
                } else {
                    publishMsg = notRetainedPublish(topic1, qos, payload);
                }
                this.messageSender.sendPublish(targetSession, publishMsg);
            } else {
                if (!targetSession.isCleanSession()) {
                    if(LOG.isDebugEnabled()){
                        LOG.debug("Storing pending PUBLISH inactive message. CId={}, topicFilter={}, qos={}",
                            sub.getClientId(), sub.getTopicFilter(), qos);
                    }
                    // store the message in targetSession queue to deliver
                    targetSession.enqueue(pubMsg);
                }
            }
        }
        String str = convertByteBufToString(origPayload);
        /*Object obj = JSONObject.parse(str);
		JSONObject jsonObject = (JSONObject) obj;
		RealTimeMessage message = new RealTimeMessage();
		message.setClientId(jsonObject.getString("clientid"));
		message.setPhotoTime(jsonObject.getTimestamp("time"));
		List<Equipment> equipments = new ArrayList<Equipment>();
		Set<String> set = jsonObject.keySet();
		for (String string : set) {
			if(!string.equals("clientid")&&!string.equals("time")) {
				Equipment equipment = new Equipment();
				equipment.setEquipmentCode(string);
				List<DataPoint> dataPoints = new ArrayList<DataPoint>();
				JSONArray arr = jsonObject.getJSONArray(string);
				for (int j = 0; j < arr.size(); j++) {
					// 循环对象，并通过getString("属性名");来获得值
					JSONObject tempJson = (JSONObject) arr.get(j);
					DataPoint dataPoint = new DataPoint();
					dataPoint.setTagCode(tempJson.getString("tagCode"));
					dataPoint.setTagDescription(tempJson.getString("description"));
					dataPoint.setTagStatus(tempJson.getString("status"));
					dataPoint.setGatherTime(tempJson.getTimestamp("timestamp"));
					dataPoint.setGatherValue(tempJson.getString("val"));
					dataPoint.setObjectType(tempJson.getString("objectType"));
					dataPoints.add(dataPoint);
				}
				equipment.setDataPoints(dataPoints);
				equipments.add(equipment);
			}
		}
		message.setEquipments(equipments);*/
        ServiceLoader<ILMGatewayMessage> serviceLoaders = ServiceLoader.load(ILMGatewayMessage.class);
        for (ILMGatewayMessage i : serviceLoaders) {
        	i.getRealTimeMessage(str, topic1);
		}
    }
    //chengye
    public String convertByteBufToString(ByteBuf buf){
        String str;
        String str2 = null;
        try {
			if(buf.hasArray()) { // 处理堆缓冲区
			    str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
			    str2 = new String(str.getBytes("UTF-8"), "UTF-8");
			} else { // 处理直接缓冲区以及复合缓冲区
			    byte[] bytes = new byte[buf.readableBytes()];
			    buf.getBytes(buf.readerIndex(), bytes);
			    str = new String(bytes, 0, buf.readableBytes());
			    str2 = new String(str.getBytes("UTF-8"), "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return str2;
    }
}
