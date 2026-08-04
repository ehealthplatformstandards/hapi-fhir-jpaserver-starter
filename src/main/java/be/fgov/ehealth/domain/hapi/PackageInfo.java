package be.fgov.ehealth.domain.hapi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackageInfo {

	private String name;
	private String version;

}
