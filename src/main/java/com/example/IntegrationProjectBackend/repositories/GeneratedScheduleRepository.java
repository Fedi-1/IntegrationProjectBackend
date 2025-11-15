package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.GeneratedSchedule;
import com.example.IntegrationProjectBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedScheduleRepository extends JpaRepository<GeneratedSchedule, Long> {

    List<GeneratedSchedule> findByStudent(Student student);

    List<GeneratedSchedule> findByStudentOrderByDayAscTimeSlotAsc(Student student);

    List<GeneratedSchedule> findByStudentAndDayOrderByTimeSlotAsc(Student student, String day);

    List<GeneratedSchedule> findBySessionId(String sessionId);

    void deleteByStudent(Student student);

    void deleteBySessionId(String sessionId);
}
