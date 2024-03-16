package com.springboot.reactor.app;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;

import com.springboot.reactor.app.model.Comentarios;
import com.springboot.reactor.app.model.Usuario;
import com.springboot.reactor.app.model.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ejemploContraPresion();
		
	}

	/**
	 * ejemplo Manejando ContraPresion con el Subscriber emplementando sus metodos
	 */
	public void ejemploContraPresion(){
		Flux.range(1, 10)
				.log()
				.subscribe(new Subscriber<Integer>() {

					private  Subscription s;
					private Integer limite = 5;
					private Integer consumido = 0;
					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
					}

					@Override
					public void onNext(Integer t) {
						log.info(t.toString());
						consumido++;
						if (consumido== limite){
							consumido= 0;
							s.request(limite);
						}

					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});
	}

	public void ejemploIntervalDesdeCreate(){
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer conntador = 0;
				@Override
				public void run() {
					emitter.next(++conntador);
					if (conntador== 10){
						timer.cancel();
						emitter.complete();
					}
					if (conntador== 5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flujo emn 5!"));
					}
				}
				}, 1000, 1000);
			})
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminadoS"));
	}

	/**
	 * Ejemplo de intervalos infinitos con el operador DelayElelments
	 * @throws InterruptedException
	 */
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				//.doOnTerminate(() -> latch.countDown())
				.doOnTerminate(latch::countDown) // La misma manera pero abreviada
				.flatMap(i ->{
					if (i >= 5){
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(i);
				})
		.map(i -> "Holo " + i)
				.retry(2)
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	/**
	 * Ejemplo con bloqueo usando un Thread.sleep() y de manera mas elegante blockLast()
	 * @throws InterruptedException
	 */
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rangos = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rangos.subscribe();

		Thread.sleep(13000);
	}

	/**
	 * Ejemplo de retraso con Bloqueo
	 */
	public void ejemploInterval(){
		Flux<Integer> rangos = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rangos.zipWith(retraso, (re, ra) -> ra)
				.doOnNext( i -> log.info(i.toString()))
				.blockLast(); //Bloquea hasta el  ultimo elemento que se emite
	}

	/**
	 * Ejemplo con zip y el operador range
	 */
	public void ejemploZipWithRangos(){
		Flux<Integer> rangos = 	Flux.range(0, 4);
		Flux.just(1, 2, 3, 4)
				.map(i -> (i*2))
				.zipWith(rangos, (uno, dos) -> String.format("primer Flux %d, Segundo Flux %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}


	/**
	 *  Ejemplo combinando flujos con zipWith Forma 2
	 */
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");

			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();

					return new UsuarioComentarios(u, c);
		});
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	/**
	 *  Ejemplo combinando flujos con FlatMap y  zipWith
	 */
	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");

			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMonoConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono, ((usuario, comentariosUsuarios) -> new UsuarioComentarios(usuario, comentariosUsuarios)));
		usuarioComentariosMonoConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	/**
	 * Ejemplo Usuario Comentario con Flatmap
	 */
		public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");
			
			return comentarios;
		});

			Mono<UsuarioComentarios> usuarioComentariosMonoConComentarios = usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c) ));
			usuarioComentariosMonoConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploCollectList( ) throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", " Guzman"));
		usuariosList.add(new Usuario("Pedro", " Fulano"));
		usuariosList.add(new Usuario("Maria", " Mengano"));
		usuariosList.add(new Usuario("Diego", " sultano"));
		usuariosList.add(new Usuario("Juan", " Mengano"));
		usuariosList.add(new Usuario("Bruce", " Lee"));
		usuariosList.add(new Usuario("Bruce", " Willy"));
		
		Flux.fromIterable(usuariosList)
		.collectList()
			.subscribe(lista -> {
				
			lista.forEach(item -> log.info(item.toString()));
			});
	
	}
	
	// Ejemplo toString
	
	public void ejemploToString( ) throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", " Guzman"));
		usuariosList.add(new Usuario("Pedro", " Fulano"));
		usuariosList.add(new Usuario("Maria", " Mengano"));
		usuariosList.add(new Usuario("Diego", " sultano"));
		usuariosList.add(new Usuario("Juan", " Mengano"));
		usuariosList.add(new Usuario("Bruce", " Lee"));
		usuariosList.add(new Usuario("Bruce", " Willy"));
		
		Flux.fromIterable(usuariosList)
		.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}else {
						return Mono.empty();
					}
						
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));
					
		
	}
	
	/** Ejemplo ejemploFlatMap */
	
	public void ejemploFlatMap( ) throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Mengano");
		usuariosList.add("Diego sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willy");
		
		Flux.fromIterable(usuariosList)
		.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					}else {
						return Mono.empty();
					}
						
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u -> log.info(u.toString()));
					
		
	}
	
/**	Ejemplo Iterable */
	
	public void ejemploIterable( ) throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Mengano");
		usuariosList.add("Diego sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willy");
		
		Flux<String> nombres = Flux.fromIterable(usuariosList); /*Flux.just("Andres Guzman", "Pedro Fulano","Maria Mengano", "Diego sultano", "Juan Mengano", "Bruce Lee", "Bruce Willy");*/
		
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario -> {
					if(usuario == null) {
						throw new RuntimeException("Nombre no puede ser vacio");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));					
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});
		

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),				
				new Runnable() {
					
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito!");
						
					}
				});
					
		
	}

}
