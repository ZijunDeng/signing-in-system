package com.xuemiao.model.repository;

import com.xuemiao.model.pdm.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dzj on 9/30/2016.
 */
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    @Query("select c from CourseEntity c where c.studentId = :studentId and (:currentWeek between c.startWeek and c.endWeek)")
    List<CourseEntity> getCoursesByStudentAndWeek(@Param("studentId") Long studentId,
                                                  @Param("currentWeek") int currentWeek);

    List<CourseEntity> findByStudentId(Long studentId);

    @Transactional
    @Modifying
    @Query("delete from CourseEntity s where s.studentId = :studentId")
    void deleteByStudentId(@Param("studentId")Long studentId);
}
