package org.winplus.serial;

public class FingerPrintAction {
	public static  int FP_OK   =   0;
	public static  int DEFAULT_TIME_OUT=100;
	int FP_action_getID(FPModuleIDT moduleID, int errorCode)
	{
		FPDataArea data_area;
		FPSendT s_send_p = new FPSendT();
		FPRecvT s_recv_p = new FPRecvT();
	    int ret;
	 /*    get a frame to send 
	    ret = FP_protocol_get_mtnce_read_id_frame(s_send_p);
	    if(FP_OK != ret)
	        return ret;

	     send 
	    ret = FP_protocol_send_mesg(s_send_p, DEFAULT_TIME_OUT);
	    if(FP_OK != ret)
	        return ret;

	     receive the responce frame 
	    ret = FP_protocol_recv_complete_frame(s_recv_p, &data_area, DEFAULT_TIME_OUT);
	    if(FP_OK != ret)
	        return ret;

	     get the useful data 
	    moduleID.ID = data_area.data;
	    moduleID.length = data_area.length;

	     error code 
	    errorCode = FP_action_get_errorCode(s_recv_p.error_code);*/
	    
	    return FP_OK;
	}
}
