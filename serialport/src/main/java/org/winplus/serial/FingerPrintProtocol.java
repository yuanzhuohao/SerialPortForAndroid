package org.winplus.serial;

public class FingerPrintProtocol {	
	static char s_forhead[] = {0xF1,0x1F,0xE2,0x2E,0xB6,0x6B,0xA8,0x8A};
	//发送的数据长度
	static int FP_protocol_get_data_area_length(FPSendT send)
	{
	    int len = 0;
	    FP_cmd_type fp_cmd_type =new FP_cmd_type();
	    if(FP_cmd_type.cmd_fingerprint == (send.cmd_type)) 
	    {
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_start){
	            len = 1;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_result){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_save_start){
	            len = 2;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_save_result){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_cancel){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_update_start){
	            len = 2;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_update_result){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_match_start){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_match_result){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_delete_start){
	            len = 3;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_delete_result){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_is_fp_id_exist){
	            len = 2;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_get_store_info){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_is_touch_sensor){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_verify_start){
	            len = 0;
	        }
	        if(send.cmd_word == FP_fp_cmd_word.fp_enroll_verify_result){
	            len = 0;
	        }
	    } 
	    else if(FP_cmd_type.cmd_system == (send.cmd_type)) 
	    {
	        if(send.cmd_word == FP_sys_cmd_word.sys_set_passwd){
	            len = 4;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_reset){
	            len = 0;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_get_count){
	            len = 0;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_get_gain){
	            len = 0;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_get_valve){
	            len = 0;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_sleep){
	            len = 1;
	        }
	        if(send.cmd_word == FP_sys_cmd_word.sys_set_enroll_max_num){
	            len = 1;
	        }

	    } 
	    else if(FP_cmd_type.cmd_maintenance == (send.cmd_type)) 
	    {
	        if(send.cmd_word == FP_mtnce_cmd_word.maintenance_read_id){
	            len = 0;
	        	}
	        if(send.cmd_word == FP_mtnce_cmd_word.maintenance_heart_beat){
	            len = 0;
	        }
	        if(send.cmd_word == FP_mtnce_cmd_word.maintenance_set_baudrate){
	            len = 4;
	        }
	        if(send.cmd_word == FP_mtnce_cmd_word.maintenance_set_com_passwd){
	            len = 4;
	        }
	   }

	    return len;
	}

	public  int FP_protocol_get_recv_data_length(FPRecvT recv)
	{
	    short length = recv.frame_head.length;
	    length = (short)(((0xff & length)<<8)+((0xff00 & length) >> 8));
	    
	    return length - 11;
	}
	
	public byte FP_protocol_get_checksum(byte data[], int length)
	{
	    int i = 0;
	     byte sum = 0;
	    
	    for(i = 0; i < length; i++)
	        sum += data[i];

	    return (byte)((~sum)+1);
	}
	
	public int FP_protocol_checkout_recv_head_checksum(FPFrameHeadT head)
	{
	    byte checksum = FP_protocol_get_checksum(head.forhead, 10);
	    if(head.checksum != checksum)
	        return FingerPintType.FP_PROTOCOL_UART_HEAD_CHECKSUM_ERROR;
	    else
	        return FingerPintType.FP_OK;
	}
	
	public int FP_protocol_checkout_recv_data_checksum(FPRecvT recv)
	{
	    byte checksum;
	    
	    short length = recv.frame_head.length;
	    length = (short)(((0xff & length)<<8)+((0xff00 & length) >> 8));
	    
	    checksum = FP_protocol_get_checksum(recv.frame_head.forhead, 10);
	    
	   /* if(*((U8Bit *)recv + sizeof(FP_frame_head_t) + length - 1) != checksum)
	        return FP_PROTOCOL_DATA_CHECKSUM_ERROR;
	    else*/
	        return FingerPintType.FP_OK;
	}
	
	public void FP_protocol_get_frame_head(FPFrameHeadT head,  short length)
	{
	    /*
	     * checksum
	     */
	    head.checksum = FP_protocol_get_checksum(head.forhead, 10);
	}

}
