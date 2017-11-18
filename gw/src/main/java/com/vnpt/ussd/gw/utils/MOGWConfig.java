/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vnpt.ussd.gw.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
//import utils.Config;

public class MOGWConfig {

    private static Logger logger = Logger.getLogger(MOGWConfig.class.getSimpleName());
    private static final String configfile = "../etc/node.conf";
    private static MOGWConfig instance = null;
//#Thu muc chua file input ma node broadcast se doc
    private String DIR_IMPORT_FILE = "./import/";
    private String DIR_IMPORTED_FILE = "./imported/";
//# So luong message se doc trong 1 lo (BULK)
    private int BULK_MESSAGE = 500;
//# Thoi gian sleep cua node broadcast sau khi gui xong 1 bulk (ms)
    private long BULK_MESSAGE_SLEEP = 1000;
//#Thoi gian dinh ky la bao lau Node se quet file input( theo giay)
    private long TIME_NODE_SCAN_FILE = 10;
//# Thu muc chua file success, day ra theo part cua moi SBB
    private String DIR_SUCCESS_PART = "./success_part/";
//# Thoi gian luu toi da trong hashSuccess(giay)
    private long TIME_PUSH_SUCCES = 5;
//# So luong ban ghi luu toi da trong hashSucess
    private int MAX_NUM_IN_HASH_SUCCESS = 1000;
//Thu muc chua file loi, day ra theo part cua moi SBB
    private String DIR_FAIL_PART = "./fail_part/";
//# Thoi gian luu toi da trong hashFail(giay), tham so nay phuc vu cho day ra file CDR fail
    private long TIME_PUSH_FAIL = 10;
//# So luong ban ghi luu toi da trong hashFail, tham so nay phuc vu cho day ra file CDR fail
    private int MAX_NUM_IN_HASH_FAIL = 500;
//    #CYCLE of TIME RELOALD ALL CONFIG (giay)
    private long TIME_CYCLE_RELOAD_ALL_CONFIG = 30 * 1000;
    private String LIST_MSC_ALLOW;
    private Map<Integer, String> MO_PROCESS_LIST = new HashMap<Integer, String>();
    private String OWN_GT;
    /**
     *  LOAD CONFIG*
     */
    private final String APP_CONFIG_FILE = "../etc/node.conf";
    private String SCTP_CONFIG_FILE;
    private String M3UA_CONFIG_FILE;
    private String SCCP_MANAGEMENT_CONFIG_FILE;
    private String SCCP_ROUTER_CONFIG_FILE;
    private String SCCP_RESOURCE_CONFIG_FILE;
    private String APP_TYPE;
    private String APP_NAME;
    private int APP_ID = 1;

    public synchronized static MOGWConfig getInstance() {
        if (instance == null) {
            instance = new MOGWConfig();
        }
        return instance;

    }

    private MOGWConfig() {
        FileReader r = null;
        Properties config = new Properties();
        try {
            r = new FileReader(APP_CONFIG_FILE);
            config = new Properties();
            config.load(r);
            DIR_IMPORT_FILE = config.getProperty("DIR_IMPORT_FILE", "./import/");
            DIR_IMPORTED_FILE = config.getProperty("DIR_IMPORTED_FILE", "./imported/");

            BULK_MESSAGE = Integer.valueOf(config.getProperty("BULK_MESSAGE", "500"));
            BULK_MESSAGE_SLEEP = Long.valueOf(config.getProperty("BULK_MESSAGE_SLEEP", "1000")); //ms
            TIME_NODE_SCAN_FILE = Long.valueOf(config.getProperty("TIME_NODE_SCAN_FILE", "10")) * 1000;//s
            DIR_SUCCESS_PART = config.getProperty("DIR_SUCCESS_PART", "./success_part/");
            TIME_PUSH_SUCCES = Long.valueOf(config.getProperty("TIME_PUSH_SUCCES", "5")) * 1000;//s
            MAX_NUM_IN_HASH_SUCCESS = Integer.valueOf(config.getProperty("MAX_NUM_IN_HASH_SUCCESS", "500"));
            DIR_FAIL_PART = config.getProperty("DIR_FAIL_PART", "./fail_part/");
            TIME_PUSH_FAIL = Long.valueOf(config.getProperty("TIME_PUSH_FAIL", "10")) * 1000;//s
            MAX_NUM_IN_HASH_FAIL = Integer.valueOf(config.getProperty("MAX_NUM_IN_HASH_FAIL", "500"));
            TIME_CYCLE_RELOAD_ALL_CONFIG = Long.valueOf(config.getProperty("TIME_CYCLE_RELOAD_ALL_CONFIG", "30")) * 1000;//s
            LIST_MSC_ALLOW = config.getProperty("LIST_MSC_ALLOW");

            String moProcessList = config.getProperty("MO_PROCESS_LIST");
            try {
                String[] tmps = moProcessList.split(";");
                if (tmps != null && tmps.length > 0) {
                    for (int i = 1; i <= tmps.length; i++) {
                        MO_PROCESS_LIST.put(i, tmps[i - 1]);
                    }
                }
            } catch (Exception e) {
                logger.error("ERROR get config MO_PROCESS_LIST: ", e);
            }

            OWN_GT = config.getProperty("OWN_GT");

            SCTP_CONFIG_FILE = config.getProperty("SCTP_CONFIG_FILE", "../etc/gw_sctp.xml");
            M3UA_CONFIG_FILE = config.getProperty("M3UA_CONFIG_FILE", "../etc/gw_m3ua.xml");
            SCCP_MANAGEMENT_CONFIG_FILE = config.getProperty("SCCP_MANAGEMENT_CONFIG_FILE", "../etc/gw_management2.xml");
            SCCP_ROUTER_CONFIG_FILE = config.getProperty("SCCP_ROUTER_CONFIG_FILE", "../etc/gw_sccprouter2.xml");
            SCCP_RESOURCE_CONFIG_FILE = config.getProperty("SCCP_RESOURCE_CONFIG_FILE", "../etc/gw_sccpresource2.xml");
            APP_TYPE = config.getProperty("APP_TYPE", "GW");
            APP_NAME = config.getProperty("APP_NAME", "gw");
            APP_ID = Integer.parseInt(config.getProperty("APP_ID", "0"));

        } catch (Exception e) {
            logger.error("error", e);
            System.exit(0);
        } finally {
            try {
                r.close();
                config.clear();
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
//      ########################################################################
//        TimerReloadAllConfig reload = new TimerReloadAllConfig();
//        try {
//            Thread th = new Thread(reload, "PROC_RELOAD_".concat(APP_NAME));
//            th.start();
//        } catch (Exception e) {
//            logger.error(e);
//        }

    }

    class TimerReloadAllConfig implements Runnable {

        @Override
        public void run() {
            FileReader r = null;
            Properties prop = null;
            try {

                r = new FileReader(configfile);
                prop = new Properties();
                prop.load(r);

                while (true) {
                    try {
                        DIR_IMPORT_FILE = prop.getProperty("DIR_IMPORT_FILE", "./import/");
                        DIR_IMPORTED_FILE = prop.getProperty("DIR_IMPORTED_FILE", "./imported/");
                        BULK_MESSAGE = Integer.valueOf(prop.getProperty("BULK_MESSAGE", "500"));
                        BULK_MESSAGE_SLEEP = Long.valueOf(prop.getProperty("BULK_MESSAGE_SLEEP", "1000")); //ms
                        TIME_NODE_SCAN_FILE = Long.valueOf(prop.getProperty("TIME_NODE_SCAN_FILE", "10")) * 1000;//s
                        DIR_SUCCESS_PART = prop.getProperty("DIR_SUCCESS_PART", "./success_part/");
                        TIME_PUSH_SUCCES = Long.valueOf(prop.getProperty("TIME_PUSH_SUCCES", "5")) * 1000;//s
                        MAX_NUM_IN_HASH_SUCCESS = Integer.valueOf(prop.getProperty("MAX_NUM_IN_HASH_SUCCESS", "500"));
                        DIR_FAIL_PART = prop.getProperty("DIR_FAIL_PART", "./fail_part/");
                        TIME_PUSH_FAIL = Long.valueOf(prop.getProperty("TIME_PUSH_FAIL", "10")) * 1000;//s
                        MAX_NUM_IN_HASH_FAIL = Integer.valueOf(prop.getProperty("MAX_NUM_IN_HASH_FAIL", "500"));
                        TIME_CYCLE_RELOAD_ALL_CONFIG = Long.valueOf(prop.getProperty("TIME_CYCLE_RELOAD_ALL_CONFIG", "30")) * 1000;//s
                        LIST_MSC_ALLOW = prop.getProperty("LIST_MSC_ALLOW");
                        Thread.sleep(TIME_CYCLE_RELOAD_ALL_CONFIG);
                    } catch (Exception ex) {
                        logger.error("error:", ex);
                    }
                }
            } catch (Exception ex) {
                logger.error("error:", ex);
            } finally {
                try {
                    r.close();
                    prop.clear();
                    System.exit(0);
                } catch (IOException ex) {
                    logger.error(ex);
                }

            }
        }
    }

    public Map<Integer, String> getMO_PROCESS_LIST() {
        return MO_PROCESS_LIST;
    }

    public String getOWN_GT() {
        return OWN_GT;
    }

    public String getDestGT(int id) {
        return MO_PROCESS_LIST.get(id);
    }

    public String getSCTP_CONFIG_FILE() {
        return SCTP_CONFIG_FILE;
    }

    public void setSCTP_CONFIG_FILE(String SCTP_CONFIG_FILE) {
        this.SCTP_CONFIG_FILE = SCTP_CONFIG_FILE;
    }

    public String getM3UA_CONFIG_FILE() {
        return M3UA_CONFIG_FILE;
    }

    public void setM3UA_CONFIG_FILE(String M3UA_CONFIG_FILE) {
        this.M3UA_CONFIG_FILE = M3UA_CONFIG_FILE;
    }

    public String getSCCP_MANAGEMENT_CONFIG_FILE() {
        return SCCP_MANAGEMENT_CONFIG_FILE;
    }

    public void setSCCP_MANAGEMENT_CONFIG_FILE(String SCCP_MANAGEMENT_CONFIG_FILE) {
        this.SCCP_MANAGEMENT_CONFIG_FILE = SCCP_MANAGEMENT_CONFIG_FILE;
    }

    public String getSCCP_ROUTER_CONFIG_FILE() {
        return SCCP_ROUTER_CONFIG_FILE;
    }

    public void setSCCP_ROUTER_CONFIG_FILE(String SCCP_ROUTER_CONFIG_FILE) {
        this.SCCP_ROUTER_CONFIG_FILE = SCCP_ROUTER_CONFIG_FILE;
    }

    public String getSCCP_RESOURCE_CONFIG_FILE() {
        return SCCP_RESOURCE_CONFIG_FILE;
    }

    public void setSCCP_RESOURCE_CONFIG_FILE(String SCCP_RESOURCE_CONFIG_FILE) {
        this.SCCP_RESOURCE_CONFIG_FILE = SCCP_RESOURCE_CONFIG_FILE;
    }

    public String getAPP_TYPE() {
        return APP_TYPE;
    }

    public void setAPP_TYPE(String APP_TYPE) {
        this.APP_TYPE = APP_TYPE;
    }

    public String getAPP_NAME() {
        return APP_NAME;
    }

    public void setAPP_NAME(String APP_NAME) {
        this.APP_NAME = APP_NAME;
    }

    public int getAPP_ID() {
        return APP_ID;
    }

    public void setAPP_ID(int APP_ID) {
        this.APP_ID = APP_ID;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        MOGWConfig.logger = logger;
    }

    public String getDIR_IMPORT_FILE() {
        return DIR_IMPORT_FILE;
    }

    public void setDIR_IMPORT_FILE(String DIR_IMPORT_FILE) {
        this.DIR_IMPORT_FILE = DIR_IMPORT_FILE;
    }

    public String getDIR_IMPORTED_FILE() {
        return DIR_IMPORTED_FILE;
    }

    public void setDIR_IMPORTED_FILE(String DIR_IMPORTED_FILE) {
        this.DIR_IMPORTED_FILE = DIR_IMPORTED_FILE;
    }

    public int getBULK_MESSAGE() {
        return BULK_MESSAGE;
    }

    public void setBULK_MESSAGE(int BULK_MESSAGE) {
        this.BULK_MESSAGE = BULK_MESSAGE;
    }

    public long getBULK_MESSAGE_SLEEP() {
        return BULK_MESSAGE_SLEEP;
    }

    public void setBULK_MESSAGE_SLEEP(long BULK_MESSAGE_SLEEP) {
        this.BULK_MESSAGE_SLEEP = BULK_MESSAGE_SLEEP;
    }

    public long getTIME_NODE_SCAN_FILE() {
        return TIME_NODE_SCAN_FILE;
    }

    public void setTIME_NODE_SCAN_FILE(long TIME_NODE_SCAN_FILE) {
        this.TIME_NODE_SCAN_FILE = TIME_NODE_SCAN_FILE;
    }

    public String getDIR_SUCCESS_PART() {
        return DIR_SUCCESS_PART;
    }

    public void setDIR_SUCCESS_PART(String DIR_SUCCESS_PART) {
        this.DIR_SUCCESS_PART = DIR_SUCCESS_PART;
    }

    public long getTIME_PUSH_SUCCES() {
        return TIME_PUSH_SUCCES;
    }

    public void setTIME_PUSH_SUCCES(long TIME_PUSH_SUCCES) {
        this.TIME_PUSH_SUCCES = TIME_PUSH_SUCCES;
    }

    public int getMAX_NUM_IN_HASH_SUCCESS() {
        return MAX_NUM_IN_HASH_SUCCESS;
    }

    public void setMAX_NUM_IN_HASH_SUCCESS(int MAX_NUM_IN_HASH_SUCCESS) {
        this.MAX_NUM_IN_HASH_SUCCESS = MAX_NUM_IN_HASH_SUCCESS;
    }

    public String getDIR_FAIL_PART() {
        return DIR_FAIL_PART;
    }

    public void setDIR_FAIL_PART(String DIR_FAIL_PART) {
        this.DIR_FAIL_PART = DIR_FAIL_PART;
    }

    public long getTIME_PUSH_FAIL() {
        return TIME_PUSH_FAIL;
    }

    public void setTIME_PUSH_FAIL(long TIME_PUSH_FAIL) {
        this.TIME_PUSH_FAIL = TIME_PUSH_FAIL;
    }

    public int getMAX_NUM_IN_HASH_FAIL() {
        return MAX_NUM_IN_HASH_FAIL;
    }

    public void setMAX_NUM_IN_HASH_FAIL(int MAX_NUM_IN_HASH_FAIL) {
        this.MAX_NUM_IN_HASH_FAIL = MAX_NUM_IN_HASH_FAIL;
    }

    public String getLIST_MSC_ALLOW() {
        return LIST_MSC_ALLOW;
    }

    public void setLIST_MSC_ALLOW(String LIST_MSC_ALLOW) {
        this.LIST_MSC_ALLOW = LIST_MSC_ALLOW;
    }
}