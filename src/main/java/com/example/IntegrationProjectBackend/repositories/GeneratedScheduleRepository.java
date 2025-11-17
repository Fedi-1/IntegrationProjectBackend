package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.GeneratedSchedule;
import com.example.IntegrationProjectBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedScheduleRepository extends JpaRepository<GeneratedSchedule, Long> {

    List<GeneratedSchedule> findByStudent(Student student);

    List<GeneratedSchedule> findByStudentOrderByDayAscTimeSlotAsc(Student student);

    List<GeneratedSchedule> findByStudentAndDayOrderByTimeSlotAsc(Student student, String day);

    List<GeneratedSchedule> findByStudentAndDay(Student student, String day);

    List<GeneratedSchedule> findByStudentAndDayAndCompleted(Student student, String day, Boolean completed);

    // Custom query to find uncompleted tasks (handles NULL as uncompleted)
    @Query("SELECT gs FROM GeneratedSchedule gs WHERE gs.student = :student AND gs.day = :day AND (gs.completed = false OR gs.completed IS NULL)")
    List<GeneratedSchedule> findUncompletedByStudentAndDay(@Param("student") Student student, @Param("day") String day);

    List<GeneratedSchedule> findBySessionId(String sessionId);

    void deleteByStudent(Student student);

    void deleteBySessionId(String sessionId);
}
