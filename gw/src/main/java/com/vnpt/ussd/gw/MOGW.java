package com.vnpt.ussd.gw;

import com.vnpt.ussd.gw.utils.MOGWConfig;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mobicents.protocols.api.AssociationType;
import org.mobicents.protocols.api.Server;
import org.mobicents.protocols.sctp.ManagementImpl;
import org.mobicents.protocols.ss7.m3ua.As;
import org.mobicents.protocols.ss7.m3ua.Asp;
import org.mobicents.protocols.ss7.m3ua.AspFactory;
import org.mobicents.protocols.ss7.m3ua.impl.M3UAManagementImpl;
import org.mobicents.protocols.ss7.sccp.Mtp3Destination;
import org.mobicents.protocols.ss7.sccp.Mtp3ServiceAccessPoint;
import org.mobicents.protocols.ss7.sccp.RemoteSignalingPointCode;
import org.mobicents.protocols.ss7.sccp.RemoteSubSystem;
import org.mobicents.protocols.ss7.sccp.Rule;
import org.mobicents.protocols.ss7.sccp.impl.SccpStackImpl;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

/**
 * @author HOPCQ
 *
 */
public class MOGW implements MOGWMBean {

    private static Logger logger = null;
    // SCCP
    private SccpStackImpl sccpStack;
    // M3UA
    private M3UAManagementImpl serverM3UAMgmt;
    // SCTP
    private ManagementImpl sctpManagement;
    ExecutorService tpes = Executors.newFixedThreadPool(4, new WorkerThreadFactory("VSMSC-GW"));
    private static MOGW instance;

    private void initializeStack() throws Exception {

        // Initialize SCTP
        this.initSCTP();

        // Initialize M3UA first
        this.initM3UA();

        // Initialize SCCP
        this.initSCCP();
    }

    public static MOGW getInstance() {
        if (instance == null) {
            instance = new MOGW();
        }
        return instance;
    }

    public MOGW() {
        try {
            initializeStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSCTP() throws Exception {
        this.sctpManagement = new ManagementImpl(MOGWConfig.getInstance().getAPP_NAME(), MOGWConfig.getInstance().getSCTP_CONFIG_FILE());
        this.sctpManagement.setSingleThread(true);
        this.sctpManagement.setConnectDelay(10000);
        this.sctpManagement.startNew();
    }

    private void initM3UA() throws Exception {
        this.serverM3UAMgmt = new M3UAManagementImpl(MOGWConfig.getInstance().getAPP_NAME(), MOGWConfig.getInstance().getM3UA_CONFIG_FILE());
        this.serverM3UAMgmt.setTransportManagement(this.sctpManagement);
        this.serverM3UAMgmt.startNew();
    }

    private void initSCCP() throws Exception {
        this.sccpStack = new SccpStackImpl(MOGWConfig.getInstance().getAPP_NAME(), MOGWConfig.getInstance().getAPP_TYPE(),
                MOGWConfig.getInstance().getSCCP_MANAGEMENT_CONFIG_FILE(), MOGWConfig.getInstance().getSCCP_ROUTER_CONFIG_FILE(),
                MOGWConfig.getInstance().getSCCP_RESOURCE_CONFIG_FILE(), MOGWConfig.getInstance().getOWN_GT(), MOGWConfig.getInstance().getMO_PROCESS_LIST());
        this.sccpStack.setMtp3UserPart(1, this.serverM3UAMgmt);
        this.sccpStack.startNew();
    }

    public static void main(String args[]) throws Exception {

        PropertyConfigurator.configure("../etc/log4j.conf");
        logger = Logger.getLogger(MOGW.class.getSimpleName());
//        MOGW.getInstance();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.viettel.vsmsc.gateway.gw:type=MOGW");
        mbs.registerMBean(MOGW.getInstance(), name);
//        MOGW.getInstance().getSccpRuleConfig();
//        MOGW.getInstance().getSccpAddressConfig();
//        MOGW.getInstance().getRemoteSpcsResource();

//        int counter = 0;
//        while (counter < 10) {
//            MOGW.getInstance().getM3uaASPFactoryList();
//            System.out.println("-----------------------------------------\n\n");
//            counter++;
//            Thread.sleep(10000);
//        }

//        MOGW.getInstance().getM3UAConfig();
//        MOGW.getInstance().getM3uaASPFactoryList();
    }

    public String getAPP_TYPE() {
        return MOGWConfig.getInstance().getAPP_TYPE();
    }

    public int getAPP_ID() {
        return MOGWConfig.getInstance().getAPP_ID();
    }

    public String getAPP_NAME() {
        return MOGWConfig.getInstance().getAPP_NAME();
    }

    public String getSCTP_CONFIG_FILE_Path() {
        return MOGWConfig.getInstance().getSCTP_CONFIG_FILE();
    }

    public String getM3UA_CONFIG_FILE_Path() {
        return MOGWConfig.getInstance().getM3UA_CONFIG_FILE();
    }

    public String getSCCP_ROUTER_CONFIG_FILE_Path() {
        return MOGWConfig.getInstance().getSCCP_ROUTER_CONFIG_FILE();
    }

    public String getSCCP_RESOURCE_CONFIG_FILE_Path() {
        return MOGWConfig.getInstance().getSCCP_RESOURCE_CONFIG_FILE();
    }

    class WorkerThreadFactory implements ThreadFactory {

        private int counter = 0;
        private String prefix = "";

        public WorkerThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, prefix + "-" + counter++);
        }
    }

    /**
     * Lay tat ca SCCP Rule config.
     */
    public String getSccpRuleConfig() {
//        this.serverM3UAMgmt.get
        Map<Integer, Rule> rules = this.sccpStack.getRouter().getRules();
        StringBuilder allRule = new StringBuilder("");
        allRule.append("ID;GTI;AI;SSN;SPC;GT;PID;SID\n");
        if (rules != null && !rules.isEmpty()) {
            for (Map.Entry<Integer, Rule> e : rules.entrySet()) {
                Rule r = e.getValue();
                allRule.append(e.getKey()).
                        append(";").append(r.getPattern().getAddressIndicator().getGlobalTitleIndicator().getValue()).
                        append(";").append(r.getPattern().getAddressIndicator().getValue()).
                        append(";").append(r.getPattern().getSubsystemNumber()).
                        append(";").append(r.getPattern().getSignalingPointCode()).
                        append(";").append(r.getPattern().getGlobalTitle().toString()).
                        append(";").append(r.getPrimaryAddressId()).
                        append(";").append(r.getSecondaryAddressId()).
                        append("\n");
            }
//            System.out.println(allRule.toString());
        }
        return allRule.toString();
    }

    /**
     * Lay tat ca SCCP Address config.
     */
    public String getSccpAddressConfig() {
        StringBuilder allAddress = new StringBuilder("");
        allAddress.append("ID;AI;SSN;SPC;GT\n");
        Map<Integer, SccpAddress> address = this.sccpStack.getRouter().getRoutingAddresses();
        if (address != null && !address.isEmpty()) {
            for (Map.Entry<Integer, SccpAddress> e : address.entrySet()) {
                SccpAddress a = e.getValue();
                allAddress.append(e.getKey()).
                        append(";").append(a.getAddressIndicator().getValue()).
                        append(";").append(a.getSubsystemNumber()).
                        append(";").append(a.getSignalingPointCode()).
                        append(";").append(a.getGlobalTitle().toString()).
                        append("\n");
            }
//            System.out.println(allAddress.toString());
        }
        return allAddress.toString();
    }

    /**
     * Lay MTP3 ServiceAccessPoint.
     */
    public String getSccpSap() {
        StringBuilder allSap = new StringBuilder("");
        allSap.append("SAPID;MTP3ID;OPC;NI;MTP3DestID,FirstDPC,LastDPC,FirstSLS,LastSLS,SLSMask\n");
        Map<Integer, Mtp3ServiceAccessPoint> allMtp3 = this.sccpStack.getRouter().getMtp3ServiceAccessPoints();
        if (allMtp3 != null && !allMtp3.isEmpty()) {
            for (Map.Entry<Integer, Mtp3ServiceAccessPoint> e : allMtp3.entrySet()) {
                Mtp3ServiceAccessPoint sap = e.getValue();
                allSap.append(e.getKey()).
                        append(";").append(sap.getMtp3Id()).
                        append(";").append(sap.getOpc()).
                        append(";").append(sap.getNi());
                StringBuilder tmpDest = new StringBuilder("");
                Map<Integer, Mtp3Destination> allDest = sap.getMtp3Destinations();
                if (allDest != null && !allDest.isEmpty()) {
                    for (Map.Entry<Integer, Mtp3Destination> d : allDest.entrySet()) {
                        Mtp3Destination dest = d.getValue();
                        tmpDest.append(d.getKey()).
                                append(",").append(dest.getFirstDpc()).
                                append(",").append(dest.getLastDpc()).
                                append(",").append(dest.getFirstSls()).
                                append(",").append(dest.getLastSls()).
                                append(",").append(dest.getSlsMask()).
                                append(":");
                    }
                }
//                allSap.append(";").append(tmpDest).append("\n");
                allSap.append(tmpDest.toString().endsWith(":") ? ";".concat(tmpDest.toString().substring(0, tmpDest.toString().length() - 1)) : "").append("\n");

            }
//            System.out.println(allSap.toString());
        }
        return allSap.toString();
    }

    /**
     * Lay Remote Signaling PointCode - SCCP Resource.
     */
    public String getRemoteSpcsResource() {
        StringBuilder allResource = new StringBuilder("");
        allResource.append("ID;RSPC;RSPCFlag;Mask\n");
        Map<Integer, RemoteSignalingPointCode> reSpcs = this.sccpStack.getSccpResource().getRemoteSpcs();
        if (reSpcs != null && !reSpcs.isEmpty()) {
            for (Map.Entry<Integer, RemoteSignalingPointCode> e : reSpcs.entrySet()) {
                RemoteSignalingPointCode re = e.getValue();
                allResource.append(e.getKey()).
                        append(";").append(e.getValue().getRemoteSpc()).
                        append(";").append(e.getValue().getRemoteSpcFlag()).
                        append(";").append(e.getValue().getMask()).
                        append("\n");
            }
//            System.out.println(allResource.toString());
        }

        return allResource.toString();
    }

    /**
     * Lay Remote Subsystem Number - SCCP Resource.
     */
    public String getRemoteSsnsResource() {
        StringBuilder allSsns = new StringBuilder("");
        allSsns.append("ID;RSPC;RSSN;RSSNFlag;MarkProhibitedWhenSpcResuming\n");
        Map<Integer, RemoteSubSystem> reSsns = this.sccpStack.getSccpResource().getRemoteSsns();

        if (reSsns != null && !reSsns.isEmpty()) {
            for (Map.Entry<Integer, RemoteSubSystem> e : reSsns.entrySet()) {
                RemoteSubSystem re = e.getValue();
                allSsns.append(e.getKey()).
                        append(";").append(e.getValue().getRemoteSpc()).
                        append(";").append(e.getValue().getRemoteSsn()).
                        append(";").append(e.getValue().getRemoteSsnFlag()).
                        append(";").append(e.getValue().getMarkProhibitedWhenSpcResuming()).
                        append("\n");
            }
//            System.out.println(allSsns.toString());
        }
        return allSsns.toString();
    }

    /**
     * Lay toan bo ASP Factory + Trang thai cac ASP.
     */
    public String getM3uaASPFactoryListOld() {
        StringBuilder allASP = new StringBuilder("");
        allASP.append("ASPFactoryID;ASPFactoryName;AssociationName;HostAddress;HostPort;PeerAddress;PeerPort;IpChannelType;AssociationType;ServerName;ASPName,ASPState\n");
        List<AspFactory> aspList = this.serverM3UAMgmt.getAspfactories();
        if (aspList != null && !aspList.isEmpty()) {
            for (AspFactory asp : aspList) {
//                StringBuilder tmpAsp = new StringBuilder("");
                allASP.append(asp.getAspid().getAspId()).
                        append(";").append(asp.getName()).
                        append(";").append(asp.getAssociation().getName()).
                        append(";").append(asp.getAssociation().getHostAddress()).
                        append(";").append(asp.getAssociation().getHostPort()).
                        append(";").append(asp.getAssociation().getPeerAddress()).
                        append(";").append(asp.getAssociation().getPeerPort()).
                        append(";").append(asp.getAssociation().getIpChannelType()).
                        append(";").append(asp.getAssociation().getAssociationType().getType()).
                        append(";").append(asp.getAssociation().getServerName());
                List<Asp> asps = asp.getAspList();
                StringBuilder tmp = new StringBuilder("");
                if (asps != null && !asps.isEmpty()) {
                    for (Asp as : asps) {
                        tmp.append(as.getName()).
                                append(",").append(as.getState().getName()).
                                append(":");
                    }
                }
                allASP.append(tmp.toString().endsWith(":") ? ";".concat(tmp.toString().substring(0, tmp.toString().length() - 1)) : "").append("\n");
            }
            System.out.println(allASP.toString());
        }
        return allASP.toString();
    }

    public String getM3uaASPFactoryList() {
        StringBuilder allASP = new StringBuilder("");
        allASP.append("ASPFactoryID;ASPFactoryName;AssociationName;HostAddress;HostPort;PeerAddress;PeerPort;IpChannelType;AssociationType;ASPState;ServerName\n");
        List<AspFactory> aspList = this.serverM3UAMgmt.getAspfactories();
        Map<String, String> allSctpServers = getServers();
        if (aspList != null && !aspList.isEmpty()) {
            for (AspFactory aspF : aspList) {
                allASP.append(aspF.getAspid().getAspId()).
                        append(";").append(aspF.getName()).
                        append(";").append(aspF.getAssociation().getName()).
                        append(";").append(aspF.getAssociation().getAssociationType().getType().equals(AssociationType.SERVER.getType()) ? allSctpServers.get(aspF.getAssociation().getServerName()).split("/")[0] : aspF.getAssociation().getHostAddress()).
                        append(";").append(aspF.getAssociation().getAssociationType().getType().equals(AssociationType.SERVER.getType()) ? allSctpServers.get(aspF.getAssociation().getServerName()).split("/")[1] : aspF.getAssociation().getHostPort()).
                        append(";").append(aspF.getAssociation().getPeerAddress()).
                        append(";").append(aspF.getAssociation().getPeerPort()).
                        append(";").append(aspF.getAssociation().getIpChannelType()).
                        append(";").append(aspF.getAssociation().getAssociationType().getType());

                List<Asp> asps = aspF.getAspList();
                StringBuilder tmp = new StringBuilder("");
                if (asps != null && !asps.isEmpty()) {
                    for (Asp as : asps) {
                        tmp.append(as.getState().getName()).append(":");
                    }
                }
                allASP.append(tmp.toString().endsWith(":") ? ";".concat(tmp.toString().substring(0, tmp.toString().length() - 1)) : "").
                        append(";").append(aspF.getAssociation().getServerName()).append(";END\n");
            }
//            System.out.println(allASP.toString());
        }
        return allASP.toString();
    }

    /**
     * Lay toan bo AS List - M3UA.
     */
    public String getM3uaASList() {
        StringBuilder allAS = new StringBuilder("");
        allAS.append("ASName;ASState;MinAspActiveForLb;Functionality;ExchangeType;IpspType;TrafficModeType;DefaultTrafficModeType;RoutingContext(s);ASPName(s)\n");
        List<As> asList = this.serverM3UAMgmt.getAppServers();
        if (asList != null && !asList.isEmpty()) {
            for (As as : asList) {
                allAS.append(as.getName()).
                        append(";").append(as.getState().getName()).
                        append(";").append(as.getMinAspActiveForLb()).
                        append(";").append(as.getFunctionality().getType()).
                        append(";").append(as.getExchangeType().getType()).
                        append(";").append(as.getIpspType().getType()).
                        append(";").append(as.getTrafficModeType().getMode()).
                        append(";").append(as.getDefaultTrafficModeType().getMode());
                long[] rcs = as.getRoutingContext().getRoutingContexts();
                StringBuilder rcTmp = new StringBuilder("");
                if (rcs != null && rcs.length > 0) {
                    for (long l : rcs) {
                        rcTmp.append(l).append(":");
                    }
                }
                allAS.append(rcTmp.toString().endsWith(":") ? ";".concat(rcTmp.toString().substring(0, rcTmp.toString().length() - 1)) : "");
                List<Asp> asps = as.getAspList();
                StringBuilder aspTmp = new StringBuilder("");
                if (asps != null && !asps.isEmpty()) {
                    for (Asp asp : asps) {
                        aspTmp.append(asp.getName()).append(":");
                    }
                }
                allAS.append(aspTmp.toString().endsWith(":") ? ";".concat(aspTmp.toString().substring(0, aspTmp.toString().length() - 1)) : "").append("\n");
            }
//            System.out.println(allAS.toString());
        }
        return allAS.toString();
    }

    /**
     * Lay tat ca cac M3UA route.
     */
    public String getM3UARoutes() {

        StringBuilder allRoutes = new StringBuilder("");
        allRoutes.append("Key;ASName(s)\n");
        Map<String, As[]> routes = this.serverM3UAMgmt.getRoute();
        if (routes != null && !routes.isEmpty()) {
            for (Map.Entry<String, As[]> e : routes.entrySet()) {
                allRoutes.append(e.getKey());
                As[] ass = e.getValue();
                StringBuilder asTmp = new StringBuilder("");
                if (ass != null && ass.length > 0) {
                    for (As as : ass) {
                        if (as != null) {
                            asTmp.append(as.getName()).append(":");
                        }
                    }
                }
                allRoutes.append(asTmp.toString().endsWith(":") ? ";".concat(asTmp.toString().substring(0, asTmp.toString().length() - 1)) : "").append("\n");

            }
//            System.out.println(allRoutes.toString());
        }

        return allRoutes.toString();
    }

    public String getSCTPServers() {
        StringBuilder allServers = new StringBuilder("");
        allServers.append("ServerName;HostAddress;HostPort\n");
        List<Server> servers = this.sctpManagement.getServers();
        if (servers != null && !servers.isEmpty()) {
            for (Server s : servers) {
                allServers.append(s.getName()).
                        append(";").append(s.getHostAddress()).
                        append(";").append(s.getHostport()).
                        append("\n");
            }
//            System.out.println(allServers.toString());
        }

        return allServers.toString();
    }

    private Map<String, String> getServers() {
        Map<String, String> allServers = new HashMap<String, String>();
        List<Server> servers = this.sctpManagement.getServers();
        if (servers != null && !servers.isEmpty()) {
            for (Server s : servers) {
                allServers.put(s.getName(), s.getHostAddress().concat("/").concat(String.valueOf(s.getHostport())));
            }
        }

        return allServers;

    }
}
