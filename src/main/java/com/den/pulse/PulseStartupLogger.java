package com.den.pulse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PulseStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PulseStartupLogger.class);

    @Override
    public void run(ApplicationArguments args) {
        log.info("=======================================================");
        log.info("[PULSE] System Online. 심박수 및 데이터 흐름 안정화 완료.");
        log.info("[PULSE] 아지트(den)에 생명력을 공급할 준비가 되었습니다.");
        log.info("=======================================================");
    }
}