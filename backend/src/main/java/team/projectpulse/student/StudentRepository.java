package team.projectpulse.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    // Find all students in a section
    List<Student> findBySectionSectionId(Integer sectionId);

    List<Student> findByTeamTeamId(Integer teamId);

    @Query("""
            select student from Student student
            where student.id = :studentId and (
                student.section.course.courseAdmin.id = :callerId or
                exists (select assignedInstructor.id from Section studentSection join studentSection.instructors assignedInstructor
                        where studentSection = student.section and assignedInstructor.id = :callerId)
            )
            """)
    Optional<Student> findByIdInLifecycleScope(@Param("studentId") Integer studentId,
                                               @Param("callerId") Integer callerId);
}
