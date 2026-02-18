package it.polito.group9.usermanagementservice.config

import javax.sql.DataSource
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class FlywayConfig {

  @FlywayDataSource
  @Bean(name = ["flywayDataSource"])
  @ConfigurationProperties(prefix = "spring.flyway")
  fun flywayDataSource(): DataSource {
    val dataSource = DriverManagerDataSource()
    dataSource.setDriverClassName("org.postgresql.Driver")
    dataSource.url = "jdbc:postgresql://localhost:5432/db_usermanagement"
    dataSource.username = "user_usermanagement"
    dataSource.password = "pass_usermanagement"
    return dataSource
  }
}
