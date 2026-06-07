package com.rishanth.flux360.service;

import com.rishanth.flux360.entity.SipTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class XirrService {

    public double calculate(
            List<SipTransaction> txns,
            double currentValue
    ) {

        if (txns == null ||
                txns.size() < 2 ||
                currentValue <= 0) {

            return 0;
        }

        txns.sort(
                Comparator.comparing(
                        SipTransaction::getInvestDate
                )
        );

        double guess = 0.12;

        double tolerance = 0.0001;

        int maxIterations = 1000;

        try {

            for (int i = 0;
                 i < maxIterations;
                 i++) {

                double f =
                        npv(
                                txns,
                                currentValue,
                                guess
                        );

                double df =
                        derivative(
                                txns,
                                currentValue,
                                guess
                        );

                if (Math.abs(df) < 1e-10) {
                    break;
                }

                double newGuess =
                        guess - (f / df);

                if (Double.isNaN(newGuess) ||
                        Double.isInfinite(newGuess)) {

                    log.warn(
                            "Invalid XIRR result calculated"
                    );

                    return 0;
                }

                if (Math.abs(newGuess - guess)
                        <= tolerance) {

                    return newGuess * 100;
                }

                guess = newGuess;
            }

        } catch (Exception e) {

            log.error(
                    "XIRR calculation failed: {}",
                    e.getMessage()
            );
        }

        return 0;
    }

    // ─────────────────────────────────────────────

    private double npv(
            List<SipTransaction> txns,
            double currentValue,
            double rate
    ) {

        double total = 0;

        long baseDate =
                txns.get(0)
                        .getInvestDate()
                        .toEpochDay();

        for (SipTransaction txn : txns) {

            long days =
                    txn.getInvestDate()
                            .toEpochDay()
                            - baseDate;

            double years = days / 365.0;

            total +=
                    -txn.getAmount().doubleValue()
                            / Math.pow(
                            1 + rate,
                            years
                    );
        }

        long currentDays =
                LocalDate.now()
                        .toEpochDay()
                        - baseDate;

        double currentYears =
                currentDays / 365.0;

        total +=
                currentValue
                        / Math.pow(
                        1 + rate,
                        currentYears
                );

        return total;
    }

    // ─────────────────────────────────────────────

    private double derivative(
            List<SipTransaction> txns,
            double currentValue,
            double rate
    ) {

        double total = 0;

        long baseDate =
                txns.get(0)
                        .getInvestDate()
                        .toEpochDay();

        for (SipTransaction txn : txns) {

            long days =
                    txn.getInvestDate()
                            .toEpochDay()
                            - baseDate;

            double years = days / 365.0;

            total +=
                    years
                            * txn.getAmount().doubleValue()
                            / Math.pow(
                            1 + rate,
                            years + 1
                    );
        }

        long currentDays =
                LocalDate.now()
                        .toEpochDay()
                        - baseDate;

        double currentYears =
                currentDays / 365.0;

        total -=
                currentYears
                        * currentValue
                        / Math.pow(
                        1 + rate,
                        currentYears + 1
                );

        return total;
    }
}