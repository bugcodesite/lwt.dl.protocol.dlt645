package org.lwt.dl.protocol.dlt645;
public class Dlt645{
public static String operatorcode = "00000000";//操作者代码
	public static String password = "00000000";    //密码 
	protected static String encode_dlt645_2007(String devicecode, int c, String hexdata) {
		byte[] data=hexdata==null?new byte[0]:funs.fromhex(hexdata);
		String tdev="0000000000000"+devicecode;
		byte[] addr=funs.fromhex(tdev.substring(tdev.length()-12));
		int dl=data.length;
		for(int i=0;i<dl/2;i++) {
			byte b=(byte) (data[i]+0x33);
			data[i]=(byte) (data[dl-i-1]+0x33);
			data[dl-i-1]=b;
		}
		if(dl%2>0) {
			data[dl/2]+=0x33;
		}
		int fenum=4;
		int cc=0x68+0x68+c+dl;
		byte[] r=new byte[fenum+dl+12];
		for(int i=0;i<fenum;i++) {
			r[i]=(byte) 0xfe;
		}
		r[fenum+0]=r[fenum+7]=0x68;
		for(int i=0;i<6;i++) {
			r[fenum+6-i]=addr[i];
			cc+=addr[i];
		}
		r[fenum+8]=(byte) c;
		r[fenum+9]=(byte) dl;
		for(int i=0;i<data.length;i++) {
			r[fenum+10+i]=data[i];
			cc+=data[i];
		}
		r[fenum+dl+10]=(byte)cc;
		r[fenum+dl+11]=0x16;
		return funs.tohex(r);
	}
	public static String read(String devicecode,String di) {
		return encode_dlt645_2007(devicecode,0x11,di);
	}
	/**
		可设置参数
		04	00	0B	01	DD hh	2	日时	*	*	每月结算日
		04	09	0B	05	X.XXX	2		*	*	功率因数阈值
		04	09	0B	01	NN.NNNN	3	kW	*	*	过载事件有功功率触发下限
		04	09	0B	03	NN.NNNN	3	kW	*	*	夜间过载功率阈值
		04	09	0B	04	NN.NNNN	3	kW	*	*	功率波动阈值
		04	00	01	01	YYMMDDWW	4	年月日星期	*	*	日期及星期
		04	00	01	02	hhmmss	3	时分秒	*	*	时间
		04	09	0B	07	hhmm	2	时分	*	*	白天起始时间
		04	09	0B	06	hhmm	2	时分	*	*	夜晚起始时间
		04	09	0B	02	ss	1	秒	*	*	过载判定时长
		04	09	0B	08	ss	1	秒	*	*	功率/功率因数波动判定时长

	 * @param devicecode
	 * @param di
	 * @param dv
	 * @return
	 */
	public static String write(String devicecode,String di,String dv) {
		return encode_dlt645_2007(devicecode,0x14,dv+operatorcode+password+di);
	}
	public static String setOpen(String devicecode,String di,boolean toOpen) {
		String dtms="251220153238";
		return encode_dlt645_2007(devicecode,0x1C,dtms+(toOpen?"001C":"001A")+operatorcode+password);
	}
	public static String setWarn(String devicecode,String di,boolean toWarn) {
		String dtms="251220153238";
		return encode_dlt645_2007(devicecode,0x1C,dtms+(toWarn?"002A":"002B")+operatorcode+password);
	}
	public static String setDate(String devicecode,String di,String strDate) {
		String dtms="";//年月日周
		String strdtm=strDate;
		java.util.Calendar cl=java.util.Calendar.getInstance();
		//cl.setFirstDayOfWeek(java.util.Calendar.MONDAY);
		if(strdtm!=null) {
			try {
				cl.setTime(java.text.DateFormat.getDateInstance().parse(strdtm));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		dtms=new java.text.SimpleDateFormat("yyMMdd").format(cl.getTime())+("0"+(cl.get(java.util.Calendar.DAY_OF_WEEK)+1));
		return encode_dlt645_2007(devicecode,0x14,dtms+" "+operatorcode+password+" 04000101");
	}
	public static String setTime(String devicecode,String di,String strTime) {
		String dtms="";//时分秒
		String strdtm=strTime;
		java.util.Calendar cl=java.util.Calendar.getInstance();
		if(strdtm!=null) {
			strdtm=strdtm.replace(":", "");
			if(strdtm.length()==6) {
				dtms=strdtm;
			}else {
				//格式错误
				dtms=new java.text.SimpleDateFormat("hhmmss").format(cl.getTime());
			}
		}else {
			dtms=new java.text.SimpleDateFormat("hhmmss").format(cl.getTime());
		}
		return encode_dlt645_2007(devicecode,0x14,dtms+" "+operatorcode+password+" 04000102");
	}
	//清华大学扩展指令
	
	/**
	 * 04	00	0B	01	DD hh	2	日时	*	*	每月结算日
	 * @param devicecode
	 * @param day
	 * @param hour
	 * @return
	 */
	public static String setSettleDay(String devicecode,int day,int hour) {
		String hh="0"+(hour%60);
		hh=hh.substring(hh.length()-2);
		String dd="0"+(day%29);//不能大于28
		dd=dd.substring(dd.length()-2);
		return encode_dlt645_2007(devicecode,0x14,hh+dd+" "+operatorcode+password+"04000B01");
	}
	/**
	 * 04	09	0B	05	X.XXX	2		*	*	功率因数阈值
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setPowerFactorLimit(String devicecode,double val) {
		String dv="0000"+(Math.round(val*1000)%10000);
		dv=dv.substring(dv.length()-4);
		return encode_dlt645_2007(devicecode,0x14,dv+" "+operatorcode+password+"04090B05");
	}
	/**
	 * 04	09	0B	01	NN.NNNN	3	kW	*	*	过载事件有功功率触发下限
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setMinOverloadPower(String devicecode,double val) {
		String dv="000000"+(Math.round(val*10000)%1000000);
		dv=dv.substring(dv.length()-6);
		return encode_dlt645_2007(devicecode,0x14,dv+" "+operatorcode+password+"04090B01");
	}
	/**
	 * 04	09	0B	03	NN.NNNN	3	kW	*	*	夜间过载功率阈值
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setNightOverloadPower(String devicecode,double val) {
		String dv="000000"+(Math.round(val*10000)%1000000);
		dv=dv.substring(dv.length()-6);
		return encode_dlt645_2007(devicecode,0x14,dv+" "+operatorcode+password+"04090B03");
	}
	/**
	 * 04	09	0B	04	NN.NNNN	3	kW	*	*	功率波动阈值
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setMaxPowerFloat(String devicecode,double val) {
		String dv="000000"+(Math.round(val*10000)%1000000);
		dv=dv.substring(dv.length()-6);
		return encode_dlt645_2007(devicecode,0x14,dv+" "+operatorcode+password+"04090B04");
	}
	
	/**
	 * 04	09	0B	02	ss	1	秒	*	*	过载判定时长
	 * @param devicecode
	 * @param day
	 * @param hour
	 * @return
	 */
	public static String setOverloadPowerPeroid(String devicecode,int val) {
		String hval="0"+(val%100);
		hval=hval.substring(hval.length()-2);
		return encode_dlt645_2007(devicecode,0x14,hval+" "+operatorcode+password+"04090B02");
	}
	/**
	 * 04	09	0B	08	ss	1	秒	*	*	功率/功率因数波动判定时长
	 * @param devicecode
	 * @param day
	 * @param hour
	 * @return
	 */
	public static String setWavePowerTime(String devicecode,int val) {
		String hval="0"+(val%100);
		hval=hval.substring(hval.length()-2);
		return encode_dlt645_2007(devicecode,0x14,hval+" "+operatorcode+password+"04090B08");
	}
	/**
	 * 04	09	0B	07	hhmm	2	时分	*	*	白天起始时间
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setDayStartTime(String devicecode,int h,int m) {
		String hh="0"+(h%24);
		hh=hh.substring(hh.length()-2);
		String mm="0"+(m%60);//不能大于28
		mm=mm.substring(mm.length()-2);
		return encode_dlt645_2007(devicecode,0x14,mm+hh+" "+operatorcode+password+"04090B07");
	}
	/**
	 * 04	09	0B	06	hhmm	2	时分	*	*	夜晚起始时间
	 * @param devicecode
	 * @param val
	 * @return
	 */
	public static String setNightStartTime(String devicecode,int h,int m) {
		String hh="0"+(h%24);
		hh=hh.substring(hh.length()-2);
		String mm="0"+(m%60);//不能大于28
		mm=mm.substring(mm.length()-2);
		return encode_dlt645_2007(devicecode,0x14,mm+hh+" "+operatorcode+password+"04090B06");
	}

	public static Map<String, String> decode_dlt645_2007(String hexdata) {
		//从二进制数据解析，将所有信息作为参数返回，其中devicecode 必须，意指设备地址
		if(null==hexdata) {
			return null;
		}
		//将十六进制字串转换成字节数组，如
		//String str="FE FE FE FE 68 20 64 31 32 03 00 68 93 06 53 97 64 65 36 33 6F 16"//转换前
		//Byte[] data=[0xFE,0xFE,0xFE,0xFE,0x68,0x20,0x64,0x31,0x32,0x03,0x00,0x68,0x93,0x06,0x53,0x97,0x64,0x65,0x36,0x33,0x6F,0x16];//转换后
		int offset=0;
		byte[] data=funs.fromhex(hexdata);
		int dl=data.length;
		boolean validdata=true;
		//检查有效性，排除前导FE
		while(data[offset]!=((byte)0x68)) {
			if(data[offset]!=(byte)0xfe) {
				validdata=false;
			}
			offset++;
			dl--;
			if(dl<=0) {
				//数据完了没找到0x68
				return null;
			}
		}
		//检查第二个0x68
		validdata=validdata&&(dl>=12)&&(data[offset+7]==((byte)0x68));
		if(validdata) {
			String devcecode=funs.tohex(data,offset+1,6,true);
			int c=funs.toint(data[offset+8]);
			int dlen=funs.toint(data[offset+9]);
			int cs=funs.checksum(data, offset, offset+dlen+10);
			//校验有效性,个别命令需要忽略校验(命令正确但校验不对)
			boolean cs_valid=(cs==((data[offset+dlen+10]&0xff)));
			String dataarea=dlen>0?funs.tohex33(data,offset+10,dlen,true):"";
			/**
			 * 解析详细数据信息
			 */
			switch(c) {
				case 0x91:
					//抄表返回结果
					return parseData(devcecode,c,dlen,funs.tohex33(data,offset+10,4,true),dataarea,cs_valid);
				default:
					return parseData(devcecode,c,dlen,dataarea,cs_valid);
			}
			
			
		}
		return null;
	}
	
	
	private static Map<String, String> parseData(String devcecode, int c, int dl, String dt, String dataarea,
			boolean cs_valid) {
		Map<String,String> r=new HashMap<String,String>();
		r.put("deviceCode", ""+devcecode);
		r.put("controlCode", ""+c);
		if("00000000".equalsIgnoreCase(dt)) {
			//组合有功总电量
			r.put("电量",dataarea.substring(0,6)+"."+dataarea.substring(6,8));
		}else if("04000101".equalsIgnoreCase(dt)) {
			r.put("日期","20"+dataarea.substring(0,2)+"-"+dataarea.substring(2,4)+"-"+dataarea.substring(4,6));
		}else if("04000102".equalsIgnoreCase(dt)) {
			r.put("时间",dataarea.substring(0,2)+":"+dataarea.substring(2,4)+":"+dataarea.substring(4,6) );
		}else if("02010100".equalsIgnoreCase(dt)) {
			r.put("电压",dataarea.substring(0,3)+"."+dataarea.substring(3,4));						
		}else if("02020100".equalsIgnoreCase(dt)) {
			r.put("电流",dataarea.substring(0,3)+"."+dataarea.substring(3,6));						
		}else if("02030000".equalsIgnoreCase(dt)) {
			r.put("功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
		}else if("02040000".equalsIgnoreCase(dt)) {
			r.put("无功功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
		}else if("02060000".equalsIgnoreCase(dt)) {
			r.put("功率因数",dataarea.substring(0,1)+"."+dataarea.substring(1,4) );
		}else if("0201FF00".equalsIgnoreCase(dt)) {
			if(dl>=10) {
				//三相
				r.put("C电压",dataarea.substring(0,3)+"."+dataarea.substring(3,4));
				r.put("B电压",dataarea.substring(4,7)+"."+dataarea.substring(7,8));
				r.put("A电压",dataarea.substring(8,11)+"."+dataarea.substring(11,12));
				
				r.put("parsenum", "3");
			}else {
				//单相
				r.put("电压",dataarea.substring(0,3)+"."+dataarea.substring(3,4));
			}
		}else if("0202FF00".equalsIgnoreCase(dt)) {
			if(dl>=13) {
				//三相
				r.put("C电流",dataarea.substring(0,3)+"."+dataarea.substring(3,6));
				r.put("B电流",dataarea.substring(6,9)+"."+dataarea.substring(9,12));
				r.put("A电流",dataarea.substring(12,15)+"."+dataarea.substring(15,18));
				
				r.put("parsenum", "3");
			}else {
				//单相
				r.put("电流",dataarea.substring(0,3)+"."+dataarea.substring(3,6));
			}
		}else if("0203FF00".equalsIgnoreCase(dt)) {
			if(dl>=16) {
				//功率
				r.put("C有功功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
				r.put("B有功功率",dataarea.substring(6,8)+"."+dataarea.substring(8,12));
				r.put("A有功功率",dataarea.substring(12,14)+"."+dataarea.substring(14,18));
				r.put("有功功率",dataarea.substring(18,20)+"."+dataarea.substring(20,24));
				r.put("parsenum", "3");
			}else {
				//单相
				r.put("功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
			}
		}else if("0204FF00".equalsIgnoreCase(dt)) {
			if(dl>=16) {
				//功率
				r.put("C无功功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
				r.put("B无功功率",dataarea.substring(6,8)+"."+dataarea.substring(8,12));
				r.put("A无功功率",dataarea.substring(12,14)+"."+dataarea.substring(14,18));
				r.put("无功功率",dataarea.substring(18,20)+"."+dataarea.substring(20,24));
				r.put("parsenum", "3");
			}else {
				//单相
				r.put("无功功率",dataarea.substring(0,2)+"."+dataarea.substring(2,6));
			}
		}else if("0204FF00".equalsIgnoreCase(dt)) {
			if(dl>=12) {
				r.put("C功率因数",dataarea.substring(0,1)+"."+dataarea.substring(1,4));
				r.put("B功率因数",dataarea.substring(4,5)+"."+dataarea.substring(5,8));
				r.put("A功率因数",dataarea.substring(8,9)+"."+dataarea.substring(9,12));
				r.put("功率因数",dataarea.substring(12,13)+"."+dataarea.substring(13,16));
				r.put("parsenum", "3");
			}else {
				r.put("功率因数",dataarea.substring(0,1)+"."+dataarea.substring(1,4) );
			}
		}else if("00FF0000".equalsIgnoreCase(dt)) {
			if(dl>=24) {
				int l=dataarea.length()-8;
				r.put("电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("正向有功电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("反向有功电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("无功电量1",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("无功电量2",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				r.put("parsenum", "3");
			}else {
				r.put("电量",dataarea.substring(0,6)+"."+dataarea.substring(6,8));
			}
		}else if("0000FF00".equalsIgnoreCase(dt)) {
			if(dl>=24) {
				int l=dataarea.length()-8;
				r.put("电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("费率1电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("费率2电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("费率3电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				l-=8;
				r.put("费率4电量",dataarea.substring(l-8,l-2)+"."+dataarea.substring(l-2,l));
				
			}else {
				r.put("电量",dataarea.substring(0,6)+"."+dataarea.substring(6,8));
			}
		}
		return null;
	}
	private static Map<String, String> parseData(String devcecode, int c, int dlen, String dataarea, boolean cs_valid) {
		Map<String,String> r=new HashMap<String,String>();
		r.put("deviceCode", ""+devcecode);
		r.put("controlCode", ""+c);
		
		return r;
	}


	/**
	 * 
	 * @author Administrator
	 *
	 */
	public static class funs{
		protected final static String[] HEXCHARS="0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F".split(",");
		public static final class Arrays{

			public static String last(String[] ss, boolean allocempty) {
				int i=ss==null?-1:ss.length-1;
				while(i>=0) {
					if("".equalsIgnoreCase(ss[i])&&(!allocempty)) {
						i--;
						continue;
					}
					return ss[i];
				}
				return null;
			}
			
		}
		public static int checksum(byte[] data, int spos, int epos) {
			int r=0;
			for(int i=spos;i<epos;i++) {
				r+=0xff&data[i];
			}
			return r&0xff;
		}

		public static Date parsetime(String tm) {
			SimpleDateFormat utcFormater = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");//20180412T034306Z
		    utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {
				return utcFormater.parse(tm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		public static String tohex(byte[] data) {
			if(data==null) {
				return "";
			}
			StringBuffer sb=new StringBuffer();
			for(byte b:data) {
				sb.append(tohex(b));
			}
			return sb.toString();
		}

		public static String tohex(byte b) {
			int iv=b;
			if(iv<0) {
				iv+=256;
			}
			return HEXCHARS[iv>>4]+HEXCHARS[iv&0x0f];
		}
		public static int toint(byte v) {
			int iv=v;
			if(iv<0) {
				iv+=256;
			}
			return iv;
		}
		public static String tohex(int v) {
			int iv=v;
			if(iv<0) {
				iv+=256;
			}
			return HEXCHARS[(iv>>4)]+HEXCHARS[iv&0x0f];
		}
		public static String tohex(byte[] data, int offset, int len, boolean revert) {
			if(data==null) {
				return "";
			}
			StringBuffer sb=new StringBuffer();
			if(revert) {
				for(int i=offset+len-1;i>=offset;i--) {
					sb.append(tohex(data[i]));
				}
			}else {
				for(int i=offset;i<offset+len;i++) {
					sb.append(tohex(data[i]));
				}
			}
			return sb.toString();
		}
		public static String tohex33(byte[] data, int offset, int len, boolean revert) {
			if(data==null) {
				return "";
			}
			StringBuffer sb=new StringBuffer();
			if(revert) {
				for(int i=offset+len-1;i>=offset;i--) {
					sb.append(tohex(data[i]-((byte)0x33)));
				}
			}else {
				for(int i=offset;i<offset+len;i++) {
					sb.append(tohex(data[i]-((byte)0x33)));
				}
			}
			return sb.toString();
		}

		public static double trim(double t, int i) {
			int p=(int)Math.pow(10,i);
			return ((double)Math.round(t*p))/p;
		}

		public static byte[] fromhex(String s) {
			int b=-1;
			int rl=0;
			byte[] r=new byte[s.length()];
			for(int i=0;i<s.length();i++) {
				int c=s.charAt(i);
				if('0'<=c&&c<='9') {
					c-='0';
				}else if('a'<=c&&c<='f') {
					c-='a';
					c+=10;
				}else if('A'<=c&&c<='F') {
					c-='A';
					c+=10;
				}else if(b<0){
					continue;
				}
				if(b<0) {
					b=c;
				}else{
					r[rl]=(byte) ((b<<4)+c);
					rl++;
					b=-1;
				}
			}
			return java.util.Arrays.copyOf(r, rl);
		}

		public static byte[] revert(byte[] buf) {
			int halfl=buf.length/2;
			for(int i=0;i<halfl;i++) {
				byte b=buf[i];
				buf[i]=buf[buf.length-1-i];
				buf[buf.length-1-i]=b;
			}
			return buf;
		}

		public static boolean[] tobin(int f, int c) {
			if(c<1) {
				return new boolean[0];
			}
			int n=f;
			boolean[] r=new boolean[c];
			for(int i=0;i<c;i++) {
				r[i]=(n&1)==1;
				n>>=1;
			}
			return r;
		}
		public static boolean[] tobin(byte[] data) {
			if(data==null) {
				return new boolean[0];
			}
			boolean[] r=new boolean[data.length*8];
			
			for(int i=0;i<data.length;i++) {
				int n=(256+data[i])%256;
				for(int j=0;j<8;j++) {
					r[i*8+j]=(n&1)==1;
					n>>=1;
				}
			}
			return r;
		}
		public static int toint(boolean[] b) {
			int r=0;
			for(int i=b.length-1;i>=0;i--) {
				r=r<<1;
				r+=b[i]?1:0;
			}
			return r;
		}

		public static long multiply_long(long...ls) {
			long r=1;
			for(long l0:ls) {
				r*=l0;
			}
			return r;
		}

		public static String tryutf8string(String s) {
			if(s!=null) {
				try {
					ByteBuffer bb = ByteBuffer.wrap(s.getBytes("iso-8859-1"));
					String s1=""+java.nio.charset.Charset.forName("iso-8859-1").decode(bb);
					if(java.util.Arrays.equals(s1.getBytes(),s.getBytes())) {
						return new String(s.getBytes("iso-8859-1"),"utf-8");
					}
				} catch (Exception e) {
				}
			}
			return s;
		}

		public static void fillbcd(int v, byte[] r, int offset, int len) {
			try {
			int t=v;
			for(int i=0;i<len;i++) {
				int b=t%100;
				t/=100;
				r[offset+i]=(byte)(((b/10)<<4)+b%10);
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		public static int tryToInt(Object v, int defaultvalue) {
			if(null!=v) {
				if(v instanceof Number) {
					return ((Number)v).intValue();
				}
				if(v instanceof String) {
					int r=0;
					String sv = (String)v;
					String[] ssv=sv.split("\\D");
					for(int i=0;i<ssv.length;i++) {
						String sv0=ssv[i];
						if(sv0.length()<3) {
							r*=100;
						}else if(sv0.length()<5) {
							r*=10000;
						}else if(sv0.length()<7) {
							r*=1000000;
						}else{
							r*=1000000;
						}
						try{
							r+=Integer.parseInt(sv0);
						}catch(Exception e1) {
							
						}
						
					}
					return r;
				}
			}
			return defaultvalue;
		}
	}
	public static String zerodel(String dataitems){
		String datas = "";
		for(int i=0;i>dataitems.length();i++){
			if(Integer.parseInt(dataitems.substring(i)) == 0 || "0".equalsIgnoreCase(dataitems.substring(i, i+1))){
				continue;
			}
			datas = dataitems.substring(i);
		}
		return datas;
	}
}
}
