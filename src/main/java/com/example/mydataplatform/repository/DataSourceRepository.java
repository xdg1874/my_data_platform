package com.example.mydataplatform.repository;

import com.example.mydataplatform.entity.DataSource;
import com.example.mydataplatform.enums.DataSourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源Repository
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {

    /**
     * 根据状态查询数据源
     */
    List<DataSource> findByStatus(DataSourceStatus status);

    /**
     * 根据类型查询数据源
     */
    List<DataSource> findByType(String type);

    /**
     * 根据名称查询数据源
     */
    DataSource findByName(String name);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 分页查询数据源（支持条件过滤）
     */
    @Query("SELECT d FROM DataSource d WHERE " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:keyword IS NULL OR d.name LIKE %:keyword% OR d.description LIKE %:keyword%)")
    Page<DataSource> findDataSources(@Param("type") String type,
                                   @Param("status") DataSourceStatus status,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
}
