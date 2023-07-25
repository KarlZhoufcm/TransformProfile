package fctg.profile.transform.beans;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiBean{
	@JsonProperty("TimeStamp")
	private Long TimeStamp;
	@JsonProperty("Signature")
	private String Signature;
//	@Value("${AppName}")
	@JsonProperty("AppName")
	private String AppName;
//	@Value("${CpConfigGuid}")
	@JsonProperty("CpConfigGuid")
	private String CpConfigGuid;
//	public ApiBean() {
//		this.TimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
//	}
//	public Long getTimeStamp() {
//		return TimeStamp;
//	}
//	public void setTimeStamp(Long timeStamp) {
//		TimeStamp = timeStamp;
//	}
//	public String getSignature() {
//		return Signature;
//	}
//	public void setSignature(String signature) {
//		Signature = signature;
//	}
//	public String getAppName() {
//		return AppName;
//	}
//	public void setAppName(String appName) {
//		AppName = appName;
//	}
//	public String getCpConfigGuid() {
//		return CpConfigGuid;
//	}
//	public void setCpConfigGuid(String cpConfigGuid) {
//		CpConfigGuid = cpConfigGuid;
//	}
}
