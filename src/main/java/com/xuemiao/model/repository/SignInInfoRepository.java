package com.xuemiao.model.repository;

import com.xuemiao.model.pdm.SignInInfoEntity;
import com.xuemiao.model.pdm.primaryKey.StudentIdAndOperDateKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

/**
 * Created by dzj on 9/30/2016.
 */
@Component
public interface SignInInfoRepository extends JpaRepository<SignInInfoEntity, StudentIdAndOperDateKey> {
    List<SignInInfoEntity> findByOperDate(Date date);

    @Query("select distinct s.operDate from SignInInfoEntity s")
    List<Date> getAllSignInInfoDate();

    @Query("select max(s.operDate) from SignInInfoEntity s")
    Date getLatestSignInInfoDate();

    @Transactional
    @Modifying
    @Query("delete from SignInInfoEntity s where s.studentId = :studentId")
    void deleteByStudentId(@Param("studentId")Long studentId);
}
