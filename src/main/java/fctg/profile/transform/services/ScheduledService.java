package fctg.profile.transform.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

	@Scheduled(cron = "${ScheduledCron_sftp}")
	public void autoTask() {
		Thread t = new Thread(new autoSftpTask());
		t.start();
	}
}
