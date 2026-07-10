package com.BachLe.ewallet.common.cache;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor

/*

  Ở đây ta viết các hàm helper để thay đổi cache ngay sau khi commit transaction:

  - Giảm code logic lằng nhằng trong TransactionService

  - Tránh Stale Data (Dữ liệu cũ): Nếu dùng @CacheEvict mặc định, cache bị xóa trước khi DB commit.
  Nếu có một request xem số dư bay vào đúng tích tắc giữa lúc đó, nó sẽ đọc số dư cũ từ DB rồi nạp
  lại vào Cache. afterCommit triệt tiêu khe hở thời gian này.

  - Đảm bảo tính toàn vẹn: Nếu Transaction bị Rollback (do lỗi logic hoặc lỗi mạng), Cache sẽ không
  bị xóa vô ích. Hệ thống vẫn giữ được trạng thái ổn định.

  - Performance: Việc xóa cache được đẩy ra sau cùng, giúp giảm thiểu thời gian chiếm giữ khóa (Lock)
  trong Database cho các luồng khác.

 */
public class CacheHelper {

    private final CacheManager cacheManager;

    public void evictAfterCommit(String cacheName, Object key){
        if(TransactionSynchronizationManager.isActualTransactionActive()){

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    Cache cache = cacheManager.getCache(cacheName);

                    if(cache != null){
                        cache.evict(key);
                    }
                }
            }

            );
        }
    }
}
