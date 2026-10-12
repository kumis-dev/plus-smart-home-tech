package agr;

import agr.starter.AggregationStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregationApplication {
    public static void main(String[] args) {
        // Запуск Spring Boot приложения при помощи вспомогательного класса SpringApplication
        // метод run возвращает назад настроенный контекст, который мы можем использовать для
        // получения настроенных бинов
        ConfigurableApplicationContext context = SpringApplication.run(AggregationApplication.class, args);

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}
