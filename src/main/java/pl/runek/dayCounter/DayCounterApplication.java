package pl.runek.dayCounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.stream.Stream;

@SpringBootApplication
public class DayCounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DayCounterApplication.class, args);
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("holidays")
                .addScript("base.sql")
                .build();
    }

}

record Holiday(@Id LocalDate theDay, String name) {
}

@Repository
interface HolidayRepo extends CrudRepository<Holiday, LocalDate> {

    @Query("select * from holiday where the_day >= :date1 and the_day <= :date2")
    Stream<Holiday> findAllBetweenDates(@Param("date1") LocalDate date1, @Param("date2") LocalDate date2);
}
