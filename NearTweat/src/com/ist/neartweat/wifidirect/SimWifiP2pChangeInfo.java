package com.ist.neartweat.wifidirect;

public class SimWifiP2pChangeInfo {
	private SimWifiP2pInfo ginfo;
	private SimWifiP2pDeviceList deviceList;
	private SimWifiP2pDeviceList devices;
	public SimWifiP2pChangeInfo( SimWifiP2pInfo ginfo,SimWifiP2pDeviceList deviceList){
		this.setGinfo(ginfo);
		this.setDeviceList(deviceList);
	}
	public SimWifiP2pChangeInfo(SimWifiP2pInfo ginfo2,
			SimWifiP2pDeviceList deviceList2, SimWifiP2pDeviceList devices) {
		this.setGinfo(ginfo2);
		this.setDeviceList(deviceList2);
		this.setDevices(devices);
	}
	public SimWifiP2pInfo getGinfo() {
		return ginfo;
	}
	public void setGinfo(SimWifiP2pInfo ginfo) {
		this.ginfo = ginfo;
	}
	public SimWifiP2pDeviceList getDevices() {
		return devices;
	}
	public void setDevices(SimWifiP2pDeviceList devices) {
		this.devices = devices;
	}
	public SimWifiP2pDeviceList getDeviceList() {
		return deviceList;
	}
	public void setDeviceList(SimWifiP2pDeviceList deviceList) {
		this.deviceList = deviceList;
	}
}
