/**
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition(name: "Qubino Dimmer Plus", namespace: "smartthings", author: "Zeeflyboy") {
        capability "Energy Meter"
        capability "Actuator"
        capability "Switch"
        capability "Power Meter"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"
        capability "Switch Level"

        command "reset"
        command "changeSingleParamAfterSecure"
        command "configureAfterSecure"

        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x86,0x72,0x5A,0x73,0x20,0x27,0x25,0x26,0x30,0x71,0x32,0x60,0x85,0x8E,0x59,0x70,0xEF,0x20"
    }

    simulator {
        status "on": "command: 2603, payload: FF"
        status "off": "command: 2603, payload: 00"
        status "09%": "command: 2603, payload: 09"
        status "10%": "command: 2603, payload: 0A"
        status "33%": "command: 2603, payload: 21"
        status "66%": "command: 2603, payload: 42"
        status "99%": "command: 2603, payload: 63"

        for (int i = 0; i <= 10000; i += 1000) {
            status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
        }
        for (int i = 0; i <= 100; i += 10) {
            status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
        }

        ["FF", "00", "09", "0A", "21", "42", "63"].each { val -> reply "2001$val,delay 100,2602": "command: 2603, payload: $val" }
    }
    // tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "switch level.setLevel"
            }
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "default", label: '${currentValue} W'
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "default", label: '${currentValue} kWh'
        }
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: 'reset kWh', action: "reset"
        }
        standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "configure", label: '', action: "configureAfterSecure", icon: "st.secondary.configure"
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main(["switch", "power", "energy"])
        details(["switch", "power", "energy", "configureAfterSecure", "refresh", "reset"])
    }

    preferences {
      def paragraph = "GROUP 0 - The Dimmer behavior - Basic functionalities"
      input name: "param1", type: "number", range: "0..1", defaultValue: "0", required: true,
          title: paragraph + "\n\n" +
          "1. Input 1 Switch Type. " +
          "Defines the type of switch. " +
          "0 - Mono-stable push type switch.\n" +
          "1 - Bi-stable toggle type switch.\n" +
          "Default value: 0."

      input name: "param2", type: "number", range: "0..1", defaultValue: "0", required: true,
          title: "2. Input 2 Switch Type. " +
          "Defines the type of switch. " +
          "0 - Mono-stable push type switch.\n" +
          "1 - Bi-stable toggle type switch.\n" +
          "Default value: 0."

      input name: "param3", type: "number", range: "0..1", defaultValue: "0", required: true,
          title: "3. Input 2 Contact Type. " +
          "Defines the contact type for Input 2. " +
          "0 - NO normally open input type.\n" +
          "1 - NC normally closed input type.\n" +
          "Default value: 0."

      input name: "param4", type: "number", range: "0..1", defaultValue: "0", required: true,
          title: "3. Input 3 Contact Type. " +
          "Defines the contact type for Input 3. " +
          "0 - NO normally open input type.\n" +
          "1 - NC normally closed input type.\n" +
          "Default value: 0."

      input name: "param10", type: "enum", defaultValue: "255", required: true,
          options: ["0": "0",
            "1": "1",
            "2": "2",
            "255": "255"
          ],
          title: "10. ALL ON/ALL OFF function. " +
          "Allows for activation/deactivation of Z-Wave commands enabling/disabling all devices located in direct range of the main controller.\n" +
          "Available settings:\n" +
          "0 = All ON not active, All OFF not active,\n" +
          "1 = All ON not active, All OFF active,\n" +
          "2 = All ON active, All OFF not active,\n" +
          "255 = All ON active, All OFF active.\n" +
          "Default value: 255."

      input name: "param11", type: "number", range: "0..32536", defaultValue: "0", required: true,
        title: "11. Automatic turning off output after set time. " +
        "Defines the time in seconds before auto off.\n" +
        "Available settings: 1-32536 (step is 1 second, 0 is disabled).\n" +
        "Default value: 0."

      input name: "param12", type: "number", range: "0..32535", defaultValue: "0", required: true,
        title: "12. Automatic turning on output after set time. " +
        "Defines the time in seconds before auto on.\n" +
        "Available settings: 1-32535 (step is 1 second, 0 is disabled).\n" +
        "Default value: 0."

      input name: "param20", type: "number", range: "0..1", defaultValue: "0", required: true,
        title: "20. Enable/Disable 3 way switch. " +
        "Dimming is done by push button to L1 by default, 3way switch dimming can be controlled by push button connected to L1 and L2.\n" +
        "Available settings:\n" +
        "0 - Single push button (connected to L1).\n" +
        "1 - 3 way switch (connected to L1 and L2).\n" +
        "Default value: 0."

      input name: "param21", type: "number", range: "0..1", defaultValue: "0", required: true,
        title: "21. Enable/Disable Double click function. " +
        "If enabled, a fast double click will set dimming at maximum power.\n" +
        "Available settings:\n" +
        "0 - Double Click disabled.\n" +
        "1 - Double Click enabled.\n" +
        "Default value: 0."

      input name: "param30", type: "number", range: "0..1", defaultValue: "0", required: true,
        title: "30. State of the device after a power failure. " +
        "Defines whether the Dimmer will return to the last state before power failure.\n" +
        "Available settings:\n" +
        "0 = Dimmer restores its state before power failure.\n" +
        "1 = Dimmer does not save the state before a power failure, it returns to „off” position,\n" +
        "Default value: 0."

      input name: "param40", type: "number", range: "0..100", defaultValue: "5", required: true,
        title: "40. Power Reporting in Watts on power change. " +
        "Power report is sent only when actual Power in Watts in real time changes by more than set percentage. " +
        "If power change is less than 1W, report is not sent regardless of percentage set.\n" +
        "Available settings:\n" +
        "0 - Reporting disabled.\n" +
        "1-100 - 1% to 100% reporting enabled.\n" +
        "Default value: 5."

      input name: "param42", type: "number", range: "0..32767", defaultValue: "0", required: true,
        title: "42. Power Reporting in Watts by time interval. " +
        "Power report is sent with time interval set by entered value (in seconds). " +
        "Available settings:\n" +
        "0 = Disabled.\n" +
        "1-32767 - time interval in seconds.\n" +
        "Default value: 0."

      input name: "param60", type: "number", range: "0..98", defaultValue: "1", required: true,
        title: "60. Minimum Dimming Value. " +
        "Defines the minimum Dimming power level as a percentage.\n" +
        "Available settings:\n" +
        "1-98 - Minimum dimming percentage is set by entered value.\n" +
        "Default value: 1."

      input name: "param61", type: "number", range: "2..99", defaultValue: "99", required: true,
        title: "61. Maximum Dimming Value. " +
        "Defines the Maximum Dimming power level as a percentage.\n" +
        "Available settings:\n" +
        "2-99 - Maximum dimming percentage is set by entered value.\n" +
        "Default value: 99."

      input name: "param65", type: "number", range: "50..255", defaultValue: "100", required: true,
        title: "65. Dimming time (soft on/off). " +
        "Set value determines time between min and max dimming values by short press of button or through UI.\n" +
        "Available settings:\n" +
        "50-255 - 500ms to 2550ms, step is 10ms.\n" +
        "Default value: 100."

      input name: "param66", type: "number", range: "1..255", defaultValue: "3", required: true,
        title: "66. Dimming time while key is held. " +
        "Set value determines time between min and max dimming values by continuous hold of push button L1 or associated device.\n" +
        "Available settings:\n" +
        "1-255 - 1s to 255s, step is 1s.\n" +
        "Default value: 3."

      input name: "param67", type: "number", range: "0..1", defaultValue: "0", required: true,
        title: "67. Ignore start level. " +
        "This parameter is used with association group 3.\n" +
        "Available settings:\n" +
        "0 = Respect start level\n" +
        "1 = Ignore start level.\n" +
        "Default value: 0."

      input name: "param68", type: "number", range: "0..127", defaultValue: "0", required: true,
        title: "68. Dimming duration. " +
        "This parameter is used with association group 3, the duration field MUST specify the time that the transition should take.\n" +
        "Available settings:\n" +
        "0 - Dimming duration according to parameter 66.\n" +
        "1-127 - Dimming duration in seconds.\n" +
        "Default value: 0."

      input name: "param100", type: "enum", defaultValue: "0", required: true,
        options: ["0": "0",
            "1": "1",
            "2": "2",
            "3": "3",
            "4": "4",
            "5": "5",
            "6": "6",
            "9": "9"
        ],
        title: "100. Enable/Disable endpoints L2 or select notification type and event.\n" +
        "Note1: After parameter Change, first exclude module (without setting parameters to default) then wait at least 30s to re-include.\n" +
        "Note2: When the parameter is set to 9, the notifications are sent for home security.\n" +
        "Available settings:\n" +
        "0 = Endpoint L2 disabled\n" +
        "1 = Home security, motion detection, unknown loc.\n" +
        "2 = CO, Carbon Monoxide detected, unknown loc.\n" +
        "3 = CO2, Carbon Dioxide detected, unknown loc.\n" +
        "4 = Water Alarm, Water leak detected, unknown loc.\n" +
        "5 = Heat Alarm, Overheat detected, unknown loc.\n" +
        "6 = Smoke Alarm, Smoke detected, unknown loc.\n" +
        "9 = Sensor binary.\n" +
        "Default value: 0."

      input name: "param101", type: "enum", defaultValue: "0", required: true,
        options: ["0": "0",
            "1": "1",
            "2": "2",
            "3": "3",
            "4": "4",
            "5": "5",
            "6": "6",
            "9": "9"
        ],
        title: "101. Enable/Disable endpoints L3 or select notification type and event.\n" +
        "Note1: After parameter Change, first exclude module (without setting parameters to default) then wait at least 30s to re-include.\n" +
        "Note2: When the parameter is set to 9, the notifications are sent for home security.\n" +
        "Available settings:\n" +
        "0 = Endpoint L3 disabled\n" +
        "1 = Home security, motion detection, unknown loc.\n" +
        "2 = CO, Carbon Monoxide detected, unknown loc.\n" +
        "3 = CO2, Carbon Dioxide detected, unknown loc.\n" +
        "4 = Water Alarm, Water leak detected, unknown loc.\n" +
        "5 = Heat Alarm, Overheat detected, unknown loc.\n" +
        "6 = Smoke Alarm, Smoke detected, unknown loc.\n" +
        "9 = Sensor binary.\n" +
        "Default value: 0."

      input name: "param110", type: "number", range: "0..32536", defaultValue: "32536", required: true,
        title: "110. Temperature sensor offset settings. " +
        "Set value is added or subtracted to actual measured value by sensor.\n" +
        "Available settings:\n" +
        "32536 = offset is 0.0C.\n" +
        "1-100 = Value from 0.1C to 10.0C is added to actual measured temperature.\n" +
        "1001-1100 = Value from -0.1C to -10.0C is subtracted from actual measured temperature.\n" +
        "Default value: 0."

      input name: "param120", type: "number", range: "0..127", defaultValue: "5", required: true,
        title: "120. Temperature sensor reporting.\n" +
        "If digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter.\n" +
        "Available settings:\n" +
        "0 = Reporting disabled\n" +
        "1-127 = 0.1C to 12.7C, step is 0.1C.\n" +
        "Default value: 5."
    }
}

// parse events into attributes
def parse(String description) {
    def result = null
    if (description != "updated") {
        def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32: 3])
        if (cmd) {
            result = zwaveEvent(cmd)
            log.debug("'$description' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
        }
    }
    result
}

/*        def updated() {
    response(refresh())
}
*/
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
    def result = []
    def value = (cmd.value ? "on" : "off")
    def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
    result << switchEvent
    if (cmd.value) {
        result << createEvent(name: "level", value: cmd.value, unit: "%")
    }
    if (switchEvent.isStateChange) {
        result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    if (cmd.meterType == 1) {
        if (cmd.scale == 0) {
            return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
        } else if (cmd.scale == 1) {
            return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
        } else if (cmd.scale == 2) {
            return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        } else {
            return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
        }
    }
}

def on() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format(),
    ], 5000)
}

def off() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format(),
    ], 5000)
}

def poll() {
    delayBetween([
        zwave.meterV2.meterGet(scale: 0).format(),
        zwave.meterV2.meterGet(scale: 2).format(),
    ], 1000)
}

def refresh() {
    delayBetween([
        zwave.switchMultilevelV1.switchMultilevelGet().format(),
        zwave.meterV2.meterGet(scale: 0).format(),
        zwave.meterV2.meterGet(scale: 2).format(),
    ], 1000)
}

def setLevel(level) {
    if (level > 99) level = 99
    delayBetween([
        zwave.basicV1.basicSet(value: level).format(),
        zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 5000)
}

def configureAfterSecure() {
    log.debug "configureAfterSecure()"
    def cmds = secureSequence([
        zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: param1.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: param2.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: param3.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: param4.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 10, size: 2, scaledConfigurationValue: param5.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 11, size: 2, scaledConfigurationValue: param6.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 12, size: 2, scaledConfigurationValue: param7.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: param8.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: param9.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: param10.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: param11.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: param13.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 60, size: 1, scaledConfigurationValue: param15.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 61, size: 1, scaledConfigurationValue: param16.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 65, size: 2, scaledConfigurationValue: param19.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 66, size: 2, scaledConfigurationValue: param20.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 67, size: 1, scaledConfigurationValue: param21.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 68, size: 1, scaledConfigurationValue: param22.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 100, size: 1, scaledConfigurationValue: param23.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 101, size: 1, scaledConfigurationValue: param24.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 110, size: 2, scaledConfigurationValue: param25.toInteger()),
        zwave.configurationV1.configurationSet(parameterNumber: 120, size: 1, scaledConfigurationValue: param26.toInteger()),

    ])

    // Register for Group 1
    if (paramAssociationGroup1) {
        cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
    } else {
        cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
    }
    // Register for Group 2
    if (paramAssociationGroup2) {
        cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]))
    } else {
        cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]))
    }
    // Register for Group 3
    if (paramAssociationGroup3) {
        cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: [zwaveHubNodeId]))
    } else {
        cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: [zwaveHubNodeId]))
    }
    // Register for Group 4
    if (paramAssociationGroup4) {
        cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 4, nodeId: [zwaveHubNodeId]))
    } else {
        cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier: 4, nodeId: [zwaveHubNodeId]))
    }
    // Register for Group 5
    if (paramAssociationGroups5) {
        cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 5, nodeId: [zwaveHubNodeId]))
    } else {
        cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier: 5, nodeId: [zwaveHubNodeId]))
    }

    cmds
}

def configure() {
    // Wait until after the secure exchange for this
    log.debug "configure()"
}

def updated() {
    log.debug "updated()"
    response(["delay 2000"] + configureAfterSecure() + refresh())
}

private secure(physicalgraph.zwave.Command cmd) {
    log.trace(cmd)
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay = 200) {
    log.debug "$commands"
    delayBetween(commands.collect { secure(it) }, delay)
}
