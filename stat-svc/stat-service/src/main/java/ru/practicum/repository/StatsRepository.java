package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(e.app, e.uri,
                        CASE WHEN :unique = true THEN COUNT(DISTINCT e.ip)
                             ELSE COUNT(e) END)
            FROM EndpointHit e
            WHERE e.timestamp BETWEEN :start AND :end
                AND (:uris IS NULL OR e.uri IN :uris)
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e) DESC
            """)
    List<ViewStatsDto> getStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris,
                                @Param("unique") boolean unique);

}
