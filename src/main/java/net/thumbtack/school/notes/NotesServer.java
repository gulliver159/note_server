package net.thumbtack.school.notes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class NotesServer {

	public static void main(String[] args) {
		log.info("Start application");
		SpringApplication.run(NotesServer.class, args);
	}

}

