package fctg.profile.transform.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

import fctg.profile.transform.Utils.HttpClientUtil;
import fctg.profile.transform.Utils.SFTPUtil;
import fctg.profile.transform.beans.ApiBean;
import fctg.profile.transform.exception.MyException;

@Component
public class SFTPImpl {

	private static final Logger logger = LoggerFactory.getLogger(SFTPImpl.class);

	public String getVaildApproverfile(List<String[]> approversList) {
		String approverSFTPFile = "";
		Optional<String[]> findFirst = approversList.stream()
				.sorted((e1, e2)->(-Double.compare(Double.parseDouble(e1[0]), Double.parseDouble(e2[0]))))
				.findFirst();
		approverSFTPFile = findFirst.get()[1];
		return approverSFTPFile;
	}
	
	public String getVaildProfile(List<String> lsList) {
		String profilesSFTPFile = "";
		for (String string : lsList) {
			if(!string.toLowerCase().contains("approver") && string.contains(getTimestamp())) {
				profilesSFTPFile = string;
			}
		}
		return profilesSFTPFile;
	}
	
	/**
	 * 
	 * @param lsList
	 * @return List<String[]>
	 * 	0: date
	 * 	1: name
	 * [0]20210929 [1]ATOS_HR_Feed_20210929_CN01_Approvers.csv
	 * [0]20211018 [1]ATOS_HR_Feed_20211018_CN01_approvers.csv
	 * [0]20211025 [1]ATOS_HR_Feed_20211025_CN01_Approvers.csv
	 */
	public List<String[]> getApproversList(List<String> lsList){
		List<String[]> ApproversList = new ArrayList<String[]>();
		for (String string : lsList) {
			if(string.toLowerCase().contains("_approver"))
			{
				ApproversList.add(new String[] {string.split("_")[3], string});
			}
		}
		return ApproversList;
	}
	
	/**
	 * 
	 * @param lsList
	 * @return List<String[]>
	 * 	0: date
	 * 	1: name
	 * [0]20211208 [1]ATOS_HR_Feed_20211208_CN01_GlobalApprovers.csv
	 */
	public List<String[]> getApproversList_G(List<String> lsList){
		List<String[]> ApproversList = new ArrayList<String[]>();
		for (String string : lsList) {
			if(string.toLowerCase().contains("_globalapprover"))
			{
				String GlobalApproverFileDate = string.split("_")[3];
				try {
					Integer.parseInt(GlobalApproverFileDate);
					ApproversList.add(new String[] {string.split("_")[3], string});
				}catch(Exception ex) {
					logger.error("file name is error :"+ string);
				}
			}
		}
		return ApproversList;
	}
	
	public List<String> MyLs(SFTPUtil sftp, String sftp_lspath) {
		List<String> fileList = new ArrayList<String>();
		try {
			Vector<?> listFiles = sftp.listFiles(sftp_lspath);
			for (LsEntry entry : (Vector<LsEntry>) listFiles) {
				fileList.add(entry.getFilename());
				logger.info(" ls result : " + entry.getFilename());
			}
		} catch (SftpException e) {
			logger.info(" ls error : "+ e.getMessage());
			throw new MyException(e);
		} 
		return fileList;
	}

//	today -1 : 20211110
	private String getTimestamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDateTime currentTime = LocalDateTime.now().minusDays(1);
		return currentTime.format(formatter);
	}
	
	public String getTimestampforPrefix() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");
		return now.format(f);
	}
	
	public void DeleteFile(String filePath, String uuid) {
		File f = new File(filePath);
		if(f.exists()) 
		{
			System.gc();
			logger.info(uuid+" drop file "+f.getName()+" : "+f.delete());
		}
	}
	
	public Map<String, String> getEmailByUserIdHandler(Map<String, String> maps, String baseFileName, int uid, int email, String csv_charset) {
		try {
			CsvReader csvReader = new CsvReader(baseFileName, ',', Charset.forName(csv_charset));
			int i = 0;
			while (csvReader.readRecord()) {
				if (i > 0) {
					if(csvReader.get(email)!="") {
						maps.put(csvReader.get(uid), csvReader.get(email));
						logger.info("提取数据-文件-"+baseFileName+"-数据: Uid("+uid+")_"+csvReader.get(uid)+" Email("+email+")_"+csvReader.get(email));
					}else {
						logger.info("提取数据-文件-"+baseFileName+"-数据: Uid("+uid+")_"+csvReader.get(uid)+" Email("+email+")_无邮箱");
					}
				}
				i++;
			}
			csvReader.close();
		} catch (UnsupportedEncodingException e) {
			throw new MyException(e);
		} catch (FileNotFoundException e) {
			throw new MyException(e);
		} catch (IOException e) {
			throw new MyException(e);
		}
		return maps;
	}
	
	public Map<String, String> getEmailByUserIdHandler(Map<String, String> maps, String baseFileName, String uidColunmName, String emailColunmName, String csv_charset){
		try {
			CsvReader csvReader = new CsvReader(baseFileName, ',', Charset.forName(csv_charset));
			String uid = "";
			String ems = "";
//			是否成功读取了头记录。
			if(csvReader.readHeaders()) {
				String[] headers = csvReader.getHeaders();
				for (String string : headers) {
//					environment.getProperty("Approvers.userid.column")
					if(string.toLowerCase().contains(uidColunmName)) {
						uid = string;
					}
//					environment.getProperty("Approvers.emails.column")
					if(string.toLowerCase().contains(emailColunmName)) {
						ems = string;
					}
				}
				if(uid == "" || ems == "") {
					logger.info("提取数据-文件-"+baseFileName+"- 在表头"+Arrays.toString(headers)+"中未能识别"+uidColunmName+","+emailColunmName+"，放弃操作此文件。");
				}else {
					while (csvReader.readRecord()) {
						if(csvReader.get(ems)!="") {
							maps.put(csvReader.get(uid), csvReader.get(ems));
							logger.info("提取数据-文件-"+baseFileName+"-数据: Uid("+uid+")_"+csvReader.get(uid)+" Email("+ems+")_"+csvReader.get(ems));
						}else{
							logger.info("提取数据-文件-"+baseFileName+"-数据: Uid("+uid+")_"+csvReader.get(uid)+" Email("+ems+")_无邮箱");
						}
					}
				}
			}
			csvReader.close();
		} catch (UnsupportedEncodingException e) {
			throw new MyException(e);
		} catch (FileNotFoundException e) {
			throw new MyException(e);
		} catch (IOException e) {
			throw new MyException(e);
		}
		return maps;
	}
	
	public String profileHandler(Map<String, String> maps, String profileName, String newProfileName, String csv_charset, String profile_target_column_position) {
		String eid = "";
		try {
			CsvWriter csvWriter = new CsvWriter(newProfileName, ',', Charset.forName(csv_charset));
			CsvReader csvReader = new CsvReader(profileName, ',', Charset.forName(csv_charset));

			csvReader.readHeaders();
			String[] headers = csvReader.getHeaders();

			List<String> listHeader = new ArrayList<String>(Arrays.asList(headers));
			listHeader.add("approver_email");
			String[] newHeaders = listHeader.toArray(new String[listHeader.size()]);
			csvWriter.writeRecord(newHeaders);
			StringBuilder sblog = new StringBuilder();
			while (csvReader.readRecord()) {
				String[] tmpColumn = new String[csvReader.getColumnCount() + 1];
				for (int c = 0; c < csvReader.getColumnCount(); c++) {
					tmpColumn[c] = csvReader.get(c);
				}
				//无审批人ID
				if(csvReader.get(Integer.parseInt(profile_target_column_position))==null || csvReader.get(Integer.parseInt(profile_target_column_position))=="") {
					tmpColumn[csvReader.getColumnCount()] = "";
				}else {
					//从map中未能取到审批邮箱
					if (maps.get(csvReader.get(Integer.parseInt(profile_target_column_position))) == null) {
						eid += csvReader.get(Integer.parseInt(profile_target_column_position))+",";
						tmpColumn[csvReader.getColumnCount()] = "error:"+csvReader.get(Integer.parseInt(profile_target_column_position));
					}else {
						tmpColumn[csvReader.getColumnCount()] = maps.get(csvReader.get(Integer.parseInt(profile_target_column_position)));
					}
				}
				
				csvWriter.writeRecord(tmpColumn);
				for (String string : tmpColumn) {
					sblog.append(string+"\t");
				}
				logger.info(sblog.toString());
				sblog.delete(0, sblog.length());
			}
			csvWriter.close();
			csvReader.close();
		} catch (FileNotFoundException e) {
			throw new MyException(e);
		} catch (IOException e) {
			throw new MyException(e);
		}
		return eid;
	}
	
	public void manuallyUploadSFTP(String localPath, String remoteFolder, String remotePath, String uuid, String sftp_user, String sftp_pass, String sftp_server, int port) {
		SFTPUtil sftp = new SFTPUtil(sftp_user, sftp_pass, sftp_server, port);   
        sftp.login();   
        File file = new File(localPath);
        InputStream is;
		try {
			is = new FileInputStream(file);
			sftp.upload(remoteFolder, remotePath, is);
			is.close();
		} catch (FileNotFoundException e) {
			throw new MyException(e);
		} catch (SftpException e) {
			throw new MyException(e);
		} catch (IOException e) {
			throw new MyException(e);
		} finally {
			sftp.logout(); 
			DeleteFile(localPath, uuid);
		}
	}
	
	@Autowired
	HttpClientUtil httpClientUtil;
	
	public void CallChinaHub(String json, String url) {
		logger.info("Call Api result: "+httpClientUtil.doPost(url, json));
	}
	
	public String getCallJson(String AppName, String CpConfigGuid, String Api_pass) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			long ts = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
			ApiBean apiBean = new ApiBean();
			apiBean.setAppName(AppName);
			apiBean.setCpConfigGuid(CpConfigGuid);
			apiBean.setSignature(genSignature(ts, AppName, Api_pass));
			apiBean.setTimeStamp(ts);
			return  mapper.writeValueAsString(apiBean);
		} catch (JsonProcessingException e) {
			throw new MyException(e);
		}
	}
	
	private String genSignature(long ts, String AppName, String Api_pass) {
		return DigestUtils.md5DigestAsHex(
				(
					DigestUtils.md5DigestAsHex((Long.toString(ts)+AppName).getBytes())+Api_pass
				).getBytes()
			);
	}
}
