package com.yinhe.protocol.dlt645;

public class Dlt645Base {

	public Dlt645Base(String devicecode, int c, int dlen, String dataarea, boolean cs_valid) {
		deviceCode = devicecode;
		controlCode = c;
		dataLength = dlen;
		hexData = dataarea;
		csValid = cs_valid;

	}

	public static String encode(String devicecode, int controlcode, String hexdata) {
		byte[] data = hexdata == null ? new byte[0] : util.fromhex(hexdata);
		String tdev = "0000000000000" + devicecode;
		byte[] addr = util.fromhex(tdev.substring(tdev.length() - 12));
		int dl = data.length;
		for (int i = 0; i < dl / 2; i++) {
			byte b = (byte) (data[i] + 0x33);
			data[i] = (byte) (data[dl - i - 1] + 0x33);
			data[dl - i - 1] = b;
		}
		if (dl % 2 > 0) {
			data[dl / 2] += 0x33;
		}
		int fenum = 4;
		int cc = 0x68 + 0x68 + (0xff & controlcode) + dl;
		byte[] r = new byte[fenum + dl + 12];
		for (int i = 0; i < fenum; i++) {
			r[i] = (byte) 0xfe;
		}
		r[fenum + 0] = r[fenum + 7] = 0x68;
		for (int i = 0; i < 6; i++) {
			r[fenum + 6 - i] = addr[i];
			cc += addr[i];
		}
		r[fenum + 8] = (byte) controlcode;
		r[fenum + 9] = (byte) dl;
		for (int i = 0; i < data.length; i++) {
			r[fenum + 10 + i] = data[i];
			cc += data[i];
		}
		r[fenum + dl + 10] = (byte) cc;
		r[fenum + dl + 11] = 0x16;
		return util.tohex(r);
	}

	public static Dlt645Base decode(String hexdata) {
		if (null == hexdata||"".equals(hexdata)) {
			return null;
		}
		int offset = 0;
		byte[] data = util.fromhex(hexdata);
		int dl = data.length;
		boolean validdata = true;
		// 检查有效性，排除前导FE
		while (data[offset] != ((byte) 0x68)) {
			/*if (data[offset] != (byte) 0xfe) {
				validdata = false;
			}*/
			offset++;
			dl--;
			if (dl <= 0) {
				// 数据完了没找到0x68
				return null;
			}
		}
		// 检查第二个0x68
		validdata = validdata && (dl >= 12) && (data[offset + 7] == ((byte) 0x68));
		if (validdata) {
			String devicecode = util.tohex(data, offset + 1, 6, true);
			int c = util.toint(data[offset + 8]);
			int dlen = util.toint(data[offset + 9]);
			int cs = util.checksum(data, offset, offset + dlen + 10);
			// 校验有效性,个别命令需要忽略校验(命令正确但校验不对)
			boolean cs_valid = (cs == ((data[offset + dlen + 10] & 0xff)));
			String dataarea = dlen > 0 ? util.tohex33(data, offset + 10, dlen, true) : "";
			return new Dlt645Base(devicecode, c, dlen, dataarea, cs_valid);
		}
		return null;
	}

	@Override
	public String toString() {
		return encode(this.deviceCode, this.controlCode, this.hexData);
	}

	public int getControlCode() {
		return controlCode;
	}

	public void setControlCode(int controlCode) {
		this.controlCode = controlCode;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public boolean getCsValid() {
		return csValid;
	}

	public void setCsValid(boolean csValid) {
		this.csValid = csValid;
	}

	public String getHexData() {
		return hexData;
	}

	public void setHexData(String hexData) {
		this.hexData = hexData;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	private int controlCode;
	private int dataLength;
	private boolean csValid;
	private String hexData;
	private String deviceCode;
}
