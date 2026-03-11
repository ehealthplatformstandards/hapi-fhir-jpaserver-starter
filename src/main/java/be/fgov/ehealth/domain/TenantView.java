package be.fgov.ehealth.domain;

public class TenantView {

	private Integer id;
	private String tenantLabel;
	private String tenantDescription;
	private String tenantEhboxSenderId;
	private String tenantApiKey;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getTenantLabel() {
		return tenantLabel;
	}

	public void setTenantLabel(final String tenantLabel) {
		this.tenantLabel = tenantLabel;
	}

	public String getTenantDescription() {
		return tenantDescription;
	}

	public void setTenantDescription(final String tenantDescription) {
		this.tenantDescription = tenantDescription;
	}

	public String getTenantEhboxSenderId() {
		return tenantEhboxSenderId;
	}

	public void setTenantEhboxSenderId(final String tenantEhboxSenderId) {
		this.tenantEhboxSenderId = tenantEhboxSenderId;
	}

	public String getTenantApiKey() {
		return tenantApiKey;
	}

	public void setTenantApiKey(final String tenantApiKey) {
		this.tenantApiKey = tenantApiKey;
	}
}
