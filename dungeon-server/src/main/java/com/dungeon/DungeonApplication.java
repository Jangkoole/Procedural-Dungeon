package com.dungeon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DungeonApplication {

    public static void main(String[] args) {
        SpringApplication.run(DungeonApplication.class, args);
    }
}
