package fctg.profile.transform.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.jcraft.jsch.SftpException;

import fctg.profile.transform.Utils.ApplicationContextProvider;
import fctg.profile.transform.Utils.SFTPUtil;
import fctg.profile.transform.exception.MyException;

public class autoSftpTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(autoSftpTask.class);
	private Environment environment;
	private SFTPImpl sftpImpl;
	private String manuallyProfile;

	@Override
	public void run() {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String apvFileName = "";
		String apvFileName_G = "";
		String proFileName = "";
		String newProfileName = "";
		String profilesSFTPFile = "";
		List<String> lsList = new ArrayList<String>();
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"),
				environment.getProperty("sftp.server"), 22);
		sftp.login();
//		get valid data
		try {
			lsList = sftpImpl.MyLs(sftp, environment.getProperty("sftp.lspath"));
			List<String[]> approversList = sftpImpl.getApproversList(lsList);
			List<String[]> approversList_G = sftpImpl.getApproversList_G(lsList);
			
			if(manuallyProfile==null) {
				profilesSFTPFile = sftpImpl.getVaildProfile(lsList);
			}else {
				profilesSFTPFile = manuallyProfile;
			}
			
//			Vector<?> listFiles = sftp.listFiles(environment.getProperty("sftp.lspath"));
//			for (LsEntry entry : (Vector<LsEntry>)listFiles) {
//				if(entry.getFilename().toLowerCase().contains("approver"))
//				{
//					ApproversList.add(new String[] {entry.getFilename().split("_")[3], entry.getFilename()});
//				}
//				if(!entry.getFilename().toLowerCase().contains("approver") && entry.getFilename().contains(getTimestamp())) {
//					profilesSFTPFile = entry.getFilename();
//				}
//				logger.info(entry.getFilename());
//			}

//			Check this loop whether the data file is valid or not
			if (profilesSFTPFile == "") {
				logger.info("无有效Profile文件，任务结束");
				return;
			}

//			Get latest approver file
//			Optional<String[]> findFirst = approversList.stream()
//					.sorted((e1, e2)->(-Double.compare(Double.parseDouble(e1[0]), Double.parseDouble(e2[0]))))
//					.findFirst();
//			approverSFTPFile = findFirst.get()[1];
			String approverSFTPFile = "";
			if(approversList.size()>0) {
				approverSFTPFile = sftpImpl.getVaildApproverfile(approversList);
			}else {
				logger.info("未获取到有效审批列表，任务结束");
				return;
			}
			String approverSFTPFile_G = "";
			if(approversList_G.size()>0) {
				approverSFTPFile_G = sftpImpl.getVaildApproverfile(approversList_G);
			}else {
				logger.info("未获取到有效国际审批列表，任务结束");
				return;
			}

			logger.info("处理文件：" + approverSFTPFile + " - " + approverSFTPFile_G + " - " + profilesSFTPFile);

//			Definition file full name on local path
			apvFileName = environment.getProperty("file.temp.path") + System.getProperty("file.separator")
				+ approverSFTPFile;
			apvFileName_G = environment.getProperty("file.temp.path") + System.getProperty("file.separator")
				+ approverSFTPFile_G;
			proFileName = environment.getProperty("file.temp.path") + System.getProperty("file.separator")
				+ profilesSFTPFile;
			newProfileName = environment.getProperty("file.temp.path") + System.getProperty("file.separator")
					+ profilesSFTPFile.split("\\.")[0] + "_auto.csv";

//			Check folder
			File dir = new File(environment.getProperty("file.temp.path"));
			if (!dir.exists())
				dir.mkdir();

//			Remove same name file in local
			sftpImpl.DeleteFile(apvFileName, uuid);
			sftpImpl.DeleteFile(apvFileName_G, uuid);
			if(manuallyProfile==null) {
				sftpImpl.DeleteFile(proFileName, uuid);
			}
			sftpImpl.DeleteFile(newProfileName, uuid);
//			Download file from SFTP
			sftp.download(environment.getProperty("sftp.lspath"), approverSFTPFile, apvFileName);
			sftp.download(environment.getProperty("sftp.lspath"), approverSFTPFile_G, apvFileName_G);
			if(manuallyProfile==null) {
				sftp.download(environment.getProperty("sftp.lspath"), profilesSFTPFile, proFileName);
			}

//			Extraction data
			Map<String, String> maps = new HashMap<String, String>();

//			maps = sFTPEmplement.getEmailByUserIdHandler(maps, apvFileName, 
//					Integer.parseInt(environment.getProperty("basefile.userid.column.position")), 
//					Integer.parseInt(environment.getProperty("basefile.emails.column.position")),
//					environment.getProperty("csv.charset"));
//			
//			maps = sFTPEmplement.getEmailByUserIdHandler(maps, proFileName, 
//					Integer.parseInt(environment.getProperty("profile.userid.column.position")), 
//					Integer.parseInt(environment.getProperty("profile.emails.column.position")),
//					environment.getProperty("csv.charset"));

//			Approvers
			maps = sftpImpl.getEmailByUserIdHandler(maps, apvFileName,
					environment.getProperty("Approvers.userid.column"),
					environment.getProperty("Approvers.emails.column"),
					environment.getProperty("csv.charset"));
//			GlobalApprovers
			maps = sftpImpl.getEmailByUserIdHandler(maps, apvFileName_G,
					environment.getProperty("GlobalApprovers.userid.column"),
					environment.getProperty("GlobalApprovers.emails.column"),
					environment.getProperty("csv.charset"));
//			proFileName
			maps = sftpImpl.getEmailByUserIdHandler(maps, proFileName,
					environment.getProperty("Profile.userid.column"),
					environment.getProperty("Profile.emails.column"),
					environment.getProperty("csv.charset"));

//			Remove empty data
			maps.remove("");
//			Create new Profile File
			sftpImpl.profileHandler(maps, proFileName, newProfileName, environment.getProperty("csv.charset"),
					environment.getProperty("profile.target.column.position"));
//			Upload file to SFTP
//			need change to new dir for manually upload path
			sftp.upload(environment.getProperty("sftp.priex"), new File(newProfileName).getName(),
					new FileInputStream(new File(newProfileName)));
//			Call Api
			if(Boolean.parseBoolean(environment.getProperty("Callable_ChinaHub"))) {
				sftpImpl.CallChinaHub(
						sftpImpl.getCallJson(environment.getProperty("AppName"),
						environment.getProperty("CpConfigGuid"), environment.getProperty("Api_pass")),
						environment.getProperty("api_url"));
			}
		} catch (SftpException e) {
			throw new MyException(e);
		} catch (FileNotFoundException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new MyException(e);
		} finally {
			sftp.logout();
			sftpImpl.DeleteFile(apvFileName, uuid);
			sftpImpl.DeleteFile(apvFileName_G, uuid);
			sftpImpl.DeleteFile(proFileName, uuid);
			sftpImpl.DeleteFile(newProfileName, uuid);
		}
	}

	public autoSftpTask() {
		init();
	}
	
	public autoSftpTask(String profile) {
		init();
		this.manuallyProfile = profile;
	}
	
	private void init() {
		this.environment = ApplicationContextProvider.getApplicationContext().getEnvironment();
		this.sftpImpl = (SFTPImpl) ApplicationContextProvider.getApplicationContext()
				.getBean("SFTPImpl");
	}

}
