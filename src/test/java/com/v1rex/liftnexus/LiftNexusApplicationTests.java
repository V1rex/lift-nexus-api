package com.v1rex.liftnexus;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.config.TimefoldTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({TestContainersConfiguration.class, TimefoldTestConfig.class})
@ActiveProfiles("test")
class LiftNexusApplicationTests {

  @Test
  void contextLoads() {}
}
