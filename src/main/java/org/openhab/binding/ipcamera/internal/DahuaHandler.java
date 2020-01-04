/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.ipcamera.internal;

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * The {@link DahuaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

public class DahuaHandler extends ChannelDuplexHandler {
    IpCameraHandler ipCameraHandler;
    String nvrChannel;

    public DahuaHandler(ThingHandler handler, String nvrChannel) {
        ipCameraHandler = (IpCameraHandler) handler;
        this.nvrChannel = nvrChannel;
    }

    // This handles the incoming http replies back from the camera.
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String content = null;
        try {
            content = msg.toString();
            if (!content.isEmpty()) {
                ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
            }
            // determine if the motion detection is turned on or off.
            if (content.contains("table.MotionDetect[0].Enable=true")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("ON"));
            } else if (content.contains("table.MotionDetect[" + nvrChannel + "].Enable=false")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("OFF"));
            }
            // Handle motion alarm
            if (content.contains("Code=VideoMotion;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
            } else if (content.contains("Code=VideoMotion;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle item taken alarm
            if (content.contains("Code=TakenAwayDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_ITEM_TAKEN);
            } else if (content.contains("Code=TakenAwayDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle item left alarm
            if (content.contains("Code=LeftDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_ITEM_LEFT);
            } else if (content.contains("Code=LeftDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle CrossLineDetection alarm
            if (content.contains("Code=CrossLineDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
            } else if (content.contains("Code=CrossLineDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_LINE_CROSSING_ALARM, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // determine if the audio alarm is turned on or off.
            if (content.contains("table.AudioDetect[0].MutationDetect=true")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("ON"));
            } else if (content.contains("table.AudioDetect[0].MutationDetect=false")) {
                ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("OFF"));
            }
            // Handle AudioMutation alarm
            if (content.contains("Code=AudioMutation;action=Start;index=0")) {
                ipCameraHandler.audioDetected();
            } else if (content.contains("Code=AudioMutation;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_AUDIO_ALARM, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstAudioAlarm = false;
                ipCameraHandler.audioAlarmUpdateSnapshot = false;
            }
            // Handle AudioMutationThreshold alarm
            if (content.contains("table.AudioDetect[0].MutationThreold=")) {
                String value = ipCameraHandler.returnValueFromString(content, "table.AudioDetect[0].MutationThreold=");
                ipCameraHandler.setChannelState(CHANNEL_THRESHOLD_AUDIO_ALARM, PercentType.valueOf(value));
            }
            // Handle FaceDetection alarm
            if (content.contains("Code=FaceDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_FACE_DETECTED);
            } else if (content.contains("Code=FaceDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle ParkingDetection alarm
            if (content.contains("Code=ParkingDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_PARKING_ALARM);
            } else if (content.contains("Code=ParkingDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_PARKING_ALARM, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle CrossRegionDetection alarm
            if (content.contains("Code=CrossRegionDetection;action=Start;index=0")) {
                ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
            } else if (content.contains("Code=CrossRegionDetection;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_FIELD_DETECTION_ALARM, OnOffType.valueOf("OFF"));
                ipCameraHandler.firstMotionAlarm = false;
                ipCameraHandler.motionAlarmUpdateSnapshot = false;
            }
            // Handle External Input alarm
            if (content.contains("Code=AlarmLocal;action=Start;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("ON"));
            } else if (content.contains("Code=AlarmLocal;action=Stop;index=0")) {
                ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("OFF"));
            }
            // Handle External Input alarm2
            if (content.contains("Code=AlarmLocal;action=Start;index=1")) {
                ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT2, OnOffType.valueOf("ON"));
            } else if (content.contains("Code=AlarmLocal;action=Stop;index=1")) {
                ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT2, OnOffType.valueOf("OFF"));
            }
        } finally {
            ReferenceCountUtil.release(msg);
            content = null;
        }
    }

    public String encodeSpecialChars(String text) {
        String Processed = null;
        try {
            Processed = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {

        }
        return Processed;
    }

    // This handles the commands that come from the Openhab event bus.
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.toString() == "REFRESH") {
            switch (channelUID.getId()) {
                case CHANNEL_THRESHOLD_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=AudioDetect[0]");
                    return;
                case CHANNEL_ENABLE_AUDIO_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=AudioDetect[0]");
                    return;
                case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=CrossLineDetection[0]");
                    return;
                case CHANNEL_ENABLE_MOTION_ALARM:
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=getConfig&name=MotionDetect[0]");
                    return;
            }
            return; // Return as we have handled the refresh command above and don't need to
                    // continue further.
        } // end of "REFRESH"
        switch (channelUID.getId()) {
            case CHANNEL_TEXT_OVERLAY:
                String text = encodeSpecialChars(command.toString());
                if ("".contentEquals(text)) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoWidget[0].CustomTitle[1].EncodeBlend=false");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&VideoWidget[0].CustomTitle[1].EncodeBlend=true&VideoWidget[0].CustomTitle[1].Text="
                                    + text);
                }
                return;
            case CHANNEL_API_ACCESS:
                if (command.toString() != null) {
                    ipCameraHandler.logger.info("API Access was sent this command :{}", command.toString());
                    ipCameraHandler.sendHttpGET(command.toString());
                    ipCameraHandler.setChannelState(CHANNEL_API_ACCESS, StringType.valueOf(""));
                }
                return;
            case CHANNEL_ENABLE_LED:
                ipCameraHandler.setChannelState(CHANNEL_AUTO_LED, OnOffType.valueOf("OFF"));
                if ("0".equals(command.toString()) || "OFF".equals(command.toString())) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Off");
                } else if ("ON".equals(command.toString())) {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Manual");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Manual&Lighting[0][0].MiddleLight[0].Light="
                                    + command.toString());
                }
                return;
            case CHANNEL_AUTO_LED:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.setChannelState(CHANNEL_ENABLE_LED, UnDefType.valueOf("UNDEF"));
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Auto");
                }
                return;
            case CHANNEL_THRESHOLD_AUDIO_ALARM:
                int threshold = Math.round(Float.valueOf(command.toString()));

                if (threshold == 0) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationThreold=1");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationThreold=" + threshold);
                }
                return;
            case CHANNEL_ENABLE_AUDIO_ALARM:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationDetect=true&AudioDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&AudioDetect[0].MutationDetect=false");
                }
                return;
            case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
                if ("ON".equals(command.toString())) {

                } else {

                }
                return;
            case CHANNEL_ENABLE_MOTION_ALARM:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.sendHttpGET(
                            "/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=true&MotionDetect[0].EventHandler.Dejitter=1");
                } else {
                    ipCameraHandler
                            .sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&MotionDetect[0].Enable=false");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[0].Mode=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[0].Mode=0");
                }
                return;
            case CHANNEL_ACTIVATE_ALARM_OUTPUT2:
                if ("ON".equals(command.toString())) {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[1].Mode=1");
                } else {
                    ipCameraHandler.sendHttpGET("/cgi-bin/configManager.cgi?action=setConfig&AlarmOut[1].Mode=0");
                }
                return;
        }
    }

    // If a camera does not need to poll a request as often as snapshots, it can be
    // added here. Binding steps through the list.
    public ArrayList<String> getLowPriorityRequests() {
        ArrayList<String> lowPriorityRequests = new ArrayList<String>(1);
        return lowPriorityRequests;
    }
}