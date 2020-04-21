metadata {
    definition (name: "Pool Aqualink Parent", namespace: "aqualink", author: "MC") {
        capability "Initialize"
        capability "Switch"
<<<<<<< HEAD
=======
// Added the following capabilities
//        capability "TemperatureMeasurement"
//        capability "Sensor"
>>>>>>> 645c8e2149d0a9823c5f7bd1b480fefa18a84ce2

        attribute "contact", "string"
        attribute "session_id", "string"
        attribute "auth_token", "string"
        attribute "user_id", "string"
        
        attribute "label", "string"
        attribute "poolStatus", "string"
        attribute "spaTemp", "string"
        attribute "poolTemp", "string"
        attribute "airTemp", "string"
        attribute "spaDesiredTemp", "string"
        attribute "poolDesiredTemp", "string"
        attribute "freezeProtection", "string"
        attribute "spaMode", "string"
        attribute "poolPump", "string"
        attribute "gasHeater", "string"
        attribute "solarHeater", "string"
        attribute "subtype", "string"
        attribute "devtype", "string"
        
        command "GetSerial", ["String"]
        command "UpdateDevices", null
        command "Refresh", null
        command "DeleteChild", ["String"]
        command "TogglePoolPump", null
        command "ToggleSpaPump", null
        command "ToggleSolarHeat", null
        command "ToggleGasHeat", null
    }

    preferences {
        section("Device Settings:") {
            input "serial_number", "string", title:"Pool Serial Number", description: "", required: true, displayDuringSetup: true
            input "email", "string", title:"Userid - Email", description: "", required: true, displayDuringSetup: true
            input "password", "string", title:"User Password", description: "", required: true, displayDuringSetup: true
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
            input name: "autoUpdate", type: "bool", title: "Enable Auto updating", defaultValue: true
            input name: "aux_EA", type: "bool", title: "Enable AUX EA Port", defaultValue: false
        }
    }


}

void installed() {
    log.warn "..."
    initialize()
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

void parse(String description) {
    

}

def get_poolStatus(){
    //log.info device.currentValue("poolStatus")
    return device.currentValue("poolStatus")
}

void DeleteChild(String device){
    deleteChildDevice(device) 
}

void UpdateDevices(){
    Updateinfo()
}

void Refresh(){
    Refreshinfo()
}

void updated() {
    log.info "updated..."
    initialize()
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
    
    if (autoUpdate) runIn(3600, Updateinfo)
}

void uninstalled() {
    String thisId = device.id
    deleteChildDevice("${thisId}-Pool_Temp")
    deleteChildDevice("${thisId}-Spa_Temp")
    deleteChildDevice("${thisId}-Air_Temp")
}

void initialize() {
    LoginGetSession()
    CreateChildren()
    updatedeviceinfo()
}

def CreateChild2(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}

def CreateChildTemp(String Unit, String label){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
        cd = addChildDevice( "Pool Aqualink Child Temps", "${thisId}-${Unit}", [name: "${label}", isComponent: true])
    }
    return cd 
}

void updatecolor(){
    device.updateSetting("colorlightenable", [value: true, type: "bool"])
    device.updateSetting("devtype", [value: "2", type: "String"])
}

void updatedevtype(String devtype){
    device.updateSetting("devtype", [value: devtype, type: "String"])
}

void CreateChild(String Unit, String label, String devtype){
    
    if("AUX${Unit}" != label && label != "AUX-11") {
        if(devtype == "2") {
            //Unit = "${Unit}-Color"
            cd = CreateChild2(Unit, label)
            cd.updatecolor()
        } else {
            cd = CreateChild2(Unit, label)
            cd.updatedevtype(devtype)
            //cd.removeSetting("serial_number")
        }
        if (logEnable) log.info "Unit is ${Unit}   DeviceType is ${devtype}"
    }
    
}

void CreateChildren(){
   
    devices = GetDevices()
    CreateChild("1", devices.devices_screen[3].aux_1[1].label, devices.devices_screen[3].aux_1[3].type )
    CreateChild("2", devices.devices_screen[4].aux_2[1].label, devices.devices_screen[4].aux_2[3].type )
    CreateChild("3", devices.devices_screen[5].aux_3[1].label, devices.devices_screen[5].aux_3[3].type )
    CreateChild("4", devices.devices_screen[6].aux_4[1].label, devices.devices_screen[6].aux_4[3].type )
    CreateChild("5", devices.devices_screen[7].aux_5[1].label, devices.devices_screen[7].aux_5[3].type )
    CreateChild("6", devices.devices_screen[8].aux_6[1].label, devices.devices_screen[8].aux_6[3].type )
    CreateChild("7", devices.devices_screen[9].aux_7[1].label, devices.devices_screen[9].aux_7[3].type )
    if (aux_EA) { CreateChild("8", devices.devices_screen[10].aux_EA[1].label, devices.devices_screen[10].aux_EA[3].type ) } 
//   CreateChild("FilterPump", "FilterPump", "0")
   
//   CreateChildTemp("Pool_Temp", "Pool Temp")
//   if(SpaTemp){
//       CreateChildTemp("Spa_Temp", "Spa Temp" )
//   }
//   if(AirTemp){
//       CreateChildTemp("Air_Temp", "Air Temp" )
//   }
    
}

def fetchChild(String Unit){
    String thisId = device.id
    def cd = getChildDevice("${thisId}-${Unit}")
    if (!cd) {
         return null
    }
    return cd 
}

void Updateinfo(){
    
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
    
    if (autoUpdate) runIn(3600, Updateinfo)
}

void Refreshinfo(){
    
    if(!session_id) {
        LoginGetSession()
    }
    refreshstateinfo()
    
    if (autoUpdate) runIn(600, Refreshinfo)
}

void notifychild(String chunit, String status, String label, String devtype, String subtype){
    def cd = fetchChild(chunit)
    if(cd){
        cd.parsechild(status, label, devtype, subtype)
    }
}

void parsechild(String status2, String label, String devtype, String subtype) {
    device.name = label
    status= status2
    if(status == "1") {
        sendEvent(name: "switch", value: "on")        
    } else {
        sendEvent(name: "switch", value: "off")
    }
    device.updateSetting("devtype", devtype)
    sendEvent(name:"devtype", value:devtype)
    state.devtype = devtype
}

void updatedeviceinfo(){
    devices = GetDevices()
   
    try {  notifychild("1",devices.devices_screen[3].aux_1[0].state, devices.devices_screen[3].aux_1[1].label, devices.devices_screen[3].aux_1[3].type, devices.devices_screen[3].aux_1[4].subtype )  } catch (Exception e) { log.warn "aux_1 failed: ${e.message}" }
    try {  notifychild("2",devices.devices_screen[4].aux_2[0].state, devices.devices_screen[4].aux_2[1].label, devices.devices_screen[4].aux_2[3].type, devices.devices_screen[4].aux_2[4].subtype )  } catch (Exception e) { log.warn "aux_2 failed: ${e.message}" }
    try {  notifychild("3",devices.devices_screen[5].aux_3[0].state, devices.devices_screen[5].aux_3[1].label, devices.devices_screen[5].aux_3[3].type, devices.devices_screen[5].aux_3[4].subtype )  } catch (Exception e) { log.warn "aux_3 failed: ${e.message}" }
    try {  notifychild("4",devices.devices_screen[6].aux_4[0].state, devices.devices_screen[6].aux_4[1].label, devices.devices_screen[6].aux_4[3].type, devices.devices_screen[6].aux_4[4].subtype )  } catch (Exception e) { log.warn "aux_4 failed: ${e.message}" }
    try {  notifychild("5",devices.devices_screen[7].aux_5[0].state, devices.devices_screen[7].aux_5[1].label, devices.devices_screen[7].aux_5[3].type, devices.devices_screen[7].aux_5[4].subtype )  } catch (Exception e) { log.warn "aux_5 failed: ${e.message}" }
    try {  notifychild("6",devices.devices_screen[8].aux_6[0].state, devices.devices_screen[8].aux_6[1].label, devices.devices_screen[8].aux_6[3].type, devices.devices_screen[8].aux_6[4].subtype )  } catch (Exception e) { log.warn "aux_6 failed: ${e.message}" }
    try {  notifychild("7",devices.devices_screen[9].aux_7[0].state, devices.devices_screen[9].aux_7[1].label, devices.devices_screen[9].aux_7[3].type, devices.devices_screen[9].aux_7[4].subtype )  } catch (Exception e) { log.warn "aux_7 failed: ${e.message}" }
    if (aux_EA) { try {  notifychild("8",devices.devices_screen[10].aux_EA[0].state, devices.devices_screen[10].aux_EA[1].label, devices.devices_screen[10].aux_EA[3].type, devices.devices_screen[10].aux_EA[4].subtype )  } catch (Exception e) { log.warn "aux_EA failed: ${e.message}" } }
    
}

void refreshstateinfo(){
    devices = GetHomeScreenInfo()
    
    state.configval0    = devices.home_screen[0].status
    state.configval4    = devices.home_screen[4].spa_temp
    state.configval5    = devices.home_screen[5].pool_temp    
    state.configval6    = devices.home_screen[6].air_temp
    state.configval7    = devices.home_screen[7].spa_set_point
    state.configval8    = devices.home_screen[8].pool_set_point
    state.configval10   = devices.home_screen[10].freeze_protection
    state.configval11   = devices.home_screen[11].spa_pump
    state.configval12   = devices.home_screen[12].pool_pump
    state.configval13   = devices.home_screen[13].spa_heater
    state.configval15   = devices.home_screen[15].solar_heater

    sendEvent(name: "status", value: state.configval0, descriptionText: "Aqualink State: ${state.configval0}")
    sendEvent(name: "poolPump", value: state.configval12=="0" ? 'Off' : 'On', descriptionText: "Pool pump: ${state.configval12}")
    sendEvent(name: "spaTemp", value: state.configval4, descriptionText: "Spa Temperature: ${state.configval4}")
    sendEvent(name: "poolTemp", value: state.configval5, descriptionText: "Pool Temperature: ${state.configval5}")
    sendEvent(name: "airTemp", value: state.configval6, descriptionText: "Air Temperature: ${state.configval6}")
    sendEvent(name: "spaSetTemp", value: state.configval7, descriptionText: "Spa Set Temperature: ${state.configval7}")
    sendEvent(name: "poolSetTemp", value: state.configval8, descriptionText: "Pool Set Temperature: ${state.configval8}")
    sendEvent(name: "spaMode", value: state.configval11=="0" ? 'Off' : 'On', descriptionText: "Spa Mode: ${state.configval11}")
    sendEvent(name: "gasHeater", value: state.configval13=="0" ? 'Off' : 'On', descriptionText: "Gas Heating: ${state.configval13}")
    sendEvent(name: "solarHeater", value: state.configval15=="0" ? 'Off' : 'On', descriptionText: "Solar Heating: ${state.configval15}")

}



def LoginGetSession(){
    
    def wxURI = "https://support.iaqualink.com/users/sign_in.json"
    
	def requestParams =
	[
		uri:  wxURI,
		requestContentType: "application/json",
        body: """{"api_key": "EOOEMOW4YR6QNB07", "email": "${email}", "password": "${password}"}"""
	]

	httpPost(requestParams)
	{
	  response ->
		if (response?.status == 200)
		{
            updatedeviceinfo
            if (logEnable) response.data.authentication_token
            if (logEnable) response.data.session_id
            
            device.updateSetting("session_id", [value: response.data.session_id, type: "String"])
            session_id = response.data.session_id
            settings.session_id = response.data.session_id
            auth_token = response.data.authentication_token
            user_id = response.data.id
			return response.data.session_id
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}

void rundeviceupdate(){
    if(!session_id) {
        LoginGetSession()
    }
    updatedeviceinfo()
    refreshstateinfo()
}

void on(){
/*
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    //log.info "${unit}"
    parent.rundeviceupdate()
    //log.info "  ${device.currentValue('switch')} "
    //log.info "  ${device.currentValue('subtype')}"
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'off'){
        if (logEnable) log.info "Toggle Device ON  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(devtype == "2"){
            parent.OperateColorLightParent(unit,settings.lightcolor)
        } else {
            parent.OperateDeviceParent(unit)
        }
        sendEvent(name: "switch", value: "on")        
    } else {
        if (logEnable) log.info "Device Already ON  ${device}"
    }
*/
<<<<<<< HEAD

    refreshstateinfo()
//    log.info state.configval12
    
    if (state.configval12=='1') {
        log.info "Pool pump already ON."
    } else {
        if (logEnable) log.info "Turning on Pool pump..."
    }    
=======
    parent.rundeviceupdate()
    
>>>>>>> 645c8e2149d0a9823c5f7bd1b480fefa18a84ce2

}

void off(){
/*
    unit=device.deviceNetworkId.substring(device.deviceNetworkId.indexOf("-")+1)
    parent.rundeviceupdate()
    log.info "${unit}    ${device.currentValue('switch')} "
    devtype = device.currentValue('devtype')
    
    if(device.currentValue('switch') == 'on'){
        if (logEnable) log.info "Toggle Device OFF  ${device}"
        if(unit == "FilterPump"){
            parent.TogglePoolPump()  
        } else if(devtype == "2"){
            parent.OperateColorLightParent(unit,"0")
        } else {
            parent.OperateDeviceParent(unit)
        }
        sendEvent(name: "switch", value: "off")        
    } else {
        if (logEnable) log.info "Device Already OFF  ${device}"
    }
*/
    refreshstateinfo()
//    log.info state.configval12
    
    if (state.configval12=='0') {
        log.info "Pool pump already OFF."
    } else {
        if (logEnable) log.info "Turning off Pool pump..."
    }
}

void OperateDeviceParent(String num){
    if(!session_id) {
        LoginGetSession()
    }
    OperateDevice(num)
}

void OperateDevice(String num){
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_aux_${num}&serial=${serial_number}&sessionID=${session_id}"
    if (logEnable) log.info wxURI2
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
}


void GetSerial() {
    
    if(!session_id) {
        LoginGetSession()
    }
    serial_number = GetSerialfromAqualink()
    sendEvent(name: "serial", value: "${serial_number}")
    
}

def GetSerialfromAqualink() {
    
    def wxURI2 = "https://support.iaqualink.com//devices.json?api_key=EOOEMOW4YR6QNB07&authentication_token=${auth_token}&user_id=${user_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
            serial_number = response.data.serial_number[0]
            
            
            device.updateSetting("serial_number", [value: "${serial_number}", type: "string"])
            
			return serial_number
		}
		else
		{
			log.warn "${response?.status}"
		}
	}

}

def TogglePoolPump() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperatePoolPump();
    refreshstateinfo()  

}

def OperatePoolPump() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_pool_pump&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
       
}

def ToggleSpaPump() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperateSpaPump();
    refreshstateinfo()

}

def OperateSpaPump() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_spa_pump&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}

def ToggleSolarHeat() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperateSolarHeat();
    refreshstateinfo()
   
}

def OperateSolarHeat() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_solar_heater&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}


def ToggleGasHeat() {
    
    if(!session_id) {
        LoginGetSession()
    }
    
    OperateGasHeat();
    refreshstateinfo()
  
}

def OperateGasHeat() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=set_spa_heater&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
}


def GetHomeScreenInfo() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=get_home&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}

def GetDevices() {
    
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&command=get_devices&serial=${serial_number}&sessionID=${session_id}"
    
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
    
    
}

void OperateColorLightParent(String num, String color){
    if(!session_id) {
        LoginGetSession()
    }
    OperateColorLight(num,color)
}

void OperateColorLight(String num,String light_color){
    def wxURI2 = "https://iaqualink-api.realtime.io/v1/mobile/session.json?actionID=command&aux=${num}&command=set_light&light=${light_color}&serial=${serial_number}&sessionID=${session_id}"
    log.info wxURI2
    def requestParams2 =
	[
		uri:  wxURI2,
	]
    httpGet(requestParams2)
	{
	  response ->
		if (response?.status == 200)
		{
            if (logEnable) log.info response.data
			return response.data
		}
		else
		{
			log.warn "${response?.status}"
		}
	}
}
