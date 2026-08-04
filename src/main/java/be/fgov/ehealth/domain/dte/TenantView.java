package be.fgov.ehealth.domain.dte;

import lombok.Data;

@Data
public class TenantView {

	private Integer id;
	private String tenantLabel;
	private String tenantDescription;
	private String tenantEhboxSenderId;
	private String tenantApiKey;
	
}
