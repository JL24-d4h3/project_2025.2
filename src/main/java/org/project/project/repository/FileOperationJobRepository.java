package org.project.project.repository;

import org.project.project.model.entity.FileOperationJob;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileOperationJobRepository extends JpaRepository<FileOperationJob, Long> {

    // Find by user
    List<FileOperationJob> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Find by user ID
    @Query("SELECT j FROM FileOperationJob j WHERE j.usuario.usuarioId = :userId ORDER BY j.createdAt DESC")
    List<FileOperationJob> findByUserId(@Param("userId") Long userId);

    // Find by user and status
    List<FileOperationJob> findByUsuarioAndStatusOrderByCreatedAtDesc(
            Usuario usuario, FileOperationJob.JobStatus status);

    // Find by user ID and status
    @Query("SELECT j FROM FileOperationJob j WHERE j.usuario.usuarioId = :userId " +
           "AND j.status = :status ORDER BY j.createdAt DESC")
    List<FileOperationJob> findByUserIdAndStatus(@Param("userId") Long userId, 
                                                 @Param("status") FileOperationJob.JobStatus status);

    // Find by status
    List<FileOperationJob> findByStatusOrderByCreatedAtAsc(FileOperationJob.JobStatus status);

    // Find by status IN
    @Query("SELECT j FROM FileOperationJob j WHERE j.status IN :statuses ORDER BY j.createdAt ASC")
    List<FileOperationJob> findByStatusIn(@Param("statuses") List<FileOperationJob.JobStatus> statuses);

    // Find pending jobs
    @Query("SELECT j FROM FileOperationJob j WHERE j.status = 'PENDING' ORDER BY j.createdAt ASC")
    List<FileOperationJob> findPendingJobs();

    // Find in-progress jobs
    @Query("SELECT j FROM FileOperationJob j WHERE j.status IN ('PENDING', 'PROCESSING') " +
           "ORDER BY j.createdAt ASC")
    List<FileOperationJob> findInProgressJobs();

    // Find by operation type
    List<FileOperationJob> findByOperationTypeOrderByCreatedAtDesc(
            FileOperationJob.OperationType operationType);

    // Find recent jobs by user
    @Query("SELECT j FROM FileOperationJob j WHERE j.usuario.usuarioId = :userId " +
           "AND j.createdAt > :since ORDER BY j.createdAt DESC")
    List<FileOperationJob> findRecentByUserId(@Param("userId") Long userId, 
                                             @Param("since") LocalDateTime since);

    // Count by user and status
    @Query("SELECT COUNT(j) FROM FileOperationJob j WHERE j.usuario.usuarioId = :userId " +
           "AND j.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, 
                                @Param("status") FileOperationJob.JobStatus status);

    // Find old completed jobs
    @Query("SELECT j FROM FileOperationJob j WHERE j.status IN ('COMPLETED', 'FAILED', 'CANCELLED') " +
           "AND j.completedAt < :before")
    List<FileOperationJob> findOldCompletedJobs(@Param("before") LocalDateTime before);

    // Delete old completed jobs
    void deleteByStatusInAndCompletedAtBefore(List<FileOperationJob.JobStatus> statuses, 
                                             LocalDateTime before);

    // Método adicional para FileOperationJobService
    List<FileOperationJob> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
