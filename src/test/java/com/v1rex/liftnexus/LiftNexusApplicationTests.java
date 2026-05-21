package com.v1rex.liftnexus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Skipping full context load until we configure Testcontainers")
class LiftNexusApplicationTests {

  @Test
  void contextLoads() {}
}
