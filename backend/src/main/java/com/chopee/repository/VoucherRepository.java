package com.chopee.repository;

import com.chopee.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    // Tìm voucher hợp lệ của toàn sàn hoặc của shop
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.startDate <= :now AND v.endDate >= :now AND v.usedCount < v.usageLimit")
    Optional<Voucher> findValidVoucher(@Param("code") String code, @Param("now") LocalDateTime now);

    List<Voucher> findByShopId(Long shopId);
    List<Voucher> findByShopIsNull();
}

