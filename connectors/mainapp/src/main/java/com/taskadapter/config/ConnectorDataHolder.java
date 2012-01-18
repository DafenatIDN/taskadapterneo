package com.taskadapter.config;

import com.taskadapter.connector.definition.ConnectorConfig;

public class ConnectorDataHolder {
	/**
	 * this no-args constructor is required for GSon.
	 */
	public ConnectorDataHolder() {
	}

	private String type;
	private ConnectorConfig data;

	public ConnectorDataHolder(String type2, ConnectorConfig data) {
		this.type = type2;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public ConnectorConfig getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConnectorDataHolder other = (ConnectorDataHolder) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
}