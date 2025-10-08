package com.cheolhyeon.diary.app.event.s3;


import com.cheolhyeon.diary.diary.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3EventListener {
    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onRollback(S3RollbackCleanup payload) {
        for (String key : payload.getImageKeys()) {
            try {
                s3Service.delete(key);
            } catch (Exception e) {
                log.warn("S3 Images Warn {}", e.getMessage(), e);
            }
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommit(S3RollbackCleanup payload) {
        for (String imageKey : payload.getImageKeys()) {
            log.info("COMMIT : {}", imageKey );
        }
    }
}
