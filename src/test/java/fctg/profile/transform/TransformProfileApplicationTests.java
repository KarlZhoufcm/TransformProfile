package fctg.profile.transform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.util.DigestUtils;

import com.csvreader.CsvReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

import fctg.profile.transform.Utils.GetBeanUtil;
import fctg.profile.transform.Utils.HttpClientUtil;
import fctg.profile.transform.Utils.SFTPUtil;
import fctg.profile.transform.beans.ApiBean;
import fctg.profile.transform.exception.MyException;
import fctg.profile.transform.services.SFTPImpl;
import fctg.profile.transform.services.autoSftpTask;

@SpringBootTest
class TransformProfileApplicationTests {

	@Autowired
	Environment environment;
	
	@Autowired
	HttpClientUtil httpClientUtil;
	
	@Autowired 
	ApiBean apiBean;
	
	@Test
	public void testbool() {
		System.out.println(environment.getProperty("Callable_ChinaHub"));
		System.out.println(Boolean.parseBoolean(environment.getProperty("Callable_ChinaHub").toString()));
		if(Boolean.parseBoolean(environment.getProperty("Callable_ChinaHub"))) {
			System.out.println("true");
		}else {
			System.out.println("false");
		}
		
	}
	
	@Test
	public void test111() {
		new autoSftpTask().run();
	}
	
	@Test
	public void autobean() throws InterruptedException {
		System.out.println(apiBean.toString());
		Thread.sleep(5000);
		System.out.println(GetBeanUtil.getBean(ApiBean.class).toString());
		Thread.sleep(5000);
		System.out.println(GetBeanUtil.getBean(ApiBean.class).toString());
	}
	
	
	@Test
	public void CSVReader() {
		try {
			CsvReader csvReader = new CsvReader("c:\\logs\\ATOS_HR_Feed_20211208_CN01_GlobalApprovers.csv", ',', Charset.forName("UTF-8"));
//			是否成功读取了头记录。
//			System.out.println(csvReader.readHeaders());
//			String[] headers = csvReader.getHeaders();
//			
//			System.out.println(headers.length);
//			for (String string : headers) {
//				
//				System.out.println(string);
//			}
			String uid = "";
			String ems = "";
			if(csvReader.readHeaders()) {
				String[] headers = csvReader.getHeaders();
				System.out.println(Arrays.toString(headers));
				for (String string : headers) {
					if(string.toLowerCase().contains(environment.getProperty("Approvers.userid.column"))) {
						uid = string;
					}
					if(string.toLowerCase().contains(environment.getProperty("Approvers.emails.column"))) {
						ems = string;
					}
				}
				if(uid == "" || ems == "") {
					System.out.println("error");
				}else {
					while (csvReader.readRecord()) {
						System.out.println(csvReader.get("user_id") + "-" + csvReader.get("emails"));
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
	}
	
	
	
	
	@Test
	public void Json() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 设置日期格式
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(dateFormat);
		
//		System.out.println("10位时间戳："+LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
//        System.out.println("13位时间戳："+LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
//		
//		Timestamp timestamp= Timestamp.valueOf(LocalDateTime.now());
//		System.out.println("13位时间戳："+timestamp.getTime());
		long ts = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
		apiBean = new ApiBean();
		apiBean.setAppName(environment.getProperty("AppName"));
		apiBean.setCpConfigGuid(environment.getProperty("CpConfigGuid"));
		String Signature = DigestUtils.md5DigestAsHex((
				DigestUtils.md5DigestAsHex(
						(Long.toString(ts)+environment.getProperty("AppName")).getBytes()
						)+environment.getProperty("Api_pass")).getBytes()
				);
		apiBean.setSignature(Signature);
		apiBean.setTimeStamp(ts);
		
		System.out.println(mapper.writeValueAsString(apiBean));
		System.out.println("Call Api result: "+httpClientUtil.doPost(environment.getProperty("api_url"), mapper.writeValueAsString(apiBean)));
	}
	
	@Test
	public void httpClient() {
		String json = "";
		String url = "";
		
//		jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 设置日期格式
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(dateFormat);
		
				
		Map<String, Object> testMap = new HashMap<>();
        testMap.put("Services", "Fcm");
        testMap.put("Class", "Altman.Services.FcmClient.HrFeed");
        testMap.put("Method", "ManualTask");
        Map<String, Object> TransferParameter = new HashMap<>();
        Map<String, Object> Request = new HashMap<>();
        Request.put("FileName", "");
        Request.put("FileByte", null);
        Request.put("CpConfigGuid", UUID.randomUUID().toString());
        Request.put("IsFileFromSftp", 1);
        TransferParameter.put("Request", Request);
        testMap.put("TransferParameter", TransferParameter);
		
        try {
            String jsonStr = mapper.writeValueAsString(testMap);
            System.out.println("Map转为字符串：" + jsonStr);
            try {
                Map<String, Object> testMapDes = mapper.readValue(jsonStr, Map.class);
                System.out.println("字符串转Map：" + testMapDes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
//		String str = httpClientUtil.doPost(url, json);
	}
	
	@Test
	public void md5() {
		String md5Password = DigestUtils.md5DigestAsHex("aaa".getBytes());
		System.out.println(md5Password);
	}
	
	
	@Autowired
	SFTPImpl sFTPEmplement;
	
	@Test
	public void TestMainTask() {
//		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22); 
//		sftp.login(); 
//		List<String> lsList = sFTPEmplement.MyLs(sftp, environment);
//		List<String[]> approversList = sFTPEmplement.getApproversList(lsList);
//		for (String[] strings : approversList) {
//			System.out.println(strings[0]+"-"+strings[1]);
//		}
		
		autoSftpTask task = new autoSftpTask();
		task.run();
	}
	
//	
	@Test
	public void Test() {
		new autoSftpTask();
	}
	
//	Attribute of command ls last update time
	@Test
	public void lsTest() throws SftpException {
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22);
		sftp.login();  
		Vector<LsEntry> listFiles = (Vector<LsEntry>) sftp.listFiles("/Atos/ATOSCN");
		for (LsEntry object : listFiles) {
			
//			System.out.println(object.getLongname());
//			System.out.println(object.getAttrs());
//			System.out.println(object.getAttrs().getMTime());
//			System.out.println(object.getAttrs().getMtimeString());
//			Date date = new Date(object.getAttrs().getMTime() * 1000L);
//			System.out.println(date.toGMTString());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(object.getAttrs().getMTime() * 1000L), ZoneOffset.of("+8"));
			System.out.print(localDateTime.toString()+"\t");
			System.out.println(object.getFilename());
//			System.out.println("=======================================");
		}
		sftp.logout(); 
	}
	
	@Test
	void contextLoads() throws SftpException {
		List<String[]> tmpList = new ArrayList<String[]>();
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22); 
		sftp.login();  
		Vector<LsEntry> listFiles = (Vector<LsEntry>) sftp.listFiles("/Atos/ATOSCN");
		for (LsEntry object : listFiles) {
			System.out.println(object.getFilename()+"\t"+object.getLongname());
			System.out.println(object.getAttrs());
			if(object.getFilename().toLowerCase().contains("approvers"))
			{
				tmpList.add(new String[] {object.getFilename().split("_")[3], object.getFilename()});
			}
		}
		sftp.logout(); 
		
		Optional<String[]> findFirst = tmpList.stream()
		.sorted((e1, e2)->(-Double.compare(Double.parseDouble(e1[0]), Double.parseDouble(e2[0]))))
		.findFirst();
		
		System.out.println("last approver file is "+findFirst.get()[1]);
		
	}
	
	@Test
	public void downloadFromSftp() throws FileNotFoundException, SftpException {
//		ATOS_HR_Feed_20211109_CN01.csv				getTimestamp()
//		ATOS_HR_Feed_20211101_CN01_Approvers.csv	get last one
		System.out.println(getTimestamp());
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22); 
		sftp.login();  
		sftp.download("/Atos/ATOSCN", "ATOS_HR_Feed_20211118_CN01.csv", "c:\\logs\\tempFile\\ATOS_HR_Feed_20211118_CN01.csv");
		sftp.logout(); 
	}
	
	@Test
	public void TestTimeStamp()
	{
		System.out.println(getTimestamp());
	}
	
//	20211110
	public String getTimestamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDateTime currentTime = LocalDateTime.now().minusDays(1);
		return currentTime.format(formatter);
	}

	public static void main(String[] args)  throws IOException{
//		File csvData = new File("c:/logs/aaa.csv");
//		CSVParser parse = CSVParser.parse(csvData, java.nio.charset.Charset.forName("utf-8"), CSVFormat.DEFAULT);
//		List<CSVRecord> list = parse.getRecords();
//		for (CSVRecord csvRecord : list) {
//			for (int i=0; i<csvRecord.size(); i++) {
//				System.out.print(csvRecord.get(i)+"\t");
//			}
//			System.out.println();
//		}
	}
	
//	@Test
//	public void Upload() throws FileNotFoundException, SftpException {
//		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22);
//		sftp.login(); 
//		sftp.upload(environment.getProperty("sftp.lspath"), 
//				new File("C:\\logs\\ATOS_HR_Feed_20211116_CN01.csv").getName(), 
//				new FileInputStream(new File("C:\\logs\\ATOS_HR_Feed_20211116_CN01.csv"))
//				);
//		sftp.upload(environment.getProperty("sftp.lspath"), 
//				new File("C:\\logs\\ATOS_HR_Feed_20211117_CN01.csv").getName(), 
//				new FileInputStream(new File("C:\\logs\\ATOS_HR_Feed_20211117_CN01.csv"))
//				);
//		sftp.logout();
//	}
	
}
