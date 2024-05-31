package com.turkcell.TurkcellCRM.customerService;



import com.turkcell.TurkcellCRM.CoreService.annotations.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//@EnableFeignClients
@EnableSecurity()

//@ComponentScan(basePackages = {"com.turkcell.TurkcellCRM.CoreService.configuration"})

public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

}
