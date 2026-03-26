package naeil.gen_coupon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class GenCouponApplication {

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("현재 시간은?? {}", LocalDateTime.now());

		LocalDate today = LocalDate.now();
		String startDate = today.format(DateTimeFormatter.ISO_DATE);
		log.info("today ?? {}", startDate);

		String endDate = today.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
		log.info("6months ago ?? {}", endDate);
		SpringApplication.run(GenCouponApplication.class, args);
	}
}
