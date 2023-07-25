package fctg.profile.transform.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jcraft.jsch.SftpException;

import fctg.profile.transform.Utils.SFTPUtil;
import fctg.profile.transform.exception.MyException;
import fctg.profile.transform.services.SFTPImpl;
import fctg.profile.transform.services.autoSftpTask;

@Controller
public class FileUploadController {

	private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	private Environment environment;
	
	@Autowired
	SFTPImpl sFTPEmplement;

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	private String getLsPathFromReq(String lsPath) {
		if(lsPath.equals("default")) {
			return "sftp.lspath";
		}else {
			return "sftp.manually.upload.priex";
		}
	}
	
	@GetMapping("/mydownload/{lspath}/{file}")
	public String mydownload(@PathVariable("lspath") String lsPath, @PathVariable("file") String fileName, RedirectAttributes ra) {
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22); 
		sftp.login();  
		String localFile = environment.getProperty("file.temp.path") + System.getProperty("file.separator") + fileName;
		try {
			sftp.download(environment.getProperty(getLsPathFromReq(lsPath)), fileName, localFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			sftp.logout();
		}
		ra.addFlashAttribute("newProfileName", localFile);
		ra.addFlashAttribute("uuid");
		return "redirect:/downloadFile";
	}
	
	@ResponseBody
	@PostMapping("/getLsFileList")
	public List<String> getLsFileList(HttpServletRequest req) {
		List<String> lsList = new ArrayList<String>();
		SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"), environment.getProperty("sftp.server"), 22); 
		sftp.login();  
		lsList = sFTPEmplement.MyLs(sftp, environment.getProperty(getLsPathFromReq(req.getParameter("lsPath"))));
		sftp.logout();
		return lsList;
	}
	
	@Autowired
	private SFTPImpl sftpImpl;

	/**
	 * //xls:application/vnd.ms-excel //csv:application/vnd.ms-excel
	 * //xlsx:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
	 * //pdf:application/pdf
	 * 
	 */
	@PostMapping("upload")
	public String upload(@RequestPart("file2") MultipartFile file2, @RequestParam("option") String option,
			HttpServletRequest request, Model model) {
		String uuid = UUID.randomUUID().toString().replace("-", "");
//		String option = "sftp";
		String result = "default";
		String maxSize = environment.getProperty("spring.servlet.multipart.max-file-size");
		int maxSizeInt = Integer.parseInt(maxSize.substring(0, maxSize.length() - 2));
		logger.info(uuid+"-input pfile "+file2.getName()+", type "+file2.getContentType()+", size "+file2.getSize());
		if (file2.isEmpty()) {
			result = "文件不能为空";
			logger.info(uuid+"-check file is empty");
		} else if (!file2.getContentType().equals("application/vnd.ms-excel")) {
			result = "只允许上传CSV";
			logger.info(uuid+"-check file's type isn't CSV");
		} else if (file2.getSize() >= maxSizeInt * 1024 * 1024) {
			result = "上传文件太大";
			logger.info(uuid+"-check file is more than limit size");
		} else {
			String realPath = environment.getProperty("file.temp.path");
			File dir = new File(realPath);
			if (!dir.exists())
				dir.mkdir();
			
			try {
				String prefix = sftpImpl.getTimestampforPrefix()+"_";
				//保存文件到本地临时路径
				file2.transferTo(new File(dir, prefix+file2.getOriginalFilename()));
				//打开sftp连接
				SFTPUtil sftp = new SFTPUtil(environment.getProperty("sftp.user"), environment.getProperty("sftp.pass"),
						environment.getProperty("sftp.server"), 22);
				try {
					sftp.login();
					//上传文件到SFTP
					sftp.upload(environment.getProperty("sftp.manually.upload.priex"), new File(prefix+file2.getOriginalFilename()).getName(),
							new FileInputStream(new File(dir, prefix+file2.getOriginalFilename())));
				} catch (SftpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					sftp.logout();
				}
				
				new autoSftpTask(prefix+file2.getOriginalFilename()).run();
				
				result = "上传成功";
			} catch (IllegalStateException e) {
				throw new MyException(e);
			} catch (IOException e) {
				throw new MyException(e);
			}
		}
		
		
		model.addAttribute("result", result);
//		return "redirect:/excelhandler";
		return "index";
	}

	@GetMapping("/downloadFile")
	private void downloadFile(@ModelAttribute("newProfileName") String localPath, 
			@ModelAttribute("uuid") String uuid, 
			HttpServletResponse response) throws IOException {
        //读取文件
        File file = new File(localPath);
        //获取文件输入流
        FileInputStream is = new FileInputStream(file);
        //attachement; 附件下载， inline在线打开
        response.setHeader("content-disposition", "attachment;fileName="+file.getName());
        //取出响应输出流
        ServletOutputStream os = response.getOutputStream();
        //文件拷贝2
        IOUtils.copy(is, os);
        //关流方式（优雅）
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
        sFTPEmplement.DeleteFile(localPath, uuid);
	}


}
