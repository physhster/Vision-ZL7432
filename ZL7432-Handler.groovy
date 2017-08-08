/**
 *  
 *	ZL 7432US Dual Relay Device Type
 *  
 *	Initial Author: Unknown
 *  Maintainer: physhster
 *	Date: 2017-08-07
 */
 
metadata {
	definition (name: "ZL 7432US Dual Relay", namespace: "ZL 7432US", author: "NA") {
        capability "Switch"
        capability "Polling"
        capability "Zw Multichannel"
        capability "Refresh"
        attribute "switch", "string"
        attribute "switch2", "string"

        command "on"
        command "off"
        command "on2"	
        command "off2"
        command "reset"

        //fingerprint deviceId: "0x1001", inClusters:"0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x71, 0x2B, 0x2C, 0x75, 0x7A, 0x60, 0x32, 0x70"
	}

    simulator {
        status "on": "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }

    tiles {    
        standardTile("switch", width:3, height:2 ,"device.switch",canChangeIcon: true) {
	   state "on", label: "ON", action: "off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
	   state "off", label: "OFF", action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        //standardTile("switch2", width:3, height: 2, "device.switch2",canChangeIcon: true) {
        //    state "on", label: "ON", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        //    state "off", label: "OFF", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        //}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
	   state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        details(["switch","refresh"])
    }

}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

//def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
    /*def result
    if (cmd.value == 0) {
        result = createEvent(name: "switch", value: "off")
    } else {
        result = createEvent(name: "switch", value: "on")
    }
    return result*/
//}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    //response(delayBetween(result, 1000)) // returns the result of reponse()
    response(delayBetween(result, 0)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd){
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    //response(delayBetween(result, 1000)) // returns the result of reponse()
    response(delayBetween(result, 0)) // returns the result of reponse()
}

/*def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    def result
    if (cmd.scale == 0) {
        result = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
    } else if (cmd.scale == 1) {
        result = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
    } else {
        result = createEvent(name: "power", value: cmd.scaledMeterValue, unit: "W")
    }
    return result
}*/

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd){
    //log.debug "multichannelv3.MultiChannelCapabilityReport $cmd"
    if (cmd.endPoint == 2 ) {
        def currstate = device.currentState("switch2").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch2", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        	sendEvent(name: "switch2", value: "on", isStateChange: true, display: false)
    }
    else if (cmd.endPoint == 1 ) {
        def currstate = device.currentState("switch").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
       		sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def switchname = ""
	switch(cmd.sourceEndPoint){
    	case 1:
        	log.debug "Setting switch | switch$cmd.sourceEndPoint"
        	switchname = "switch"
    		break
        case 2:
        	log.debug "Setting switch2"
        	switchname = "switch2"
	   break;
   }
   def map = [ name: "$switchname" ]
   switch(cmd.commandClass) {
      case 32:
	if (cmd.parameter == [0]) {
	   map.value = "off"
	}
	if (cmd.parameter == [255]) {
	   map.value = "on"
	}
	createEvent(map)
	break
      case 37:
	if (cmd.parameter == [0]) {
	   map.value = "off"
	}
	if (cmd.parameter == [255]) {
	   map.value = "on"
	}
	createEvent(map)
	break
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def refresh() {
	def cmds = []
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
	//delayBetween(cmds, 500)
    delayBetween(cmds, 0)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def poll() {
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
	//delayBetween(cmds, 500)
    delayBetween(cmds, 0)
}

/*def reset() {
    delayBetween([
        zwave.meterV2.meterReset().format(),
        zwave.meterV2.meterGet().format()
    ], 1000)
}*/

/*def configure() {
	log.debug "configure() called"
    def cmds = []
    //if (deviceType.value == deviceType.value) log.debug "Statement True"
    if (deviceType != null && deviceType.value != null) {
    switch (deviceType.value as String) {
       case "1":
       log.debug "Configuring device as Philio"
       cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [3]).format()	
       cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       break
       case "2":
       log.debug "Configuring device as Enerwave"
       cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [1]).format()	
       cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       break
       case "3":
       log.debug "Configuring device as Monoprice"
       break
       default:
       log.debug "No valid device type chosen"
       break
    }
    }
    
    if ( cmds != [] && cmds != null ) return delayBetween(cmds, 2000) else return
}*/
/**
* Triggered when Done button is pushed on Preference Pane

def updated(){
	log.debug "Preferences have been changed. Attempting configure()"
    def cmds = configure()
    response(cmds)
}*/

def on() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], 0)
}

def off() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], 0)
}

def on2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 0)
}

def off2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 0)
}
