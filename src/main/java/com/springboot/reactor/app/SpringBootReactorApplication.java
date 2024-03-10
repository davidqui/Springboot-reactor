package com.springboot.reactor.app;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Flux<String> nombres = Flux.just("Andres", "Pedro","Maria", "Diego", "Juan")
//				.doOnNext(System.out::println);
				.doOnNext(e -> {
					if(e.isEmpty()) {
						throw new RuntimeException("Nombre no puede ser vacio");
					}
					System.out.println(e);					
				})
				.map(nombre -> {
					return nombre.toUpperCase();
				});
		

		nombres.subscribe(e -> log.info(e),
				error -> log.error(error.getMessage()),				
				new Runnable() {
					
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito!");
						
					}
				});
					
		
	}

}
