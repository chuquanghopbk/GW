/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vnpt.ussd.gw;

public interface MOGWMBean {

    public String getAPP_TYPE();

    public int getAPP_ID();

    public String getAPP_NAME();

    public String getSCTP_CONFIG_FILE_Path();

    public String getM3UA_CONFIG_FILE_Path();

    public String getSCCP_ROUTER_CONFIG_FILE_Path();

    public String getSCCP_RESOURCE_CONFIG_FILE_Path();

    //Sigtran config
    public String getSccpRuleConfig();

    public String getSccpAddressConfig();

    public String getSccpSap();

    public String getRemoteSpcsResource();

    public String getRemoteSsnsResource();

    public String getM3uaASPFactoryList();

    public String getM3uaASList();

    public String getM3UARoutes();

    public String getSCTPServers();
}
