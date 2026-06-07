package com.rishanth.flux360.scheduler;

import com.rishanth.flux360.entity.SipInvestment;
import com.rishanth.flux360.repository.SipInvestmentRepository;
import com.rishanth.flux360.service.SipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SipScheduler {

    private final SipInvestmentRepository sipRepo;

    private final SipService sipService;

    @Scheduled(cron = "0 0 10 * * ?")
    public void autoExecuteSips() {

        int today =
                LocalDate.now().getDayOfMonth();

        List<SipInvestment> activeSips =
                sipRepo.findByActiveTrue();

        for (SipInvestment sip : activeSips) {

            if (sip.getSipDay() == today) {

                try {

                    sipService.executeSipNow(
                            sip.getId(),
                            sip.getUser()
                    );

                } catch (Exception e) {

                    log.warn(
                            "SIP execution failed for SIP id {} : {}",
                            sip.getId(),
                            e.getMessage()
                    );
                }
            }
        }
    }
}