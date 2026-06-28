package br.com.moreiracruz.erp.modules.inventory.application;

import br.com.moreiracruz.erp.modules.inventory.domain.model.ReservaEstoque;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReleaseReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.ReservaEstoqueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled job that expires stale {@code ACTIVE} reservations whose TTL has elapsed.
 *
 * <p>Runs at a fixed delay controlled by the property
 * {@code inventory.expiry.check-interval-ms} (default 60 000 ms / 1 minute).
 * Each reservation is released individually so that a single failure does not
 * prevent the rest from being processed.
 */
@Component
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReleaseReserveUseCase releaseReserveUseCase;
    private final ReservaEstoqueRepository reservaRepository;

    public ReservationExpiryScheduler(ReleaseReserveUseCase releaseReserveUseCase,
                                       ReservaEstoqueRepository reservaRepository) {
        this.releaseReserveUseCase = releaseReserveUseCase;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Finds every {@code ACTIVE} reservation whose {@code expiresAt} is in the past
     * and releases it via {@link ReleaseReserveUseCase#release(java.util.UUID)}.
     */
    @Scheduled(fixedDelayString = "${inventory.expiry.check-interval-ms:60000}")
    @Transactional
    public void expireStaleReservations() {
        List<ReservaEstoque> expired = reservaRepository.findExpiredActive(Instant.now());

        for (ReservaEstoque r : expired) {
            try {
                releaseReserveUseCase.release(r.getUuid());
            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", r.getUuid(), e.getMessage(), e);
            }
        }
    }
}
