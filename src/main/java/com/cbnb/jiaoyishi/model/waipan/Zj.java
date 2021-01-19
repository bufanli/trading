package com.cbnb.jiaoyishi.model.waipan;

/**
 * @Author: hcf
 * @Description:
 * @Date: Create in 16:54 2021/1/12
 */
public class Zj {
	private String qhgsmc;

	private String wsmc;

	private String qhgszh;

	private String lx;

	private String value;

	@Override
	public String toString() {
		return "{" +
				"'qhgsmc':'" + qhgsmc + "'" +
				", 'wsmc':'" + wsmc + "'" +
				", 'qhgszh':'" + qhgszh + "'" +
				", 'lx':'" + lx + "'" +
				", 'value':'" + value + "'" +
				"}";
	}

	public String getQhgsmc() {
		return qhgsmc;
	}

	public void setQhgsmc(String qhgsmc) {
		this.qhgsmc = qhgsmc;
	}

	public String getWsmc() {
		return wsmc;
	}

	public void setWsmc(String wsmc) {
		this.wsmc = wsmc;
	}

	public String getQhgszh() {
		return qhgszh;
	}

	public void setQhgszh(String qhgszh) {
		this.qhgszh = qhgszh;
	}

	public String getLx() {
		return lx;
	}

	public void setLx(String lx) {
		this.lx = lx;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
