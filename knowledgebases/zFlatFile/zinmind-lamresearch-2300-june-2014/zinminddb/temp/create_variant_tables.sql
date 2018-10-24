drop table D23_SV_UPLIFT_RATE_349
/
create table D23_SV_UPLIFT_RATE_349
(
D23_SV_CUST_REGION char(30) not null,
D23_SV_UPLIFT_RATE double precision not null,
D23_SV_HOURLY_RATE double precision not null
)
/
drop table D23_SV_MODULE_COST_349
/
create table D23_SV_MODULE_COST_349
(
D23_SV_MODULE_TYPE char(30) not null,
D23_SV_MODULE_COST double precision not null,
D23_SV_MODULE_ENGR_HRS double precision not null
)
/
drop table D23_PG_UPLIFT_RATE_349
/
create table D23_PG_UPLIFT_RATE_349
(
D23_SV_CUST_REGION char(30) not null,
D23_SV_UPLIFT_RATE double precision not null,
D23_SV_HOURLY_RATE double precision not null
)
/
drop table D23_PG_MODULE_COST_349
/
create table D23_PG_MODULE_COST_349
(
D23_SV_MODULE_TYPE char(30) not null,
D23_SV_MODULE_COST double precision not null,
D23_SV_MODULE_ENGR_HRS double precision not null
)
/
drop table SV_PRICE_349
/
create table SV_PRICE_349
(
D23_SV_PRODUCT_FAMILY char(10) not null,
D23_SV_REGION_FAMILY char(10) not null,
D23_SV_CUSTOM_SERVICE_ENHAN char(30) not null,
D23_SV_VARIANT char(30) not null
)
/
drop table AL_COST_OPT_TABLE_349
/
create table AL_COST_OPT_TABLE_349
(
T_INDEX char(10) not null,
T_COST double precision not null
)
/
drop table COST_OPT_TABLE_349
/
create table COST_OPT_TABLE_349
(
T_INDEX char(10) not null,
T_COST double precision not null
)
/
drop table D23_BK_PUMP_Q_NQ_349
/
create table D23_BK_PUMP_Q_NQ_349
(
T_BK_PUMP char(30) not null,
T_BK_PUMP_Q_NQ char(30) not null
)
/
drop table D23_TCU_Q_NQ_349
/
create table D23_TCU_Q_NQ_349
(
T_TCU char(30) not null,
T_TCU_Q_NQ char(30) not null
)
/
drop table CUST_OPT_TABLE_349
/
create table CUST_OPT_TABLE_349
(
CUST_NAME char(30) not null,
CUST_OPT char(30) not null
)
/
drop table D23_BP_CB_SIZE_349
/
create table D23_BP_CB_SIZE_349
(
T_BK_PUMP char(30) not null,
T_BK_PUMP_CB char(30) not null
)
/
drop table GIB_ALLOWED_VTM_349
/
create table GIB_ALLOWED_VTM_349
(
D23_GIB_ALLOWED char(01) not null,
D23_ONBOARD_GB_IN_1AND6 char(01) not null,
D23_OFFBOARD_GB_IN_2345 char(01) not null,
D23_OFFBOARD_GB_SAME_VTM char(01) not null
)
/
drop table GIB_ALLOWED_TABLE2_349
/
create table GIB_ALLOWED_TABLE2_349
(
D23_GIB_ALLOWED char(01) not null,
D23_2X2 char(01) not null,
D23_OFFBOARD_GB_SAME char(01) not null,
D23_OFFBOARD_GB_IN_2AND3 char(01) not null,
D23_GAS_BOX_ROOT char(30) not null
)
/
drop table JS_GIB_ALLOWED_349
/
create table JS_GIB_ALLOWED_349
(
D23_OFFBOARD_GB_SAME char(01) not null,
D23_JS_A_FOR_GIB char(10) not null,
D23_JS_B_FOR_GIB char(10) not null,
D23_GIB_ALLOWED char(01) not null
)
/
drop table D23_TCU_CB_SIZE_349
/
create table D23_TCU_CB_SIZE_349
(
T_TCU char(30) not null,
T_TCU_CB char(30) not null,
T_ESC char(30) not null,
T_CHILLER char(30) not null,
T_PM_FAC char(30) not null,
T_FUTURE1 char(30) not null,
T_FUTURE2 char(30) not null,
T_FUTURE3 char(30) not null
)
/
drop table ALLOWED_GASFLOWS_349
/
create table ALLOWED_GASFLOWS_349
(
PM_FAMILY char(30) not null,
ALLOWED_GASFLOW char(07) not null,
GASTYPE char(30) not null
)
/
drop table A_ALLOWED_GASFLOWS_349
/
create table A_ALLOWED_GASFLOWS_349
(
PM_FAMILY char(30) not null,
ALLOWED_GASFLOW char(07) not null,
GASTYPE char(30) not null
)
/
drop table CUST_NAME_TABLE_349
/
create table CUST_NAME_TABLE_349
(
CUST_NAME char(30) not null,
CUST_ID char(10) not null
)
/
drop table PM2GASBOX_DEF_2300_349
/
create table PM2GASBOX_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_3D1_DEF_2300_349
/
create table PM_GB_3D1_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_3D2_DEF_2300_349
/
create table PM_GB_3D2_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_3DX_DEF_349
/
create table PM_GBJS_3DX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_BVLX_DEF_349
/
create table PM_GBJS_BVLX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_BVLX_DEF_349
/
create table PM_GB_BVLX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_DFCX_DEF_349
/
create table PM_GBJS_DFCX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_GAS_FEED_DEF_349
/
create table PM_GB_GAS_FEED_DEF_349
(
T_GF char(10) not null,
T_PM char(10) not null,
T_GB char(06) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GASBOX_DEF_2300_349
/
create table PM_GASBOX_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_WFR char(10) not null,
T_GB char(06) not null,
T_GSPNL char(10) not null,
T_PM char(10) not null,
T_CHAMB char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null
)
/
drop table PM_ICS_GB_DEF_2300_349
/
create table PM_ICS_GB_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_HTD_LINE char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_M3_GB_DEF_2300_349
/
create table PM_M3_GB_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_HTD_LINE char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_M4_GB_DEF_2300_349
/
create table PM_M4_GB_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_HTD_LINE char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_M5_DEF_349
/
create table PM_GBJS_M5_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_M5_DEF_2300_349
/
create table PM_GB_M5_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_METALX_DEF_349
/
create table PM_GBJS_METALX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_HTD_LINE char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_P11_DEF_2300_349
/
create table PM_GB_P11_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_P5_DEF_2300_349
/
create table PM_GB_P5_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_P6_DEF_2300_349
/
create table PM_GB_P6_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_P7_DEF_2300_349
/
create table PM_GB_P7_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GB_PB_DEF_2300_349
/
create table PM_GB_PB_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_POLYX_DEF_349
/
create table PM_GBJS_POLYX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_AC3 char(06) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_S3_GB_DEF_2300_349
/
create table PM_S3_GB_DEF_2300_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_HTD_LINE char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table PM_GBJS_STRIPX_DEF_349
/
create table PM_GBJS_STRIPX_DEF_349
(
T_LINE double precision not null,
T_GAS char(20) not null,
T_FLOW char(10) not null,
T_PM char(10) not null,
T_GSPNL char(10) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null,
T_F4 char(10) not null,
T_F5 char(10) not null
)
/
drop table D23_2X2_349
/
create table D23_2X2_349
(
D23_2X2 char(01) not null,
D23_GASBOX_1AND4 char(10) not null,
D23_GASBOX_2AND3 char(10) not null
)
/
drop table DACP_FLAG_349
/
create table DACP_FLAG_349
(
T_FLAG char(15) not null,
T_PLATFORM char(08) not null,
T_POS1 char(15) not null,
T_POS2 char(15) not null,
T_POS3 char(15) not null,
T_POS4 char(15) not null,
T_POS5 char(15) not null,
T_POS6 char(15) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null
)
/
drop table DACP_FLAG_2_349
/
create table DACP_FLAG_2_349
(
T_FLAG char(15) not null,
T_PLATFORM char(08) not null,
T_POS1 char(15) not null,
T_POS2 char(15) not null,
T_POS3 char(15) not null,
T_POS4 char(15) not null,
T_POS5 char(15) not null,
T_POS6 char(15) not null,
T_F1 char(10) not null,
T_F2 char(10) not null,
T_F3 char(10) not null
)
/
drop table LCR_PRICING_349
/
create table LCR_PRICING_349
(
T_PLATFORM char(08) not null,
T_DELIV_OPTION char(08) not null,
T_CUST_REGION char(08) not null,
T_FACTOR0 double precision not null,
T_FACTOR1 double precision not null,
T_FACTOR2 double precision not null,
T_FACTOR3 double precision not null,
T_FACTOR4 double precision not null
)
/
