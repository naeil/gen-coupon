package naeil.gen_coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@SpringBootApplication
public class GenCouponApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("현재 시간은??" + LocalDateTime.now());

		LocalDate today = LocalDate.now();
		String startDate = today.format(DateTimeFormatter.ISO_DATE);
		System.out.println("today ??" + startDate);

		String endDate = today.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
		System.out.println("6months ago ??" + endDate);
		SpringApplication.run(GenCouponApplication.class, args);
	}
}
